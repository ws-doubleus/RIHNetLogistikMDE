package at.rihnet.rihnetlogistikmde.ui.umlagerung.buchung;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.ReadListener;

import java.io.IOError;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityBuchungBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Lagerplatz;
import at.rihnet.rihnetlogistikmde.models.LagerplatzBestand;
import at.rihnet.rihnetlogistikmde.models.SelectLine.ManualStorageCreated;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.main.MainActivity;

public class BuchungActivity extends AppCompatActivity {
    private static final String TAG = "RIHNet";
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;
    private Artikel artikel;
    private int position;
    private SharedPreferences prefs;
    private SqlServerData sqlServerData;
    private LoadingDialogFragment loadingDialogFragment;
    private EditText et_menge;
    private AppCompatSpinner acs_lager;
    private AppCompatSpinner acs_lagerplatz;
    private List<String> lager;
    private final List<Lagerplatz> lagerplatz = new ArrayList<>();
    private ArrayAdapter<String> adapterLager;
    private ArrayAdapter<Lagerplatz> adapterLagerplatz;
    private BuchungViewModel buchungViewModel;
    private String previousQuery = "";
    private MenuItem menuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityBuchungBinding binding = ActivityBuchungBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            buchungViewModel = new ViewModelProvider(this).get(BuchungViewModel.class);

            loadingDialogFragment = LoadingDialogFragment.newInstance("Umlagerung wird gebucht...");
            loadingDialogFragment.setCancelable(false);

            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String standort = prefs.getString("standort", null);
            String ipadresse = prefs.getString("ipadresse", "");
            String port = prefs.getString("port", "");
            String datenbank = prefs.getString("datenbank", "");
            String instance = prefs.getString("instance", "");
            String benutzername = prefs.getString("benutzername", "");
            String kennwort = prefs.getString("kennwort", "");
            sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

            artikel = (Artikel) getIntent().getSerializableExtra("artikel");
            position = getIntent().getIntExtra("position", 0);

            Toolbar toolbar = binding.toolbar;
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());

            Button btn_abbrechen = binding.btnAbbrechen;
            btn_abbrechen.setOnClickListener(view -> finish());

            Button btn_umlagern = binding.btnUmlagern;
            btn_umlagern.setOnClickListener(view -> {
                loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");
                new Handler().postDelayed(this::doIt, 300);

            });

            acs_lager = binding.acsLager;
            acs_lagerplatz = binding.acsLagerplatz;
            AppCompatImageButton btn_remove = binding.btnRemove;
            AppCompatImageButton btn_add = binding.btnAdd;
            et_menge = binding.etMenge;
            TextView tv_artikelnummer = binding.tvArtikelnummer;
            TextView tv_bezeichnung = binding.tvBezeichnung;
            TextView tv_zusatz = binding.tvZusatz;
            TextView tv_hstartikelnummer = binding.tvHstartikelnummer;
            TextView tv_seriennummercharge_label = binding.tvSeriennummerchargeLabel;
            TextView tv_seriennummercharge = binding.tvSeriennummercharge;

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
                tv_seriennummercharge_label.setText("---:");
                tv_seriennummercharge.setText("");
            }

            lager = CommunicationSql.getZiellager(sqlServerData, standort);

            adapterLager = new ArrayAdapter<>(getApplicationContext(), R.layout.item_spinner, lager);
            acs_lager.setAdapter(adapterLager);
            acs_lager.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String selectedItem = (String) adapterView.getItemAtPosition(i);
                    lagerplatz.clear();
                    List<Lagerplatz> lb = CommunicationSql.getLagerplatzByLager(sqlServerData, selectedItem);
                    lagerplatz.addAll(lb);
                    adapterLagerplatz = new ArrayAdapter<>(getApplicationContext(), R.layout.item_spinner, lagerplatz);
                    acs_lagerplatz.setAdapter(adapterLagerplatz);
                    if (buchungViewModel.getSearch().getValue() != null && !Objects.requireNonNull(buchungViewModel.getSearch().getValue()).isEmpty()) {
                        Lagerplatz lb1 = lagerplatz.stream().filter(f -> f.getEan().equals(buchungViewModel.getSearch().getValue())).findFirst().orElse(null);
                        if (lb1 != null) {
                            int positionLagerplatz = adapterLagerplatz.getPosition(lb1);
                            acs_lagerplatz.setSelection(positionLagerplatz);
                        } else {
                            Toast.makeText(getApplicationContext(), "Keine gültige Lagerplatz-EAN!", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

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
                    if (!editable.toString().isEmpty() && Integer.parseInt(editable.toString()) >= artikel.getMenge()) {
                        btn_add.setEnabled(false);
                        btn_add.setImageAlpha(50);
                    } else {
                        btn_add.setEnabled(true);
                        btn_add.setImageAlpha(255);
                    }
                }
            };
            et_menge.addTextChangedListener(mengeTextWatcher);
            et_menge.setText(String.valueOf(artikel.getMenge()));

            btn_remove.setOnClickListener(v -> {
                if (et_menge.getText().length() > 0) {
                    int value = Integer.parseInt(et_menge.getText().toString());
                    if (value > 1) {
                        value--;
                    } else {
                        value = 1;
                    }
                    et_menge.setText(String.valueOf(value));
                } else {
                    et_menge.setText("1");
                }
            });

            btn_add.setOnClickListener(v -> {
                if (et_menge.getText().length() > 0) {
                    et_menge.setText(String.valueOf(Integer.parseInt(et_menge.getText().toString()) + 1));
                } else {
                    et_menge.setText("1");
                }
            });

            buchungViewModel.getSearch().observe(this, this::doSearch);
        } catch (IOError | Exception error) {
            Intent i = new Intent(BuchungActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeManager == null) {
            barcodeManager = new BarcodeManager();
        }
        try {
            readListener = decodeResult -> {
                String result = decodeResult.getText().substring(0, decodeResult.getText().length() - 1);
                menuItem.expandActionView();
                searchView.setQuery(result, true);
            };
            barcodeManager.addReadListener(readListener);
        } catch (DecodeException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeManager != null) {
            try {
                barcodeManager.removeReadListener(readListener);
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.umlagerung_menu, menu);
        menuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setQueryHint("Lagerplatz...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.equals(previousQuery)) {
                    previousQuery = query;
                    if (!query.isEmpty()) {
                        buchungViewModel.setSearch(query);
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    public void doIt() {
        try {
            String appKey = prefs.getString("appkey", "");
            String baseAddress = prefs.getString("baseaddress", "");
            String userName = prefs.getString("username", "");
            String password = prefs.getString("password", "");
            String standort = prefs.getString("standort", "");
            String adressnummer = prefs.getString("adressnummer", "");
            //String mitarbeiternummer = prefs.getString("mitarbeiternummer", "");

            if (CommunicationSelectLine.login(appKey, baseAddress, userName, password)) {
                android.util.Log.i(TAG, "Anmeldung erfolgreich!");
                ManualStorageCreated manualStorageCreated = CommunicationSelectLine.createManualStorage(adressnummer, standort, artikel.getLager());
                if (manualStorageCreated != null) {
                    android.util.Log.i(TAG, "Belegnummer: " + manualStorageCreated.getManualStorageNumber() + " | Manuelle Lagerung => Beleg erfolgreich erstellt!");
                    ManualStorageCreated msc = CommunicationSelectLine.storePosition(manualStorageCreated.getManualStorageNumber(), artikel, acs_lager.getSelectedItem().toString(), ((Lagerplatz) acs_lagerplatz.getSelectedItem()).getLagerplatzId(), Integer.parseInt(et_menge.getText().toString()));
                    Intent intent = new Intent();
                    if (msc != null) {
                        intent.putExtra("menge", Integer.parseInt(et_menge.getText().toString()));
                        intent.putExtra("position", position);
                        setResult(1, intent);
                        android.util.Log.i(TAG, "Belegnummer: " + msc.getManualStorageNumber() + " | Manuelle Lagerung => Belegposition erfolgreich erstellt!");
                    } else {
                        intent.putExtra("menge", 0);
                        intent.putExtra("position", position);
                        setResult(1, intent);
                    }
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("menge", 0);
                    intent.putExtra("position", position);
                    setResult(1, intent);
                }
            } else {
                android.util.Log.e(TAG, "Anmeldung war nicht erfolgreich!");
                Intent intent = new Intent();
                intent.putExtra("menge", 0);
                intent.putExtra("position", position);
                setResult(1, intent);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            Intent intent = new Intent();
            intent.putExtra("menge", 0);
            intent.putExtra("position", position);
            setResult(1, intent);
        }
        if (loadingDialogFragment != null) {
            loadingDialogFragment.dismiss();
        }
        previousQuery = "";
        finish();
    }

    public void doSearch(String search) {
        CommunicationCommon.hideKeyboard(this);
        if (search != null && artikel != null && !search.isEmpty()) {
            String standort = prefs.getString("standort", null);
            LagerplatzBestand lb = CommunicationSql.getLagerplatzBestandByStandortEan(sqlServerData, standort, search);
            if (lb != null && lager.contains(lb.getLager0())) {
                int positionLager = adapterLager.getPosition(lb.getLager0());
                acs_lager.setSelection(positionLager);
            } else {
                Toast.makeText(getApplicationContext(), "Keine gültige Lagerplatz-EAN!", Toast.LENGTH_LONG).show();
                buchungViewModel.setSearch("");
            }
        }
        previousQuery = "";
    }
}


