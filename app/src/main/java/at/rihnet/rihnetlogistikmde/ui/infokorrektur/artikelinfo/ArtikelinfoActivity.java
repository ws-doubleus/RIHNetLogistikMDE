package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelinfo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.ReadListener;

import java.io.IOError;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityArtikelinfoBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.LagerplatzBestand;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.main.MainActivity;
import android.text.InputFilter;

public class ArtikelinfoActivity extends AppCompatActivity {
    private static final String TAG = "RIHNet";
    private ArtikelinfoViewModel artikelinfoViewModel;
    private BarcodeManager barcodeManager = null;
    private ReadListener readListener = null;
    private LoadingDialogFragment loadingDialogFragment;
    private String previousQuery = "";
    private ScrollView sv_data;
    private TextView tv_empty;
    private MenuItem menuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        at.rihnet.rihnetlogistikmde.databinding.ActivityArtikelinfoBinding binding = ActivityArtikelinfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            Toolbar toolbar = binding.toolbar;
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setLogo(R.drawable.ic_article);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());

            sv_data = binding.svData;
            tv_empty = binding.tvEmpty;
            TextView tv_artikelnummer = binding.tvArtikelnummer;
            TextView tv_bezeichnung = binding.tvBezeichnung;
            TextView tv_zusatz = binding.tvZusatz;
            TextView tv_hstartikelnummer = binding.tvHstartikelnummer;
            TextView tv_eannummer = binding.tvEannummer;
            LinearLayoutCompat ll_lager = binding.llLager;

            loadingDialogFragment = LoadingDialogFragment.newInstance("Artikelinfo wird geladen...");
            loadingDialogFragment.setCancelable(false);

            artikelinfoViewModel = new ViewModelProvider(this).get(ArtikelinfoViewModel.class);
            artikelinfoViewModel.getSearch().observe(this, this::doSearch);
            artikelinfoViewModel.getArtikel().observe(this, a -> {
                if (a == null) {
                    sv_data.setVisibility(View.GONE);
                    tv_empty.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Artikelnummer: " + previousQuery + " wurde nicht gefunden!", Toast.LENGTH_LONG).show();
                    previousQuery = "";
                } else {
                    if(a.getArtikelnummer().equals(("Error"))){
                        return;
                    }
                    sv_data.setVisibility(View.VISIBLE);
                    tv_empty.setVisibility(View.GONE);
                    tv_artikelnummer.setText(a.getArtikelnummer());
                    tv_bezeichnung.setText(a.getBezeichnung());
                    tv_zusatz.setText(a.getZusatz());
                    tv_hstartikelnummer.setText(a.getHstArtikelnummer());
                    tv_eannummer.setText(a.getEannummer());

                    LayoutInflater layoutInflater = getLayoutInflater();
                    List<String> distinctLager = a.getLagerBestandList().stream().map(LagerplatzBestand::getLager0).distinct().collect(Collectors.toList());
                    ll_lager.removeAllViews();
                    for (String lager : distinctLager) {
                        View itemLager = layoutInflater.inflate(R.layout.item_lager, ll_lager, false);
                        TextView tv_lager = itemLager.findViewById(R.id.tv_lager);
                        tv_lager.setText(lager);
                        List<LagerplatzBestand> distinctLagerplatz = a.getLagerBestandList().stream().filter(f -> f.getLager0().equals(lager)).collect(Collectors.toList());
                        LinearLayoutCompat ll_lagerplatz = itemLager.findViewById(R.id.ll_lagerplatz);
                        for (LagerplatzBestand lb : distinctLagerplatz) {
                            View itemLagerplatz = layoutInflater.inflate(R.layout.item_lagerplatz, ll_lagerplatz, false);
                            TextView tv_lagerplatz = itemLagerplatz.findViewById(R.id.tv_lagerplatz);
                            TextView tv_bestand = itemLagerplatz.findViewById(R.id.tv_bestand);
                            tv_lagerplatz.setText(lb.getBezeichnung());
                            tv_bestand.setText(String.valueOf(lb.getBestand()));
                            ll_lagerplatz.addView(itemLagerplatz);
                        }
                        ll_lager.addView(itemLager);
                    }
                }
            });
        } catch (IOError | Exception error) {
            Intent i = new Intent(ArtikelinfoActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
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
                menuItem.expandActionView();
                searchView.setQuery(result, false);
                if (!result.equals(previousQuery)) {
                    previousQuery = result;
                    artikelinfoViewModel.setSearch(previousQuery);
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
        getMenuInflater().inflate(R.menu.artikelinfo_menu, menu);
        menuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menuItem.getActionView();

        assert searchView != null;

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(18);
        searchEditText.setFilters(filters);

        searchView.setQueryHint("Suchen...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.equals(previousQuery)) {
                    previousQuery = query;
                    if (!query.isEmpty()) {
                        artikelinfoViewModel.setSearch(query);
                        menuItem.collapseActionView();
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
        CommunicationCommon.hideKeyboard(this);
        if (search != null && !search.isEmpty()) {
            loadingDialogFragment.show(getSupportFragmentManager(), "fragment_loading_dialog");
            new Handler().postDelayed(() -> {
                LoadArtikelAsyncTask loadArtikelAsyncTask = new LoadArtikelAsyncTask();
                loadArtikelAsyncTask.execute(search);
            }, 300);
        }
    }

    public class LoadArtikelAsyncTask extends AsyncTaskExecutorService<String, Void, Artikel> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Artikel doInBackground(String s) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String standort = prefs.getString("standort", null);
                String ipadresse = prefs.getString("ipadresse", "");
                String port = prefs.getString("port", "");
                String datenbank = prefs.getString("datenbank", "");
                String instance = prefs.getString("instance", "");
                String benutzername = prefs.getString("benutzername", "");
                String kennwort = prefs.getString("kennwort", "");
                SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);
                Artikel artikel = CommunicationSql.getArtikel(sqlServerData, s, standort);
                if (artikel != null) {
                    artikel.setLagerBestandList(CommunicationSql.getLagerByArtikelnummer(sqlServerData, artikel.getArtikelnummer(), standort));
                }
                return artikel;
            } catch (IOError | Exception error) {
                return new Artikel("Error", "", "", "", "", "", 0, 0, "", "", "", "", 0, "", Kategorie.UMLAGERUNG);
            }
        }

        @Override
        protected void onPostExecute(Artikel artikel) {
            if (artikel == null) {
                artikelinfoViewModel.setArtikel(null);
                artikelinfoViewModel.setSearch(null);
                Toast.makeText(getApplicationContext(), "Keine gültige Artikelnummer!", Toast.LENGTH_LONG).show();
            } else {
                if (artikel.getArtikelnummer().equals("Error")) {
                    Intent i = new Intent(ArtikelinfoActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
                    artikelinfoViewModel.setArtikel(artikel);
                    artikelinfoViewModel.setSearch(null);
            }
            if (loadingDialogFragment != null) {
                loadingDialogFragment.dismiss();
            }
            previousQuery = "";
        }
    }


}