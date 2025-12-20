package at.rihnet.rihnetlogistikmde;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import org.json.JSONException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private static final String TAG = "MainActivity";

    private LoadingDialogFragment loadingDialogFragment;
    private ActivityResultLauncher<Intent> settingsLauncher;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private MaterialToolbar toolbar;
    private TextView tvStatus; // optional (wenn du es im Layout hast)

    // Cards
    private MaterialCardView cardInfokorrektur, cardWarenausgang, cardWareneingang, cardUmlagerung, cardInventur, cardPaketlabel;

    // Icons
    private ImageView iconInfokorrektur, iconWarenausgang, iconWareneingang, iconUmlagerung, iconInventur, iconPaketlabel;

    // TextViews
    private TextView textInfokorrektur, textWarenausgang, textWareneingang, textUmlagerung, textInventur, textPaketlabel;

    // --- Connection UI state ---
    private enum ConnectionState { CHECKING, ONLINE, OFFLINE }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Optional: falls du einen Status-Text im Layout hast
        //tvStatus = findViewById(R.id.tv_connection_status); // wenn nicht vorhanden -> einfach aus layout entfernen ODER diese Zeile löschen

        // Cards
        cardInfokorrektur = findViewById(R.id.btn_infokorrektur);
        cardWarenausgang = findViewById(R.id.btn_warenausgang);
        cardWareneingang = findViewById(R.id.btn_wareneingang);
        cardUmlagerung = findViewById(R.id.btn_umlagerung);
        cardInventur = findViewById(R.id.btn_inventur);
        cardPaketlabel = findViewById(R.id.btn_paketlabel);

        // Icons
        iconInfokorrektur = findViewById(R.id.icon_infokorrektur);
        iconWarenausgang = findViewById(R.id.icon_warenausgang);
        iconWareneingang = findViewById(R.id.icon_wareneingang);
        iconUmlagerung = findViewById(R.id.icon_umlagerung);
        iconInventur = findViewById(R.id.icon_inventur);
        iconPaketlabel = findViewById(R.id.icon_paketlabel);

        // Text
        textInfokorrektur = findViewById(R.id.text_infokorrektur);
        textWarenausgang = findViewById(R.id.text_warenausgang);
        textWareneingang = findViewById(R.id.text_wareneingang);
        textUmlagerung = findViewById(R.id.text_umlagerung);
        textInventur = findViewById(R.id.text_inventur);
        textPaketlabel = findViewById(R.id.text_paketlabel);

        // Click listeners (Guard)
        cardInfokorrektur.setOnClickListener(v -> {
            if (!cardInfokorrektur.isEnabled()) return;
            startActivity(new Intent(this, InfoKorrekturActivity.class));
        });
        cardWarenausgang.setOnClickListener(v -> {
            if (!cardWarenausgang.isEnabled()) return;
            startActivity(new Intent(this, WarenausgangActivity.class));
        });
        cardWareneingang.setOnClickListener(v -> {
            if (!cardWareneingang.isEnabled()) return;
            startActivity(new Intent(this, WareneingangActivity.class));
        });
        cardUmlagerung.setOnClickListener(v -> {
            if (!cardUmlagerung.isEnabled()) return;
            startActivity(new Intent(this, UmlagerungActivity.class));
        });
        cardInventur.setOnClickListener(v -> {
            if (!cardInventur.isEnabled()) return;
            startActivity(new Intent(this, InventurActivity.class));
        });
        cardPaketlabel.setOnClickListener(v -> {
            if (!cardPaketlabel.isEnabled()) return;
            startActivity(new Intent(this, PaketlabelActivity.class));
        });

        loadingDialogFragment = LoadingDialogFragment.newInstance("Verbindung wird geprüft…");
        loadingDialogFragment.setCancelable(false);

        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> testSqlAndApiConnection()
        );

        // Start: UI auf CHECKING
        setConnectionUiState(ConnectionState.CHECKING, null);
        testSqlAndApiConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            settingsLauncher.launch(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_test) {
            startActivity(new Intent(this, VerbindungenTestenActivity.class));
            return true;
        } else if (id == R.id.action_info) {
            InfoDialogFragment.newInstance().show(getSupportFragmentManager(), "fragment_info_dialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void testSqlAndApiConnection() {
        if (!isFinishing() && !loadingDialogFragment.isAdded()) {
            loadingDialogFragment.show(getSupportFragmentManager(), "loading");
        }

        setConnectionUiState(ConnectionState.CHECKING, null);

        executorService.execute(() -> {
            String errorMessage = doTestSqlAndApiInBackground();

            mainHandler.post(() -> {
                if (!isFinishing() && loadingDialogFragment.isAdded()) {
                    loadingDialogFragment.dismissAllowingStateLoss();
                }

                if (errorMessage == null || errorMessage.isEmpty()) {
                    setConnectionUiState(ConnectionState.ONLINE, null);
                } else {
                    setConnectionUiState(ConnectionState.OFFLINE, errorMessage);
                    Toast.makeText(this, "Fehler: " + errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, errorMessage);
                }
            });
        });
    }

    private String doTestSqlAndApiInBackground() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

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

        if (ipadresse.isEmpty() || port.isEmpty() || datenbank.isEmpty() || instance.isEmpty()
                || benutzer.isEmpty() || kennwort.isEmpty()
                || appKey.isEmpty() || baseAddress.isEmpty()
                || userName.isEmpty() || password.isEmpty()) {
            return "Einstellungen für den Verbindungsaufbau sind unvollständig!";
        }

        // 1) SQL
        try {
            Connection connection = Db.conn();
            if (connection == null) return "SQL-Server nicht erreichbar.";
            connection.close();
        } catch (Exception e) {
            return "SQL-Server nicht erreichbar: " + e.getMessage();
        }

        // 2) API
        try {
            if (!CommunicationSelectLine.login(appKey, baseAddress, userName, password)) {
                return "Keine Verbindung mit der SelectLine-API möglich!";
            }
        } catch (JSONException | NoSuchAlgorithmException | KeyManagementException e) {
            return "Fehler bei der SelectLine-API-Verbindung: " + e.getMessage();
        }

        return "";
    }

    // -------------------- UI State --------------------

    private void setConnectionUiState(ConnectionState state, String errorMessage) {
        switch (state) {
            case CHECKING:
                setAllCardsEnabled(false);
                if (toolbar != null) toolbar.setSubtitle("Verbindung wird geprüft…");
                if (tvStatus != null) tvStatus.setText("Status: Prüfe Verbindung…");
                break;

            case ONLINE:
                setAllCardsEnabled(true);
                if (toolbar != null) toolbar.setSubtitle(null);
                if (tvStatus != null) tvStatus.setText("Status: Online");
                break;

            case OFFLINE:
                setAllCardsEnabled(false);
                if (toolbar != null) toolbar.setSubtitle("Offline / Verbindung fehlt");
                if (tvStatus != null) {
                    tvStatus.setText(errorMessage == null ? "Status: Offline" : "Status: Offline – " + errorMessage);
                }
                break;
        }
    }

    // -------------------- M3-konformes Disabled Styling --------------------

    private static int withAlpha(int color, float alpha01) {
        int a = Math.round(255f * alpha01);
        return (color & 0x00FFFFFF) | (a << 24);
    }

    private void applyCardEnabledState(MaterialCardView card, ImageView icon, TextView text, boolean enabled) {
        // Theme tokens (Material 3)
        int primary = MaterialColors.getColor(card, com.google.android.material.R.attr.colorOnSurface);
        int onSurface = MaterialColors.getColor(card, com.google.android.material.R.attr.colorOnSurface);
        int surfaceContainerLow = MaterialColors.getColor(card, com.google.android.material.R.attr.colorSurfaceContainerLow);
        int outline = MaterialColors.getColor(card, com.google.android.material.R.attr.colorOutline);

        // M3 typische disabled Alphas
        int enabledContent = primary;
        int enabledContainer = surfaceContainerLow;
        int enabledStroke = withAlpha(outline, 0.35f);

        int disabledContent = withAlpha(onSurface, 0.38f);   // ✅ M3 disabled content
        int disabledContainer = withAlpha(onSurface, 0.12f); // ✅ M3 disabled container
        int disabledStroke = withAlpha(outline, 0.12f);      // ✅ M3 disabled stroke

        int contentColor = enabled ? enabledContent : disabledContent;
        int containerColor = enabled ? enabledContainer : disabledContainer;
        int strokeColor = enabled ? enabledStroke : disabledStroke;

        card.setEnabled(enabled);
        card.setClickable(enabled);
        card.setFocusable(enabled);

        card.setCardBackgroundColor(containerColor);
        card.setStrokeColor(strokeColor);

        icon.setImageTintList(ColorStateList.valueOf(contentColor));
        text.setTextColor(contentColor);

        // optional: minimal ausgegraut zusätzlich
        float alpha = enabled ? 1f : 0.85f;
        icon.setAlpha(alpha);
        text.setAlpha(alpha);
    }

    private void setAllCardsEnabled(boolean enabled) {
        setCardEnabled(cardInfokorrektur, enabled);
        setCardEnabled(cardWarenausgang, enabled);
        setCardEnabled(cardWareneingang, enabled);
        setCardEnabled(cardUmlagerung, enabled);
        setCardEnabled(cardInventur, enabled);
        setCardEnabled(cardPaketlabel, enabled);
    }

    private void setCardEnabled(MaterialCardView card, boolean enabled) {
        if (card == null) return;
        card.setEnabled(enabled);
        card.setClickable(enabled);
        card.setFocusable(enabled);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }
}
