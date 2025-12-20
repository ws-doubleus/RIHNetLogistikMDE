package at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.beleg;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentWareneingangBestellbezugBelegBinding;
import at.rihnet.rihnetlogistikmde.models.Beleg;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Lagerplatz;
import at.rihnet.rihnetlogistikmde.models.Lieferant;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.sqlite.BelegDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.sqlite.QueueBelegpositionDAO;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.WareneingangBestellbezugViewModel;

public class BelegFragment extends Fragment {

    private static final String TAG = "RIHNet";
    private FragmentWareneingangBestellbezugBelegBinding binding;
    private OnChangeTab changeTab;
    private LoadingDialogFragment loadingDialogFragment;

    // ViewModel
    private WareneingangBestellbezugViewModel wareneingangBestellbezugViewModel;

    // Room DAOs
    private BelegDAO belegDAO;
    private QueueBelegpositionDAO queueBelegpositionDAO;

    // SqlServerData für CommunicationSql
    private SqlServerData sqlServerData;

    // UI-Elemente
    private AppCompatSpinner acs_lieferanten;
    private AppCompatSpinner acs_belege;
    private AppCompatSpinner acs_lager;
    private AppCompatSpinner acs_lagerplatz;
    private TextView tv_bestellungen_label;
    private Button btn_weiter;
    private EditText et_lieferscheinnummer;

    // Adapter
    private ArrayAdapter<Lieferant> adapterLieferanten;
    private ArrayAdapter<Beleg> adapterBelege;
    private ArrayAdapter<String> adapterLager;
    private ArrayAdapter<Lagerplatz> adapterLagerplatz;

    // Daten
    private final List<Beleg> belege = new ArrayList<>();
    private final List<Lagerplatz> lagerplatzList = new ArrayList<>();
    private Beleg savedBeleg = null;

    // Interface zum Tab-Wechsel
    public interface OnChangeTab {
        void onChangeTab(int id);
    }

    // Hilfsklasse für Initial Load
    private static class InitialDataResult {
        List<String> lager;
        List<Lieferant> lieferanten;
        Beleg savedBeleg;

        InitialDataResult(List<String> lager, List<Lieferant> lieferanten, Beleg savedBeleg) {
            this.lager = lager;
            this.lieferanten = lieferanten;
            this.savedBeleg = savedBeleg;
        }
    }

    // -------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWareneingangBestellbezugBelegBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Loading-Dialog
        loadingDialogFragment = LoadingDialogFragment.newInstance("Daten werden geladen...");
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");

        // Datenbank initialisieren
        initDatabase();

        // 1) ViewModel laden
        initViewModel();

        // 2) UI-Referenzen und SQL-Server-Data einrichten und SharedPreferences laden
        initUIReferences();
        initSqlServerData();

        // 3) Adapter mit leeren Listen vorbereiten
        initAdapter();

        // 4) Listener setzen (Spinner etc.)
        initListeners();

        // 5) Daten laden (Async)
        loadInitialDataAsync();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        changeTab = (OnChangeTab) context;
    }

    // -------------------------------------------------------------
    // Initialisierung / Setup
    // -------------------------------------------------------------
    private void initDatabase() {
        MyDatabase myDatabase = MyDatabase.getInstance(requireContext());
        belegDAO = myDatabase.getBelegDAO();
        queueBelegpositionDAO = myDatabase.getQueueBelegpositionDAO();
    }

    private void initViewModel() {
        wareneingangBestellbezugViewModel = new ViewModelProvider(requireActivity()).get(WareneingangBestellbezugViewModel.class);
    }

    private void initUIReferences() {
        acs_lieferanten = binding.acsLieferanten;
        tv_bestellungen_label = binding.tvBestellungenLabel;
        acs_belege = binding.acsBestellungen;
        acs_lager = binding.acsLager;
        acs_lagerplatz = binding.acsLagerplatz;
        btn_weiter = binding.btnWeiter;
        et_lieferscheinnummer = binding.etLieferscheinnummer;
    }

    private void initSqlServerData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String ipadresse = prefs.getString("ipadresse", "");
        String port = prefs.getString("port", "");
        String datenbank = prefs.getString("datenbank", "");
        String instance = prefs.getString("instance", "");
        String benutzer = prefs.getString("benutzername", "");
        String kennwort = prefs.getString("kennwort", "");
        boolean allebelege = prefs.getBoolean("allebelege", false);

        sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzer, kennwort);

        // Wenn "allebelege" true -> Bestellungs-Spinner ausblenden
        if (allebelege) {
            tv_bestellungen_label.setVisibility(View.GONE);
            acs_belege.setVisibility(View.GONE);
        }
    }

    private void initAdapter() {
        adapterLieferanten = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, new ArrayList<>());
        acs_lieferanten.setAdapter(adapterLieferanten);

        adapterBelege = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, new ArrayList<>());
        acs_belege.setAdapter(adapterBelege);

        adapterLager = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, new ArrayList<>());
        acs_lager.setAdapter(adapterLager);

        adapterLagerplatz = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, new ArrayList<>());
        acs_lagerplatz.setAdapter(adapterLagerplatz);
    }

    /**
     * Initiales Laden aller Spinner-Daten und des gespeicherten Belegs im Hintergrund.
     */
    private void loadInitialDataAsync() {
        new AsyncTaskExecutorService<Void, Void, InitialDataResult>() {
            @Override
            protected InitialDataResult doInBackground(Void params) throws Exception {
                try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                    String standort = prefs.getString("standort", "");

                    // Parallel laden (besser: nacheinander, da Netzwerk-Calls blockieren)
                    List<String> lager = CommunicationSql.getZiellager(sqlServerData, standort);
                    List<Lieferant> lieferanten = CommunicationSql.getLieferantenBelegByStatus(sqlServerData);
                    Beleg saved = belegDAO.getBeleg(Kategorie.WARENEINGANGBESTELLBEZUG);

                    return new InitialDataResult(lager, lieferanten, saved);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading initial data", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(InitialDataResult result) {
                if (!isAdded()) return;

                if (result != null) {
                    // Lager
                    adapterLager.clear();
                    if (result.lager != null) adapterLager.addAll(result.lager);
                    adapterLager.notifyDataSetChanged();
                    wareneingangBestellbezugViewModel.setLager(result.lager);

                    // Lieferanten
                    adapterLieferanten.clear();
                    if (result.lieferanten != null) adapterLieferanten.addAll(result.lieferanten);
                    adapterLieferanten.notifyDataSetChanged();
                    wareneingangBestellbezugViewModel.setLieferanten(result.lieferanten);

                    // Gespeicherter Beleg wiederherstellen
                    savedBeleg = result.savedBeleg;
                    restoreSavedBelegState();
                }

                if (loadingDialogFragment != null) {
                    loadingDialogFragment.dismiss();
                }
            }
        }.execute(null);
    }

    /**
     * Stellt den UI-Zustand basierend auf dem gespeicherten Beleg wieder her.
     */
    private void restoreSavedBelegState() {
        if (savedBeleg != null) {
            // Lieferscheinnummer
            if (!et_lieferscheinnummer.getText().toString().equals(savedBeleg.getLieferscheinnummer())) {
                et_lieferscheinnummer.setText(savedBeleg.getLieferscheinnummer());
            }

            // Lieferant selektieren
            Lieferant lf = savedBeleg.getLieferant();
            if (lf != null) {
                for (int i = 0; i < adapterLieferanten.getCount(); i++) {
                    Lieferant item = adapterLieferanten.getItem(i);
                    if (item != null && Objects.equals(item.getNummer(), lf.getNummer())) {
                        acs_lieferanten.setSelection(i);
                        break;
                    }
                }
            }

            // Lager selektieren (triggert dann onItemSelected für Lagerplätze)
            String lager = savedBeleg.getLager();
            if (lager != null) {
                int pos = adapterLager.getPosition(lager);
                if (pos >= 0) {
                    acs_lager.setSelection(pos);
                }
            }

            wareneingangBestellbezugViewModel.setBeleg(savedBeleg);
        }
    }

    private void initListeners() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Lieferanten-Spinner
        acs_lieferanten.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Lieferant selected = (Lieferant) parent.getItemAtPosition(position);
                handleLieferantSelectedAsync(selected, prefs);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                belege.clear();
                adapterBelege.clear();
            }
        });

        // Belege-Spinner
        acs_belege.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Beleg selectedBeleg = (Beleg) parent.getItemAtPosition(position);
                handleBelegSelectedAsync(selectedBeleg, prefs);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                wareneingangBestellbezugViewModel.setBelegposition(new ArrayList<>());
            }
        });

        // Lager-Spinner
        acs_lager.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedLager = (String) adapterView.getItemAtPosition(i);
                handleLagerSelectedAsync(selectedLager);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                lagerplatzList.clear();
                adapterLagerplatz.clear();
            }
        });

        et_lieferscheinnummer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable editable) {
                btn_weiter.setEnabled(!editable.toString().isEmpty());
            }
        });

        btn_weiter.setOnClickListener(view -> handleWeiterClicked());
    }

    private void handleLieferantSelectedAsync(Lieferant selectedItem, SharedPreferences prefs) {
        if (selectedItem == null) return;
        wareneingangBestellbezugViewModel.setBelegposition(new ArrayList<>());
        boolean allebelege = prefs.getBoolean("allebelege", false);

        new AsyncTaskExecutorService<Void, Void, List<Beleg>>() {
            // Zwischenspeicher für Positionen im "allebelege" Fall (muss als Feld deklariert sein!)
            private List<Belegposition> loadedPositions = null;

            @Override
            protected List<Beleg> doInBackground(Void params) throws Exception {
                 if (allebelege) {
                    loadedPositions = CommunicationSql.getBelegpByBelegtypAdressnummer(sqlServerData, "B", selectedItem.getNummer());
                    List<Belegposition> queue = queueBelegpositionDAO.getBelegpositionByKategorie(Kategorie.WARENEINGANGBESTELLBEZUG);
                    
                    // Queue abziehen
                     for (Belegposition bp : queue) {
                        Optional<Belegposition> foundPos = loadedPositions.stream().filter(b ->
                                b.getArtikelnummer().equals(bp.getArtikelnummer()) &&
                                        b.getBelegnummer().equals(bp.getBelegnummer()) &&
                                        b.getPostext().equals(bp.getPostext())).findFirst();
                        foundPos.ifPresent(belegposition ->
                                belegposition.setOffen(Math.max(0, belegposition.getOffen() - bp.getMenge())));
                    }
                    return null;
                 } else {
                     return CommunicationSql.getBelegByBelegtypAdressnummer(sqlServerData, "B", selectedItem.getNummer());
                 }
            }

            @Override
            protected void onPostExecute(List<Beleg> result) {
                if (!isAdded()) return;

                if (allebelege) {
                     if (loadedPositions != null) {
                         wareneingangBestellbezugViewModel.setBelegposition(loadedPositions);
                     }
                } else {
                    belege.clear();
                    if (result != null) {
                        belege.addAll(result);
                    }
                    adapterBelege.clear();
                    adapterBelege.addAll(belege);
                    adapterBelege.notifyDataSetChanged();

                    // Wenn wir einen gespeicherten Beleg haben, selektieren wir ihn jetzt im Beleg-Spinner
                    selectSavedBelegIfExists();
                }
            }
        }.execute(null);
    }
    
    private void selectSavedBelegIfExists() {
        if (savedBeleg != null) {
            for (int i = 0; i < belege.size(); i++) {
                if (belege.get(i).getBelegnummer().equals(savedBeleg.getBelegnummer())) {
                    acs_belege.setSelection(i);
                    break;
                }
            }
        }
    }

    private void handleBelegSelectedAsync(Beleg selectedItem, SharedPreferences prefs) {
        if (selectedItem == null) {
            wareneingangBestellbezugViewModel.setBelegposition(new ArrayList<>());
            return;
        }
        
        new AsyncTaskExecutorService<Void, Void, List<Belegposition>>() {
            @Override
            protected List<Belegposition> doInBackground(Void params) throws Exception {
                try {
                    return CommunicationSql.getBelegpByBelegtypBelegnummer(sqlServerData, "B", selectedItem.getBelegnummer());
                } catch (Exception e) {
                    Log.e(TAG, "Error loading Belegpositionen", e);
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<Belegposition> result) {
                if (isAdded()) {
                    wareneingangBestellbezugViewModel.setBelegposition(result != null ? result : new ArrayList<>());
                }
            }
        }.execute(null);
    }

    private void handleLagerSelectedAsync(String selectedLager) {
        new AsyncTaskExecutorService<String, Void, List<Lagerplatz>>() {
            @Override
            protected List<Lagerplatz> doInBackground(String params) throws Exception {
                try {
                    return CommunicationSql.getLagerplatzByLager(sqlServerData, params);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading lagerplaetze", e);
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<Lagerplatz> result) {
                if (!isAdded()) return;

                lagerplatzList.clear();
                if (result != null) lagerplatzList.addAll(result);
                
                adapterLagerplatz.clear();
                adapterLagerplatz.addAll(lagerplatzList);
                adapterLagerplatz.notifyDataSetChanged();
                
                wareneingangBestellbezugViewModel.setLagerplaetze(lagerplatzList);

                // Wenn gespeicherter Beleg da ist und Lager übereinstimmt, selektieren
                if (savedBeleg != null && Objects.equals(selectedLager, savedBeleg.getLager())) {
                    Lagerplatz lp = savedBeleg.getLagerplatz();
                    if (lp != null) {
                        for (int i = 0; i < lagerplatzList.size(); i++) {
                            if (lagerplatzList.get(i).getLagerplatzId() == lp.getLagerplatzId()) {
                                acs_lagerplatz.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
        }.execute(selectedLager);
    }

    private void handleWeiterClicked() {
        Lieferant lieferant = (Lieferant) acs_lieferanten.getSelectedItem();
        String lager = acs_lager.getSelectedItem() != null ? acs_lager.getSelectedItem().toString() : "";
        Lagerplatz lp = (Lagerplatz) acs_lagerplatz.getSelectedItem();
        String lieferscheinnummer = et_lieferscheinnummer.getText().toString();

        Beleg beleg = new Beleg(
                lieferscheinnummer,
                "S",
                "",
                lieferant,
                lager,
                lp,
                Kategorie.WARENEINGANGBESTELLBEZUG
        );

        if (acs_belege.getVisibility() == View.VISIBLE && acs_belege.getSelectedItem() instanceof Beleg) {
            Beleg spinnerBeleg = (Beleg) acs_belege.getSelectedItem();
            beleg.setBelegnummer(spinnerBeleg.getBelegnummer());
        }

        wareneingangBestellbezugViewModel.setBeleg(beleg);

        // Speichern Async
        new AsyncTaskExecutorService<Beleg, Void, Void>() {
            @Override
            protected Void doInBackground(Beleg params) throws Exception {
                if (belegDAO != null) {
                    belegDAO.deleteBelegByKategorie(Kategorie.WARENEINGANGBESTELLBEZUG);
                    belegDAO.insert(params);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (isAdded() && changeTab != null) {
                    changeTab.onChangeTab(R.id.navigation_artikel);
                }
            }
        }.execute(beleg);

        logBeleg(beleg);
    }

    private void logBeleg(Beleg beleg) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(beleg);
            Log.e(TAG, jsonString);
        } catch (JsonProcessingException e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
    }
}
