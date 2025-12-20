package at.rihnet.rihnetlogistikmde.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.Db;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityMainBinding;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.info.InfoDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.InfoKorrekturActivity;
import at.rihnet.rihnetlogistikmde.ui.inventur.InventurActivity;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.paketlabel.PaketlabelActivity;
import at.rihnet.rihnetlogistikmde.ui.settings.SettingsActivity;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.UmlagerungActivity;
import at.rihnet.rihnetlogistikmde.ui.verbindungentesten.VerbindungenTestenActivity;
import at.rihnet.rihnetlogistikmde.ui.warenausgang.WarenausgangActivity;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.WareneingangActivity;

public class MainActivity extends AppCompatActivity {

    private LoadingDialogFragment loadingDialogFragment;

    private ActivityResultLauncher<Intent> someActivityResultLauncher;

    private MaterialCardView btn_umlagerung;
    private MaterialCardView btn_infokorrektur;
    private MaterialCardView btn_warenausgang;
    private MaterialCardView btn_wareneingang;
    private MaterialCardView btn_inventur;
    private MaterialCardView btn_paketlabel;

    // Executor für Hintergrund-Operationen
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setLogo(R.drawable.ic_transit_enterexit);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        // Buttons
        btn_umlagerung = binding.btnUmlagerung;
        btn_infokorrektur = binding.btnInfokorrektur;
        btn_warenausgang = binding.btnWarenausgang;
        btn_wareneingang = binding.btnWareneingang;
        btn_inventur = binding.btnInventur;
        btn_paketlabel = binding.btnPaketlabel;

        // OnClickListener
        btn_umlagerung.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, UmlagerungActivity.class)));
        btn_infokorrektur.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, InfoKorrekturActivity.class)));
        btn_warenausgang.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, WarenausgangActivity.class)));
        btn_wareneingang.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, WareneingangActivity.class)));
        btn_inventur.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, InventurActivity.class)));
        btn_paketlabel.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, PaketlabelActivity.class)));

        // Loading-Dialog
        loadingDialogFragment = LoadingDialogFragment.newInstance(
                "Verbindung zum SQL-Server und zur SelectLine-API wird geprüft..."
        );
        loadingDialogFragment.setCancelable(false);

        // ActivityResultLauncher
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == 1) {
                        // Wenn aus den Settings zurück und ResultCode == 1, erneut Verbindung prüfen
                        testSqlConnection();
                    }
                }
        );

        // Direkt beim Start testen
        testSqlConnection();
    }

    // ---------------------------------------------
    // Menü
    // ---------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            someActivityResultLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_test) {
            startActivity(new Intent(this, VerbindungenTestenActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_info) {
            showInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InfoDialogFragment infoDialogFragment = InfoDialogFragment.newInstance();
        infoDialogFragment.show(fm, "fragment_info_dialog");
    }

    // ---------------------------------------------
    // Verbindung prüfen
    // ---------------------------------------------
    private void testSqlConnection() {
        // Lade-Dialog zeigen
        loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");

        // Optional: kleiner Delay, rein aus optischen Gründen
        new Handler().postDelayed(() -> {
            // Asynchron ausführen
            executorService.execute(() -> {
                String result = doTestSqlConnectionInBackground();

                // Anschließend im Main-Thread UI aktualisieren
                runOnUiThread(() -> {
                    if (result.isEmpty()) {
                        // Alles OK -> Buttons aktivieren
                        btn_umlagerung.setEnabled(true);
                        btn_infokorrektur.setEnabled(true);
                        btn_warenausgang.setEnabled(true);
                        btn_wareneingang.setEnabled(true);
                        btn_inventur.setEnabled(true);
                        btn_paketlabel.setEnabled(true);
                    } else {
                        // Verbindung fehlgeschlagen -> Buttons deaktivieren
                        btn_umlagerung.setEnabled(false);
                        btn_infokorrektur.setEnabled(false);
                        btn_warenausgang.setEnabled(false);
                        btn_wareneingang.setEnabled(false);
                        btn_inventur.setEnabled(false);
                        btn_paketlabel.setEnabled(false);

                        Toast.makeText(getApplicationContext(), "Error: " + result, Toast.LENGTH_LONG).show();
                        Log.e("WS", result);
                    }
                    if (loadingDialogFragment != null) {
                        loadingDialogFragment.dismissAllowingStateLoss();
                    }
                });
            });
        }, 300);
    }

    /**
     * Führt den eigentlichen Verbindungscheck aus (SQL + SelectLine).
     * Wird im Hintergrund-Thread ausgeführt.
     */
    private String doTestSqlConnectionInBackground() {

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // --- Werte aus Preferences -------------------------------------------------
        String ipadresse = p.getString("ipadresse", "");
        String port = p.getString("port", "");
        String datenbank = p.getString("datenbank", "");
        String instance = p.getString("instance", "");
        String benutzer = p.getString("benutzername", "");
        String kennwort = p.getString("kennwort", "");

        String appKey = p.getString("appkey", "");
        String baseAddress = p.getString("baseaddress", "");
        String userName = p.getString("username", "");
        String password = p.getString("password", "");

        // --- Plausibilitäts‑Check --------------------------------------------------
        if (ipadresse.isEmpty() || port.isEmpty() || datenbank.isEmpty() || instance.isEmpty()
                || benutzer.isEmpty() || kennwort.isEmpty()
                || appKey.isEmpty() || baseAddress.isEmpty()
                || userName.isEmpty() || password.isEmpty()) {
            return "Einstellungen für den Verbindungsaufbau sind unvollständig!";
        }

        // --- SQL‑Verbindung testen -------------------------------------------------
        try {
            Connection connection = Db.conn();
            // OK – falls nicht, wird Exception geworfen
        } catch (Exception e) {
            return "SQL‑Server nicht erreichbar: " + e.getMessage();
        }

        // --- SelectLine-API Verbindung testen --------------------------------------
        try {
            if (!CommunicationSelectLine.login(appKey, baseAddress, userName, password)) {
                return "Error: Keine Verbindung mit der SelectLine-API möglich!";
            }
        } catch (JSONException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }

        return "";
    }
}
