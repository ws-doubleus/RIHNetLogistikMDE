package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelabbuchen.artikel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.io.IOError;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentArtikelaBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelabbuchen.ArtikelAbbuchenViewModel;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.main.MainActivity;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.artikel.ChargeActivity;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.artikel.SeriennummerActivity;

public class ArtikelaFragment extends Fragment implements MenuProvider {
    //private static final String TAG = "RIHNet";
    private LoadingDialogFragment loadingDialogFragment;
    private FragmentArtikelaBinding binding;
    private OnChangeTab changeTab;
    private OnSearchArtikel searchArtikel;
    private ArtikelaViewModel artikelaViewModel;
    private EditText et_menge;
    private AppCompatImageButton btn_add;
    private AppCompatImageButton btn_seriennummercharge;
    private TextView tv_artikelnummer;
    private TextView tv_seriennummercharge_label;
    private TextView tv_seriennummercharge;
    private ArtikelAbbuchenViewModel artikelAbbuchenViewModel;
    private Button btn_weiter;
    private Artikel artikel;
    private String previousQuery = "";

    public interface OnChangeTab {
        void onChangeTab(int id);
    }

    public interface OnSearchArtikel {
        void onSearchArtikel(MenuItem menuItem, SearchView searchView);
    }

    // S => T404953
    // C => T410645
    // O => T410643
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        artikelaViewModel = new ViewModelProvider(this).get(at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelabbuchen.artikel.ArtikelaViewModel.class);
        binding = FragmentArtikelaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadingDialogFragment = LoadingDialogFragment.newInstance("Artikel wird geladen...");
        loadingDialogFragment.setCancelable(false);

        artikelAbbuchenViewModel = new ViewModelProvider(requireActivity()).get(ArtikelAbbuchenViewModel.class);

        final TextView tv_bezeichnung = binding.tvBezeichnung;
        final TextView tv_zusatz = binding.tvZusatz;
        final TextView tv_hstartikelnummer = binding.tvHstartikelnummer;
        final AppCompatImageButton btn_remove = binding.btnRemove;
        final Button btn_reset = binding.btnReset;

        et_menge = binding.etMenge;
        tv_artikelnummer = binding.tvArtikelnummer;
        tv_seriennummercharge_label = binding.tvSeriennummerchargeLabel;
        tv_seriennummercharge = binding.tvSeriennummercharge;
        btn_add = binding.btnAdd;
        btn_seriennummercharge = binding.btnSeriennummercharge;
        btn_weiter = binding.btnWeiter;

        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == 1 && result.getData() != null) {
                        Bundle bundle = result.getData().getExtras();
                        assert bundle != null;
                        String seriennummer = bundle.getString("seriennummer");
                        String lager = bundle.getString("lager");
                        artikel.setSeriennummer(seriennummer);
                        artikel.setLager(lager);
                        artikelaViewModel.setArtikel(artikel);
                    } else if (result.getResultCode() == 2 && result.getData() != null) {
                        Bundle bundle = result.getData().getExtras();
                        assert bundle != null;
                        String charge = bundle.getString("charge");
                        String lager = bundle.getString("lager");
                        int bestand = bundle.getInt("bestand");
                        artikel.setCharge(charge);
                        artikel.setLager(lager);
                        artikel.setBestand(bestand);
                        artikelaViewModel.setArtikel(artikel);
                    }
                }
        );

        btn_remove.setOnClickListener(view -> {
            if (et_menge.getText().length() > 0) {
                int value = Integer.parseInt(et_menge.getText().toString());
                if (value > 1) {
                    value--;
                } else {
                    value = 1;
                }
                et_menge.setText(String.valueOf(value));
                if (artikel != null) {
                    artikel.setMenge(value);
                    artikelaViewModel.setArtikel(artikel);
                }
            } else {
                et_menge.setText("1");
                if (artikel != null) {
                    artikel.setMenge(1);
                    artikelaViewModel.setArtikel(artikel);
                }
            }
        });
        btn_remove.setEnabled(false);
        btn_remove.setImageAlpha(50);

        TextWatcher mengeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("0")) {
                    et_menge.setText("1");
                }
                if (editable.toString().isEmpty() || editable.toString().equals("0") || editable.toString().equals("1")) {
                    btn_remove.setEnabled(false);
                    btn_remove.setImageAlpha(50);
                } else {
                    btn_remove.setEnabled(true);
                    btn_remove.setImageAlpha(255);
                }
            }
        };
        et_menge.addTextChangedListener(mengeTextWatcher);

        TextWatcher seriennummerTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        tv_seriennummercharge.addTextChangedListener(seriennummerTextWatcher);

        btn_add.setOnClickListener(view -> {
            if (et_menge.getText().length() > 0) {
                if (artikel != null) {
                    artikel.setMenge(Integer.parseInt(et_menge.getText().toString()) + 1);
                    artikelaViewModel.setArtikel(artikel);
                } else {
                    et_menge.setText(String.valueOf(Integer.parseInt(et_menge.getText().toString()) + 1));
                }
            } else {
                if (artikel != null) {
                    artikel.setMenge(1);
                    artikelaViewModel.setArtikel(artikel);
                } else {
                    et_menge.setText("1");
                }
            }
        });

        btn_seriennummercharge.setOnClickListener(view -> {
            if (artikel.getSerieCharge().equals("S")) {

                Bundle bundle = new Bundle();
                bundle.putString("artikelnummer", artikel.getArtikelnummer());
                Intent intent = new Intent(getActivity(), SeriennummerActivity.class);
                intent.putExtras(bundle);
                someActivityResultLauncher.launch(intent);
            } else if (artikel.getSerieCharge().equals("C")) {
                Bundle bundle = new Bundle();
                bundle.putString("artikelnummer", artikel.getArtikelnummer());
                Intent intent = new Intent(getActivity(), ChargeActivity.class);
                intent.putExtras(bundle);
                someActivityResultLauncher.launch(intent);
            }
        });

        btn_reset.setOnClickListener(view -> {
            et_menge.setEnabled(true);
            btn_add.setEnabled(true);
            btn_add.setImageAlpha(255);
            artikelaViewModel.resetArtikel();
            artikelAbbuchenViewModel.setArtikel(null);
            ((InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(requireView().getWindowToken(), 0);
        });

        btn_weiter.setOnClickListener(view -> {
            artikelAbbuchenViewModel.setArtikel(artikel);
            changeTab.onChangeTab(R.id.navigation_lager);
        });

        artikelaViewModel.getArtikel().observe(getViewLifecycleOwner(), artikel -> {
            this.artikel = artikel;
            if (artikel != null && artikel.getBestand() > 0 && !artikel.getSerieCharge().equals("S")) {
                if (artikel.getArtikelnummer().equals(("Error"))) {
                    return;
                }
                et_menge.setText(String.valueOf(artikel.getMenge()));
                tv_artikelnummer.setText(artikel.getArtikelnummer());
                tv_bezeichnung.setText(artikel.getBezeichnung());
                tv_zusatz.setText(artikel.getZusatz());
                tv_hstartikelnummer.setText(artikel.getHstArtikelnummer());
                if (artikel.getSerieCharge().equals("S")) {
                    tv_seriennummercharge_label.setText(R.string.artikel_seriennummer);
                    tv_seriennummercharge.setText(artikel.getSeriennummer());
                    setSeriennummerChargeViewVisibility(View.VISIBLE);
                } else if (artikel.getSerieCharge().equals("C")) {
                    tv_seriennummercharge_label.setText(R.string.artikel_charge);
                    tv_seriennummercharge.setText(artikel.getCharge());
                    setSeriennummerChargeViewVisibility(View.VISIBLE);
                } else {
                    tv_seriennummercharge_label.setText("---:");
                    tv_seriennummercharge.setText(null);
                    setSeriennummerChargeViewVisibility(View.GONE);
                }
                setBtnWeiterEnabled();
            } else {
                if (artikel == null) {
                    Toast.makeText(getContext(), "Artikelnummer: " + artikelAbbuchenViewModel.getSearchArtikel().getValue() + " wurde nicht gefunden!", Toast.LENGTH_LONG).show();
                } else if (artikel.getSerieCharge().equals("S")) {
                    Toast.makeText(getContext(), "Artikelnummer: " + artikelAbbuchenViewModel.getSearchArtikel().getValue() + "\nArtikel mit Seriennummer werden noch nicht unterstützt!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Artikelnummer: " + artikelAbbuchenViewModel.getSearchArtikel().getValue() + " | Bestand: 0 | Artikel kann nicht abgebucht werden!", Toast.LENGTH_LONG).show();
                }
                et_menge.setText("1");
                tv_artikelnummer.setText("");
                tv_bezeichnung.setText("");
                tv_zusatz.setText("");
                tv_hstartikelnummer.setText("");
                tv_seriennummercharge.setText("");
                btn_weiter.setEnabled(false);
                setSeriennummerChargeViewVisibility(View.GONE);
                artikelAbbuchenViewModel.setArtikel(null);
            }

        });

        artikelAbbuchenViewModel.getSearchArtikel().observe(getViewLifecycleOwner(), this::doSearch);

        artikelAbbuchenViewModel.getResetArtikel().observe(getViewLifecycleOwner(), reset -> {
            if (reset) {
                artikelaViewModel.resetArtikel();
                artikelAbbuchenViewModel.setResetArtikel(false);
            }
        });

        setSeriennummerChargeViewVisibility(View.GONE);
        btn_weiter.setEnabled(false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        changeTab = (OnChangeTab) context;
        searchArtikel = (OnSearchArtikel) context;
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.artikelabbuchen_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(18);
        searchEditText.setFilters(filters);

        searchView.setQueryHint("Suchen...");
        searchArtikel.onSearchArtikel(menuItem, searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.equals(previousQuery)) {
                    previousQuery = query;
                    if (!query.isEmpty()) {
                        artikelAbbuchenViewModel.setSearchArtikel(query);
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
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    private void doSearch(String search) {
        CommunicationCommon.hideKeyboard(requireActivity());
        if (search != null && !search.isEmpty()) {
            loadingDialogFragment.show(getChildFragmentManager(), "fragment_loading_dialog");
            new Handler().postDelayed(() -> {
                LoadArtikelAsyncTask loadArtikelAsyncTask = new LoadArtikelAsyncTask();
                loadArtikelAsyncTask.execute(search);
            }, 300);
        }
    }

    private void setSeriennummerChargeViewVisibility(int visibility) {
        tv_seriennummercharge_label.setVisibility(visibility);
        tv_seriennummercharge.setVisibility(visibility);
        btn_seriennummercharge.setVisibility(visibility);
    }

    private void setBtnWeiterEnabled() {
        if (!et_menge.getText().toString().isEmpty() && !tv_artikelnummer.getText().toString().isEmpty()) {
            Artikel artikel = artikelaViewModel.getArtikel().getValue();
            assert artikel != null;
            if (artikel.getSerieCharge().equals("O")) {
                btn_weiter.setEnabled(true);
            } else {
                btn_weiter.setEnabled(tv_seriennummercharge.getText().length() > 0);
            }
        } else {
            btn_weiter.setEnabled(false);
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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                String standort = prefs.getString("standort", null);
                String ipadresse = prefs.getString("ipadresse", "");
                String port = prefs.getString("port", "");
                String datenbank = prefs.getString("datenbank", "");
                String instance = prefs.getString("instance", "");
                String benutzername = prefs.getString("benutzername", "");
                String kennwort = prefs.getString("kennwort", "");
                SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

                return CommunicationSql.getArtikel(sqlServerData, s, standort);
            } catch (IOError | Exception error) {
                return new Artikel("Error", "", "", "", "", "", 0, 0, "", "", "", "", 0, "", Kategorie.ARTIKELABBUCHEN);
            }
        }

        @Override
        protected void onPostExecute(Artikel artikel) {
            if (artikel == null) {
                et_menge.setEnabled(true);
                btn_add.setEnabled(true);
                btn_add.setImageAlpha(255);
                setSeriennummerChargeViewVisibility(View.GONE);
            } else {
                if (artikel.getArtikelnummer().equals("Error")) {
                    Intent i = new Intent(getActivity(), MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
                if (artikel.getSerieCharge().equals("S")) {
                    artikel.setMenge(1);
                    et_menge.setText("1");
                    et_menge.setEnabled(false);
                    btn_add.setEnabled(false);
                    btn_add.setImageAlpha(50);
                } else {
                    artikel.setMenge(Integer.parseInt(et_menge.getText().toString()));
                    et_menge.setEnabled(true);
                    btn_add.setEnabled(true);
                    btn_add.setImageAlpha(255);
                }
                setSeriennummerChargeViewVisibility(View.VISIBLE);

            }
            previousQuery = "";
            artikelaViewModel.setArtikel(artikel);
            artikelAbbuchenViewModel.setSearchArtikel(null);
            if (loadingDialogFragment != null) {
                loadingDialogFragment.dismiss();
            }
        }
    }
}