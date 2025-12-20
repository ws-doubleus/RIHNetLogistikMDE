package at.rihnet.rihnetlogistikmde.ui.infokorrektur.lagerplatzinfo;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.ReadListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityLagerplatzinfoBinding;
import at.rihnet.rihnetlogistikmde.models.Lagerplatz;
import at.rihnet.rihnetlogistikmde.models.LagerplatzBestand;
import at.rihnet.rihnetlogistikmde.models.Lagerplatzinfo;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;

public class LagerplatzinfoActivity extends AppCompatActivity {
    private static final String TAG = "RIHNet/Lagerplatzinfo";
    private ActivityLagerplatzinfoBinding binding;
    private AppCompatSpinner spLager;
    private AppCompatSpinner spLagerplatz;
    private RecyclerView rvInfo;
    private SearchView searchView;
    private MenuItem searchMenuItem;
    private final List<Lagerplatz> lagerplatzList = new ArrayList<>();
    private final List<Lagerplatzinfo> infoList = new ArrayList<>();
    private ArrayAdapter<String> adapterLager;
    private ArrayAdapter<Lagerplatz> adapterLagerplatz;
    private LagerplatzinfoRecyclerViewAdapter infoAdapter;
    private LagerplatzinfoViewModel viewModel;
    private ExecutorService executor;
    private Handler mainHandler;
    private BarcodeManager barcodeManager;
    private ReadListener readListener;
    private LoadingDialogFragment loadingDialog;
    private SqlServerData sqlServerData;
    private String standort;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLagerplatzinfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initExecutor();
        initToolbar();
        initDatabaseConnection();
        initUIReferences();
        initRecyclerView();
        initSpinners();
        initViewModel();
        initBarcode();
        loadSpinnerData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseBarcode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void initExecutor() {
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void initToolbar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_fmd_bad);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initDatabaseConnection() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        standort = prefs.getString("standort", null);
        sqlServerData = new SqlServerData(
                prefs.getString("ipadresse", ""),
                prefs.getString("port", ""),
                prefs.getString("datenbank", ""),
                prefs.getString("instance", ""),
                prefs.getString("benutzername", ""),
                prefs.getString("kennwort", "")
        );
    }

    private void initUIReferences() {
        spLager = binding.acsLager;
        spLagerplatz = binding.acsLagerplatz;
        rvInfo = binding.rvLagerplatzinfo;
        loadingDialog = LoadingDialogFragment.newInstance("Lagerplatzinfo wird geladen...");
        loadingDialog.setCancelable(false);
    }

    private void initRecyclerView() {
        infoAdapter = new LagerplatzinfoRecyclerViewAdapter(infoList);
        rvInfo.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rvInfo.setHasFixedSize(true);
        rvInfo.setItemAnimator(new DefaultItemAnimator());
        rvInfo.setAdapter(infoAdapter);
    }

    private void initSpinners() {
        spLager.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLager = (String) parent.getItemAtPosition(position);
                loadLagerplatzForLager(selectedLager);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                lagerplatzList.clear();
                adapterLagerplatz.notifyDataSetChanged();
            }
        });

        spLagerplatz.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Lagerplatz lp = (Lagerplatz) parent.getItemAtPosition(position);
                loadLagerplatzinfo(lp);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {/* no‑op */}
        });
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(LagerplatzinfoViewModel.class);
        viewModel.getSearch().observe(this, this::searchByEan);
        viewModel.getLagerplatzinfo().observe(this, l -> {
            infoList.clear();
            infoList.addAll(l);
            infoAdapter.notifyDataSetChanged();
        });
    }

    private void initBarcode() {
        barcodeManager = new BarcodeManager();
        try {
            readListener = decodeResult -> {
                String ean = decodeResult.getText().replaceAll("[\\r\\n]+$", "");
                triggerSearch(ean);
            };
            barcodeManager.addReadListener(readListener);
        } catch (DecodeException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private void releaseBarcode() {
        if (barcodeManager != null && readListener != null) {
            try {
                barcodeManager.removeReadListener(readListener);
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lagerplatzinfo_menu, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        if (searchView == null) return true;

        EditText et = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        searchView.setQueryHint("Suchen...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                triggerSearch(query);
                searchMenuItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {return false;}
        });
        return true;
    }

    private void triggerSearch(String query) {
        if (query == null || query.trim().isEmpty()) return;
        viewModel.setSearch(query.trim());
    }

    private void loadSpinnerData() {
        executor.execute(() -> {
            List<String> lager;
            try {
                lager = CommunicationSql.getZiellager(sqlServerData, standort);
            } catch (Exception e) {
                Log.e(TAG, "SQL Error: " + e.getMessage());
                lager = new ArrayList<>();
            }
            List<String> finalLager = lager;
            mainHandler.post(() -> {
                adapterLager = new ArrayAdapter<>(getApplicationContext(), R.layout.item_spinner, finalLager);
                spLager.setAdapter(adapterLager);
            });
        });
    }

    private void loadLagerplatzForLager(String lager) {
        executor.execute(() -> {
            lagerplatzList.clear();
            lagerplatzList.addAll(CommunicationSql.getLagerplatzByLager(sqlServerData, lager));
            mainHandler.post(() -> {
                adapterLagerplatz = new ArrayAdapter<>(getApplicationContext(), R.layout.item_spinner, lagerplatzList);
                spLagerplatz.setAdapter(adapterLagerplatz);
            });
        });
    }

    private void loadLagerplatzinfo(Lagerplatz lp) {
        if (lp == null) return;
        executor.execute(() -> {
            List<Lagerplatzinfo> infos = CommunicationSql.getLagerplatzArtikelnummerByLagerplatzId(sqlServerData, standort, lp.getLagerplatzId());
            mainHandler.post(() -> viewModel.setLagerplatzinfo(infos));
        });
    }

    private void searchByEan(String ean) {
        if (ean == null || ean.isEmpty()) return;
        if (loadingDialog != null) loadingDialog.show(getSupportFragmentManager(), "loading");

        executor.execute(() -> {
            LagerplatzLagerResult result = queryLagerplatzByEan(ean);
            mainHandler.post(() -> {
                if (result == null) {
                    Toast.makeText(getApplicationContext(), "Keine gültige Lagerplatz-EAN!", Toast.LENGTH_LONG).show();
                } else {
                    selectSpinnerValues(result);
                }
                if (loadingDialog != null) loadingDialog.dismiss();
            });
        });
    }

    private LagerplatzLagerResult queryLagerplatzByEan(String ean) {
        LagerplatzBestand lb = CommunicationSql.getLagerplatzBestandByStandortEan(sqlServerData, standort, ean);
        if (lb == null) return null;
        return new LagerplatzLagerResult(lb.getLager0(), lb.getLagerplatzId(), ean);
    }

    private void selectSpinnerValues(LagerplatzLagerResult res) {
        int lagerPos = adapterLager.getPosition(res.lager);
        if (lagerPos >= 0) spLager.setSelection(lagerPos);

        Lagerplatz lp = lagerplatzList.stream().filter(l -> l.getEan().equals(res.ean)).findFirst().orElse(null);
        if (lp != null && adapterLagerplatz != null) {
            int lpPos = adapterLagerplatz.getPosition(lp);
            if (lpPos >= 0) spLagerplatz.setSelection(lpPos);
        }
    }

    private static class LagerplatzLagerResult {
        final String lager;
        final int lagerplatzId;
        final String ean;
        LagerplatzLagerResult(String lager, int lagerplatzId, String ean) {
            this.lager = lager; this.lagerplatzId = lagerplatzId; this.ean = ean;
        }
    }
}
