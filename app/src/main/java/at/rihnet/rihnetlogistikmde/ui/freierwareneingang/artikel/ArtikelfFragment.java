package at.rihnet.rihnetlogistikmde.ui.freierwareneingang.artikel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOError;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentArtikelfBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Beleg;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Lagerplatz;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.sqlite.QueueDAO;
import at.rihnet.rihnetlogistikmde.ui.freierwareneingang.FreierWareneingangViewModel;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.main.MainActivity;

public class ArtikelfFragment extends Fragment implements MenuProvider {
    private static final String TAG = "RIHNet";
    private LoadingDialogFragment loadingDialogFragment;
    private FragmentArtikelfBinding binding;
    private OnChangeTab changeTab;
    private SqlServerData sqlServerData;
    private OnSearchArtikel searchArtikel;
    private ArtikelViewModel artikelViewModel;
    private EditText et_menge;
    private AppCompatImageButton btn_add;
    private TextView tv_artikelnummer;
    private TextView tv_seriennummercharge_label;
    private EditText et_seriennummercharge;
    private FreierWareneingangViewModel freierWareneingangViewModel;
    private Button btn_hinzufuegen;
    private Artikel artikel;
    private String previousQuery = "";
    private QueueDAO queueDAO;
    private AppCompatSpinner acs_lager;
    private AppCompatSpinner acs_lagerplatz;
    private ArrayAdapter<String> adapterLager;
    private ArrayAdapter<Lagerplatz> adapterLagerplatz;
    private final List<Lagerplatz> lagerplatz = new ArrayList<>();

    public interface OnChangeTab {
        void onChangeTab(int id);
    }

    public interface OnSearchArtikel {
        void onSearchArtikel(MenuItem menuItem, SearchView searchView);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        artikelViewModel = new ViewModelProvider(this).get(ArtikelViewModel.class);
        binding = FragmentArtikelfBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadingDialogFragment = LoadingDialogFragment.newInstance("Artikel wird geladen...");
        loadingDialogFragment.setCancelable(false);

        freierWareneingangViewModel = new ViewModelProvider(requireActivity()).get(FreierWareneingangViewModel.class);
        Beleg beleg = freierWareneingangViewModel.getBeleg().getValue();

        /*
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(beleg);
            Log.e(TAG, jsonString);
        } catch (JsonProcessingException e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
         */

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String standort = prefs.getString("standort", "");
        String ipadresse = prefs.getString("ipadresse", "");
        String port = prefs.getString("port", "");
        String datenbank = prefs.getString("datenbank", "");
        String instance = prefs.getString("instance", "");
        String benutzername = prefs.getString("benutzername", "");
        String kennwort = prefs.getString("kennwort", "");
        sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

        final TextView tv_bezeichnung = binding.tvBezeichnung;
        final TextView tv_zusatz = binding.tvZusatz;
        final TextView tv_hstartikelnummer = binding.tvHstartikelnummer;
        final AppCompatImageButton btn_remove = binding.btnRemove;
        final Button btn_reset = binding.btnReset;

        et_menge = binding.etMenge;
        tv_artikelnummer = binding.tvArtikelnummer;
        tv_seriennummercharge_label = binding.tvSeriennummerchargeLabel;
        et_seriennummercharge = binding.etSeriennummercharge;
        btn_add = binding.btnAdd;
        btn_hinzufuegen = binding.btnHinzufuegen;
        acs_lager = binding.acsLager;
        acs_lagerplatz = binding.acsLagerplatz;

        List<String> lager;
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
                lagerplatz.clear();
                List<Lagerplatz> lb = CommunicationSql.getLagerplatzByLager(sqlServerData, selectedItem);
                lagerplatz.addAll(lb);
                adapterLagerplatz = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lagerplatz);
                acs_lagerplatz.setAdapter(adapterLagerplatz);

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

        btn_remove.setOnClickListener(view -> {
            if (et_menge.getText().length() > 0) {
                int value = Integer.parseInt(et_menge.getText().toString());
                if (value > 1) {
                    value--;
                } else {
                    value = 1;
                }
                et_menge.setText(String.valueOf(value));
                if (artikel != null) {
                    artikel.setMenge(value);
                    artikelViewModel.setArtikel(artikel);
                }
            } else {
                et_menge.setText("1");
                if (artikel != null) {
                    artikel.setMenge(1);
                    artikelViewModel.setArtikel(artikel);
                }
            }
        });
        btn_remove.setEnabled(false);
        btn_remove.setImageAlpha(50);

        TextWatcher mengeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("0")) {
                    et_menge.setText("1");
                }
                if (editable.toString().isEmpty() || editable.toString().equals("0") || editable.toString().equals("1")) {
                    btn_remove.setEnabled(false);
                    btn_remove.setImageAlpha(50);
                } else {
                    btn_remove.setEnabled(true);
                    btn_remove.setImageAlpha(255);
                }
            }
        };
        et_menge.addTextChangedListener(mengeTextWatcher);

        TextWatcher seriennummerTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setBtnWeiterEnabled();
            }
        };
        et_seriennummercharge.addTextChangedListener(seriennummerTextWatcher);

        btn_add.setOnClickListener(view -> {
            if (et_menge.getText().length() > 0) {
                if (artikel != null) {
                    artikel.setMenge(Integer.parseInt(et_menge.getText().toString()) + 1);
                    artikelViewModel.setArtikel(artikel);
                } else {
                    et_menge.setText(String.valueOf(Integer.parseInt(et_menge.getText().toString()) + 1));
                }
            } else {
                if (artikel != null) {
                    artikel.setMenge(1);
                    artikelViewModel.setArtikel(artikel);
                } else {
                    et_menge.setText("1");
                }
            }
        });

        btn_reset.setOnClickListener(view -> {
            et_menge.setEnabled(true);
            btn_add.setEnabled(true);
            btn_add.setImageAlpha(255);
            artikelViewModel.resetArtikel();
            //TODO freierWareneingangViewModel.setArtikel(null);
            ((InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(requireView().getWindowToken(), 0);
        });

        btn_hinzufuegen.setOnClickListener(view -> {
            artikel.setKategorie(Kategorie.FREIERWARENEINGANG);
            artikel.setLager(acs_lager.getSelectedItem().toString());
            artikel.setLagerplatz(acs_lagerplatz.getSelectedItem().toString());
            artikel.setLagerplatzId(((Lagerplatz) acs_lagerplatz.getSelectedItem()).getLagerplatzId());
            artikel.setSeriennummer(et_seriennummercharge.getText().toString());
            freierWareneingangViewModel.addQueue(artikel);
            queueDAO.insert(artikel);

            et_menge.setEnabled(true);
            btn_add.setEnabled(true);
            btn_add.setImageAlpha(255);
            artikelViewModel.resetArtikel();
        });

        artikelViewModel.getArtikel().observe(getViewLifecycleOwner(), artikel -> {
            this.artikel = artikel;
            if (artikel != null && artikel.getBestand() > 0) {
                if (artikel.getArtikelnummer().equals(("Error"))) {
                    return;
                }
                et_menge.setText(String.valueOf(artikel.getMenge()));
                tv_artikelnummer.setText(artikel.getArtikelnummer());
                tv_bezeichnung.setText(artikel.getBezeichnung());
                tv_zusatz.setText(artikel.getZusatz());
                tv_hstartikelnummer.setText(artikel.getHstArtikelnummer());
                if (artikel.getSerieCharge().equals("S")) {
                    tv_seriennummercharge_label.setText(R.string.artikel_seriennummer);
                    et_seriennummercharge.setText(artikel.getSeriennummer());
                    setSeriennummerChargeViewVisibility(View.VISIBLE);
                } else if (artikel.getSerieCharge().equals("C")) {
                    tv_seriennummercharge_label.setText(R.string.artikel_charge);
                    et_seriennummercharge.setText(artikel.getCharge());
                    setSeriennummerChargeViewVisibility(View.VISIBLE);
                } else {
                    tv_seriennummercharge_label.setText("---:");
                    et_seriennummercharge.setText(null);
                    setSeriennummerChargeViewVisibility(View.GONE);
                }
                setBtnWeiterEnabled();
            } else {
                if (artikel == null) {
                    Toast.makeText(getContext(), "Artikelnummer: " + freierWareneingangViewModel.getSearchArtikel().getValue() + " wurde nicht gefunden!", Toast.LENGTH_LONG).show();
                    artikelViewModel.resetArtikel();
                } else {
                    Toast.makeText(getContext(), "Artikelnummer: " + freierWareneingangViewModel.getSearchArtikel().getValue() + " | Bestand: 0 | Artikel kann nicht umgelagert werden!", Toast.LENGTH_LONG).show();
                }
                et_menge.setText("1");
                tv_artikelnummer.setText("");
                tv_bezeichnung.setText("");
                tv_zusatz.setText("");
                tv_hstartikelnummer.setText("");
                et_seriennummercharge.setText("");
                btn_hinzufuegen.setEnabled(false);
                setSeriennummerChargeViewVisibility(View.GONE);
                //TODO freierWareneingangViewModel.setArtikel(null);
            }
        });

        freierWareneingangViewModel.getSearchArtikel().observe(getViewLifecycleOwner(), this::doSearch);

        freierWareneingangViewModel.getResetArtikel().observe(getViewLifecycleOwner(), reset -> {
            if (reset) {
                artikelViewModel.resetArtikel();
                //TODO freierWareneingangViewModel.setResetArtikel(false);
            }
        });

        setSeriennummerChargeViewVisibility(View.GONE);
        btn_hinzufuegen.setEnabled(false);

        MyDatabase myDatabase = Room.databaseBuilder(requireContext(), MyDatabase.class, "rihnetdatabase").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        queueDAO = myDatabase.getQueueDAO();

        if (beleg != null) {
            acs_lager.setSelection(adapterLager.getPosition(beleg.getLager()));
            /*lagerplatz.clear();
            List<Lagerplatz> lb = CommunicationSql.getLagerplatzByLager(sqlServerData, beleg.getLager());
            lagerplatz.addAll(lb);
            adapterLagerplatz = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lagerplatz);
            acs_lagerplatz.setAdapter(adapterLagerplatz);
            Log.e(TAG, "beleg.getLagerplatz().getBezeichnung(): " + beleg.getLagerplatz().getBezeichnung());
            acs_lagerplatz.setSelection(adapterLagerplatz.getPosition(beleg.getLagerplatz()));*/
        }

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
        searchArtikel = (OnSearchArtikel) context;
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.freierwareneingang_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        assert searchView != null;

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(18);
        searchEditText.setFilters(filters);

        searchView.setQueryHint("Suchen...");
        searchArtikel.onSearchArtikel(menuItem, searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.equals(previousQuery)) {
                    previousQuery = query;
                    if (!query.isEmpty()) {
                        freierWareneingangViewModel.setSearchArtikel(query);
                        menuItem.collapseActionView();
                    }
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
        if (search != null && !search.isEmpty()) {
            loadingDialogFragment.show(getChildFragmentManager(), "fragment_loading_dialog");
            new Handler().postDelayed(() -> {
                LoadArtikelAsyncTask loadArtikelAsyncTask = new LoadArtikelAsyncTask();
                loadArtikelAsyncTask.execute(search);
            }, 300);
        }
    }

    private void setSeriennummerChargeViewVisibility(int visibility) {
        tv_seriennummercharge_label.setVisibility(visibility);
        et_seriennummercharge.setVisibility(visibility);
    }

    private void setBtnWeiterEnabled() {
        if (!et_menge.getText().toString().isEmpty() && !tv_artikelnummer.getText().toString().isEmpty()) {
            Artikel artikel = artikelViewModel.getArtikel().getValue();
            assert artikel != null;
            if (artikel.getSerieCharge().equals("O")) {
                btn_hinzufuegen.setEnabled(true);
            } else {
                btn_hinzufuegen.setEnabled(et_seriennummercharge.getText().length() > 0);
            }
        } else {
            btn_hinzufuegen.setEnabled(false);
        }
    }

    public class LoadArtikelAsyncTask extends AsyncTaskExecutorService<String, Void, Artikel> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Artikel doInBackground(String s) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                String standort = prefs.getString("standort", "");
                String ipadresse = prefs.getString("ipadresse", "");
                String port = prefs.getString("port", "");
                String datenbank = prefs.getString("datenbank", "");
                String instance = prefs.getString("instance", "");
                String benutzername = prefs.getString("benutzername", "");
                String kennwort = prefs.getString("kennwort", "");
                SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

                return CommunicationSql.getArtikel(sqlServerData, s, standort);
            } catch (IOError | Exception error) {
                return new Artikel("Error", "", "", "", "", "", 0, 0, "", "", "", "", 0, "", Kategorie.UMLAGERUNG);
            }
        }

        @Override
        protected void onPostExecute(Artikel artikel) {
            if (artikel == null) {
                et_menge.setEnabled(true);
                btn_add.setEnabled(true);
                btn_add.setImageAlpha(255);
                setSeriennummerChargeViewVisibility(View.GONE);

            } else {
                if (artikel.getArtikelnummer().equals("Error")) {
                    Intent i = new Intent(getActivity(), MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
                if (artikel.getSerieCharge().equals("S")) {
                    artikel.setMenge(1);
                    et_menge.setText("1");
                    et_menge.setEnabled(false);
                    btn_add.setEnabled(false);
                    btn_add.setImageAlpha(50);
                } else {
                    artikel.setMenge(Integer.parseInt(et_menge.getText().toString()));
                    et_menge.setEnabled(true);
                    btn_add.setEnabled(true);
                    btn_add.setImageAlpha(255);
                }
                setSeriennummerChargeViewVisibility(View.VISIBLE);
            }
            previousQuery = "";
            artikelViewModel.setArtikel(artikel);
            freierWareneingangViewModel.setSearchArtikel(null);
            if (loadingDialogFragment != null) {
                loadingDialogFragment.dismiss();
            }
        }
    }
}