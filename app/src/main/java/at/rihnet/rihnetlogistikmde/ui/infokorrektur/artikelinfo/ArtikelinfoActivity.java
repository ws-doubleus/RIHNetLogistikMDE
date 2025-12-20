// =================================================================================================
// ArtikelinfoActivity.java  – volle, getestete Version (Dialog‑RefCount + SearchView‑Reset)
// =================================================================================================

package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelinfo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.ReadListener;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.audio.SoundPoolManager;
import at.rihnet.rihnetlogistikmde.databinding.ActivityArtikelinfoBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.LagerplatzBestand;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.main.MainActivity;

public class ArtikelinfoActivity extends AppCompatActivity {

    private static final String TAG = "RIHNet/Artikelinfo";
    private static final String TAG_LOADING_DIALOG = "loading_artikelinfo";

    private ActivityArtikelinfoBinding binding;
    private ScrollView svData;
    private TextView tvEmpty;
    private SearchView searchView;
    private MenuItem searchMenuItem;
    private ArtikelinfoViewModel viewModel;
    private ExecutorService executor;
    private Handler mainHandler;
    private final AtomicInteger pendingRequests = new AtomicInteger(0);
    private BarcodeManager barcodeManager;
    private ReadListener readListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtikelinfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initExecutor();
        initToolbar();
        initUIReferences();
        initViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBarcodeReader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseBarcodeReader();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
        forceHideLoadingDialog();
    }

    private void initExecutor() {
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void initToolbar() {
        Toolbar tb = binding.toolbar;
        setSupportActionBar(tb);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_article);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        tb.setNavigationOnClickListener(v -> finish());
    }

    private void initUIReferences() {
        svData = binding.svData;
        tvEmpty = binding.tvEmpty;
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(ArtikelinfoViewModel.class);
        viewModel.getSearch().observe(this, this::performSearch);
        viewModel.getArtikel().observe(this, this::renderArtikel);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.artikelinfo_menu, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        if (searchView == null) return true;
        EditText et = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
        searchView.setQueryHint("Suchen...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                triggerSearch(q);
                clearAndCollapseSearchView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String n) {
                return false;
            }
        });
        return true;
    }

    private void triggerSearch(String query) {
        if (query != null && !query.trim().isEmpty()) viewModel.setSearch(query.trim());
    }

    private void clearAndCollapseSearchView() {
        if (searchView != null) searchView.setQuery("", false);
        if (searchMenuItem != null) searchMenuItem.collapseActionView();
    }

    private void initBarcodeReader() {
        if (barcodeManager == null) barcodeManager = new BarcodeManager();
        try {
            readListener = dr -> {
                String result = dr.getText().replaceAll("[\\r\\n]+$", "");
                mainHandler.post(() -> {
                    triggerSearch(result);
                    clearAndCollapseSearchView();
                });
            };
            barcodeManager.addReadListener(readListener);
        } catch (DecodeException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private void releaseBarcodeReader() {
        if (barcodeManager != null && readListener != null) {
            try {
                barcodeManager.removeReadListener(readListener);
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    private void performSearch(String artikelNr) {
        CommunicationCommon.hideKeyboard(this);
        if (artikelNr == null || artikelNr.isEmpty()) return;
        showLoadingDialog();
        executor.execute(() -> {
            Artikel a = loadArtikelFromDb(artikelNr);
            mainHandler.post(() -> {
                handleLoadedArtikel(a);
                hideLoadingDialog();
            });
        });
    }

    private void showLoadingDialog() {
        if (pendingRequests.incrementAndGet() == 1) {
            LoadingDialogFragment dlg = LoadingDialogFragment.newInstance("Artikelinfo wird geladen...");
            dlg.setCancelable(false);
            dlg.show(getSupportFragmentManager(), TAG_LOADING_DIALOG);
        }
    }

    private void hideLoadingDialog() {
        if (pendingRequests.decrementAndGet() == 0) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_LOADING_DIALOG);
            if (f instanceof DialogFragment) ((DialogFragment) f).dismissAllowingStateLoss();
        }
    }

    private void forceHideLoadingDialog() {
        pendingRequests.set(0);
        Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_LOADING_DIALOG);
        if (f instanceof DialogFragment) ((DialogFragment) f).dismissAllowingStateLoss();
    }

    private Artikel loadArtikelFromDb(@NonNull String artikelNr) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String standort = prefs.getString("standort", null);
            SqlServerData sql = new SqlServerData(
                    prefs.getString("ipadresse", ""), prefs.getString("port", ""), prefs.getString("datenbank", ""), prefs.getString("instance", ""), prefs.getString("benutzername", ""), prefs.getString("kennwort", ""));
            Artikel artikel = CommunicationSql.getArtikel(sql, artikelNr, standort);
            if (artikel != null)
                artikel.setLagerBestandList(CommunicationSql.getLagerByArtikelnummer(sql, artikel.getArtikelnummer(), standort));
            return artikel;
        } catch (Exception e) {
            Log.e(TAG, "DB‑Error: " + e.getMessage());
            return new Artikel("Error", "", "", "", "", "", 0, 0, "", "", "", "", 0, "", Kategorie.UMLAGERUNG);
        }
    }

    private void handleLoadedArtikel(Artikel a) {
        if (a == null) {
            SoundPoolManager.getInstance(this.getApplicationContext()).playError();
            Toast.makeText(this, "Keine gültige Artikelnummer!", Toast.LENGTH_LONG).show();
            viewModel.setArtikel(null);
            return;
        }
        if ("Error".equals(a.getArtikelnummer())) {
            restartApp();
            return;
        }
        viewModel.setArtikel(a);
    }

    private void renderArtikel(Artikel artikel) {
        if (artikel == null) {
            svData.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        svData.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        binding.tvArtikelnummer.setText(artikel.getArtikelnummer());
        binding.tvBezeichnung.setText(artikel.getBezeichnung());
        binding.tvZusatz.setText(artikel.getZusatz());
        binding.tvHstartikelnummer.setText(artikel.getHstArtikelnummer());
        binding.tvEannummer.setText(artikel.getEannummer());
        LayoutInflater inflater = getLayoutInflater();
        binding.llLager.removeAllViews();
        List<String> lager = artikel.getLagerBestandList().stream().map(LagerplatzBestand::getLager0).distinct().collect(Collectors.toList());
        for (String lg : lager) {
            View item = inflater.inflate(R.layout.item_lager, binding.llLager, false);
            ((TextView) item.findViewById(R.id.tv_lager)).setText(lg);
            LinearLayoutCompat ll = item.findViewById(R.id.ll_lagerplatz);
            artikel.getLagerBestandList().stream().filter(lb -> lb.getLager0().equals(lg)).forEach(lb -> {
                View row = inflater.inflate(R.layout.item_lagerplatz, ll, false);
                ((TextView) row.findViewById(R.id.tv_lagerplatz)).setText(lb.getBezeichnung());
                ((TextView) row.findViewById(R.id.tv_bestand)).setText(String.valueOf(lb.getBestand()));
                ll.addView(row);
            });
            binding.llLager.addView(item);
        }
    }

    private void restartApp() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}


