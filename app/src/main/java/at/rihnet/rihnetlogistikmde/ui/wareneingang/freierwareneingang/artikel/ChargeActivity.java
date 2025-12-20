package at.rihnet.rihnetlogistikmde.ui.wareneingang.freierwareneingang.artikel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Optional;

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityChargeBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.SeriennummerCharge;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;

public class ChargeActivity extends AppCompatActivity {
    private static final String TAG = "RIHNet";
    private ActivityChargeBinding binding;
    private ChargeRecyclerViewAdapter chargeRecyclerViewAdapter;
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;
    private LoadingDialogFragment loadingDialogFragment;
    private SeriennummerChargeViewModel seriennummerChargeViewModel;

    // Helper class for passing params to Async Task
    private static class LoadParams {
        String artikelnummer;
        List<Artikel> queueList;

        LoadParams(String artikelnummer, List<Artikel> queueList) {
            this.artikelnummer = artikelnummer;
            this.queueList = queueList;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialogFragment = LoadingDialogFragment.newInstance("Chargen werden geladen...");
        loadingDialogFragment.setCancelable(false);

        seriennummerChargeViewModel = new ViewModelProvider(this).get(SeriennummerChargeViewModel.class);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        Button btn_abbrechen = binding.btnAbbrechen;
        btn_abbrechen.setOnClickListener(view -> finish());

        RecyclerView rv_charge = binding.rvCharge;
        
        // Initialer leerer Adapter
        chargeRecyclerViewAdapter = new ChargeRecyclerViewAdapter(new ArrayList<>());
        chargeRecyclerViewAdapter.setOnItemClickListener(position -> {
            SeriennummerCharge seriennummer = chargeRecyclerViewAdapter.GetSeriennummerChargeAt(position);
            Intent intent = new Intent();
            intent.putExtra("charge", seriennummer.getNummer());
            intent.putExtra("lager", seriennummer.getLager());
            intent.putExtra("bestand", seriennummer.getBestand());
            setResult(2, intent);
            finish();
        });

        rv_charge.setLayoutManager(new LinearLayoutManager(this));
        rv_charge.setHasFixedSize(true);
        rv_charge.setItemAnimator(new DefaultItemAnimator());
        rv_charge.setAdapter(chargeRecyclerViewAdapter);

        // Daten aus Intent holen
        String artikelnummer = getIntent().getStringExtra("artikelnummer");
        //noinspection unchecked
        List<Artikel> queueList = (List<Artikel>) getIntent().getSerializableExtra("queueList");

        // Daten laden (Async)
        if (artikelnummer != null) {
            loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");
            LoadChargeAsyncTask task = new LoadChargeAsyncTask();
            task.execute(new LoadParams(artikelnummer, queueList));
        }
    }

    private void updateUI(List<SeriennummerCharge> list) {
        TextView tv_empty = binding.tvEmpty;
        RecyclerView rv_charge = binding.rvCharge;

        seriennummerChargeViewModel.setSeriennummerCharge(list);
        chargeRecyclerViewAdapter = new ChargeRecyclerViewAdapter(list);
        chargeRecyclerViewAdapter.setOnItemClickListener(position -> {
            SeriennummerCharge seriennummer = chargeRecyclerViewAdapter.GetSeriennummerChargeAt(position);
            Intent intent = new Intent();
            intent.putExtra("charge", seriennummer.getNummer());
            intent.putExtra("lager", seriennummer.getLager());
            intent.putExtra("bestand", seriennummer.getBestand());
            setResult(2, intent);
            finish();
        });
        rv_charge.setAdapter(chargeRecyclerViewAdapter);

        if (list != null && !list.isEmpty()) {
            rv_charge.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        } else {
            rv_charge.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        }
    }

    public class LoadChargeAsyncTask extends AsyncTaskExecutorService<LoadParams, Void, List<SeriennummerCharge>> {
        @Override
        protected List<SeriennummerCharge> doInBackground(LoadParams params) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String standort = prefs.getString("standort", "");
                String ipadresse = prefs.getString("ipadresse", "");
                String port = prefs.getString("port", "");
                String datenbank = prefs.getString("datenbank", "");
                String instance = prefs.getString("instance", "");
                String benutzername = prefs.getString("benutzername", "");
                String kennwort = prefs.getString("kennwort", "");
                SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

                List<SeriennummerCharge> scList = CommunicationSql.getSeriennummerCharge(sqlServerData, params.artikelnummer, standort);
                List<SeriennummerCharge> filteredList = new ArrayList<>(scList);
                
                // Filtern basierend auf Queue
                if (params.queueList != null) {
                    List<SeriennummerCharge> toRemove = new ArrayList<>();
                    
                    for (SeriennummerCharge item : filteredList) {
                        int sum = params.queueList.stream()
                                .filter(f -> f.getCharge() != null && f.getCharge().equals(item.getNummer()))
                                .mapToInt(Artikel::getMenge)
                                .sum();
                                
                        if (sum >= item.getBestand()) {
                            toRemove.add(item);
                        } else {
                            item.setBestand(item.getBestand() - sum);
                        }
                    }
                    filteredList.removeAll(toRemove);
                }
                
                return filteredList;
            } catch (Exception e) {
                Log.e(TAG, "Error loading charges", e);
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<SeriennummerCharge> result) {
            updateUI(result);
            if (loadingDialogFragment != null) {
                loadingDialogFragment.dismiss();
            }
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
                    // Optional: Implementierung für Barcode-Scan um Suche zu filtern
                    String barcode = decodeResult.getText();
                    if (barcode != null && chargeRecyclerViewAdapter != null) {
                        runOnUiThread(() -> chargeRecyclerViewAdapter.getFilter().filter(barcode));
                    }
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
                if (chargeRecyclerViewAdapter != null && chargeRecyclerViewAdapter.getFilter() != null) {
                    chargeRecyclerViewAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
        return true;
    }
}
