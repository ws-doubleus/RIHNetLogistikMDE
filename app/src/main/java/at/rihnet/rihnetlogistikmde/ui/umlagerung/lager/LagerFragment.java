package at.rihnet.rihnetlogistikmde.ui.umlagerung.lager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentUmlagerungLagerBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.LagerplatzBestand;
import at.rihnet.rihnetlogistikmde.models.SeriennummerCharge;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.sqlite.QueueDAO;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.UmlagerungViewModel;

public class LagerFragment extends Fragment implements MenuProvider {
    //private static final String TAG = "RIHNet";
    private FragmentUmlagerungLagerBinding binding;
    private OnChangeTab changeTab;
    private OnSearchLager searchLager;
    private UmlagerungViewModel umlagerungViewModel;
    private Artikel artikel;
    private final List<String> lager = new ArrayList<>();
    private final List<LagerplatzBestand> lagerplatz = new ArrayList<>();
    private AppCompatSpinner acs_lager;
    private AppCompatSpinner acs_lagerplatz;
    private ArrayAdapter<String> adapterLager;
    private ArrayAdapter<LagerplatzBestand> adapterLagerplatz;
    private QueueDAO queueDAO;
    private SqlServerData sqlServerData;

    public interface OnChangeTab {
        void onChangeTab(int id);
    }

    public interface OnSearchLager {
        void onSearchLager(MenuItem menuItem, SearchView searchView);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUmlagerungLagerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        umlagerungViewModel = new ViewModelProvider(requireActivity()).get(UmlagerungViewModel.class);
        artikel = umlagerungViewModel.getArtikel().getValue();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String standort = prefs.getString("standort", "");
        String ipadresse = prefs.getString("ipadresse", "");
        String port = prefs.getString("port", "");
        String datenbank = prefs.getString("datenbank", "");
        String instance = prefs.getString("instance", "");
        String benutzername = prefs.getString("benutzername", "");
        String kennwort = prefs.getString("kennwort", "");
        sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

        TextView tv_artikelnummer = binding.tvArtikelnummer;
        TextView tv_bezeichnung = binding.tvBezeichnung;
        TextView tv_zusatz = binding.tvZusatz;
        TextView tv_hstartikelnummer = binding.tvHstartikelnummer;
        TextView tv_seriennummercharge_label = binding.tvSeriennummerchargeLabel;
        TextView tv_seriennummercharge = binding.tvSeriennummercharge;
        TextView tv_menge = binding.tvMenge;
        Button btn_queue = binding.btnQueue;
        acs_lager = binding.acsLager;
        acs_lagerplatz = binding.acsLagerplatz;

        if (artikel == null) {
            btn_queue.setEnabled(false);
        } else {
            if (artikel.getSerieCharge().equals(("O"))) {
                List<SeriennummerCharge> seriennummerChargeList = CommunicationSql.getSeriennummerCharge(sqlServerData, artikel.getArtikelnummer(), standort);
                for (SeriennummerCharge sc : seriennummerChargeList) {
                    lager.add(sc.getLager());
                }
            } else {
                lager.add(artikel.getLager());
            }

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
            btn_queue.setEnabled(true);
        }

        adapterLager = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lager);
        acs_lager.setAdapter(adapterLager);
        acs_lager.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(i);
                lagerplatz.clear();

                List<Artikel> a = Objects.requireNonNull(umlagerungViewModel.getQueue().getValue()).stream().filter(f -> f.getArtikelnummer().equals(artikel.getArtikelnummer())).collect(Collectors.toList());
                String serie = "";
                if (artikel.getSerieCharge().equals("S")) {
                    serie = artikel.getSeriennummer();
                } else if (artikel.getSerieCharge().equals("C")) {
                    serie = artikel.getCharge();
                }
                List<LagerplatzBestand> lb = CommunicationSql.getLagerplatzBestand(sqlServerData, artikel.getArtikelnummer(), selectedItem, serie);
                lagerplatz.addAll(lb);
                //TODO fehlerhaft Lagerplatz - Charge ????
                for (LagerplatzBestand lb1 : lb) {
                    int sum = a.stream().filter(f -> f.getLagerplatz().equals(lb1.getBezeichnung())).mapToInt(Artikel::getMenge).sum();
                    if (sum >= lb1.getBestand()) {
                        lagerplatz.remove(lb1);
                    }
                }
                adapterLagerplatz = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lagerplatz);
                acs_lagerplatz.setAdapter(adapterLagerplatz);
                if(umlagerungViewModel.getSearchLager().getValue() != null && !umlagerungViewModel.getSearchLager().getValue().isEmpty()){
                    LagerplatzBestand lb0 = lagerplatz.stream().filter(f -> f.getEan().equals(umlagerungViewModel.getSearchLager().getValue())).findFirst().orElse(null);
                    if(lb0 != null) {
                        acs_lagerplatz.setSelection(adapterLagerplatz.getPosition(lb0));
                    }else{
                        umlagerungViewModel.setSearchLager(null);
                        Toast.makeText(requireContext(), "Keine gültige Lagerplatz-EAN!", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btn_queue.setOnClickListener(view -> {
            artikel.setLager(acs_lager.getSelectedItem().toString());
            artikel.setKategorie(Kategorie.UMLAGERUNG);
            LagerplatzBestand lb = (LagerplatzBestand) acs_lagerplatz.getSelectedItem();
            if (lb != null) {
                artikel.setLagerplatz(lb.getBezeichnung());
                artikel.setLagerplatzId(lb.getLagerplatzId());
                if (artikel.getMenge() > lb.getBestand()) {
                    artikel.setMenge(lb.getBestand());
                    Toast.makeText(requireContext(), "Die Menge wurde auf " + lb.getBestand() + " Stück reduziert, da der Bestand für diese Charge nicht größer ist !", Toast.LENGTH_LONG).show();
                }
                umlagerungViewModel.addQueue(artikel);
                queueDAO.insert(artikel);

                tv_artikelnummer.setText("");
                tv_bezeichnung.setText("");
                tv_zusatz.setText("");
                tv_hstartikelnummer.setText("");
                tv_seriennummercharge_label.setVisibility(View.GONE);
                tv_seriennummercharge.setText("");
                tv_seriennummercharge.setVisibility(View.GONE);
                tv_menge.setText("");
                acs_lager.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_spinner, new ArrayList<>()));
                acs_lagerplatz.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_spinner, new ArrayList<>()));
                btn_queue.setEnabled(false);
                umlagerungViewModel.setResetArtikel(true);
                umlagerungViewModel.setArtikel(null);
                changeTab.onChangeTab(R.id.navigation_artikel);
            }
        });

        umlagerungViewModel.getSearchLager().observe(getViewLifecycleOwner(), this::doSearch);

        //MyDatabase myDatabase = Room.databaseBuilder(requireContext(), MyDatabase.class, "rihnetdatabase").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        MyDatabase myDatabase = MyDatabase.getInstance(requireContext());
        queueDAO = myDatabase.getQueueDAO();

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
                    umlagerungViewModel.setSearchLager(query);
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
            LagerplatzBestand lb = CommunicationSql.getLagerplatzBestandByEan(sqlServerData, artikel.getArtikelnummer(), search);
            if (lb != null && lager.contains(lb.getLager0())) {
                int positionLager = adapterLager.getPosition(lb.getLager0());
                acs_lager.setSelection(positionLager);
                LagerplatzBestand lb1 = lagerplatz.stream().filter(f -> f.getEan().equals(search)).findFirst().orElse(null);
                if (lb1 != null) {
                    int positionLagerplatz = adapterLagerplatz.getPosition(lb1);
                    acs_lagerplatz.setSelection(positionLagerplatz);
                }
            } else {
                Toast.makeText(requireContext(), "Keine gültige Lagerplatz-EAN!", Toast.LENGTH_LONG).show();
                umlagerungViewModel.setSearchLager(null);
            }
        }
    }
}