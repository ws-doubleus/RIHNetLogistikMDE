package at.rihnet.rihnetlogistikmde.ui.paketlabel.beleg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentPaketlabelBelegBinding;
import at.rihnet.rihnetlogistikmde.models.Adress;
import at.rihnet.rihnetlogistikmde.models.BelegInfo;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Lieferbedingung;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.paketlabel.PaketlabelViewModel;

import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;


public class BelegFragment extends Fragment {
    private final String TAG = "RIHNet|BelegFragment";
    private FragmentPaketlabelBelegBinding binding;
    private SharedPreferences prefs;
    private PaketlabelViewModel paketlabelViewModel;
    private BelegInfo belegInfo;
    private LoadingDialogFragment loadingDialogFragment;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private AutoCompleteTextView acs_lieferbedingungen;
    private ArrayAdapter<Lieferbedingung> adapterLieferbedingungen;
    private FloatingActionButton fab_camera;
    private BadgeDrawable photoBadge;
    private TextView tv_bezeichnung;
    private TextView tv_zusatz;
    private SearchBar searchBar;
    private SearchView searchView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPaketlabelBelegBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        searchBar = binding.belegSearchBar;
        searchView = binding.belegSearchView;

        searchView.setupWithSearchBar(searchBar);

// Start: SearchView versteckt
        searchView.setVisibility(View.GONE);

// Wenn SearchView schließt -> wieder ganz ausblenden
        searchView.addTransitionListener((sv, oldState, newState) -> {
            boolean hidden = newState == SearchView.TransitionState.HIDDEN
                    || newState == SearchView.TransitionState.HIDING;
            if (hidden) {
                searchView.setVisibility(View.GONE);
            }
        });

        searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            String q = v.getText().toString();
            paketlabelViewModel.setSearchQuery(q);
            searchBar.setText(q);   // damit der Text in der SearchBar sichtbar bleibt
            searchView.hide();
            return true;
        });



        loadingDialogFragment = LoadingDialogFragment.newInstance("Daten werden geladen...");
        loadingDialogFragment.setCancelable(false);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        initViewModel();
        initUIReferences();
        initBadgeObserver();
        initAdapter();
        loadSpinnerData();
        initListeners();

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null)
                    {
                        Bundle bundle = result.getData().getExtras();
                        assert bundle != null;
                        ArrayList<String> paths = bundle.getStringArrayList(CameraActivity.EXTRA_PHOTO_PATHS);
                        assert paths != null;
                        paths.forEach(f -> paketlabelViewModel.addPhoto(new File(f)));
                    } else if (result.getResultCode() == 2) {
                        Log.e(TAG, "result.getResultCode() == 2");
                    }
                }
        );

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (loadingDialogFragment != null && loadingDialogFragment.isAdded()) {
            loadingDialogFragment.dismissAllowingStateLoss();
        }
    }

    private void initViewModel() {
        paketlabelViewModel = new ViewModelProvider(requireActivity()).get(PaketlabelViewModel.class);
        belegInfo = paketlabelViewModel.getBelegInfo().getValue();

        paketlabelViewModel.getSearchQuery().observe(getViewLifecycleOwner(), query -> {
            if (query == null || query.isEmpty()) return;

            // SearchView öffnen + Text setzen
            searchView.setVisibility(View.VISIBLE);
            searchView.show();
            searchView.getEditText().setText(query);
            searchView.getEditText().setSelection(query.length());

            // Optional auch die SearchBar befüllen
            searchBar.setText(query);
        });

    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void initUIReferences() {
        if (belegInfo == null) {
            belegInfo = new BelegInfo("L", "123", LocalDateTime.now(), "", "", "", "", "", "", "", "", "", "", "", "1", 0, LocalDateTime.now(), "", new Adress(1,"","","", "", "", "", "", "", "", "", ""), null, Kategorie.PAKETLABEL);
        }

        searchBar = binding.belegSearchBar;
        searchView = binding.belegSearchView;


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        // Lieferbedingung
        acs_lieferbedingungen = binding.acsLieferbedingungen;
        tv_bezeichnung = binding.tvBezeichnung;
        tv_zusatz = binding.tvZusatz;

        // Beleg
        TextView tv_belegtyp = binding.tvBelegtyp;
        TextView tv_belegnummer = binding.tvBelegnummer;
        TextView tv_datum = binding.tvDatum;

        tv_belegtyp.setText(belegInfo.getBelegtyp());
        tv_belegnummer.setText(belegInfo.getBelegnummer());
        if (belegInfo.getDatum() != null) {
            tv_datum.setText(belegInfo.getDatum().format(formatter));
        }

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

        if (belegInfo.getAbweichendeLieferadresse() != null) {
            tv_lieferadresse_name.setText(belegInfo.getAbweichendeLieferadresse().getName());
            tv_lieferadresse_strasse.setText(belegInfo.getAbweichendeLieferadresse().getStrasse());
            tv_lieferadresse_plz.setText(belegInfo.getAbweichendeLieferadresse().getPlz());
            tv_lieferadresse_ort.setText(belegInfo.getAbweichendeLieferadresse().getOrt());
            tv_lieferadresse_land.setText(belegInfo.getAbweichendeLieferadresse().getLand());
        }

        // FAB
        photoBadge = BadgeDrawable.create(requireContext());
        photoBadge.setVisible(false);
        photoBadge.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red_a700));
        photoBadge.setBadgeTextColor(Color.WHITE);

        photoBadge.setBadgeGravity(BadgeDrawable.TOP_START);
        photoBadge.setHorizontalOffset(dp(24));
        photoBadge.setVerticalOffset(dp(28));

        fab_camera = binding.fabCamera;
        fab_camera.post(() -> BadgeUtils.attachBadgeDrawable(photoBadge, fab_camera));
    }

    private void initBadgeObserver() {
        paketlabelViewModel.getPhotoCount().observe(getViewLifecycleOwner(), count -> {
            if (photoBadge == null) return;

            int c = (count == null) ? 0 : count;
            if (c <= 0) {
                photoBadge.setVisible(false);
            } else {
                photoBadge.setVisible(true);
                photoBadge.setNumber(c);
            }
        });
    }

    private void initListeners() {
        acs_lieferbedingungen.setOnItemClickListener((parent, view, position, id) -> {
            Lieferbedingung lieferbedingung = (Lieferbedingung) parent.getItemAtPosition(position);
            if (lieferbedingung != null) {
                tv_bezeichnung.setText(lieferbedingung.getBezeichnung());
                tv_zusatz.setText(lieferbedingung.getZusatz());
                if (!lieferbedingung.getNummer().equals(belegInfo.getLieferbedingung())) {
                    belegInfo.setLieferbedingung(lieferbedingung.getNummer());
                    paketlabelViewModel.setBelegInfo(belegInfo);
                }
            }
        });


        fab_camera.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(CameraActivity.EXTRA_DOCUMENT_KEY, belegInfo.getBelegtyp() + belegInfo.getBelegnummer());
            Intent intent = new Intent(getActivity(), CameraActivity.class);
            intent.putExtras(bundle);
            someActivityResultLauncher.launch(intent);
        });
    }

    private void initAdapter() {
        adapterLieferbedingungen = new ArrayAdapter<>(requireContext(), R.layout.list_item, new ArrayList<>());
        acs_lieferbedingungen.setAdapter(adapterLieferbedingungen);
    }

    private void loadSpinnerData() {
        if (Objects.requireNonNull(paketlabelViewModel.getLieferbedingungen().getValue()).isEmpty()) {
            if (!loadingDialogFragment.isAdded()) {
                loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");
            }
            Future<List<Lieferbedingung>> futureLieferbedingung = paketlabelViewModel.getExecutorService().submit(CommunicationSql::getLieferbed);
            paketlabelViewModel.getExecutorService().execute(() -> {
                try {
                    List<Lieferbedingung> lieferbedingungen = futureLieferbedingung.get();
                    requireActivity().runOnUiThread(() -> {
                        updateSpinnerUI(lieferbedingungen);

                        if (loadingDialogFragment != null) {
                            loadingDialogFragment.dismissAllowingStateLoss();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Fehler beim Laden der Spinner-Daten (parallel): " + e.getMessage());
                    requireActivity().runOnUiThread(() -> {
                        if (loadingDialogFragment != null) {
                            loadingDialogFragment.dismissAllowingStateLoss();
                        }
                    });
                }
            });
        } else {
            updateSpinnerUI(paketlabelViewModel.getLieferbedingungen().getValue());
        }
    }

    private void updateSpinnerUI(List<Lieferbedingung> lieferbedingungen) {
        paketlabelViewModel.setLieferbedingungen(lieferbedingungen);
        adapterLieferbedingungen.clear();
        adapterLieferbedingungen.add(new Lieferbedingung("---", "", ""));
        adapterLieferbedingungen.addAll(lieferbedingungen);
        adapterLieferbedingungen.notifyDataSetChanged();

        for (Lieferbedingung lb : lieferbedingungen) {
            if (lb.getNummer().equals(belegInfo.getLieferbedingung())) {
                acs_lieferbedingungen.setText(lb.toString(), false);
                tv_bezeichnung.setText(String.format("Bezeichnung: %s", lb.getBezeichnung()));
                tv_zusatz.setText(String.format("Zusatz: %s", lb.getZusatz()));
                break;
            }
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
