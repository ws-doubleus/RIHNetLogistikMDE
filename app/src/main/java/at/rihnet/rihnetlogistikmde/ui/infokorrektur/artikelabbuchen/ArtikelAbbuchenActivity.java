package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelabbuchen;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.ReadListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityArtikelAbbuchenBinding;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelabbuchen.artikel.ArtikelaFragment;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelabbuchen.lager.LageraFragment;

public class ArtikelAbbuchenActivity extends AppCompatActivity implements ArtikelaFragment.OnChangeTab, LageraFragment.OnChangeTab, ArtikelaFragment.OnSearchArtikel, LageraFragment.OnSearchLager {
    private final String TAG = "RIHNet";
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;
    private BottomNavigationView navView;
    private MenuItem menuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        at.rihnet.rihnetlogistikmde.databinding.ActivityArtikelAbbuchenBinding binding = ActivityArtikelAbbuchenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_playlist_add);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        navView = binding.navView;
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_artikelabbuchen);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onResume() {
        super.onResume();
        CommunicationSql.connection = null;
        if (barcodeManager == null) {
            barcodeManager = new BarcodeManager();
        }
        try {
            readListener = decodeResult -> {
                String result = decodeResult.getText().substring(0, decodeResult.getText().length() - 1);
                menuItem.expandActionView();
                searchView.setQuery(result, true);
            };
            barcodeManager.addReadListener(readListener);
        } catch (DecodeException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeManager != null) {
            try {
                barcodeManager.removeReadListener(readListener);
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    @Override
    public void onChangeTab(int id) {
        navView.setSelectedItemId(id);
    }

    @Override
    public void onSearchArtikel(MenuItem menuItem, SearchView searchView) {
        this.menuItem = menuItem;
        this.searchView = searchView;
    }

    @Override
    public void onSearchLager(MenuItem menuItem, SearchView searchView) {
        this.menuItem = menuItem;
        this.searchView = searchView;
    }
}