package at.rihnet.rihnetlogistikmde.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.android.material.button.MaterialButton;

import java.sql.Connection;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityMainBinding;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.freierwareneingang.FreierWareneingangActivity;
import at.rihnet.rihnetlogistikmde.ui.info.InfoDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.InfoKorrekturActivity;
import at.rihnet.rihnetlogistikmde.ui.inventur.InventurActivity;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.settings.SettingsActivity;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.UmlagerungActivity;
import at.rihnet.rihnetlogistikmde.ui.verbindungentesten.VerbindungenTestenActivity;

public class MainActivity extends AppCompatActivity {
    //private final String TAG = "RIHNet";
    private LoadingDialogFragment loadingDialogFragment;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private MaterialButton btn_umlagerung;
    private MaterialButton btn_infokorrektur;
    private MaterialButton btn_freierwareneingang;
    private MaterialButton btn_inventur;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        at.rihnet.rihnetlogistikmde.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setLogo(R.drawable.ic_transit_enterexit);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        btn_umlagerung = binding.btnUmlagerung;
        btn_infokorrektur = binding.btnInfokorrektur;
        btn_freierwareneingang = binding.btnFreierwareneingang;
        btn_inventur = binding.btnInventur;

        btn_umlagerung.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, UmlagerungActivity.class)));

        btn_infokorrektur.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, InfoKorrekturActivity.class)));

        btn_freierwareneingang.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, FreierWareneingangActivity.class)));

        btn_inventur.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, InventurActivity.class)));

        loadingDialogFragment = LoadingDialogFragment.newInstance("Verbindung zum SQL-Server und zur SelectLine-API wird geprüft...");
        loadingDialogFragment.setCancelable(false);

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == 1) {
                        testSqlConnection();
                    }
                });

        testSqlConnection();
    }

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

    private void testSqlConnection() {
        loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");
        new Handler().postDelayed(() -> {
            TestSqlConnectionAsyncTask testSqlConnectionAsyncTask = new TestSqlConnectionAsyncTask();
            testSqlConnectionAsyncTask.execute();
        }, 300);
    }

    public class TestSqlConnectionAsyncTask extends AsyncTaskExecutorService<Void, Void, String> {

        @Override
        protected String doInBackground(Void unused) throws Exception {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String ipadresse = prefs.getString("ipadresse", "");
                String port = prefs.getString("port", "");
                String datenbank = prefs.getString("datenbank", "");
                String instance = prefs.getString("instance", "");
                String benutzername = prefs.getString("benutzername", "");
                String kennwort = prefs.getString("kennwort", "");
                String appKey = prefs.getString("appkey", "");
                String baseAddress = prefs.getString("baseaddress", "");
                String userName = prefs.getString("username", "");
                String password = prefs.getString("password", "");
                if(ipadresse.isEmpty() || port.isEmpty() || datenbank.isEmpty() || instance.isEmpty() || benutzername.isEmpty() || kennwort.isEmpty() || appKey.isEmpty() || baseAddress.isEmpty() || userName.isEmpty() || password.isEmpty()){
                    return "Einstellungen für den Verbindungsaufbau der App nicht vollständig!";
                }
                SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);
                Connection connection = CommunicationSql.getConnection(sqlServerData);
                if (connection == null) {
                    return "Error: Keine Verbindung zum SQL-Server möglich!";
                }
                if (!CommunicationSelectLine.login(appKey, baseAddress, userName, password)) {
                    return "Error: Keine Verbindung mit der SelectLine-API möglich!";
                }
            } catch (Exception e) {
                return e.getMessage();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.isEmpty()) {
                btn_umlagerung.setEnabled(true);
                btn_umlagerung.setEnabled(true);
                btn_infokorrektur.setEnabled(true);
                btn_freierwareneingang.setEnabled(true);
                btn_inventur.setEnabled(true);
            } else {
                btn_umlagerung.setEnabled(false);
                btn_infokorrektur.setEnabled(false);
                btn_freierwareneingang.setEnabled(false);
                btn_inventur.setEnabled(false);
                Toast.makeText(getApplicationContext(), "Error: " + s, Toast.LENGTH_LONG).show();
            }
            if (loadingDialogFragment != null) {
                loadingDialogFragment.dismiss();
            }
        }
    }
}