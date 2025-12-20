package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.beleg;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentWarenausgangBelegBinding;
import at.rihnet.rihnetlogistikmde.models.BelegInfo;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Lieferbedingung;
import at.rihnet.rihnetlogistikmde.models.Paket;
import at.rihnet.rihnetlogistikmde.models.Verarbeitungskennzeichen;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.LogistikViewModel;
import at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.pakete.PaketeActivity;

public class BelegFragment extends Fragment {
    private static final String TAG = "RIHNet";
    private LoadingDialogFragment loadingDialogFragment;
    private FragmentWarenausgangBelegBinding binding;
    private BelegInfo belegInfo;
    private SharedPreferences prefs;
    private LogistikViewModel logistikViewModel;
    private AppCompatSpinner acs_lieferbedingungen;
    private AppCompatSpinner acs_verarbeitungskennzeichen;
    private TextView tv_bezeichnung;
    private TextView tv_zusatz;
    private TextView tv_anzahl_lieferschein;
    private TextView tv_anzahl_rechnungen;
    private TextView tv_autorechnung;
    private NumberPicker np_pakete;
    private Button btn_verwalten;
    private CardView cv_paketanbindung;
    private CardView cv_pakete;
    private RecyclerView rv_pakete;
    private TextView tv_empty;
    private PaketeRecyclerViewAdapter paketeRecyclerViewAdapter;
    private List<Paket> pakete = new ArrayList<>();
    private ArrayAdapter<Lieferbedingung> adapterLieferbedingungen;
    private ArrayAdapter<Verarbeitungskennzeichen> adapterVerarbeitungskennzeichen;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private FloatingActionButton fab_camera;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWarenausgangBelegBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadingDialogFragment = LoadingDialogFragment.newInstance("Daten werden geladen...");
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == 1 && result.getData() != null) {
                        Bundle bundle = result.getData().getExtras();
                        assert bundle != null;
                        ArrayList<String> paths = bundle.getStringArrayList(CameraActivity.EXTRA_PHOTO_PATHS);
                        assert paths != null;
                        paths.forEach(f -> logistikViewModel.addPhoto(new File(f)));
                    } else if (result.getResultCode() == 2) {
                        Log.e(TAG, "result.getResultCode() == 2");
                    }
                }
        );

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        initViewModel();
        initUIReferences();
        initAdapter();
        loadSpinnerData();
        initListeners();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initViewModel() {
        logistikViewModel = new ViewModelProvider(requireActivity()).get(LogistikViewModel.class);

        logistikViewModel.getPakete().observe(getViewLifecycleOwner(), new Observer<>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onChanged(List<Paket> pakets) {
                        pakete = pakets;
                        paketeRecyclerViewAdapter.setPakete(pakete);
                        paketeRecyclerViewAdapter.notifyDataSetChanged();
                        btn_verwalten.setEnabled(!pakete.isEmpty());
                        updateEmptyView();
                    }
                }
        );

        belegInfo = logistikViewModel.getBelegInfo().getValue();
    }

    private void initUIReferences() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        // Lieferbedingung
        acs_lieferbedingungen = binding.acsLieferbedingungen;
        tv_bezeichnung = binding.tvBezeichnung;
        tv_zusatz = binding.tvZusatz;

        // Verarbeitungskennzeichen
        acs_verarbeitungskennzeichen = binding.acsVerarbeitungskennzeichen;
        tv_anzahl_lieferschein = binding.tvAnzahlLieferschein;
        tv_anzahl_rechnungen = binding.tvAnzahlRechnungen;
        tv_autorechnung = binding.tvAutorechnung;

        // Paketanbindung
        cv_paketanbindung = binding.cvPaketanbindung;
        btn_verwalten = binding.btnVerwalten;
        np_pakete = binding.npPakete;
        np_pakete.setMinValue(0);
        np_pakete.setMaxValue(100);
        np_pakete.setWrapSelectorWheel(false);
        boolean paketanbindung = prefs.getBoolean("paketanbindung", false);
        if (paketanbindung) {
            cv_paketanbindung.setVisibility(VISIBLE);
            np_pakete.setValue(Optional.ofNullable(logistikViewModel.getPaketeAnzahl().getValue()).orElse(0));
        } else {
            cv_paketanbindung.setVisibility(GONE);
        }

        // Pakete
        cv_pakete = binding.cvPakete;
        boolean autoworker = prefs.getBoolean("autoworker", false);
        if (autoworker) {
            cv_pakete.setVisibility(VISIBLE);
        } else {
            cv_pakete.setVisibility(GONE);
        }
        rv_pakete = binding.rvPakete;
        tv_empty = binding.tvEmpty;
        paketeRecyclerViewAdapter = new PaketeRecyclerViewAdapter(pakete);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rv_pakete.setLayoutManager(layoutManager);
        rv_pakete.setHasFixedSize(true);
        DividerItemDecoration divider = new DividerItemDecoration(requireContext(), layoutManager.getOrientation());
        rv_pakete.addItemDecoration(divider);
        rv_pakete.setAdapter(paketeRecyclerViewAdapter);

        // Beleg
        TextView tv_belegtyp = binding.tvBelegtyp;
        TextView tv_belegnummer = binding.tvBelegnummer;
        TextView tv_datum = binding.tvDatum;

        tv_belegtyp.setText(belegInfo.getBelegtyp());
        tv_belegnummer.setText(belegInfo.getBelegnummer());
        tv_datum.setText(belegInfo.getDatum().format(formatter));

        // Kunde
        TextView tv_kunde_name = binding.tvKundeName;
        TextView tv_kunde_strasse = binding.tvKundeStrasse;
        TextView tv_kunde_plz = binding.tvKundePlz;
        TextView tv_kunde_ort = binding.tvKundeOrt;
        TextView tv_kunde_land = binding.tvKundeLand;

        tv_kunde_name.setText(belegInfo.getName());
        tv_kunde_strasse.setText(belegInfo.getStrasse());
        tv_kunde_plz.setText(belegInfo.getPlz());
        tv_kunde_ort.setText(belegInfo.getOrt());
        tv_kunde_land.setText(belegInfo.getLand());

        // Lieferadresse
        TextView tv_lieferadresse_name = binding.tvLieferadresseName;
        TextView tv_lieferadresse_strasse = binding.tvLieferadresseStrasse;
        TextView tv_lieferadresse_plz = binding.tvLieferadressePlz;
        TextView tv_lieferadresse_ort = binding.tvLieferadresseOrt;
        TextView tv_lieferadresse_land = binding.tvLieferadresseLand;

        tv_lieferadresse_name.setText(belegInfo.getAbweichendeLieferadresse().getName());
        tv_lieferadresse_strasse.setText(belegInfo.getAbweichendeLieferadresse().getStrasse());
        tv_lieferadresse_plz.setText(belegInfo.getAbweichendeLieferadresse().getPlz());
        tv_lieferadresse_ort.setText(belegInfo.getAbweichendeLieferadresse().getOrt());
        tv_lieferadresse_land.setText(belegInfo.getAbweichendeLieferadresse().getLand());

        // FAB
        fab_camera = binding.fabCamera;
    }

    private void initAdapter() {
        adapterLieferbedingungen = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, new ArrayList<>());
        acs_lieferbedingungen.setAdapter(adapterLieferbedingungen);

        adapterVerarbeitungskennzeichen = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, new ArrayList<>());
        acs_verarbeitungskennzeichen.setAdapter(adapterVerarbeitungskennzeichen);
    }

    private void loadSpinnerData() {
        if (Objects.requireNonNull(logistikViewModel.getLieferbedingungen().getValue()).isEmpty() || Objects.requireNonNull(logistikViewModel.getVerarbeitungskennzeichen().getValue()).isEmpty()) {
            // Lieferbedingungen laden
            Future<List<Lieferbedingung>> futureLieferbedingung = logistikViewModel.getExecutorService().submit(CommunicationSql::getLieferbed);

            // Verarbeitungskennzeichen laden
            Future<List<Verarbeitungskennzeichen>> futureVerarbeitungskennzeichen = logistikViewModel.getExecutorService().submit(CommunicationSql::getVerarbeitungskennzeichen);

            logistikViewModel.getExecutorService().execute(() -> {
                try {
                    List<Lieferbedingung> lieferbedingungen = futureLieferbedingung.get();
                    List<Verarbeitungskennzeichen> verarbeitungskennzeichen = futureVerarbeitungskennzeichen.get();

                    requireActivity().runOnUiThread(() -> {
                        logistikViewModel.setLieferbedingungen(lieferbedingungen);

                        // Lieferbedingung
                        adapterLieferbedingungen.clear();
                        adapterLieferbedingungen.add(new Lieferbedingung("---", "", ""));
                        adapterLieferbedingungen.addAll(lieferbedingungen);
                        adapterLieferbedingungen.notifyDataSetChanged();
                        logistikViewModel.setLieferbedingungen(lieferbedingungen);

                        for (int i = 0; i < lieferbedingungen.size(); i++) {
                            if (lieferbedingungen.get(i).getNummer().equals(belegInfo.getLieferbedingung())) {
                                acs_lieferbedingungen.setSelection(i + 1);
                                break;
                            }
                        }

                        // Verarbeitungskennzeichen
                        adapterVerarbeitungskennzeichen.clear();
                        adapterVerarbeitungskennzeichen.add(new Verarbeitungskennzeichen(0, 0, 0, false));
                        adapterVerarbeitungskennzeichen.addAll(verarbeitungskennzeichen);
                        adapterVerarbeitungskennzeichen.notifyDataSetChanged();
                        logistikViewModel.setVerarbeitungskennzeichen(verarbeitungskennzeichen);

                        for (int i = 0; i < verarbeitungskennzeichen.size(); i++) {
                            if (verarbeitungskennzeichen.get(i).getId() == belegInfo.getVerarbeitungskennzeichen().getId()) {
                                acs_verarbeitungskennzeichen.setSelection(i + 1);
                            }
                        }

                        if (loadingDialogFragment != null) {
                            loadingDialogFragment.dismiss();
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Fehler beim Laden der Spinner-Daten (parallel): " + e.getMessage());
                }
            });
        } else {
            // Lieferbedingung
            List<Lieferbedingung> lieferbedingungen = logistikViewModel.getLieferbedingungen().getValue();
            adapterLieferbedingungen.clear();
            adapterLieferbedingungen.add(new Lieferbedingung("---", "", ""));
            adapterLieferbedingungen.addAll(lieferbedingungen);
            adapterLieferbedingungen.notifyDataSetChanged();

            for (int i = 0; i < lieferbedingungen.size(); i++) {
                if (lieferbedingungen.get(i).getNummer().equals(belegInfo.getLieferbedingung())) {
                    acs_lieferbedingungen.setSelection(i);
                    break;
                }
            }

            // Verarbeitungskennzeichen
            List<Verarbeitungskennzeichen> verarbeitungskennzeichen = logistikViewModel.getVerarbeitungskennzeichen().getValue();
            adapterVerarbeitungskennzeichen.clear();
            adapterVerarbeitungskennzeichen.add(new Verarbeitungskennzeichen(0, 0, 0, false));
            adapterVerarbeitungskennzeichen.addAll(verarbeitungskennzeichen);
            adapterVerarbeitungskennzeichen.notifyDataSetChanged();

            for (int i = 0; i < verarbeitungskennzeichen.size(); i++) {
                if (verarbeitungskennzeichen.get(i).getId() == belegInfo.getVerarbeitungskennzeichen().getId()) {
                    acs_verarbeitungskennzeichen.setSelection(i);
                }
            }

            if (loadingDialogFragment != null) {
                loadingDialogFragment.dismiss();
            }
        }
    }

    private void initListeners() {
        acs_lieferbedingungen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Lieferbedingung lieferbedingung = (Lieferbedingung) parent.getItemAtPosition(position);
                tv_bezeichnung.setText(lieferbedingung.getBezeichnung());
                tv_zusatz.setText(lieferbedingung.getZusatz());
                if (!lieferbedingung.getNummer().equals(belegInfo.getLieferbedingung())) {
                    belegInfo.setLieferbedingung(lieferbedingung.getNummer());
                    logistikViewModel.setBelegInfo(belegInfo);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        acs_verarbeitungskennzeichen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Verarbeitungskennzeichen verarbeitungskennzeichen = (Verarbeitungskennzeichen) parent.getItemAtPosition(position);
                tv_anzahl_lieferschein.setText(String.valueOf(verarbeitungskennzeichen.getAnzahlLieferscheine()));
                tv_anzahl_rechnungen.setText(String.valueOf(verarbeitungskennzeichen.getAnzahlRechnungen()));
                tv_autorechnung.setText(String.valueOf(verarbeitungskennzeichen.isAutoRechnung()));
                belegInfo.setVerarbeitungskennzeichen(verarbeitungskennzeichen);
                logistikViewModel.setBelegInfo(belegInfo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        np_pakete.setOnValueChangedListener(this::onValueChange);

        btn_verwalten.setOnClickListener(v -> {
            List<Paket> paketeList = new ArrayList<>();
            List<Belegposition> belegpositionList = logistikViewModel.getGebucht().getValue();
            List<Integer> pakete = belegpositionList.stream()
                    .map(Belegposition::getPaketNummer)
                    .filter(paketNummer -> paketNummer > 0)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Integer, List<Belegposition>> byPaket = belegpositionList.stream().collect(Collectors.groupingBy(Belegposition::getPaketNummer));

            for (Integer paketNummer : pakete) {
                List<Belegposition> positionen = byPaket.getOrDefault(paketNummer, Collections.emptyList());

                Paket p = new Paket(paketNummer, "", 0, 0);
                p.setPositionen(positionen);
                paketeList.add(p);
            }

            Intent intent = new Intent(getActivity(), PaketeActivity.class);
            intent.putExtra("EXTRA_PAKETE", (Serializable) paketeList);
            someActivityResultLauncher.launch(intent);
        });

        fab_camera.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(CameraActivity.EXTRA_DOCUMENT_KEY, belegInfo.getBelegtyp() + belegInfo.getBelegnummer());
            Intent intent = new Intent(getActivity(), CameraActivity.class);
            intent.putExtras(bundle);
            someActivityResultLauncher.launch(intent);
        });
    }

    private void onValueChange(NumberPicker picker, int oldValue, int newValue) {
        logistikViewModel.setPaketeAnzahl(newValue);
    }

    private void updateEmptyView() {
        if (!pakete.isEmpty()) {
            rv_pakete.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(GONE);
        } else {
            rv_pakete.setVisibility(GONE);
            tv_empty.setVisibility(View.VISIBLE);
        }
    }
}