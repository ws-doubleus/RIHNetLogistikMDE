package at.rihnet.rihnetlogistikmde.ui.umlagerung;

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

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityUmlagerungBinding;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.artikel.ArtikelFragment;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.lager.LagerFragment;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.queue.QueueFragment;

public class UmlagerungActivity extends AppCompatActivity implements ArtikelFragment.OnChangeTab, ArtikelFragment.OnSearchArtikel, LagerFragment.OnChangeTab, LagerFragment.OnSearchLager, QueueFragment.OnSearchQueue {
    private final String TAG = "RIHNet";
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;
    private BottomNavigationView navView;
    private MenuItem menuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        at.rihnet.rihnetlogistikmde.databinding.ActivityUmlagerungBinding binding = ActivityUmlagerungBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //UmlagerungViewModel umlagerungViewModel = new ViewModelProvider(this).get(UmlagerungViewModel.class);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_u_turn_right);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        navView = binding.navView;

        //AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_artikel, R.id.navigation_lager, R.id.navigation_queue).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_umlagerung);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
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
//                    switch (navView.getSelectedItemId()){
//                        case R.id.navigation_artikel:
//                        case R.id.navigation_lager:
//                            menuItem.expandActionView();
//                            searchView.setQuery(result, true);
//                            //umlagerungViewModel.setSearchArtikel(result);
//                            break;
//
//                        case R.id.navigation_queue:
//                            menuItem.expandActionView();
//                            searchView.setQuery(result, false);
//                            //umlagerungViewModel.setSearchQueue(result);
//                            break;
//                        //default:
//                            //throw new IllegalStateException("Unexpected value: " + navView.getSelectedItemId());
//                    }
                    int currentId = navView.getSelectedItemId();

                    // FIX: Use if-else instead of switch
                    if (currentId == R.id.navigation_artikel || currentId == R.id.navigation_lager) {
                        menuItem.expandActionView();
                        searchView.setQuery(result, true);
                        //umlagerungViewModel.setSearchArtikel(result);
                    } else if (currentId == R.id.navigation_queue) {
                        menuItem.expandActionView();
                        searchView.setQuery(result, false);
                        //umlagerungViewModel.setSearchQueue(result);
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
    public void onSearchLager(MenuItem menuItem, SearchView searchView) {
        this.menuItem = menuItem;
        this.searchView = searchView;
    }

    @Override
    public void onSearchQueue(MenuItem menuItem, SearchView searchView) {
        this.menuItem = menuItem;
        this.searchView = searchView;
    }
}