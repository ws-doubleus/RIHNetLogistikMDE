package at.rihnet.rihnetlogistikmde.ui.freierwareneingang.artikel;

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

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityChargeBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.SeriennummerCharge;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;

public class ChargeActivity extends AppCompatActivity {
    private static final String TAG = "RIHNet";
    private ChargeRecyclerViewAdapter chargeRecyclerViewAdapter;
    private final List<SeriennummerCharge> seriennummerChargeList = new ArrayList<>();
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityChargeBinding binding = ActivityChargeBinding.inflate(getLayoutInflater());
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

        List<SeriennummerCharge> scList = CommunicationSql.getSeriennummerCharge(sqlServerData, artikelnummmer, standort);
        seriennummerChargeList.addAll(scList);
        for (SeriennummerCharge item : scList) {
            assert queueList != null;
            int sum = queueList.stream().filter(f -> f.getCharge().equals(item.getNummer())).mapToInt(Artikel::getMenge).sum();
            if (sum >= item.getBestand()) {
                seriennummerChargeList.remove(item);
            } else {
                seriennummerChargeList.stream().filter(f -> f.getNummer().equals(item.getNummer())).findAny().get().setBestand(item.getBestand() - sum);
            }
        }

        SeriennummerChargeViewModel seriennummerChargeViewModel = new ViewModelProvider(this).get(SeriennummerChargeViewModel.class);
        seriennummerChargeViewModel.setSeriennummerCharge(seriennummerChargeList);

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

        RecyclerView rv_charge = binding.rvCharge;
        TextView tv_empty = binding.tvEmpty;
        chargeRecyclerViewAdapter = new ChargeRecyclerViewAdapter(seriennummerChargeViewModel.getSeriennummerCharge().getValue());
        chargeRecyclerViewAdapter.setOnItemClickListener(position -> {
            SeriennummerCharge seriennummer = chargeRecyclerViewAdapter.GetSeriennummerChargeAt(position);
            Intent intent = new Intent();
            intent.putExtra("charge", seriennummer.getNummer());
            intent.putExtra("lager", seriennummer.getLager());
            intent.putExtra("bestand", seriennummer.getBestand());
            setResult(2, intent);
            finish();
            //TODO add data to MAIN-List<>
        });
        rv_charge.setLayoutManager(new LinearLayoutManager(this));
        rv_charge.setHasFixedSize(true);
        rv_charge.setItemAnimator(new DefaultItemAnimator());
        rv_charge.setAdapter(chargeRecyclerViewAdapter);

        if (chargeRecyclerViewAdapter.getItemCount() > 0) {
            rv_charge.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        } else {
            rv_charge.setVisibility(View.GONE);
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
                    //TODO barcodeManager ReadListener

                    //decodeResult.getText().substring(0, decodeResult.getText().length() - 1);
                }
            };
            barcodeManager.addReadListener(readListener);
        } catch (DecodeException e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeManager != null) {
            try {
                barcodeManager.removeReadListener(readListener);
            } catch (Exception e) {
                Log.e(TAG, String.format("%s", e.getMessage()));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.umlagerung_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setQueryHint("Suchen...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                chargeRecyclerViewAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
}