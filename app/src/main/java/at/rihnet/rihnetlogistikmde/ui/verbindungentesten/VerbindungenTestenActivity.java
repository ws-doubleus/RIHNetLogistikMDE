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

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityVerbindungenTestenBinding;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;

public class VerbindungenTestenActivity extends AppCompatActivity {
    //private final String TAG = "RIHNet";
    private SharedPreferences prefs;
    private LoadingDialogFragment loadingDialogFragment;
    private MaterialRadioButton rb_sqlserver;
    private ImageView iv_status;
    private TextView tv_status;
    private TextView tv_error;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        ActivityVerbindungenTestenBinding binding = ActivityVerbindungenTestenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setLogo(R.drawable.ic_network_check);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        loadingDialogFragment = LoadingDialogFragment.newInstance("Verbindung wird geprüft...");
        loadingDialogFragment.setCancelable(false);

        rb_sqlserver = binding.rbSqlserver;
        MaterialRadioButton rb_selectlineapi = binding.rbSelectlineapi;
        iv_status = binding.ivStatus;
        tv_status = binding.tvStatus;
        tv_error = binding.tvError;
        Button btn_reset = binding.btnReset;
        Button btn_testen = binding.btnTesten;

        rb_sqlserver.setOnCheckedChangeListener((compoundButton, b) -> reset());

        rb_selectlineapi.setOnCheckedChangeListener((compoundButton, b) -> reset());

        btn_reset.setOnClickListener(view -> reset());

        btn_testen.setOnClickListener(view -> {
            reset();
            if (rb_sqlserver.isChecked()) {
                testSqlServerConnection();
            } else {
                testSelectLineApiConnection();
            }
        });
    }

    private void reset() {
        iv_status.setVisibility(View.GONE);
        tv_status.setVisibility(View.GONE);
        tv_error.setVisibility(View.GONE);
    }

    private void testSqlServerConnection() {
        loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");
        new Handler().postDelayed(() -> {
            TestSqlServerConnectionAsyncTask testSqlServerConnectionAsyncTask = new TestSqlServerConnectionAsyncTask();
            testSqlServerConnectionAsyncTask.execute();
        }, 700);
    }

    private void testSelectLineApiConnection() {
        loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");
        new Handler().postDelayed(() -> {
            TestSelectLineApiConnectionAsyncTask testSelectLineApiConnectionAsyncTask = new TestSelectLineApiConnectionAsyncTask();
            testSelectLineApiConnectionAsyncTask.execute();
        }, 700);
    }

    public class TestSqlServerConnectionAsyncTask extends AsyncTaskExecutorService<Void, Void, String> {

        @Override
        protected String doInBackground(Void unused) throws Exception {
            try {
                String ipadresse = prefs.getString("ipadresse", "");
                String port = prefs.getString("port", "");
                String datenbank = prefs.getString("datenbank", "");
                String instance = prefs.getString("instance", "");
                String benutzername = prefs.getString("benutzername", "");
                String kennwort = prefs.getString("kennwort", "");
                SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);
                Connection connection = CommunicationSql.getConnection(sqlServerData);
                if (connection == null) {
                    return "Error: Keine Verbindung zum SQL-Server möglich!";
                }
            } catch (Exception e) {
                return e.getMessage();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.isEmpty()) {
                iv_status.setVisibility(View.VISIBLE);
                iv_status.setImageResource(R.drawable.ic_check_circle_outline);
                iv_status.setColorFilter(ContextCompat.getColor(VerbindungenTestenActivity.this, R.color.green_500), android.graphics.PorterDuff.Mode.SRC_IN);
                tv_status.setText(getResources().getText(R.string.text_verbindungen_testen_ok));
                tv_status.setTextColor(ContextCompat.getColor(VerbindungenTestenActivity.this, R.color.green_500));
                tv_status.setVisibility(View.VISIBLE);
                tv_error.setText(null);
                tv_error.setVisibility(View.GONE);
            } else {
                iv_status.setVisibility(View.VISIBLE);
                iv_status.setImageResource(R.drawable.ic_error_outline);
                iv_status.setColorFilter(ContextCompat.getColor(VerbindungenTestenActivity.this, R.color.red_500), android.graphics.PorterDuff.Mode.SRC_IN);
                tv_status.setText(getResources().getText(R.string.text_verbindungen_testen_error));
                tv_status.setTextColor(ContextCompat.getColor(VerbindungenTestenActivity.this, R.color.red_500));
                tv_status.setVisibility(View.VISIBLE);
                tv_error.setText(s);
                tv_error.setVisibility(View.VISIBLE);
            }
            if (loadingDialogFragment != null) {
                loadingDialogFragment.dismiss();
            }
        }
    }

    public class TestSelectLineApiConnectionAsyncTask extends AsyncTaskExecutorService<Void, Void, String> {

        @Override
        protected String doInBackground(Void unused) throws Exception {
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
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.isEmpty()) {
                iv_status.setVisibility(View.VISIBLE);
                iv_status.setImageResource(R.drawable.ic_check_circle_outline);
                iv_status.setColorFilter(ContextCompat.getColor(VerbindungenTestenActivity.this, R.color.green_500), android.graphics.PorterDuff.Mode.SRC_IN);
                tv_status.setText(getResources().getText(R.string.text_verbindungen_testen_ok));
                tv_status.setTextColor(ContextCompat.getColor(VerbindungenTestenActivity.this, R.color.green_500));
                tv_status.setVisibility(View.VISIBLE);
                tv_error.setText(null);
                tv_error.setVisibility(View.GONE);
            } else {
                iv_status.setVisibility(View.VISIBLE);
                iv_status.setImageResource(R.drawable.ic_error_outline);
                iv_status.setColorFilter(ContextCompat.getColor(VerbindungenTestenActivity.this, R.color.red_500), android.graphics.PorterDuff.Mode.SRC_IN);
                tv_status.setText(getResources().getText(R.string.text_verbindungen_testen_error));
                tv_status.setTextColor(ContextCompat.getColor(VerbindungenTestenActivity.this, R.color.red_500));
                tv_status.setVisibility(View.VISIBLE);
                tv_error.setText(s);
                tv_error.setVisibility(View.VISIBLE);
            }
            if (loadingDialogFragment != null) {
                loadingDialogFragment.dismiss();
            }
        }
    }
}