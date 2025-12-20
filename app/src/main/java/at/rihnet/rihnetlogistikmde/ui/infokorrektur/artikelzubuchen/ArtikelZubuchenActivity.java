package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelzubuchen;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.ReadListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityArtikelZubuchenBinding;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelzubuchen.artikel.ArtikelFragment;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelzubuchen.lager.LagerFragment;

public class ArtikelZubuchenActivity extends AppCompatActivity implements ArtikelFragment.OnChangeTab, LagerFragment.OnChangeTab, ArtikelFragment.OnSearchArtikel, LagerFragment.OnSearchLager {
    private static final String TAG = "RIHNet";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private BarcodeManager barcodeManager;
    private ReadListener readListener;
    private BottomNavigationView navView;
    private MenuItem searchMenuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityArtikelZubuchenBinding binding = ActivityArtikelZubuchenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initToolbar(binding.toolbar);
        navView = binding.navView;
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_artikelzubuchen);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBarcode();
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

    private void initToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_playlist_add);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initBarcode() {
        if (barcodeManager == null) barcodeManager = new BarcodeManager();
        try {
            readListener = dr -> {
                String ean = dr.getText().replaceAll("[\r\n]+$", "");
                mainHandler.post(() -> {
                    if (searchMenuItem != null) searchMenuItem.expandActionView();
                    if (searchView != null) {
                        searchView.setQuery(ean, true); // löst onQueryTextSubmit aus
                        // sofort leeren & schließen → bereit für nächsten Scan
                        searchView.setQuery("", false);
                        searchMenuItem.collapseActionView();
                    }
                });
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
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onChangeTab(int id) {
        if (navView != null) navView.setSelectedItemId(id);
    }

    @Override
    public void onSearchArtikel(@NonNull MenuItem mi, @NonNull SearchView sv) {
        this.searchMenuItem = mi;
        this.searchView = sv;
    }

    @Override
    public void onSearchLager(@NonNull MenuItem mi, @NonNull SearchView sv) {
        this.searchMenuItem = mi;
        this.searchView = sv;
    }
}
