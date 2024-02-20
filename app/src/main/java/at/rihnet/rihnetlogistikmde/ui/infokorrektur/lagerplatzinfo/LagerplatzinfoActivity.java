package at.rihnet.rihnetlogistikmde.ui.infokorrektur.lagerplatzinfo;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.ReadListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityLagerplatzinfoBinding;
import at.rihnet.rihnetlogistikmde.models.LagerLagerplatz;
import at.rihnet.rihnetlogistikmde.models.LagerplatzBestand;
import at.rihnet.rihnetlogistikmde.models.Lagerplatzinfo;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;

public class LagerplatzinfoActivity extends AppCompatActivity {
    private static final String TAG = "RIHNet";
    private LagerplatzinfoViewModel lagerplatzinfoViewModel;
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;
    private LoadingDialogFragment loadingDialogFragment;
    private String previousQuery = "";
    private List<Lagerplatzinfo> lagerplatzinfoList = new ArrayList<>();
    private AppCompatSpinner acs_lager;
    private AppCompatSpinner acs_lagerplatz;
    private final List<LagerplatzBestand> lagerplatz = new ArrayList<>();
    private ArrayAdapter<String> adapterLager;
    private ArrayAdapter<LagerplatzBestand> adapterLagerplatz;
    private LagerplatzinfoRecyclerViewAdapter lagerplatzinfoRecyclerViewAdapter;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLagerplatzinfoBinding binding = ActivityLagerplatzinfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_fmd_bad);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        acs_lager = binding.acsLager;
        acs_lagerplatz = binding.acsLagerplatz;
        RecyclerView rv_lagerplatzinfo = binding.rvLagerplatzinfo;

        lagerplatzinfoRecyclerViewAdapter = new LagerplatzinfoRecyclerViewAdapter(lagerplatzinfoList);
        rv_lagerplatzinfo.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rv_lagerplatzinfo.setHasFixedSize(true);
        rv_lagerplatzinfo.setItemAnimator(new DefaultItemAnimator());
        rv_lagerplatzinfo.setAdapter(lagerplatzinfoRecyclerViewAdapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String standort = prefs.getString("standort", null);

        List<String> lager = CommunicationSql.getZiellager(standort);
        adapterLager = new ArrayAdapter<>(getApplicationContext(), R.layout.item_spinner, lager);
        acs_lager.setAdapter(adapterLager);
        acs_lager.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(i);
                lagerplatz.clear();
                List<LagerplatzBestand> lb = CommunicationSql.getLagerplatzBestandByLager(selectedItem);
                lagerplatz.addAll(lb);
                adapterLagerplatz = new ArrayAdapter<>(getApplicationContext(), R.layout.item_spinner, lagerplatz);
                acs_lagerplatz.setAdapter(adapterLagerplatz);
                if (lagerplatzinfoViewModel.getSearch().getValue() != null && lagerplatzinfoViewModel.getSearch().getValue().length() > 0) {
                    LagerplatzBestand lb0 = lagerplatz.stream().filter(f -> f.getEan().equals(lagerplatzinfoViewModel.getSearch().getValue())).findFirst().orElse(null);
                    acs_lagerplatz.setSelection(adapterLagerplatz.getPosition(lb0));
                    lagerplatzinfoViewModel.setSearch("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                lagerplatz.clear();
            }
        });

        acs_lagerplatz.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LagerplatzBestand selectedItem = (LagerplatzBestand) adapterView.getItemAtPosition(i);
                List<Lagerplatzinfo> lagerplatzinfos = CommunicationSql.getLagerplatzArtikelnummerByLagerplatzId(standort, selectedItem.getLagerplatzId());
                lagerplatzinfoViewModel.setLagerplatzinfo(lagerplatzinfos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        loadingDialogFragment = LoadingDialogFragment.newInstance("Lagerplatzinfo wird geladen...");
        loadingDialogFragment.setCancelable(false);

        lagerplatzinfoViewModel = new ViewModelProvider(this).get(LagerplatzinfoViewModel.class);
        lagerplatzinfoViewModel.getSearch().observe(this, this::doSearch);
        lagerplatzinfoViewModel.getLagerplatzinfo().observe(this, l -> {
            lagerplatzinfoList = l;
            lagerplatzinfoRecyclerViewAdapter.setLagerplatzinfo(lagerplatzinfoList);
            lagerplatzinfoRecyclerViewAdapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeManager == null) {
            barcodeManager = new BarcodeManager();
        }
        try {
            readListener = decodeResult -> {
                String result = decodeResult.getText().substring(0, decodeResult.getText().length() - 1);
                if (!result.equals(previousQuery)) {
                    previousQuery = result;
                    lagerplatzinfoViewModel.setSearch(previousQuery);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lagerplatzinfo_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setQueryHint("Suchen...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.equals(previousQuery)) {
                    previousQuery = query;
                    if (query.length() > 0) {
                        lagerplatzinfoViewModel.setSearch(query);
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    private void doSearch(String search) {
        if (search != null && search.length() > 0) {
            loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");
            new Handler().postDelayed(() -> {
                LoadLagerLagerplatzAsyncTask loadLagerLagerplatzAsyncTask = new LoadLagerLagerplatzAsyncTask();
                loadLagerLagerplatzAsyncTask.execute(search);
            }, 300);
        }
    }

    public class LoadLagerLagerplatzAsyncTask extends AsyncTaskExecutorService<String, Void, LagerLagerplatz> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected LagerLagerplatz doInBackground(String s) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String standort = prefs.getString("standort", null);
            LagerplatzBestand lb = CommunicationSql.getLagerplatzBestandByStandortEan(standort, s);
            if (lb != null) {
                return new LagerLagerplatz(
                        lb.getLager(),
                        lb.getLagerplatzId(),
                        s
                );
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(LagerLagerplatz lagerLagerplatz) {
            if (lagerLagerplatz != null) {
                acs_lager.setSelection(adapterLager.getPosition(lagerLagerplatz.getLager()));
                lagerplatz.stream().filter(f -> f.getEan().equals(lagerplatzinfoViewModel.getSearch().getValue())).findFirst().ifPresent(lb -> acs_lagerplatz.setSelection(adapterLagerplatz.getPosition(lb)));
            } else {
                Toast.makeText(getApplicationContext(), "Keine gültige Lagerplatz-EAN!", Toast.LENGTH_LONG).show();
            }
            if (loadingDialogFragment != null) {
                loadingDialogFragment.dismiss();
            }
            previousQuery = "";
        }
    }
}