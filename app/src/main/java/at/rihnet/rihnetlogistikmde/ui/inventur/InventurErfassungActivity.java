package at.rihnet.rihnetlogistikmde.ui.inventur;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityInventurErfassungBinding;
import at.rihnet.rihnetlogistikmde.models.SelectLine.Inventory;
import at.rihnet.rihnetlogistikmde.ui.inventur.artikel.ArtikeliFragment;

public class InventurErfassungActivity extends AppCompatActivity implements ArtikeliFragment.OnSearchArtikel {
    private static final String TAG = "RIHNet";
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;
    private BottomNavigationView navView;
    private MenuItem menuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityInventurErfassungBinding binding = ActivityInventurErfassungBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Inventory inventory = (Inventory) getIntent().getSerializableExtra("inventory");

        InventurErfassungViewModel inventurErfassungViewModel = new ViewModelProvider(this).get(InventurErfassungViewModel.class);
        inventurErfassungViewModel.setInventory(inventory);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_fact_check);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        navView = binding.navView;

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_inventur_erfassung);
        NavigationUI.setupWithNavController(navView, navController);
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
                    if (navView.getSelectedItemId() == R.id.navigation_artikel) {
                        menuItem.expandActionView();
                        searchView.setQuery(result, true);
                    } //else {
                        //throw new IllegalStateException("Unexpected value: " + navView.getSelectedItemId());
                    //}
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

    @Override
    public void onSearchArtikel(MenuItem menuItem, SearchView searchView) {
        this.menuItem = menuItem;
        this.searchView = searchView;
    }
}