package at.rihnet.rihnetlogistikmde.ui.inventur;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityInventurErfassungBinding;
import at.rihnet.rihnetlogistikmde.models.SelectLine.Inventory;
import at.rihnet.rihnetlogistikmde.ui.inventur.artikel.ArtikelFragment;

import com.datalogic.device.ErrorManager;

public class InventurErfassungActivity extends AppCompatActivity implements ArtikelFragment.OnSearchArtikel {
    private static final String TAG = "RIHNet";
//    private BarcodeManager barcodeManager = null;
//    private ReadListener readListener = null;
    private BottomNavigationView navView;
    private MenuItem menuItem;
    private SearchView searchView;
    private InventurErfassungViewModel inventurErfassungViewModel;
    private ScanQueueProcessor scanQueueProcessor;

    private Inventory inventory;

    BarcodeManager decoder = null;
    ReadListener listener = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityInventurErfassungBinding binding = ActivityInventurErfassungBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inventory = (Inventory) getIntent().getSerializableExtra("inventory");

        inventurErfassungViewModel = new ViewModelProvider(this).get(InventurErfassungViewModel.class);
        inventurErfassungViewModel.setInventory(inventory);
        InventurRepository inventurRepository = new InventurRepository(null);
        inventurErfassungViewModel.setInventoryRepository(inventurRepository);

        scanQueueProcessor = new ScanQueueProcessor(inventurErfassungViewModel, 50);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_fact_check);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            Integer pending = inventurErfassungViewModel.getPendingTasks().getValue();
            int count = (pending != null) ? pending : 0;

            if (count > 0) {
                Toast.makeText(
                        InventurErfassungActivity.this,
                        "Es werden noch " + count + " Scans verarbeitet. Bitte warten...",
                        Toast.LENGTH_LONG
                ).show();
            } else {
                finish();
            }
        });


        navView = binding.navView;

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_inventur_erfassung);
        NavigationUI.setupWithNavController(navView, navController);

        inventurErfassungViewModel.getPendingTasks().observe(this, count -> {
            int value = (count != null) ? count : 0;

            // Badge auf dem LOG-Item (Menu-ID ggf. anpassen, wenn sie anders heißt)
            BadgeDrawable badge = navView.getOrCreateBadge(R.id.navigation_log);

            if (value > 0) {
                badge.setVisible(true);
                badge.setNumber(value);
            } else {
                badge.clearNumber();
                badge.setVisible(false);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Integer pending = inventurErfassungViewModel.getPendingTasks().getValue();
                int count = (pending != null) ? pending : 0;

                if (count > 0) {
                    Toast.makeText(
                            InventurErfassungActivity.this,
                            "Es werden noch " + count + " Scans verarbeitet. Bitte warten...",
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    // Callback einmalig deaktivieren und Standard-Verhalten ausführen
                    setEnabled(false);
                    InventurErfassungActivity.super.onBackPressed();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        InventurErfassungViewModel inventurErfassungViewModel = new ViewModelProvider(this).get(InventurErfassungViewModel.class);

        if (decoder == null) { // Remember an onPause call will set it to null.
            decoder = new BarcodeManager();
        }
        ErrorManager.enableExceptions(true);
        try {

            // Create an anonymous class.
            listener = new ReadListener() {

                // Implement the callback method.
                @Override
                public void onRead(DecodeResult decodeResult) {
                    String raw = decodeResult.getText();
                    if (raw == null || raw.isEmpty()) return;
                    String result = raw.substring(0, raw.length() - 1);
                    if (navView.getSelectedItemId() != R.id.navigation_artikel) {
                        return;
                    }

                    // Auto-Mode-Status aus dem ViewModel holen
                    Boolean auto = inventurErfassungViewModel.getAutoMode().getValue();
                    boolean autoMode = (auto != null && auto);

                    if (autoMode) {
                        // *** AUTO-MODUS ***
                        runOnUiThread(() -> {
                            // 1) SearchView vollständig "neutralisieren"
                            if (searchView != null) {
                                searchView.setQuery("", false);
                                searchView.clearFocus();
                            }
                            if (menuItem != null) {
                                menuItem.collapseActionView();
                            }

                            // 2) Direkt den Scan ins ViewModel feuern
                            inventurErfassungViewModel.setSearchArtikel(result);
                        });

                    } else {
                        // *** MANUELLER MODUS ***
                        runOnUiThread(() -> {
                            // 1) FOCUS von allen EditTexts wegnehmen, damit Keyboard-Wedge
                            //    möglichst NICHT in Menge/Seriennummer schreibt
                            if (getCurrentFocus() != null) {
                                getCurrentFocus().clearFocus();
                            }
                            getWindow().getDecorView().requestFocus();

                            // 2) SearchView NICHT anfassen, NICHT expandieren, NICHT setQuery(..)!
                            //    (damit kein doppelter Text zustande kommt)

                            // 3) Direkt die Suche starten – wie bei manueller Eingabe
                            inventurErfassungViewModel.setSearchArtikel(result);
                        });
                    }

                }

            };

            // Remember to add it, as a listener.
            decoder.addReadListener(listener);

        } catch (DecodeException e) {
            Log.e(TAG, "Error while trying to bind a listener to BarcodeManager", e);
        }


//        if (barcodeManager == null) {
//            barcodeManager = new BarcodeManager();
//        }
//        try {
//            readListener = new ReadListener() {
//                @Override
//                public void onRead(DecodeResult decodeResult) {
//                    String raw = decodeResult.getText();
//                    if (raw == null || raw.isEmpty()) return;
//
//                    // Dein bisheriges Abschneiden des letzten Zeichens (falls nötig)
//                    String result = raw.substring(0, raw.length() - 1);
//
//                    // Nur reagieren, wenn der Artikel-Tab aktiv ist
//                    if (navView.getSelectedItemId() != R.id.navigation_artikel) {
//                        return;
//                    }
//
//                    // Auto-Mode-Status aus dem ViewModel holen
//                    Boolean auto = inventurErfassungViewModel.getAutoMode().getValue();
//                    boolean autoMode = (auto != null && auto);
//
//                    if (autoMode) {
//                        // *** AUTO-MODUS ***
//                        runOnUiThread(() -> {
//                            // 1) SearchView vollständig "neutralisieren"
//                            if (searchView != null) {
//                                searchView.setQuery("", false);
//                                searchView.clearFocus();
//                            }
//                            if (menuItem != null) {
//                                menuItem.collapseActionView();
//                            }
//
//                            // 2) Direkt den Scan ins ViewModel feuern
//                            inventurErfassungViewModel.setSearchArtikel(result);
//                        });
//
//                    } else {
//                        // *** MANUELLER MODUS ***
//                        runOnUiThread(() -> {
//                            // 1) FOCUS von allen EditTexts wegnehmen, damit Keyboard-Wedge
//                            //    möglichst NICHT in Menge/Seriennummer schreibt
//                            if (getCurrentFocus() != null) {
//                                getCurrentFocus().clearFocus();
//                            }
//                            getWindow().getDecorView().requestFocus();
//
//                            // 2) SearchView NICHT anfassen, NICHT expandieren, NICHT setQuery(..)!
//                            //    (damit kein doppelter Text zustande kommt)
//
//                            // 3) Direkt die Suche starten – wie bei manueller Eingabe
//                            inventurErfassungViewModel.setSearchArtikel(result);
//                        });
//                    }
//                }
//            };
//
//            barcodeManager.addReadListener(readListener);
//        } catch (DecodeException e) {
//            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (barcodeManager != null) {
//            try {
//                barcodeManager.removeReadListener(readListener);
//            } catch (Exception e) {
//                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
//            }
//        }
        if (decoder != null) {
            try {
                // Unregister our listener from it and free resources.
                decoder.removeReadListener(listener);

                // Let the garbage collector take care of our reference.
                decoder = null;
            } catch (Exception e) {
                Log.e(TAG, "Error while trying to remove a listener from BarcodeManager", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (scanQueueProcessor != null) {
            scanQueueProcessor.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onSearchArtikel(MenuItem menuItem, SearchView searchView) {
        this.menuItem = menuItem;
        this.searchView = searchView;
    }
}