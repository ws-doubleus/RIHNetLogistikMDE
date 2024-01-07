package at.rihnet.rihnetlogistikmde.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;

import java.util.Objects;

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityMainBinding;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.InfoKorrekturActivity;
import at.rihnet.rihnetlogistikmde.ui.info.InfoDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.login.LoginActivity;
import at.rihnet.rihnetlogistikmde.ui.settings.SettingsActivity;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.UmlagerungActivity;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "RIHNet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        at.rihnet.rihnetlogistikmde.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setLogo(R.drawable.ic_transit_enterexit);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        MaterialButton btn_umlagerung = binding.btnUmlagerung;
        MaterialButton btn_infokorrektur = binding.btnInfokorrektur;
        MaterialButton btn_auslagerung = binding.btnAuslagerung;

        btn_umlagerung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, UmlagerungActivity.class));
            }
        });

        btn_infokorrektur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, InfoKorrekturActivity.class));
            }
        });

        btn_auslagerung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
        } else if (item.getItemId() == R.id.action_logout) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommunicationSql.connection = null;
        CommunicationSql.sqlServerData = null;
    }

    private void showInfoDialog() {
        FragmentManager fm = getSupportFragmentManager();
        InfoDialogFragment infoDialogFragment = InfoDialogFragment.newInstance();
        infoDialogFragment.show(fm, "fragment_info_dialog");
    }
}