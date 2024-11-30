package at.rihnet.rihnetlogistikmde.ui.freierwareneingang;

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
import at.rihnet.rihnetlogistikmde.databinding.ActivityFreierWareneingangBinding;
import at.rihnet.rihnetlogistikmde.ui.freierwareneingang.artikel.ArtikelfFragment;
import at.rihnet.rihnetlogistikmde.ui.freierwareneingang.beleg.BelegFragment;
import at.rihnet.rihnetlogistikmde.ui.freierwareneingang.queue.QueuefFragment;

public class FreierWareneingangActivity extends AppCompatActivity implements BelegFragment.OnChangeTab, ArtikelfFragment.OnChangeTab, ArtikelfFragment.OnSearchArtikel, QueuefFragment.OnSearchQueue {
    private final String TAG = "RIHNet";
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;
    private BottomNavigationView navView;
    private MenuItem menuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //EdgeToEdge.enable(this);

        at.rihnet.rihnetlogistikmde.databinding.ActivityFreierWareneingangBinding binding = ActivityFreierWareneingangBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_exit_to_app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        navView = binding.navView;

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_freierwareneingang);
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
                    switch (navView.getSelectedItemId()){
                        case R.id.navigation_artikel:
                            menuItem.expandActionView();
                            searchView.setQuery(result, true);
                            break;
                        case R.id.navigation_queue:
                            menuItem.expandActionView();
                            searchView.setQuery(result, false);
                            break;
                        //default:
                            //throw new IllegalStateException("Unexpected value: " + navView.getSelectedItemId());
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
}