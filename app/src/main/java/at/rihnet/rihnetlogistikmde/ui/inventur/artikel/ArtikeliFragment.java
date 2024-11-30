package at.rihnet.rihnetlogistikmde.ui.inventur.artikel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
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
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.io.IOError;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentArtikeliBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Invbasis;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.SelectLine.Inventory;
import at.rihnet.rihnetlogistikmde.models.SelectLine.InventoryArticleEdit;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.ui.inventur.InventurErfassungViewModel;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.main.MainActivity;

public class ArtikeliFragment extends Fragment implements MenuProvider {
    private static final String TAG = "RIHNet";
    private LoadingDialogFragment loadingDialogFragment;
    private FragmentArtikeliBinding binding;
    private SqlServerData sqlServerData;
    private InventurErfassungViewModel inventurErfassungViewModel;
    private ArtikelViewModel artikelViewModel;
    private AppCompatSpinner acs_lager;
    private MaterialCheckBox chk_automode;
    private Button btn_erfassen;
    private OnSearchArtikel searchArtikel;
    private String previousQuery = "";
    private EditText et_menge;
    private AppCompatImageButton btn_add;
    private TextView tv_artikelnummer;
    private Artikel artikel;
    private TextView tv_seriennummercharge_label;
    private EditText et_seriennummercharge;
    private Inventory inventory;
    private LogDAO logDAO;
    private SharedPreferences prefs;
    private MediaPlayer mediaPlayer;

    public interface OnSearchArtikel {
        void onSearchArtikel(MenuItem menuItem, SearchView searchView);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentArtikeliBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadingDialogFragment = LoadingDialogFragment.newInstance("Artikel wird geladen...");
        loadingDialogFragment.setCancelable(false);

        et_menge = binding.etMenge;
        btn_add = binding.btnAdd;
        tv_artikelnummer = binding.tvArtikelnummer;
        acs_lager = binding.acsLager;
        chk_automode = binding.chkAutomode;
        btn_erfassen = binding.btnErfassen;
        tv_seriennummercharge_label = binding.tvSeriennummerchargeLabel;
        et_seriennummercharge = binding.etSeriennummercharge;

        final TextView tv_bezeichnung = binding.tvBezeichnung;
        final TextView tv_zusatz = binding.tvZusatz;
        final TextView tv_hstartikelnummer = binding.tvHstartikelnummer;
        final AppCompatImageButton btn_remove = binding.btnRemove;
        final Button btn_reset = binding.btnReset;

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String standort = prefs.getString("standort", "");
        String ipadresse = prefs.getString("ipadresse", "");
        String port = prefs.getString("port", "");
        String datenbank = prefs.getString("datenbank", "");
        String instance = prefs.getString("instance", "");
        String benutzername = prefs.getString("benutzername", "");
        String kennwort = prefs.getString("kennwort", "");
        sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

        inventurErfassungViewModel = new ViewModelProvider(requireActivity()).get(InventurErfassungViewModel.class);
        inventurErfassungViewModel.getSearchArtikel().observe(getViewLifecycleOwner(), this::doSearch);
        inventurErfassungViewModel.getInventory().observe(getViewLifecycleOwner(), inventory -> {
            this.inventory = inventory;
            if (inventory.getKindFlag() > 1) {
                try {
                    List<Invbasis> invbasisList = CommunicationSql.getInvbasisByBelegnummerKennzeichen(sqlServerData, inventory.getNumber(), "L");
                    ArrayAdapter<Invbasis> adapterLager = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, invbasisList);
                    acs_lager.setAdapter(adapterLager);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    List<String> lagerList = CommunicationSql.getZiellager(sqlServerData, standort);
                    ArrayAdapter<String> adapterLager = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, lagerList);
                    acs_lager.setAdapter(adapterLager);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        artikelViewModel = new ViewModelProvider(this).get(ArtikelViewModel.class);
        artikelViewModel.getArtikel().observe(getViewLifecycleOwner(), artikel -> {
            this.artikel = artikel;
            if (artikel != null) {
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
                setBtnErfassenEnabled();
            } else {
                playErrorSound();
                Toast.makeText(getContext(), "Artikelnummer: " + inventurErfassungViewModel.getSearchArtikel().getValue() + " wurde nicht gefunden!", Toast.LENGTH_LONG).show();
                artikelViewModel.resetArtikel();
                et_menge.setText("1");
                tv_artikelnummer.setText("");
                tv_bezeichnung.setText("");
                tv_zusatz.setText("");
                tv_hstartikelnummer.setText("");
                et_seriennummercharge.setText("");
                btn_erfassen.setEnabled(false);
                setSeriennummerChargeViewVisibility(View.GONE);
                //TODO freierWareneingangViewModel.setArtikel(null);
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
                if (artikel != null) {
                    artikel.setSeriennummer(editable.toString());
                    setBtnErfassenEnabled();
                }
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
            et_seriennummercharge.setVisibility(View.GONE);
            tv_seriennummercharge_label.setVisibility(View.GONE);
            artikelViewModel.resetArtikel();
            ((InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(requireView().getWindowToken(), 0);
        });

        btn_erfassen.setOnClickListener(view -> doErfassen());

        chk_automode.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                et_menge.setText("1");
                et_menge.setEnabled(false);
                btn_add.setEnabled(false);
                btn_add.setImageAlpha(50);
                acs_lager.setEnabled(false);
            } else {
                et_menge.setEnabled(true);
                btn_add.setEnabled(true);
                btn_add.setImageAlpha(255);
                acs_lager.setEnabled(true);
            }
        });

        MyDatabase myDatabase = Room.databaseBuilder(requireContext(), MyDatabase.class, "rihnetdatabase").fallbackToDestructiveMigration().allowMainThreadQueries().build();
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
        searchArtikel = (OnSearchArtikel) context;
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.inventur_erfassung_menu, menu);
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
                        inventurErfassungViewModel.setSearchArtikel(query);
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
            if (chk_automode.isChecked()) {
                LoadArtikelAsyncTask loadArtikelAsyncTask = new LoadArtikelAsyncTask();
                loadArtikelAsyncTask.execute(search);
            } else {
                loadingDialogFragment.show(getChildFragmentManager(), "fragment_loading_dialog");
                new Handler().postDelayed(() -> {
                    LoadArtikelAsyncTask loadArtikelAsyncTask = new LoadArtikelAsyncTask();
                    loadArtikelAsyncTask.execute(search);
                }, 300);
            }
        }
    }

    private void doErfassen() {
        String appKey = prefs.getString("appkey", "");
        String baseAddress = prefs.getString("baseaddress", "");
        String userName = prefs.getString("username", "");
        String password = prefs.getString("password", "");
        Artikel artikel = artikelViewModel.getArtikel().getValue();
        if (artikel != null) {
            try {
                if (inventory.getKindFlag() == 1 || inventory.getKindFlag() == 3) {
                    List<Invbasis> invbasisList = CommunicationSql.getInvbasisByBelegnummerKennzeichen(sqlServerData, inventory.getNumber(), "A");
                    Invbasis result = invbasisList.stream()
                            .filter(invbasis -> invbasis.getNummer().equals(artikel.getArtikelnummer()))
                            .findFirst().orElse(null);
                    if (result == null) {
                        Toast.makeText(getContext(), "Artikelnummer: " + artikel.getArtikelnummer() + "\nArtikel ist in dieser Inventur nicht erlaubt!", Toast.LENGTH_LONG).show();
                        artikelViewModel.resetArtikel();
                        return;
                    }
                }
                if (CommunicationSelectLine.login(appKey, baseAddress, userName, password)) {
                    Log.i(TAG, "Anmeldung erfolgreich!");
                    InventoryArticleEdit inventoryArticleEdit = new InventoryArticleEdit();
                    inventoryArticleEdit.setQuantity(Double.parseDouble(et_menge.getText().toString()));
                    if (artikel.getSerieCharge().equals("S") && !et_seriennummercharge.getText().toString().isEmpty()) {
                        boolean res0 = CommunicationSql.ExistsSerieCharge(sqlServerData, et_seriennummercharge.getText().toString(), artikel.getArtikelnummer());
                        if (res0) {
                            boolean res1 = CommunicationSql.ExistsInventur(sqlServerData, inventory.getNumber(), et_seriennummercharge.getText().toString());
                            if (res1) {
                                Toast.makeText(getContext(), "Artikelnummer: " + artikel.getArtikelnummer() + "\nSeriennummer: " + et_seriennummercharge.getText().toString() + "\nArtikel wurde schon erfasst!", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                inventoryArticleEdit.setSerialnumber(et_seriennummercharge.getText().toString());
                            }
                        } else {
                            Toast.makeText(getContext(), "Artikelnummer: " + artikel.getArtikelnummer() + "\nSeriennummer: " + et_seriennummercharge.getText().toString() + "\nSeriennummer ist ungültig!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                           /* ObjectMapper objectMapper = new ObjectMapper();
                            try {
                                String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(inventoryArticleEdit);
                                Log.e(TAG, jsonString);
                            } catch (JsonProcessingException e) {
                                Log.e(TAG, String.format("%s", e.getMessage()));
                            }*/

                    at.rihnet.rihnetlogistikmde.models.Log log;
                    if (CommunicationSelectLine.updateInventoryRaiseArticleQuantity(inventory.getNumber(), acs_lager.getSelectedItem().toString(), artikel.getArtikelnummer(), inventoryArticleEdit)) {
                        log = new at.rihnet.rihnetlogistikmde.models.Log("Belegnummer: " + inventory.getNumber() + "\nArtikelnummer: " + artikel.getArtikelnummer() + "\nMenge: " + et_menge.getText().toString() + "\nInventur-Erfassung erfolgreich!", ContextCompat.getColor(requireContext(), R.color.green_500), Kategorie.INVENTUR);
                        if (!chk_automode.isChecked()) {
                            Toast.makeText(getContext(), "Artikelnummer: " + artikel.getArtikelnummer() + "\nMenge: " + et_menge.getText().toString() + "\nInventur-Erfassung erfolgreich!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        playErrorSound();
                        log = new at.rihnet.rihnetlogistikmde.models.Log("Belegnummer: " + inventory.getNumber() + "\nArtikelnummer: " + artikel.getArtikelnummer() + "\nMenge: " + et_menge.getText().toString() + "\nInventur-Erfassung fehlerhaft!", ContextCompat.getColor(requireContext(), R.color.red_500), Kategorie.INVENTUR);
                        Toast.makeText(getContext(), "Artikelnummer: " + artikel.getArtikelnummer() + "\nMenge: " + et_menge.getText().toString() + "\nInventur-Erfassung fehlerhaft!", Toast.LENGTH_LONG).show();
                    }
                    inventurErfassungViewModel.addLog(log);

                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> logDAO.insert(log));

                    artikelViewModel.resetArtikel();
                } else {
                    android.util.Log.e(TAG, "Anmeldung war nicht erfolgreich!");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void playErrorSound() {
        mediaPlayer = MediaPlayer.create(requireActivity(), R.raw.error);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.release();
                }
            });
        }
    }

    private void setSeriennummerChargeViewVisibility(int visibility) {
        tv_seriennummercharge_label.setVisibility(visibility);
        et_seriennummercharge.setVisibility(visibility);
    }

    private void setBtnErfassenEnabled() {
        if (!et_menge.getText().toString().isEmpty() && !tv_artikelnummer.getText().toString().isEmpty()) {
            Artikel artikel = artikelViewModel.getArtikel().getValue();
            assert artikel != null;
            if (artikel.getSerieCharge().equals("O")) {
                btn_erfassen.setEnabled(true);
            } else {
                btn_erfassen.setEnabled(et_seriennummercharge.getText().length() > 0);
            }
        } else {
            btn_erfassen.setEnabled(false);
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
                return new Artikel("Error", "", "", "", "", "", 0, 0, "", "", "", "", 0, "", Kategorie.INVENTUR);
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
                    return;
                }
                if (artikel.getSerieCharge().equals("S") || chk_automode.isChecked()) {
                    artikel.setMenge(1);
                    et_menge.setText("1");
                    et_menge.setEnabled(false);
                    btn_add.setEnabled(false);
                    btn_add.setImageAlpha(50);
                    acs_lager.setEnabled(false);
                } else {
                    artikel.setMenge(Integer.parseInt(et_menge.getText().toString()));
                    et_menge.setEnabled(true);
                    btn_add.setEnabled(true);
                    btn_add.setImageAlpha(255);
                    acs_lager.setEnabled(true);
                }
                setSeriennummerChargeViewVisibility(View.VISIBLE);
            }
            previousQuery = "";
            artikelViewModel.setArtikel(artikel);
            inventurErfassungViewModel.setSearchArtikel(null);
            if (chk_automode.isChecked()) {
                if (artikel != null) {
                    if (!artikel.getSerieCharge().equals("S")) {
                        doErfassen();
                    } else if (artikel.getSerieCharge().equals("S") && !artikel.getSeriennummer().isEmpty()) {
                        doErfassen();
                    }
                }
            } else {
                if (loadingDialogFragment != null) {
                    loadingDialogFragment.dismiss();
                }
            }

        }
    }
}