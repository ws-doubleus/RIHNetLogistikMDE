package at.rihnet.rihnetlogistikmde.ui.paketlabel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.search.SearchView;

import java.util.Objects;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityPaketlabelBinding;

public class PaketlabelActivity extends AppCompatActivity {

    private final String TAG = "RIHNet|PaketlabelActivity";

    private PaketlabelViewModel paketlabelViewModel;
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;

    private BottomNavigationView navView;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPaketlabelBinding binding = ActivityPaketlabelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        topAppBar = binding.topAppBar;
        setSupportActionBar(topAppBar);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());




        // ✅ WICHTIG: Klassenfelder setzen (nicht lokale Variablen!)
        navView = binding.navView;



        initNavController();
        initViewModel();


    }

    private void initViewModel() {
        paketlabelViewModel = new ViewModelProvider(this).get(PaketlabelViewModel.class);
    }

    private void initNavController() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_paketlabel);

        if (navHostFragment == null) return;

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

            // ✅ Nur im Beleg-Tab Search zeigen (deine BottomNav Destination-ID!)
            boolean searchAllowed = destination.getId() == R.id.navigation_beleg;

            // Search immer schließen beim Wechsel (damit sie nicht im Log sichtbar ist)


            // Such-Icon im Toolbar-Menü ein/ausblenden
            MenuItem searchItem = topAppBar.getMenu().findItem(R.id.action_search);
            if (searchItem != null) {
                searchItem.setVisible(searchAllowed);
            }

            // Titel setzen
            CharSequence label = destination.getLabel();
            if (label != null) {
                topAppBar.setTitle(label);
            }
        });
    }









    @Override
    protected void onResume() {
        super.onResume();

        if (barcodeManager == null) {
            barcodeManager = new BarcodeManager();
        }

        try {
            readListener = new ReadListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public void onRead(DecodeResult decodeResult) {
                    String result = decodeResult.getText().substring(0, decodeResult.getText().length() - 1);

                    // ✅ Nur im Beleg-Tab Search öffnen
                    if (navView.getSelectedItemId() == R.id.navigation_beleg) {
                        runOnUiThread(() -> {

                            if (navView.getSelectedItemId() == R.id.navigation_beleg) {
                                paketlabelViewModel.setSearchQuery(result);
                            }

                        });
                    }
                }
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
}
