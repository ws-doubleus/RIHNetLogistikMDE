package at.rihnet.rihnetlogistikmde.ui.inventur.artikel;

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
import android.util.TypedValue;
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
import at.rihnet.rihnetlogistikmde.audio.SoundPoolManager;
import at.rihnet.rihnetlogistikmde.databinding.FragmentInventurArtikelBinding;
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

public class ArtikelFragment extends Fragment implements MenuProvider {
    private static final String TAG = "RIHNet";
    private LoadingDialogFragment loadingDialogFragment;
    private FragmentInventurArtikelBinding binding;
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
    private SearchView searchView;
    private MenuItem menuItem;
    private static final String LOADING_TAG = "fragment_loading_dialog";
    private boolean isManualSearchRunning = false;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInventurArtikelBinding.inflate(inflater, container, false);
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


        // Menge nur noch über +/- oder Dialog ändern, nicht direkt tippen:
        et_menge.setFocusable(false);
        et_menge.setFocusableInTouchMode(false);
        et_menge.setCursorVisible(false);
        et_menge.setShowSoftInputOnFocus(false);
        // Menge auf maximal 6 Stellen begrenzen (z.B. max. 999999)
        et_menge.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(6)
        });
        et_menge.setOnClickListener(v -> showMengeDialog());


        final TextView tv_bezeichnung = binding.tvBezeichnung;
        final TextView tv_zusatz = binding.tvZusatz;
        final TextView tv_hstartikelnummer = binding.tvHstartikelnummer;
        final AppCompatImageButton btn_remove = binding.btnRemove;
        final Button btn_reset = binding.btnReset;


        // Buttons sollen NICHT per Tastatur/Scanner fokussiert oder ausgelöst werden
        btn_add.setFocusable(false);
        btn_add.setFocusableInTouchMode(false);

        btn_remove.setFocusable(false);
        btn_remove.setFocusableInTouchMode(false);

        btn_erfassen.setFocusable(false);
        btn_erfassen.setFocusableInTouchMode(false);

        btn_reset.setFocusable(false);
        btn_reset.setFocusableInTouchMode(false);

        acs_lager.setFocusable(false);
        acs_lager.setFocusableInTouchMode(false);



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
        inventurErfassungViewModel.setAutoMode(chk_automode.isChecked());
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
                String scanned = inventurErfassungViewModel.getSearchArtikel().getValue();
                SoundPoolManager.getInstance(getContext()).playError();
                Toast.makeText(getContext(), "Artikelnummer: " + inventurErfassungViewModel.getSearchArtikel().getValue() + " wurde nicht gefunden!", Toast.LENGTH_LONG).show();

                at.rihnet.rihnetlogistikmde.models.Log log =
                        new at.rihnet.rihnetlogistikmde.models.Log(
                                "Artikelnummer: " + scanned + " wurde nicht gefunden!",
                                ContextCompat.getColor(requireContext(), R.color.red_500),
                                Kategorie.INVENTUR
                        );
                inventurErfassungViewModel.addLog(log);

                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> logDAO.insert(log));


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
            int value = safeParseMenge(et_menge.getText().toString());
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
            int value = safeParseMenge(et_menge.getText().toString());
            value++;  // 1 -> 2, etc.

            if (artikel != null) {
                artikel.setMenge(value);
                artikelViewModel.setArtikel(artikel);
            } else {
                et_menge.setText(String.valueOf(value));
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
            // ViewModel über Auto-Status informieren
            inventurErfassungViewModel.setAutoMode(b);

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
        Fragment existing = getChildFragmentManager().findFragmentByTag("fragment_loading_dialog");
        if (existing instanceof LoadingDialogFragment) {
            ((LoadingDialogFragment) existing).dismissAllowingStateLoss();
        }
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
        menuItem  = menu.findItem(R.id.action_search);
        searchView  = (SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setOnSearchClickListener(v -> {
            // Wenn die SearchView geöffnet wird, sicherstellen, dass Menge keinen Fokus hat
            if (et_menge != null) {
                et_menge.clearFocus();
            }
        });



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

                        // 1) Menge-Feld sicher aus dem Fokus holen
                        if (et_menge != null) {
                            et_menge.clearFocus();
                        }

                        // 2) Suche anstoßen
                        inventurErfassungViewModel.setSearchArtikel(query);

                        // 3) SearchView SOFORT leeren, damit beim nächsten Scan nichts angehängt wird
                        if (searchView != null) {
                            searchView.setQuery("", false);   // Text löschen, kein neues Submit
                            searchView.clearFocus();          // Fokus weg
                        }

                        // 4) Optional: einklappen, wenn du möchtest:
                        menuItem.collapseActionView();
                    }
                }
                // Event ist verarbeitet → true
                return true;
            }


            @Override
            public boolean onQueryTextChange(String newText) {
                // Keine Auto-Logik hier.
                //  bei leerem Text previousQuery zurücksetzen.
                if (newText.isEmpty()) {
                    previousQuery = "";
                }
                return false;
            }
        });

    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    private void doSearch(String search) {
        // Fokus von allen EditTexts wegnehmen, damit kein Scan-Tastatur-Input in Menge/Serie landet
        if (binding != null) {
            binding.getRoot().requestFocus();
        }

        CommunicationCommon.hideKeyboard(requireActivity());

        if (search == null || search.isEmpty()) {
            return;
        }

        if (chk_automode.isChecked()) {
            // Auto-Modus: kein Dialog, einfach laden
            new LoadArtikelAsyncTask().execute(search);
        } else {
            // MANUELLER Modus

            // Wenn noch eine Suche läuft: neuen Scan ignorieren (sonst Dialog-Chaos)
            if (isManualSearchRunning) {
                return;
            }
            isManualSearchRunning = true;

            // Dialog nur anzeigen, wenn noch keiner mit dem Tag existiert
            if (getChildFragmentManager().findFragmentByTag(LOADING_TAG) == null) {
                loadingDialogFragment = LoadingDialogFragment.newInstance("Artikel wird geladen...");
                loadingDialogFragment.setCancelable(false);
                loadingDialogFragment.show(
                        getChildFragmentManager(),
                        LOADING_TAG
                );
            }

            new LoadArtikelAsyncTask().execute(search);
        }
    }

    private void doErfassen() {
        Artikel artikel = artikelViewModel.getArtikel().getValue();
        if (artikel == null || inventory == null) {
            return;
        }

        // Prefs einlesen
        String appKey = prefs.getString("appkey", "");
        String baseAddress = prefs.getString("baseaddress", "");
        String userName = prefs.getString("username", "");
        String password = prefs.getString("password", "");

        // Menge, Lager, Seriennummer
        double menge = Double.parseDouble(et_menge.getText().toString());
        String lager = acs_lager.getSelectedItem().toString();
        String seriennummer = et_seriennummercharge.getText().toString();

        boolean autoMode = chk_automode.isChecked();

        inventurErfassungViewModel.erfasseArtikel(
                inventory,
                artikel,
                menge,
                lager,
                seriennummer,
                autoMode,
                appKey,
                baseAddress,
                userName,
                password,
                logDAO,
                requireContext()
        );

        // danach UI ggf. zurücksetzen
        artikelViewModel.resetArtikel();
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

    public interface OnSearchArtikel {
        void onSearchArtikel(MenuItem menuItem, SearchView searchView);
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
//                String ipadresse = prefs.getString("ipadresse", "");
//                String port = prefs.getString("port", "");
//                String datenbank = prefs.getString("datenbank", "");
//                String instance = prefs.getString("instance", "");
//                String benutzername = prefs.getString("benutzername", "");
//                String kennwort = prefs.getString("kennwort", "");
                //SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);
                return CommunicationSql.getArtikel(null, s, standort);
            } catch (IOError | Exception error) {
                return new Artikel("Error", "", "", "", "", "", 0, 0, "", "", "", "", 0, "", Kategorie.INVENTUR);
            }
        }

        @Override
        protected void onPostExecute(Artikel artikel) {

            // 1) Dialog & Flag im manuellen Modus aufräumen
            if (!chk_automode.isChecked()) {
                isManualSearchRunning = false;

                try {
                    Fragment existing = getChildFragmentManager()
                            .findFragmentByTag(LOADING_TAG);
                    if (existing instanceof LoadingDialogFragment) {
                        ((LoadingDialogFragment) existing).dismissAllowingStateLoss();
                    }
                } catch (Exception ignored) {
                }
            }

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
                    int value = safeParseMenge(et_menge.getText().toString());
                    artikel.setMenge(value);
                    et_menge.setText(String.valueOf(value));
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

            if (!chk_automode.isChecked() && searchView != null) {
                searchView.setQuery("", false);   // Text löschen
                //searchView.clearFocus();          // Fokus wegnehmen (optional)

            }
            if (chk_automode.isChecked()) {
                if (artikel != null) {
                    if (!artikel.getSerieCharge().equals("S")) {
                        doErfassen();
                    } else if (artikel.getSerieCharge().equals("S") && !artikel.getSeriennummer().isEmpty()) {
                        doErfassen();
                    }
                }
            }
//            else {
//                Fragment existing = getChildFragmentManager().findFragmentByTag("fragment_loading_dialog");
//                if (existing instanceof LoadingDialogFragment) {
//                    ((LoadingDialogFragment) existing).dismissAllowingStateLoss();
//                }
//            }

        }
    }

    /**
     * Liest eine Menge aus einem String sicher:
     * - null oder "" -> 1
     * - NumberFormatException -> 1
     * - <= 0 -> 1
     * - > Integer.MAX_VALUE -> Integer.MAX_VALUE
     */
    private int safeParseMenge(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 1;
        }
        try {
            long val = Long.parseLong(text.trim());
            if (val <= 0) {
                return 1;
            }
            if (val > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;  // oder z.B. 999999, wenn du eine logische Obergrenze willst
            }
            return (int) val;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void showMengeDialog() {
        // aktuelle Menge lesen
        int current = safeParseMenge(et_menge.getText().toString());

        // Dialog-EditText vorbereiten
        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(current));
        input.setSelection(input.getText().length());
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        input.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Large);

        // --- colorAccent auflösen ---
        TypedValue accent = new TypedValue();
        requireContext()
                .getTheme()
                .resolveAttribute(androidx.appcompat.R.attr.colorAccent, accent, true);

        int textColor;
        if (accent.resourceId != 0) {
            textColor = ContextCompat.getColor(requireContext(), accent.resourceId);
        } else {
            textColor = accent.data;
        }
        input.setTextColor(textColor);

        // optional: gleiches Hintergrund-Design wie et_menge
        if (et_menge != null) {
            input.setBackground(et_menge.getBackground());
            int padL = et_menge.getPaddingLeft();
            int padT = et_menge.getPaddingTop();
            int padR = et_menge.getPaddingRight();
            int padB = et_menge.getPaddingBottom();
            input.setPadding(padL, padT, padR, padB);
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Menge eingeben")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    int value = safeParseMenge(input.getText().toString());

                    // Menge im UI setzen
                    et_menge.setText(String.valueOf(value));

                    // Menge im aktuellen Artikel (falls vorhanden) aktualisieren
                    if (artikel != null) {
                        artikel.setMenge(value);
                        artikelViewModel.setArtikel(artikel);
                    }
                })
                .setNegativeButton("ABBRECHEN", null)
                .show();
    }


}