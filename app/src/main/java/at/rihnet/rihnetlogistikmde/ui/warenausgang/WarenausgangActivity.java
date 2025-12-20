package at.rihnet.rihnetlogistikmde.ui.warenausgang;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.datalogic.decode.ReadListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityWarenausgangBinding;
import at.rihnet.rihnetlogistikmde.models.BelegInfo;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.LogistikActivity;

public class WarenausgangActivity extends AppCompatActivity {
    private static final String TAG = "RIHNet";

    private ActivityWarenausgangBinding binding;

    private WarenausgangViewModel warenausgangViewModel;

    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;
    private LoadingDialogFragment loadingDialogFragment;

    private List<BelegInfo> belegInfos = new ArrayList<>();
    private WarenausgangRecyclerViewAdapter warenausgangRecyclerViewAdapter;

    private MenuItem menuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWarenausgangBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initViewModel();
        initRecyclerView();

        try {
            barcodeManager = new BarcodeManager();
        } catch (DecodeException | SecurityException e) {
            Log.e(TAG, "Scanner konnte nicht initialisiert werden", e);
            Toast.makeText(this, "Scanner nicht verfügbar!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeManager == null) {
            barcodeManager = new BarcodeManager();
        }
        if (readListener == null) {
            readListener = decodeResult -> {
                String text = decodeResult.getText();
                if (text != null && text.length() > 1) {
                    String result = text.substring(0, text.length() - 1);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        warenausgangViewModel.setSearch(result);
                        if (searchView != null) searchView.setQuery("", false);
                        if (menuItem != null) menuItem.collapseActionView();
                    });
                }
            };
            barcodeManager.addReadListener(readListener);
        }
        loadBelege();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeManager != null && readListener != null) {
            barcodeManager.removeReadListener(readListener);
            readListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barcodeManager != null) {
            barcodeManager.release();
            barcodeManager = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.warenausgang_menu, menu);
        menuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setQueryHint("Suchen...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query == null || query.trim().isEmpty()) return true;
                warenausgangViewModel.setSearch(query.trim());
                searchView.setQuery("", false);
                searchView.clearFocus();
                menuItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    private void initToolbar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_output);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViewModel() {
        warenausgangViewModel = new ViewModelProvider(this).get(WarenausgangViewModel.class);

        warenausgangViewModel.getBelegInfos().observe(this, bi -> {
            belegInfos = bi;
            warenausgangRecyclerViewAdapter.setBelegInfos(bi);
            warenausgangRecyclerViewAdapter.notifyDataSetChanged();
            updateEmptyView();
        });

        warenausgangViewModel.getSearch().observe(this, this::doSearch);
    }

    private void initRecyclerView() {
        RecyclerView rv_belege = binding.rvBelege;

        warenausgangRecyclerViewAdapter = new WarenausgangRecyclerViewAdapter(belegInfos);
        rv_belege.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv_belege.setHasFixedSize(true);
        rv_belege.setItemAnimator(new DefaultItemAnimator());
        rv_belege.setAdapter(warenausgangRecyclerViewAdapter);

        warenausgangRecyclerViewAdapter.setOnItemClickListener(position -> {
            BelegInfo belegInfo = warenausgangRecyclerViewAdapter.getBelegInfoAt(position);
            warenausgangViewModel.setBelegInfo(belegInfo);
            Intent intent = new Intent(WarenausgangActivity.this, LogistikActivity.class);
            intent.putExtra("EXTRA_BELEGINFO", belegInfo);
            startActivity(intent);
        });
    }

    private void loadBelege() {
        loadingDialogFragment = LoadingDialogFragment.newInstance("Belege werden geladen...");
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String standort = prefs.getString("standort", null);
        Set<String> setBelegtypen = prefs.getStringSet("belegtyp", null);

        String belegtypen;
        if (setBelegtypen != null && !setBelegtypen.isEmpty()) {
            belegtypen = setBelegtypen.stream()
                    .map(eintrag -> "'" + eintrag + "'")
                    .collect(Collectors.joining(", "));
        } else {
            belegtypen = "''";
        }

        warenausgangViewModel.getExecutorService().execute(() -> {
            List<BelegInfo> result = CommunicationSql.getBelegInfoByBelegtyp(belegtypen, standort);
            belegInfos.clear();
            belegInfos.addAll(result);
            runOnUiThread(() -> {
                warenausgangViewModel.setBelegInfos(belegInfos);
                if (loadingDialogFragment != null) {
                    loadingDialogFragment.dismiss();
                }
            });
        });
    }

    private void updateEmptyView() {
        RecyclerView rv_belege = binding.rvBelege;
        TextView tv_empty = binding.tvEmpty;

        if (!belegInfos.isEmpty()) {
            rv_belege.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        } else {
            rv_belege.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        }
    }

    private void doSearch(String search) {
        CommunicationCommon.hideKeyboard(this);
        if (search == null || search.isEmpty()) return;

        if (belegInfos == null || belegInfos.isEmpty()) {
            Toast.makeText(this, "Keine Belege verfügbar!", Toast.LENGTH_SHORT).show();
            return;
        }

        BelegInfo match = belegInfos.stream().filter(f -> (f.getBelegtyp() + f.getBelegnummer()).equals(search)).findFirst().orElse(null);
        if (match == null) {
            Toast.makeText(this, "Keinen passenden Beleg gefunden!", Toast.LENGTH_SHORT).show();
        } else {
            warenausgangViewModel.setBelegInfo(match);
            Intent intent = new Intent(WarenausgangActivity.this, LogistikActivity.class);
            intent.putExtra("EXTRA_BELEGINFO", match);
            startActivity(intent);
        }
    }
}
