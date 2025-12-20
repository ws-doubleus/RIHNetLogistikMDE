package at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug;

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
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityWareneingangBestellbezugBinding;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.artikel.ArtikelFragment;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.beleg.BelegFragment;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.queue.QueueFragment;

public class WareneingangBestellbezugActivity extends AppCompatActivity
        implements BelegFragment.OnChangeTab,
        ArtikelFragment.OnSearchArtikel,
        QueueFragment.OnSearchQueue {

    private final String TAG = "RIHNet";

    // Barcode + Listener
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;

    // UI
    private BottomNavigationView navView;
    private MenuItem menuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityWareneingangBestellbezugBinding binding = ActivityWareneingangBestellbezugBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1) Toolbar einrichten
        initToolbar(binding);

        // 2) BottomNavigation + NavController
        navView = binding.navView;
        initNavController();
    }

    // ----------------------------------------------------
    // Lifecycle
    // ----------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        initBarcodeManager();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeBarcodeListener();
    }

    // ----------------------------------------------------
    // Interface-Methoden
    // ----------------------------------------------------
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
    public void onSearchQueue(MenuItem menuItem, SearchView searchView) {
        this.menuItem = menuItem;
        this.searchView = searchView;
    }

    // ----------------------------------------------------
    // Interne Hilfsmethoden
    // ----------------------------------------------------
    /**
     * Initialisiert die Toolbar inkl. Navigations-Button.
     */
    private void initToolbar(ActivityWareneingangBestellbezugBinding binding) {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_exit_to_app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Bindet den NavController an die BottomNavigationView.
     */
    private void initNavController() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_wareneingangbestellbezug);
        NavigationUI.setupWithNavController(navView, navController);
    }

    /**
     * Setzt den BarcodeManager und registriert den ReadListener.
     */
    private void initBarcodeManager() {
        if (barcodeManager == null) {
            barcodeManager = new BarcodeManager();
        }
        try {
            readListener = createReadListener();
            barcodeManager.addReadListener(readListener);
        } catch (DecodeException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    /**
     * Entfernt den Barcode-Listener, um Speicherlecks zu vermeiden.
     */
    private void removeBarcodeListener() {
        if (barcodeManager != null && readListener != null) {
            try {
                barcodeManager.removeReadListener(readListener);
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    /**
     * Erzeugt den ReadListener für Barcode-Scans.
     */
    @SuppressLint("NonConstantResourceId")
    private ReadListener createReadListener() {
        return new ReadListener() {
            @Override
            public void onRead(DecodeResult decodeResult) {
                // Letztes Zeichen entfernen
                String result = decodeResult.getText()
                        .substring(0, decodeResult.getText().length() - 1);

//                switch (navView.getSelectedItemId()) {
//                    case R.id.navigation_artikel:
//                        menuItem.expandActionView();
//                        searchView.setQuery(result, true);
//                        break;
//                    case R.id.navigation_queue:
//                        menuItem.expandActionView();
//                        searchView.setQuery(result, false);
//                        break;
//                    default:
//                        // Optionale Behandlung
//                        break;
//                }
                int selectedId = navView.getSelectedItemId();

                // REPLACE SWITCH WITH IF-ELSE
                if (selectedId == R.id.navigation_artikel) {
                    menuItem.expandActionView();
                    searchView.setQuery(result, true);
                } else if (selectedId == R.id.navigation_queue) {
                    menuItem.expandActionView();
                    searchView.setQuery(result, false);
                } else {
                    // Optionale Behandlung / Default case
                }
            }
        };
    }
}
