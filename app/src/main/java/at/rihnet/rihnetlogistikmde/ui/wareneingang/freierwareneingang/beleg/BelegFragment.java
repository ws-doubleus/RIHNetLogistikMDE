package at.rihnet.rihnetlogistikmde.ui.wareneingang.freierwareneingang.beleg;

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
import at.rihnet.rihnetlogistikmde.databinding.FragmentFreierWareneingangBelegBinding;
import at.rihnet.rihnetlogistikmde.models.Beleg;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Lagerplatz;
import at.rihnet.rihnetlogistikmde.models.Lieferant;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.sqlite.BelegDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.freierwareneingang.FreierWareneingangViewModel;

public class BelegFragment extends Fragment {
    private static final String TAG = "RIHNet";
    private FragmentFreierWareneingangBelegBinding binding;
    private OnChangeTab changeTab;
    private SqlServerData sqlServerData;
    private AppCompatSpinner acs_lieferanten;
    private AppCompatSpinner acs_lager;
    private AppCompatSpinner acs_lagerplatz;
    private ArrayAdapter<Lieferant> adapterLieferanten;
    private ArrayAdapter<String> adapterLager;
    private ArrayAdapter<Lagerplatz> adapterLagerplatz;
    private final List<Lagerplatz> lagerplatzList = new ArrayList<>();
    private Button btn_weiter;
    private EditText et_lieferscheinnummer;
    private FreierWareneingangViewModel freierWareneingangViewModel;
    private BelegDAO belegDAO;
    
    // Temporärer Speicher für den geladenen Beleg, um Spinner korrekt zu setzen
    private Beleg loadedBelegFromDb = null;

    public interface OnChangeTab {
        void onChangeTab(int id);
    }

    // Hilfsklasse für das Ergebnis des initialen Ladens
    private static class InitialDataResult {
        List<Lieferant> lieferanten;
        List<String> lager;
        Beleg savedBeleg;

        public InitialDataResult(List<Lieferant> lieferanten, List<String> lager, Beleg savedBeleg) {
            this.lieferanten = lieferanten;
            this.lager = lager;
            this.savedBeleg = savedBeleg;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFreierWareneingangBelegBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        freierWareneingangViewModel = new ViewModelProvider(requireActivity()).get(FreierWareneingangViewModel.class);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String standort = prefs.getString("standort", "");
        String ipadresse = prefs.getString("ipadresse", "");
        String port = prefs.getString("port", "");
        String datenbank = prefs.getString("datenbank", "");
        String instance = prefs.getString("instance", "");
        String benutzername = prefs.getString("benutzername", "");
        String kennwort = prefs.getString("kennwort", "");
        sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

        acs_lieferanten = binding.acsLieferanten;
        acs_lager = binding.acsLager;
        acs_lagerplatz = binding.acsLagerplatz;
        btn_weiter = binding.btnWeiter;
        et_lieferscheinnummer = binding.etLieferscheinnummer;

        // DB Singleton holen
        MyDatabase myDatabase = MyDatabase.getInstance(requireContext());
        belegDAO = myDatabase.getBelegDAO();

        // Initiales Laden aller Daten (Async)
        loadInitialData(standort);

        // Listener für Lager-Auswahl (lädt Lagerplätze nach)
        acs_lager.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedLager = (String) adapterView.getItemAtPosition(i);
                loadLagerplaetze(selectedLager);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                lagerplatzList.clear();
                if (adapterLagerplatz != null) adapterLagerplatz.notifyDataSetChanged();
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                btn_weiter.setEnabled(!editable.toString().isEmpty());
            }
        };
        et_lieferscheinnummer.addTextChangedListener(textWatcher);

        btn_weiter.setOnClickListener(view -> {
            saveBelegAndContinue();
        });

        return root;
    }

    private void loadInitialData(String standort) {
        new AsyncTaskExecutorService<Void, Void, InitialDataResult>() {
            @Override
            protected InitialDataResult doInBackground(Void params) {
                try {
                    List<String> lager = CommunicationSql.getZiellager(sqlServerData, standort);
                    List<Lieferant> lieferanten = CommunicationSql.getLieferanten(sqlServerData);
                    Beleg beleg = null;
                    if (belegDAO != null) {
                        beleg = belegDAO.getBeleg(Kategorie.FREIERWARENEINGANG);
                    }
                    return new InitialDataResult(lieferanten, lager, beleg);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading initial data", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(InitialDataResult result) {
                if (!isAdded() || result == null) return;

                // 1. Adapter setzen
                adapterLieferanten = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, result.lieferanten);
                acs_lieferanten.setAdapter(adapterLieferanten);

                adapterLager = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, result.lager);
                acs_lager.setAdapter(adapterLager);

                // 2. Gespeicherten Beleg wiederherstellen
                if (result.savedBeleg != null) {
                    loadedBelegFromDb = result.savedBeleg;
                    
                    // Lieferscheinnummer
                    if (!et_lieferscheinnummer.getText().toString().equals(loadedBelegFromDb.getLieferscheinnummer())) {
                        et_lieferscheinnummer.setText(loadedBelegFromDb.getLieferscheinnummer());
                    }

                    // Lieferant selektieren
                    if (loadedBelegFromDb.getLieferant() != null) {
                         Optional<Lieferant> lf = result.lieferanten.stream()
                                .filter(f -> Objects.equals(f.getNummer(), loadedBelegFromDb.getLieferant().getNummer()))
                                .findFirst();
                        lf.ifPresent(value -> acs_lieferanten.setSelection(adapterLieferanten.getPosition(value)));
                    }

                    // Lager selektieren (triggert onItemSelected -> lädt Lagerplätze)
                    if (loadedBelegFromDb.getLager() != null) {
                        int lagerPos = adapterLager.getPosition(loadedBelegFromDb.getLager());
                        if (lagerPos >= 0) {
                            acs_lager.setSelection(lagerPos);
                        }
                    }

                    // ViewModel updaten
                    freierWareneingangViewModel.setBeleg(loadedBelegFromDb);

                    // Logging (optional)
                    logBeleg(loadedBelegFromDb);
                }
            }
        }.execute(null);
    }

    private void loadLagerplaetze(String lagerName) {
        new AsyncTaskExecutorService<String, Void, List<Lagerplatz>>() {
            @Override
            protected List<Lagerplatz> doInBackground(String params) {
                try {
                    return CommunicationSql.getLagerplatzByLager(sqlServerData, params);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading Lagerplaetze", e);
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<Lagerplatz> result) {
                if (!isAdded()) return;

                lagerplatzList.clear();
                if (result != null) {
                    lagerplatzList.addAll(result);
                }
                adapterLagerplatz = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lagerplatzList);
                acs_lagerplatz.setAdapter(adapterLagerplatz);

                // Wenn wir gerade einen Beleg laden und das Lager übereinstimmt, selektiere auch den Lagerplatz
                if (loadedBelegFromDb != null && loadedBelegFromDb.getLager().equals(lagerName)) {
                    if (loadedBelegFromDb.getLagerplatz() != null) {
                        Optional<Lagerplatz> lp0 = lagerplatzList.stream()
                                .filter(f -> f.getLagerplatzId() == loadedBelegFromDb.getLagerplatz().getLagerplatzId())
                                .findFirst();
                        lp0.ifPresent(value -> acs_lagerplatz.setSelection(adapterLagerplatz.getPosition(value)));
                    }
                    // Nach dem Wiederherstellen Referenz löschen, damit bei manueller Änderung nicht immer überschrieben wird
                    // (Optional, je nach gewünschtem Verhalten. Hier lassen wir es, falls User hin und her wechselt)
                }
            }
        }.execute(lagerName);
    }

    private void saveBelegAndContinue() {
        if (et_lieferscheinnummer.getText().toString().isEmpty()) return;

        Lieferant lieferant = (Lieferant) acs_lieferanten.getSelectedItem();
        String lager = (String) acs_lager.getSelectedItem();
        Lagerplatz lagerplatz = (Lagerplatz) acs_lagerplatz.getSelectedItem();

        // Hinweis: Null-Checks für SelectedItem könnten hier sinnvoll sein, falls Listen leer sind
        if (lager == null || lagerplatz == null) {
             // Ggf. Toast anzeigen?
             return;
        }

        Beleg beleg = new Beleg(
                et_lieferscheinnummer.getText().toString(), 
                "S",
                "", 
                lieferant, 
                lager, 
                lagerplatz, 
                Kategorie.FREIERWARENEINGANG
        );

        freierWareneingangViewModel.setBeleg(beleg);
        logBeleg(beleg);

        // Async speichern
        new AsyncTaskExecutorService<Beleg, Void, Void>() {
            @Override
            protected Void doInBackground(Beleg params) {
                if (belegDAO != null) {
                    belegDAO.deleteBelegByKategorie(Kategorie.FREIERWARENEINGANG);
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
}
