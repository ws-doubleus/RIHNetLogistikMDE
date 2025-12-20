package at.rihnet.rihnetlogistikmde.ui.verbindungentesten;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.radiobutton.MaterialRadioButton;

import java.sql.Connection;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.Db;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityVerbindungenTestenBinding;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;

public class VerbindungenTestenActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private LoadingDialogFragment loadingDialogFragment;

    private MaterialRadioButton rb_sqlserver;
    private ImageView iv_status;
    private TextView tv_status;
    private TextView tv_error;

    // Executor für Hintergrund-Operationen
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        ActivityVerbindungenTestenBinding binding = ActivityVerbindungenTestenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_network_check);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // SharedPreferences + Lade-Dialog
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadingDialogFragment = LoadingDialogFragment.newInstance("Verbindung wird geprüft...");
        loadingDialogFragment.setCancelable(false);

        // UI-Elemente
        rb_sqlserver = binding.rbSqlserver;
        MaterialRadioButton rb_selectlineapi = binding.rbSelectlineapi;
        iv_status = binding.ivStatus;
        tv_status = binding.tvStatus;
        tv_error = binding.tvError;
        Button btn_reset = binding.btnReset;
        Button btn_testen = binding.btnTesten;

        // State zurücksetzen bei Radiobutton-Wechsel
        rb_sqlserver.setOnCheckedChangeListener((compoundButton, b) -> reset());
        rb_selectlineapi.setOnCheckedChangeListener((compoundButton, b) -> reset());

        // Reset-Button
        btn_reset.setOnClickListener(view -> reset());

        // Test-Button
        btn_testen.setOnClickListener(view -> {
            reset();
            if (rb_sqlserver.isChecked()) {
                testSqlServerConnection();
            } else {
                testSelectLineApiConnection();
            }
        });
    }

    /**
     * Setzt den UI-State zurück.
     */
    private void reset() {
        iv_status.setVisibility(View.GONE);
        tv_status.setVisibility(View.GONE);
        tv_error.setVisibility(View.GONE);
    }

    /**
     * Prüft die Verbindung zum SQL-Server (asynchron).
     */
    private void testSqlServerConnection() {
        loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");

        // Kleiner Delay von 700ms (optischer Effekt)
        new Handler().postDelayed(() -> {
            // Im Hintergrund ausführen
            executorService.execute(() -> {
                String result = doTestSqlServerConnectionInBackground();
                // Anschließend im Main-Thread das UI updaten
                runOnUiThread(() -> handleConnectionResult(result));
            });
        }, 700);
    }

    /**
     * Prüft die Verbindung zur SelectLine-API (asynchron).
     */
    private void testSelectLineApiConnection() {
        loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");

        new Handler().postDelayed(() -> {
            executorService.execute(() -> {
                String result = doTestSelectLineApiInBackground();
                runOnUiThread(() -> handleConnectionResult(result));
            });
        }, 700);
    }

    /**
     * Führt den eigentlichen SQL-Server-Test durch (Hintergrund).
     */
    private String doTestSqlServerConnectionInBackground() {
        try {
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String ipadresse = p.getString("ipadresse", "");
            String port = p.getString("port", "");
            String datenbank = p.getString("datenbank", "");
            String instance = p.getString("instance", "");
            String benutzer = p.getString("benutzername", "");
            String kennwort = p.getString("kennwort", "");

            // --- Plausibilitäts‑Check --------------------------------------------------
            if (ipadresse.isEmpty() || port.isEmpty() || datenbank.isEmpty() || instance.isEmpty()
                    || benutzer.isEmpty() || kennwort.isEmpty()) {
                return "Einstellungen für den Verbindungsaufbau sind unvollständig!";
            }
            Db.conn();
        } catch (Exception e) {
            return e.getMessage();
        }
        return ""; // Alles OK
    }

    /**
     * Führt den eigentlichen SelectLine-API-Test durch (Hintergrund).
     */
    private String doTestSelectLineApiInBackground() {
        try {
            String appKey = prefs.getString("appkey", "");
            String baseAddress = prefs.getString("baseaddress", "");
            String userName = prefs.getString("username", "");
            String password = prefs.getString("password", "");

            if (!CommunicationSelectLine.login(appKey, baseAddress, userName, password)) {
                return "Error: Keine Verbindung mit der SelectLine-API möglich!";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return ""; // Alles OK
    }

    /**
     * Zeigt das Ergebnis im UI an und schließt den Lade-Dialog.
     */
    private void handleConnectionResult(String result) {
        if (loadingDialogFragment != null) {
            loadingDialogFragment.dismiss();
        }

        if (result.isEmpty()) {
            // OK
            iv_status.setVisibility(View.VISIBLE);
            iv_status.setImageResource(R.drawable.ic_check_circle_outline);
            iv_status.setColorFilter(ContextCompat.getColor(this, R.color.green_500),
                    android.graphics.PorterDuff.Mode.SRC_IN);

            tv_status.setText(getString(R.string.text_verbindungen_testen_ok));
            tv_status.setTextColor(ContextCompat.getColor(this, R.color.green_500));
            tv_status.setVisibility(View.VISIBLE);

            tv_error.setText(null);
            tv_error.setVisibility(View.GONE);

        } else {
            // Fehler
            iv_status.setVisibility(View.VISIBLE);
            iv_status.setImageResource(R.drawable.ic_error_outline);
            iv_status.setColorFilter(ContextCompat.getColor(this, R.color.red_500),
                    android.graphics.PorterDuff.Mode.SRC_IN);

            tv_status.setText(getString(R.string.text_verbindungen_testen_error));
            tv_status.setTextColor(ContextCompat.getColor(this, R.color.red_500));
            tv_status.setVisibility(View.VISIBLE);

            tv_error.setText(result);
            tv_error.setVisibility(View.VISIBLE);
        }
    }
}
