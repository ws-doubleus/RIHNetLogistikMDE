package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelzubuchen.lager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentInfoKorrekturArtikelzubuchenLagerBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Grund;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Lagerplatz;
import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.models.SelectLine.ManualStorageCreated;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelzubuchen.ArtikelZubuchenViewModel;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;

public class LagerFragment extends Fragment implements MenuProvider {
    private static final String TAG = "RIHNet";
    private FragmentInfoKorrekturArtikelzubuchenLagerBinding binding;
    private OnChangeTab changeTab;
    private OnSearchLager searchLager;
    private ArtikelZubuchenViewModel artikelZubuchenViewModel;
    private Artikel artikel;
    private List<String> lager = new ArrayList<>();
    private final List<Lagerplatz> lagerplatz = new ArrayList<>();
    private final List<Grund> grund = new ArrayList<>();
    private AppCompatSpinner acs_lager;
    private AppCompatSpinner acs_lagerplatz;
    private AppCompatSpinner acs_grund;
    private ArrayAdapter<String> adapterLager;
    private ArrayAdapter<Lagerplatz> adapterLagerplatz;
    private SharedPreferences prefs;
    private SqlServerData sqlServerData;
    private LoadingDialogFragment loadingDialogFragment;
    private TextView tv_artikelnummer;
    private TextView tv_bezeichnung;
    private TextView tv_zusatz;
    private TextView tv_hstartikelnummer;
    private TextView tv_seriennummercharge_label;
    private TextView tv_seriennummercharge;
    private TextView tv_menge;
    private Button btn_buchen;
    private LogDAO logDAO;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface OnChangeTab {
        void onChangeTab(int id);
    }

    public interface OnSearchLager {
        void onSearchLager(MenuItem menuItem, SearchView searchView);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInfoKorrekturArtikelzubuchenLagerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        artikelZubuchenViewModel = new ViewModelProvider(requireActivity()).get(ArtikelZubuchenViewModel.class);
        artikel = artikelZubuchenViewModel.getArtikel().getValue();

        loadingDialogFragment = LoadingDialogFragment.newInstance("Einlagerung wird gebucht...");
        loadingDialogFragment.setCancelable(false);

        tv_artikelnummer = binding.tvArtikelnummer;
        tv_bezeichnung = binding.tvBezeichnung;
        tv_zusatz = binding.tvZusatz;
        tv_hstartikelnummer = binding.tvHstartikelnummer;
        tv_seriennummercharge_label = binding.tvSeriennummerchargeLabel;
        tv_seriennummercharge = binding.tvSeriennummercharge;
        tv_menge = binding.tvMenge;
        btn_buchen = binding.btnBuchen;
        acs_lager = binding.acsLager;
        acs_lagerplatz = binding.acsLagerplatz;
        acs_grund = binding.acsGrund;

        if (artikel == null) {
            btn_buchen.setEnabled(false);
        } else {
            tv_artikelnummer.setText(artikel.getArtikelnummer());
            tv_bezeichnung.setText(artikel.getBezeichnung());
            tv_zusatz.setText(artikel.getZusatz());
            tv_hstartikelnummer.setText(artikel.getHstArtikelnummer());
            if (artikel.getSerieCharge().equals("S")) {
                tv_seriennummercharge_label.setText(R.string.artikel_seriennummer);
                tv_seriennummercharge.setText(artikel.getSeriennummer());
            } else if (artikel.getSerieCharge().equals("C")) {
                tv_seriennummercharge_label.setText(R.string.artikel_charge);
                tv_seriennummercharge.setText(artikel.getCharge());
            } else {
                tv_seriennummercharge_label.setVisibility(View.GONE);
                tv_seriennummercharge.setVisibility(View.GONE);
            }
            tv_menge.setText(String.valueOf(artikel.getMenge()));
            btn_buchen.setEnabled(true);
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String standort = prefs.getString("standort", null);
        String ipadresse = prefs.getString("ipadresse", "");
        String port = prefs.getString("port", "");
        String datenbank = prefs.getString("datenbank", "");
        String instance = prefs.getString("instance", "");
        String benutzername = prefs.getString("benutzername", "");
        String kennwort = prefs.getString("kennwort", "");
        sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

        //TODO WS
        try {
            lager = CommunicationSql.getZiellager(sqlServerData, standort);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        adapterLager = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lager);
        acs_lager.setAdapter(adapterLager);
        acs_lager.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(i);
                if (artikel != null) {
                    artikel.setLager(selectedItem);
                }
                lagerplatz.clear();
                List<Lagerplatz> lb = CommunicationSql.getLagerplatzByLager(sqlServerData, selectedItem);
                lagerplatz.addAll(lb);
                adapterLagerplatz = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lagerplatz);
                acs_lagerplatz.setAdapter(adapterLagerplatz);
                android.util.Log.e(TAG, "onItemSelected()");
                if (artikelZubuchenViewModel.getSearchLager().getValue() != null && !artikelZubuchenViewModel.getSearchLager().getValue().isEmpty()) {
                    lagerplatz.stream().filter(f -> f.getEan().equals(artikelZubuchenViewModel.getSearchLager().getValue())).findFirst().ifPresent(lb0 -> acs_lagerplatz.setSelection(adapterLagerplatz.getPosition(lb0)));
                    //artikelZubuchenViewModel.setSearchLager("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        acs_lagerplatz.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Lagerplatz selectedItem = (Lagerplatz) adapterView.getItemAtPosition(i);
                if (artikel != null) {
                    artikel.setLagerplatzId(selectedItem.getLagerplatzId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        grund.clear();
        grund.addAll(CommunicationSql.getXLogistikappGruendeByType(sqlServerData, "'E', 'X'"));
        ArrayAdapter<Grund> adapterGrund = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, grund);
        acs_grund.setAdapter(adapterGrund);

        btn_buchen.setOnClickListener(view -> {
            loadingDialogFragment.show(getChildFragmentManager(), "fragment_loading_dialog");
            new Handler().postDelayed(this::doIt, 300);
        });

        artikelZubuchenViewModel.getSearchLager().observe(getViewLifecycleOwner(), this::doSearch);

        //MyDatabase myDatabase = Room.databaseBuilder(requireContext(), MyDatabase.class, "rihnetdatabase").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        MyDatabase myDatabase = MyDatabase.getInstance(requireContext());
        logDAO = myDatabase.getLogDAO();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
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
        searchLager = (OnSearchLager) context;
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.umlagerung_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(20);
        searchEditText.setFilters(filters);

        searchView.setQueryHint("Suchen...");
        searchLager.onSearchLager(menuItem, searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    artikelZubuchenViewModel.setSearchLager(query);
                    menuItem.collapseActionView();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    private void doSearch(String search) {
        CommunicationCommon.hideKeyboard(requireActivity());
        if (search != null && artikel != null && !search.isEmpty()) {
            String lager = CommunicationSql.getLagerByEan(sqlServerData, search);
            if (lager != null && this.lager.contains(lager)) {
                int positionLager = adapterLager.getPosition(lager);
                acs_lager.setSelection(positionLager);

                Lagerplatz lb1 = lagerplatz.stream().filter(f -> f.getEan().equals(search)).findFirst().orElse(null);
                if (lb1 != null) {
                    int positionLagerplatz = adapterLagerplatz.getPosition(lb1);
                    acs_lagerplatz.setSelection(positionLagerplatz);
                }


            } else {
                Toast.makeText(requireContext(), "Keine gültige Lagerplatz-EAN!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void doIt() {
        try {
            String appKey = prefs.getString("appkey", "");
            String baseAddress = prefs.getString("baseaddress", "");
            String userName = prefs.getString("username", "");
            String password = prefs.getString("password", "");
            String standort = prefs.getString("standort", "");

//            if (CommunicationSelectLine.login(appKey, baseAddress, userName, password)) {
                android.util.Log.i(TAG, "Anmeldung erfolgreich!");
                ManualStorageCreated manualStorageCreated = CommunicationSelectLine.createManualStorage(standort);
                if (manualStorageCreated != null) {
                    android.util.Log.i(TAG, "Belegnummer: " + manualStorageCreated.getManualStorageNumber() + " | Manuelle Lagerung => Beleg erfolgreich erstellt!");
                    ManualStorageCreated msc = CommunicationSelectLine.storePosition(manualStorageCreated.getManualStorageNumber(), artikel, artikel.getMenge());
                    if (msc != null) {
                        android.util.Log.i(TAG, "Belegnummer: " + msc.getManualStorageNumber() + " | Manuelle Lagerung => Belegposition erfolgreich erstellt!");
                        Log log = new at.rihnet.rihnetlogistikmde.models.Log("Artikelnummer: " + artikel.getArtikelnummer() + "\nMenge: " + artikel.getMenge() + "\nArtikel Zubuchung erfolgreich!", ContextCompat.getColor(requireContext(), R.color.green_500), Kategorie.ARTIKELZUBUCHEN);
                        artikelZubuchenViewModel.addLog(log);
                        executor.execute(() -> logDAO.insert(log));
                        int res = CommunicationSql.updateBelegFreierText1ByBelegtypBelegnummer(sqlServerData, "M", msc.getManualStorageNumber(), ((Grund) acs_grund.getSelectedItem()).getGrund());
                    }
                    //SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(LoginActivity.BENUTZER_PREFS, MODE_PRIVATE);
                    //String benutzer = sharedPreferences.getString(LoginActivity.BENUTZER, "");
                    //CommunicationSelectLine.updateManualStorageAsync(manualStorageCreated.getManualStorageNumber(),((Grund)acs_grund.getSelectedItem()).getGrund(), devicename);
                } else {
                    Log log = new at.rihnet.rihnetlogistikmde.models.Log("Artikelnummer: " + artikel.getArtikelnummer() + "\nMenge: " + artikel.getMenge() + "\nArtikel Zubuchung fehlerhaft!", ContextCompat.getColor(requireContext(), R.color.red_500), Kategorie.ARTIKELZUBUCHEN);
                    artikelZubuchenViewModel.addLog(log);
                    executor.execute(() -> logDAO.insert(log));
                }
//            } else {
//                android.util.Log.e(TAG, "Anmeldung war nicht erfolgreich!");
//                //TODO Toast
//            }
        } catch (Exception e) {
            android.util.Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            Log log = new at.rihnet.rihnetlogistikmde.models.Log("Artikelnummer: " + artikel.getArtikelnummer() + "\nMenge: 0\nArtikel Zubuchung fehlerhaft!", ContextCompat.getColor(requireContext(), R.color.red_500), Kategorie.ARTIKELZUBUCHEN);
            artikelZubuchenViewModel.addLog(log);
            executor.execute(() -> logDAO.insert(log));
        }
        tv_artikelnummer.setText("");
        tv_bezeichnung.setText("");
        tv_zusatz.setText("");
        tv_hstartikelnummer.setText("");
        tv_seriennummercharge_label.setVisibility(View.GONE);
        tv_seriennummercharge.setText("");
        tv_seriennummercharge.setVisibility(View.GONE);
        tv_menge.setText("");
        btn_buchen.setEnabled(false);
        artikelZubuchenViewModel.setResetArtikel(true);
        artikelZubuchenViewModel.setArtikel(null);
        changeTab.onChangeTab(R.id.navigation_log);
        if (loadingDialogFragment != null) {
            loadingDialogFragment.dismiss();
        }
    }
}
