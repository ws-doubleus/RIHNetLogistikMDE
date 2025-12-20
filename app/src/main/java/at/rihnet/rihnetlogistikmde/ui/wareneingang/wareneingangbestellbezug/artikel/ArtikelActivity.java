package at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.artikel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.ReadListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOError;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityArtikelBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.main.MainActivity;

public class ArtikelActivity extends AppCompatActivity {

    private final String TAG = "RIHNet";

    // Barcode
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;

    // Loading-Dialog
    private LoadingDialogFragment loadingDialogFragment;

    // Menu / Search
    private MenuItem menuItem;
    private SearchView searchView;
    private String previousQuery = "";

    // ViewModel
    private ArtikelViewModel artikelViewModel;

    // Artikel und Views
    private Artikel artikel;
    private MaterialButton btn_remove;
    private MaterialButton btn_add;
    private EditText et_menge;
    private TextInputLayout et_seriennummer_layout;
    private EditText et_seriennummer;
    private TextView tv_artikelnummer;
    private TextView tv_bezeichnung;
    private TextView tv_zusatz;
    private TextView tv_hstartikelnummer;
    private TextView tv_eannummer;
    private Button btn_abbrechen;
    private Button btn_hinzufuegen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityArtikelBinding binding = ActivityArtikelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar(binding);
        initViews(binding);
        initViewModel();
        initListeners();

        loadingDialogFragment = LoadingDialogFragment.newInstance("Artikelinfo wird geladen...");
        loadingDialogFragment.setCancelable(false);
    }

    private void initToolbar(ActivityArtikelBinding binding) {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_exit_to_app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews(ActivityArtikelBinding binding) {
        btn_remove = binding.btnRemove;
        btn_add = binding.btnAdd;
        et_menge = binding.etMenge;
        et_seriennummer_layout = binding.etSeriennummerLayout;
        et_seriennummer = binding.etSeriennummer;
        tv_artikelnummer = binding.tvArtikelnummer;
        tv_bezeichnung = binding.tvBezeichnung;
        tv_zusatz = binding.tvZusatz;
        tv_hstartikelnummer = binding.tvHstartikelnummer;
        tv_eannummer = binding.tvEannummer;
        btn_abbrechen = binding.btnAbbrechen;
        btn_hinzufuegen = binding.btnHinzufuegen;

        // Anfangszustände
        btn_remove.setEnabled(false);
        btn_hinzufuegen.setEnabled(false);
    }

    private void initViewModel() {
        artikelViewModel = new ViewModelProvider(this).get(ArtikelViewModel.class);

        artikelViewModel.getSearch().observe(this, this::doSearch);

        artikelViewModel.getArtikel().observe(this, a -> {
            this.artikel = a;
            updateUIWithArtikel(a);
        });
    }

    private void initListeners() {
        // Minus
        btn_remove.setOnClickListener(view -> {
            if (et_menge.getText().length() > 0) {
                int value = Integer.parseInt(et_menge.getText().toString());
                value = Math.max(value - 1, 1);
                et_menge.setText(String.valueOf(value));
            } else {
                et_menge.setText("1");
            }
        });

        // Plus
        btn_add.setOnClickListener(view -> {
            if (et_menge.getText().length() > 0) {
                int value = Integer.parseInt(et_menge.getText().toString()) + 1;
                et_menge.setText(String.valueOf(value));
            } else {
                et_menge.setText("1");
            }
        });

        // Menge-TextWatcher
        et_menge.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* nichts */ }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { /* nichts */ }
            @Override
            public void afterTextChanged(Editable editable) {
                handleMengeTextChanged(editable.toString());
            }
        });

        // Hinzufügen
        btn_hinzufuegen.setOnClickListener(v -> {
            if (artikel == null) return;

            Belegposition belegposition = new Belegposition(
                    artikel.getArtikelnummer(),
                    artikel.getBezeichnung(),
                    artikel.getZusatz(),
                    artikel.getSerieCharge(),
                    artikel.getSeriennummer(),
                    artikel.getCharge(),
                    Integer.parseInt(et_menge.getText().toString()),
                    0,
                    "",
                    artikel.getHstArtikelnummer(),
                    artikel.getEannummer(),
                    "",
                    1,
                    "",
                    Kategorie.WARENEINGANGBESTELLBEZUG,
                    "-",
                    "---",
                    "---",
                    "",
                    "",
                    0,
                    0,
                    0,
                    0
            );

            Intent intent = new Intent();
            intent.putExtra("belegposition", belegposition);
            setResult(1, intent);
            finish();
        });

        // Abbrechen
        btn_abbrechen.setOnClickListener(view -> finish());
    }

    private void handleMengeTextChanged(String mengeString) {
        if (mengeString.isEmpty() || mengeString.equals("0")) {
            et_menge.setText("1");
            return;
        }

        int menge = Integer.parseInt(mengeString);
        btn_remove.setEnabled(menge > 1);

        // Button aktivieren nur, wenn Artikel geladen wurde
        btn_hinzufuegen.setEnabled(artikel != null);
    }

    private void updateUIWithArtikel(Artikel artikel) {
        if (artikel == null) {
            tv_artikelnummer.setText(null);
            tv_bezeichnung.setText(null);
            tv_zusatz.setText(null);
            tv_hstartikelnummer.setText(null);
            tv_eannummer.setText(null);
            btn_hinzufuegen.setEnabled(false);
            Toast.makeText(getApplicationContext(),
                    "Artikelnummer: " + previousQuery + " wurde nicht gefunden!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        tv_artikelnummer.setText(artikel.getArtikelnummer());
        tv_bezeichnung.setText(artikel.getBezeichnung());
        tv_zusatz.setText(artikel.getZusatz());
        tv_hstartikelnummer.setText(artikel.getHstArtikelnummer());
        tv_eannummer.setText(artikel.getEannummer());

        if (artikel.getSerieCharge().equals("O")) {
            // Keine Seriennummern-Pflicht
            et_seriennummer_layout.setVisibility(View.GONE);
            // Aktivieren nur, wenn Menge eingegeben
            btn_hinzufuegen.setEnabled(!et_menge.getText().toString().isEmpty());
        } else {
            // Seriennummern-Pflicht
            et_seriennummer_layout.setVisibility(View.VISIBLE);
            // TODO: Validierung, ob Seriennummer eingegeben wurde
        }
    }

    // ---------------------------------------------------------
    // Barcode
    // ---------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeManager == null) {
            barcodeManager = new BarcodeManager();
        }
        try {
            readListener = decodeResult -> {
                String result = decodeResult.getText();
                if (!result.isEmpty()) {
                    // letztes Zeichen entfernen
                    result = result.substring(0, result.length() - 1);
                }

                if (menuItem != null) menuItem.expandActionView();
                if (searchView != null) searchView.setQuery(result, false);

                if (!result.equals(previousQuery)) {
                    previousQuery = result;
                    artikelViewModel.setSearch(previousQuery);
                }
            };
            barcodeManager.addReadListener(readListener);
        } catch (DecodeException e) {
            Log.e(TAG, "BarcodeManager Fehler: " + e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeManager != null && readListener != null) {
            try {
                barcodeManager.removeReadListener(readListener);
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    // ---------------------------------------------------------
    // Menü
    // ---------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.artikel_menu, menu);
        menuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menuItem.getActionView();

        if (searchView != null) {
            EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);

            // Länge auf 18 Zeichen beschränken
            searchEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
            searchView.setQueryHint("Suchen...");

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!query.equals(previousQuery)) {
                        previousQuery = query;
                        if (!query.isEmpty()) {
                            artikelViewModel.setSearch(query);
                            menuItem.collapseActionView();
                        }
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Falls du Echtzeit-Suche willst, kannst du hier .setSearch(newText) aufrufen
                    return false;
                }
            });
        }
        return true;
    }

    // ---------------------------------------------------------
    // Suchlogik
    // ---------------------------------------------------------
    private void doSearch(String search) {
        CommunicationCommon.hideKeyboard(this);
        if (search == null || search.isEmpty()) return;

        // Lade-Dialog anzeigen
        loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");

        // Hier nur minimaler Delay, danach asynchronen DB-/Netzwerkzugriff
        new Handler().postDelayed(() -> {
            // -> Executor im Hintergrund
            artikelViewModel.getExecutor().execute(() -> {
                Artikel resultArtikel;
                try {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String ipadresse = prefs.getString("ipadresse", "");
                    String port = prefs.getString("port", "");
                    String datenbank = prefs.getString("datenbank", "");
                    String instance = prefs.getString("instance", "");
                    String benutzername = prefs.getString("benutzername", "");
                    String kennwort = prefs.getString("kennwort", "");
                    SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

                    resultArtikel = CommunicationSql.getArtikel(sqlServerData, search);
                } catch (IOError | Exception error) {
                    // Ggf. handle Error
                    resultArtikel = new Artikel("Error", "", "", "", "", "", 0, 0,
                            "", "", "", "", 0, "", Kategorie.WARENEINGANGBESTELLBEZUG);
                }

                // Anschließend im Main-Thread UI/ViewModel aktualisieren
                Artikel finalResultArtikel = resultArtikel;
                runOnUiThread(() -> {
                    if (finalResultArtikel == null) {
                        artikelViewModel.setArtikel(null);
                    } else {
                        if ("Error".equals(finalResultArtikel.getArtikelnummer())) {
                            // z. B. Error -> App neustarten
                            Intent i = new Intent(ArtikelActivity.this, MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        } else {
                            artikelViewModel.setArtikel(finalResultArtikel);
                        }
                    }
                    artikelViewModel.setSearch(null);

                    // Lade-Dialog schließen
                    if (loadingDialogFragment != null) {
                        loadingDialogFragment.dismiss();
                    }
                    previousQuery = "";
                });
            });
        }, 300);
    }
}
