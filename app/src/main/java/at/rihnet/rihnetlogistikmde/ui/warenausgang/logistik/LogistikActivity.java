package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityLogistikBinding;
import at.rihnet.rihnetlogistikmde.models.BelegInfo;
import at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.offen.OffenFragment;

public class LogistikActivity extends AppCompatActivity implements OffenFragment.OnSearchArtikel {
    private final String TAG = "RIHNet";

    private LogistikViewModel logistikViewModel;
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;

    private BottomNavigationView navView;
    private MenuItem menuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLogistikBinding binding = ActivityLogistikBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navView = binding.navView;

        initToolbar(binding);
        initNavController();
        initViewModel();

        BelegInfo belegInfo = (BelegInfo) getIntent().getSerializableExtra("EXTRA_BELEGINFO");
        if (belegInfo != null) {
            logistikViewModel.setBelegInfo(belegInfo);
        }
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
                    if (navView.getSelectedItemId() == R.id.navigation_offen) {
                        menuItem.expandActionView();
                        searchView.setQuery(result, true);
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
    public void onSearchArtikel(MenuItem menuItem, SearchView searchView) {
        this.menuItem = menuItem;
        this.searchView = searchView;
    }

    private void initToolbar(ActivityLogistikBinding binding) {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_exit_to_app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initNavController() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_logistik);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(navView, navController);
        }
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_logistik);
        //NavigationUI.setupWithNavController(navView, navController);
    }

    private void initViewModel() {
        logistikViewModel = new ViewModelProvider(this).get(LogistikViewModel.class);
    }
}