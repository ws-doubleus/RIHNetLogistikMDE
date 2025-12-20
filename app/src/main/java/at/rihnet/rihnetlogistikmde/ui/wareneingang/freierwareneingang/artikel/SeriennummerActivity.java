package at.rihnet.rihnetlogistikmde.ui.wareneingang.freierwareneingang.artikel;

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

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivitySeriennummerBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.SeriennummerCharge;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;

public class SeriennummerActivity extends AppCompatActivity {
    private static final String TAG = "RIHNet";
    private SeriennummerRecyclerViewAdapter seriennummerRecyclerViewAdapter;
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;
    private ActivitySeriennummerBinding binding;
    private LoadingDialogFragment loadingDialogFragment;
    private SeriennummerChargeViewModel seriennummerChargeViewModel;

    // Helper class for params
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
        binding = ActivitySeriennummerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialogFragment = LoadingDialogFragment.newInstance("Seriennummern werden geladen...");
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

        RecyclerView rv_seriennummer = binding.rvSeriennummer;
        
        // Initialer Adapter (leer)
        seriennummerRecyclerViewAdapter = new SeriennummerRecyclerViewAdapter(new ArrayList<>());
        seriennummerRecyclerViewAdapter.setOnItemClickListener(position -> {
            SeriennummerCharge seriennummerCharge = seriennummerRecyclerViewAdapter.GetSeriennummerChargeAt(position);
            Intent intent = new Intent();
            intent.putExtra("seriennummer", seriennummerCharge.getNummer());
            intent.putExtra("lager", seriennummerCharge.getLager());
            setResult(1, intent);
            finish();
        });

        rv_seriennummer.setLayoutManager(new LinearLayoutManager(this));
        rv_seriennummer.setHasFixedSize(true);
        rv_seriennummer.setItemAnimator(new DefaultItemAnimator());
        rv_seriennummer.setAdapter(seriennummerRecyclerViewAdapter);

        // Daten holen
        String artikelnummmer = getIntent().getStringExtra("artikelnummer");
        //noinspection unchecked
        List<Artikel> queueList = (List<Artikel>) getIntent().getSerializableExtra("queueList");

        // Async laden starten
        if (artikelnummmer != null) {
            loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");
            new LoadSeriennummernAsyncTask().execute(new LoadParams(artikelnummmer, queueList));
        }
    }

    private void updateUI(List<SeriennummerCharge> list) {
        TextView tv_empty = binding.tvEmpty;
        RecyclerView rv_seriennummer = binding.rvSeriennummer;

        seriennummerChargeViewModel.setSeriennummerCharge(list);
        seriennummerRecyclerViewAdapter = new SeriennummerRecyclerViewAdapter(list);
        seriennummerRecyclerViewAdapter.setOnItemClickListener(position -> {
            SeriennummerCharge seriennummerCharge = seriennummerRecyclerViewAdapter.GetSeriennummerChargeAt(position);
            Intent intent = new Intent();
            intent.putExtra("seriennummer", seriennummerCharge.getNummer());
            intent.putExtra("lager", seriennummerCharge.getLager());
            setResult(1, intent);
            finish();
        });
        rv_seriennummer.setAdapter(seriennummerRecyclerViewAdapter);

        if (list != null && !list.isEmpty()) {
            rv_seriennummer.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        } else {
            rv_seriennummer.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        }
    }

    public class LoadSeriennummernAsyncTask extends AsyncTaskExecutorService<LoadParams, Void, List<SeriennummerCharge>> {
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

                List<SeriennummerCharge> loadedList = CommunicationSql.getSeriennummerCharge(sqlServerData, params.artikelnummer, standort);
                List<SeriennummerCharge> filteredList = new ArrayList<>(loadedList);

                if (params.queueList != null) {
                     // Filter logik: Wenn Seriennummer in Queue ist, entfernen
                     // Hinweis: Seriennummern sind eindeutig (Bestand meist 1). 
                     // Falls Queue Eintrag existiert, ist die SN "vergeben".
                     List<String> queueSeriennummern = params.queueList.stream()
                             .filter(a -> a.getSeriennummer() != null)
                             .map(Artikel::getSeriennummer)
                             .collect(Collectors.toList());

                     filteredList.removeIf(sn -> queueSeriennummern.contains(sn.getNummer()));
                }

                return filteredList;
            } catch (Exception e) {
                Log.e(TAG, "Error loading seriennummern", e);
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
                    if (decodeResult.getText() != null && seriennummerRecyclerViewAdapter != null) {
                         // Workaround Datalogic manchmal trailing char? User Code hatte substring
                         // Wir nehmen erst mal den vollen Text oder prüfen auf null
                        String text = decodeResult.getText();
                         // Falls nötig: text = text.trim();
                        final String filterText = text;
                        runOnUiThread(() -> seriennummerRecyclerViewAdapter.getFilter().filter(filterText));
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
                if (seriennummerRecyclerViewAdapter != null && seriennummerRecyclerViewAdapter.getFilter() != null) {
                    seriennummerRecyclerViewAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
        return true;
    }
}
