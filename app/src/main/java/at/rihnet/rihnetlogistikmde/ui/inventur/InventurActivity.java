package at.rihnet.rihnetlogistikmde.ui.inventur;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityInventurBinding;
import at.rihnet.rihnetlogistikmde.models.SelectLine.Inventory;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;

public class InventurActivity extends AppCompatActivity {
    private static final String TAG = "RIHNet";
    private final List<Inventory> inventories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityInventurBinding binding = ActivityInventurBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_task_alt);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv_inventur = binding.rvInventur;
        TextView tv_empty = binding.tvEmpty;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String standort = prefs.getString("standort", "");
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
        SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

        String standortBezeichnung = CommunicationSql.getStandortBezeichnungByStandort(sqlServerData, standort);
        try {
            if (CommunicationSelectLine.login(appKey, baseAddress, userName, password)) {
                List<Inventory> i = CommunicationSelectLine.getInventories("1", standortBezeichnung);
                if (i != null) {
                    inventories.addAll(i);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }

        InventurRecyclerViewAdapter inventurRecyclerViewAdapter = new InventurRecyclerViewAdapter(inventories);
        inventurRecyclerViewAdapter.setOnItemClickListener(position -> {
            Inventory inventory = inventurRecyclerViewAdapter.getInventoryAt(position);
            Bundle bundle = new Bundle();
            bundle.putSerializable("inventory", inventory);
            bundle.putInt("position", position);
            Intent intent = new Intent(InventurActivity.this, InventurErfassungActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        });
        rv_inventur.setLayoutManager(new LinearLayoutManager(this));
        rv_inventur.setHasFixedSize(false);
        rv_inventur.setItemAnimator(new DefaultItemAnimator());
        rv_inventur.setAdapter(inventurRecyclerViewAdapter);

        if (inventurRecyclerViewAdapter.getItemCount() > 0) {
            rv_inventur.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        } else {
            rv_inventur.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        }
    }


}