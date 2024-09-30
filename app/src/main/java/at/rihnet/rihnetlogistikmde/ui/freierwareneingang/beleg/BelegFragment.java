package at.rihnet.rihnetlogistikmde.ui.freierwareneingang.beleg;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
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
import androidx.room.Room;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentBelegBinding;
import at.rihnet.rihnetlogistikmde.models.Beleg;
import at.rihnet.rihnetlogistikmde.models.Lagerplatz;
import at.rihnet.rihnetlogistikmde.models.Lieferant;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.sqlite.BelegDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.ui.freierwareneingang.FreierWareneingangViewModel;

public class BelegFragment extends Fragment {
    private static final String TAG = "RIHNet";
    private FragmentBelegBinding binding;
    private OnChangeTab changeTab;
    private SqlServerData sqlServerData;
    private AppCompatSpinner acs_lieferanten;
    private AppCompatSpinner acs_lager;
    private AppCompatSpinner acs_lagerplatz;
    private ArrayAdapter<Lieferant> adapterLieferanten;
    private ArrayAdapter<String> adapterLager;
    private ArrayAdapter<Lagerplatz> adapterLagerplatz;
    private final List<Lagerplatz> lagerplatz = new ArrayList<>();
    private Button btn_weiter;
    private EditText et_lieferscheinnummer;
    private FreierWareneingangViewModel freierWareneingangViewModel;
    private BelegDAO belegDAO;
    private boolean doIt;

    public interface OnChangeTab {
        void onChangeTab(int id);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBelegBinding.inflate(inflater, container, false);
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

        List<Lieferant> lieferanten;
        List<String> lager;
        try {
            lager = CommunicationSql.getZiellager(sqlServerData, standort);
            lieferanten = CommunicationSql.getLieferanten(sqlServerData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        adapterLieferanten = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lieferanten);
        acs_lieferanten.setAdapter(adapterLieferanten);

        adapterLager = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lager);
        acs_lager.setAdapter(adapterLager);
        acs_lager.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String selectedItem = (String) adapterView.getItemAtPosition(i);

                lagerplatz.clear();
                List<Lagerplatz> lb = CommunicationSql.getLagerplatzByLager(sqlServerData, selectedItem);
                lagerplatz.addAll(lb);
                adapterLagerplatz = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lagerplatz);
                acs_lagerplatz.setAdapter(adapterLagerplatz);
                Beleg beleg = belegDAO.getBeleg();
                if (beleg != null && beleg.getLieferscheinnummer() != null && selectedItem.equals(beleg.getLager())) {
                    lagerplatz.clear();
                    List<Lagerplatz> lp = CommunicationSql.getLagerplatzByLager(sqlServerData, beleg.getLager());
                    lagerplatz.addAll(lp);
                    adapterLagerplatz = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lagerplatz);
                    acs_lagerplatz.setAdapter(adapterLagerplatz);
                    Optional<Lagerplatz> lp0 = lagerplatz.stream()
                            .filter(f -> f.getLagerplatzId() == beleg.getLagerplatz().getLagerplatzId())
                            .findFirst();
                    lp0.ifPresent(value -> acs_lagerplatz.setSelection(adapterLagerplatz.getPosition(value)));
                } else {
                    lagerplatz.clear();
                    List<Lagerplatz> lp = CommunicationSql.getLagerplatzByLager(sqlServerData, selectedItem);
                    lagerplatz.addAll(lp);
                    adapterLagerplatz = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lagerplatz);
                    acs_lagerplatz.setAdapter(adapterLagerplatz);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                lagerplatz.clear();
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                btn_weiter.setEnabled(!editable.toString().isEmpty());
            }
        };
        et_lieferscheinnummer.addTextChangedListener(textWatcher);

        btn_weiter.setOnClickListener(view -> {
            Beleg beleg = new Beleg(et_lieferscheinnummer.getText().toString(), (Lieferant) acs_lieferanten.getSelectedItem(), acs_lager.getSelectedItem().toString(), (Lagerplatz) acs_lagerplatz.getSelectedItem());
            freierWareneingangViewModel.setBeleg(beleg);
            belegDAO.deleteAllBeleg();
            belegDAO.insert(beleg);
            changeTab.onChangeTab(R.id.navigation_artikel);
            //TODO Json anzeigen - Beispiel!
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new Beleg(et_lieferscheinnummer.getText().toString(), (Lieferant) acs_lieferanten.getSelectedItem(), acs_lager.getSelectedItem().toString(), (Lagerplatz) acs_lagerplatz.getSelectedItem()));
                Log.e(TAG, jsonString);
            } catch (JsonProcessingException e) {
                Log.e(TAG, String.format("%s", e.getMessage()));
            }
        });

        MyDatabase myDatabase = Room.databaseBuilder(requireContext(), MyDatabase.class, "rihnetdatabase").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        belegDAO = myDatabase.getBelegDAO();

        Beleg beleg = belegDAO.getBeleg();
        if (beleg != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(beleg);
                Log.e(TAG, jsonString);
            } catch (JsonProcessingException e) {
                Log.e(TAG, String.format("%s", e.getMessage()));
            }
            if (!et_lieferscheinnummer.getText().toString().equals(beleg.getLieferscheinnummer())) {
                et_lieferscheinnummer.setText(beleg.getLieferscheinnummer());
                Optional<Lieferant> lf = lieferanten.stream()
                        .filter(f -> Objects.equals(f.getNummer(), beleg.getLieferant().getNummer()))
                        .findFirst();
                lf.ifPresent(value -> acs_lieferanten.setSelection(adapterLieferanten.getPosition(value)));
                acs_lager.setSelection(adapterLager.getPosition(beleg.getLager()));
                freierWareneingangViewModel.setBeleg(beleg);
            }
        }

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
}