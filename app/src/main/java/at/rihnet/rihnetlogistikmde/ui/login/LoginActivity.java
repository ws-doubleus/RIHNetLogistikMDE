package at.rihnet.rihnetlogistikmde.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityLoginBinding;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.info.InfoDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.main.MainActivity;
import at.rihnet.rihnetlogistikmde.ui.settings.SettingsActivity;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "RIHNet";
    private LoginViewModel loginViewModel;
    private LoadingDialogFragment loadingDialogFragment;
    public static final String BENUTZER_PREFS = "benutzerPrefs";
    public static final String BENUTZER = "benutzer";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        at.rihnet.rihnetlogistikmde.databinding.ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialogFragment = LoadingDialogFragment.newInstance("Anmeldung...");

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setLogo(R.drawable.ic_account_circle);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory()).get(LoginViewModel.class);

        final EditText usernameEditText = binding.etUsername;
        final EditText passwordEditText = binding.etPasswort;
        final Button loginButton = binding.btnLogin;
        final Button beendenButton = binding.btnBeenden;

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());

//                if (loginFormState.getUsernameError() != null) {
//                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
//                }
//                if (loginFormState.getPasswordError() != null) {
//                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
//                }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            loadingDialogFragment.dismiss();
            if (loginResult == null) {
                return;
            }
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                updateUiWithUser(loginResult.getSuccess());
            }
            setResult(Activity.RESULT_OK);

            //Complete and destroy login activity once successful
            //finish();
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString().toUpperCase(), passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            // TODO && loginFormState.isDataValid()
            if (actionId == EditorInfo.IME_ACTION_DONE && loginButton.isEnabled()) {
                tryLogin(usernameEditText.getText().toString(), passwordEditText.getText().toString());
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
            tryLogin(usernameEditText.getText().toString(), passwordEditText.getText().toString());
        });

        beendenButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + " " + model.getDisplayName() + "!";
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        SharedPreferences sharedPreferences = getSharedPreferences(BENUTZER_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BENUTZER, "");
        editor.apply();
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void tryLogin(String username, String pwd) {
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String ipadresse = prefs.getString("ipadresse", null);
                String port = prefs.getString("port", null);
                String datenbank = prefs.getString("datenbank", null);
                String instance = prefs.getString("instance", null);
                String benutzername = prefs.getString("benutzername", null);
                String kennwort = prefs.getString("kennwort", null);
                //CommunicationSql.connection = null;
                //CommunicationSql.sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

                SharedPreferences sharedPreferences = getSharedPreferences(BENUTZER_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(BENUTZER, username.toUpperCase());
                editor.apply();

                loginViewModel.login(username.toUpperCase(), pwd);
            }
        }, 400);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        //menu.removeItem(R.id.action_logout);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_info) {
            showInfoDialog();
            return true;
        } /*else if (item.getItemId() == R.id.action_logout) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void showInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InfoDialogFragment infoDialogFragment = InfoDialogFragment.newInstance();
        infoDialogFragment.show(fm, "fragment_info_dialog");
    }
}