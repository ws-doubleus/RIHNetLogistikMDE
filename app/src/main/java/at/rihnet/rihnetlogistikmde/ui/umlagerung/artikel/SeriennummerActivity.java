package at.rihnet.rihnetlogistikmde.ui.umlagerung.artikel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivitySeriennummerBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.SeriennummerCharge;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.UmlagerungViewModel;

public class SeriennummerActivity extends AppCompatActivity {
    private static final String TAG = "RIHNet";
    private SeriennummerRecyclerViewAdapter seriennummerRecyclerViewAdapter;
    private final List<SeriennummerCharge> seriennummerList = new ArrayList<>();
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        at.rihnet.rihnetlogistikmde.databinding.ActivitySeriennummerBinding binding = ActivitySeriennummerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String standort = prefs.getString("standort", "");
        String ipadresse = prefs.getString("ipadresse", "");
        String port = prefs.getString("port", "");
        String datenbank = prefs.getString("datenbank", "");
        String instance = prefs.getString("instance", "");
        String benutzername = prefs.getString("benutzername", "");
        String kennwort = prefs.getString("kennwort", "");
        SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

        String artikelnummmer = getIntent().getStringExtra("artikelnummer");
        List<Artikel> queueList = (List<Artikel>) getIntent().getSerializableExtra("queueList");

        List<SeriennummerCharge> seriennummerChargeList = CommunicationSql.getSeriennummerCharge(sqlServerData, artikelnummmer, standort);
        assert queueList != null;
        for (Artikel item: queueList) {
            SeriennummerCharge l = seriennummerChargeList.stream().filter(f -> f.getNummer().equals(item.getSeriennummer())).findAny().orElse(null)  ;
            if(l != null){
                seriennummerChargeList.remove(l);
            }
        }
        seriennummerList.addAll(seriennummerChargeList);

        SeriennummerChargeViewModel seriennummerChargeViewModel = new ViewModelProvider(this).get(SeriennummerChargeViewModel.class);
        seriennummerChargeViewModel.setSeriennummerCharge(seriennummerList);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        Button btn_abbrechen = binding.btnAbbrechen;
        btn_abbrechen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        RecyclerView rv_seriennummer = binding.rvSeriennummer;
        TextView tv_empty = binding.tvEmpty;
        seriennummerRecyclerViewAdapter = new SeriennummerRecyclerViewAdapter(seriennummerChargeViewModel.getSeriennummerCharge().getValue());
        seriennummerRecyclerViewAdapter.setOnItemClickListener(position -> {
            SeriennummerCharge seriennummerCharge = seriennummerRecyclerViewAdapter.GetSeriennummerChargeAt(position);
            Intent intent = new Intent();
            intent.putExtra("seriennummer", seriennummerCharge.getNummer());
            intent.putExtra("lager", seriennummerCharge.getLager());
            setResult(1, intent);
            finish();
            //TODO add data to MAIN-List<>
        });
        rv_seriennummer.setLayoutManager(new LinearLayoutManager(this));
        rv_seriennummer.setHasFixedSize(true);
        rv_seriennummer.setItemAnimator(new DefaultItemAnimator());
        rv_seriennummer.setAdapter(seriennummerRecyclerViewAdapter);

        if (seriennummerRecyclerViewAdapter.getItemCount() > 0) {
            rv_seriennummer.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        } else {
            rv_seriennummer.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeManager == null) {
            barcodeManager = new BarcodeManager();
        }
        try {
            readListener = new ReadListener() {
                @Override
                public void onRead(DecodeResult decodeResult) {
                    seriennummerRecyclerViewAdapter.getFilter().filter(decodeResult.getText().substring(0, decodeResult.getText().length() - 1));
                }
            };
            barcodeManager.addReadListener(readListener);
        } catch (DecodeException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeManager != null) {
            try {
                barcodeManager.removeReadListener(readListener);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.umlagerung_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Suchen...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                seriennummerRecyclerViewAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
}