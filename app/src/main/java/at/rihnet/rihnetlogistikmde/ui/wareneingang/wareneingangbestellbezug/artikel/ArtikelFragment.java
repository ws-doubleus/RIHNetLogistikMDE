package at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.artikel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentWareneingangBestellbezugArtikelBinding;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.sqlite.QueueBelegpositionDAO;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.WareneingangBestellbezugViewModel;

public class ArtikelFragment extends Fragment implements MenuProvider {

    private final String TAG = "RIHNet";
    private FragmentWareneingangBestellbezugArtikelBinding binding;
    private ArtikelRecyclerViewAdapter artikelRecyclerViewAdapter;
    private WareneingangBestellbezugViewModel wareneingangBestellbezugViewModel;
    private QueueBelegpositionDAO queueBelegpositionDAO;
    private List<Belegposition> belegpositionen = new ArrayList<>();
    private String previousQuery = "";
    private OnSearchArtikel searchArtikel;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;

    public interface OnSearchArtikel {
        void onSearchArtikel(MenuItem menuItem, SearchView searchView);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWareneingangBestellbezugArtikelBinding.inflate(inflater, container, false);

        initRoom();
        initViewModel();
        initActivityResultLauncher();
        initRecyclerView();
        initFabClick();

        wareneingangBestellbezugViewModel.getSearchArtikel().observe(getViewLifecycleOwner(), this::doSearch);

        return binding.getRoot();
    }

    private void initActivityResultLauncher() {
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == 1 && result.getData() != null) {
                        Bundle bundle = result.getData().getExtras();
                        if (bundle != null) {
                            Belegposition belegposition = (Belegposition) bundle.getSerializable("belegposition");
                            if (belegposition != null) {
                                // 1) Ins ViewModel packen
                                wareneingangBestellbezugViewModel.addQueue(belegposition);

                                // 2) Asynchron in DB speichern
                                new AsyncTaskExecutorService<Belegposition, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Belegposition params) {
                                        if (queueBelegpositionDAO != null) {
                                            queueBelegpositionDAO.insert(params);
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void result) {
                                        Log.d(TAG, "Belegposition in DB eingefügt");
                                    }
                                }.execute(belegposition);
                            }
                        }
                    }
                }
        );
    }

    private void initViewModel() {
        wareneingangBestellbezugViewModel = new ViewModelProvider(requireActivity()).get(WareneingangBestellbezugViewModel.class);
        wareneingangBestellbezugViewModel.getBelegposition().observe(getViewLifecycleOwner(), b -> {
            belegpositionen = b;
            artikelRecyclerViewAdapter.setBelegpositionen(b);
            artikelRecyclerViewAdapter.notifyDataSetChanged();
            handleEmptyView();
        });
    }

    private void initRecyclerView() {
        RecyclerView rv_artikel = binding.rvArtikel;
        artikelRecyclerViewAdapter = new ArtikelRecyclerViewAdapter(belegpositionen);

        rv_artikel.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_artikel.setHasFixedSize(true);
        rv_artikel.setItemAnimator(new DefaultItemAnimator());
        rv_artikel.setAdapter(artikelRecyclerViewAdapter);

        artikelRecyclerViewAdapter.setOnItemClickListener(position -> {
            Belegposition belegposition = artikelRecyclerViewAdapter.getBelegpositionAt(position);
            ArtikelDialogFragment artikelDialogFragment = ArtikelDialogFragment.newInstance(position, belegposition);
            artikelDialogFragment.show(getParentFragmentManager(), "artikelDialogFragment");
        });

        handleEmptyView();
    }

    private void initRoom() {
        MyDatabase myDatabase = MyDatabase.getInstance(requireContext());
        queueBelegpositionDAO = myDatabase.getQueueBelegpositionDAO();
    }

    private void handleEmptyView() {
        RecyclerView rv_artikel = binding.rvArtikel;
        TextView tv_empty = binding.tvEmpty;

        if (belegpositionen != null && !belegpositionen.isEmpty()) {
            rv_artikel.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        } else {
            rv_artikel.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        }
    }

    private void initFabClick() {
        FloatingActionButton fab_add = binding.fabAdd;
        fab_add.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ArtikelActivity.class);
            someActivityResultLauncher.launch(intent);
        });
    }

    private void doSearch(String search) {
        CommunicationCommon.hideKeyboard(requireActivity());
        if (search == null || search.isEmpty()) return;

        // Async Task für Suche
        new AsyncTaskExecutorService<String, Void, Integer>() {
            // Status codes für onPostExecute
            static final int SUCCESS = 1;
            static final int NO_DATA = 2;
            static final int NOT_FOUND = 3;

            // Zwischenspeicher für gefundene/neue Daten
            Belegposition matchedPos;
            boolean isNewInQueue;

            @Override
            protected Integer doInBackground(String query) {
                if (belegpositionen == null || belegpositionen.isEmpty()) {
                    return NO_DATA;
                }

                // Suche im RAM
                Belegposition match = null;
                for (Belegposition bp : belegpositionen) {
                    if ((bp.getArtikelnummer().equalsIgnoreCase(query) || bp.getEannummer().equalsIgnoreCase(query)) && bp.getOffen() > 0) {
                        match = bp;
                        break;
                    }
                }

                if (match == null) {
                    return NOT_FOUND;
                }

                matchedPos = match;
                
                // DB Operationen
                if (queueBelegpositionDAO != null) {
                    Belegposition queuePos = queueBelegpositionDAO.getBelegpositionByBelegtypBelegnummerArtikelnummer(
                            match.getBelegtyp(),
                            match.getBelegnummer(),
                            match.getPostext(),
                            match.getArtikelnummer(),
                            Kategorie.WARENEINGANGBESTELLBEZUG
                    );

                    if (queuePos != null) {
                        isNewInQueue = false;
                        queueBelegpositionDAO.updateBelegpositionById(queuePos.getId(), (queuePos.getMenge() + 1));
                    } else {
                        isNewInQueue = true;
                        // Clone manuell simulieren oder Konstruktor nutzen
                        Belegposition neueQueuePosition = match.clone(); // Clone muss in Model implementiert sein!
                        neueQueuePosition.setMenge(1);
                        neueQueuePosition.setOffen(0); // In Queue irrelevant
                        neueQueuePosition.setKategorie(Kategorie.WARENEINGANGBESTELLBEZUG);
                        
                        queueBelegpositionDAO.insert(neueQueuePosition);
                        // Für UI Update merken
                        matchedPos = neueQueuePosition; // ACHTUNG: Hier tauschen wir das Objekt für den UI-Queue-Add
                    }
                }
                
                return SUCCESS;
            }

            @Override
            protected void onPostExecute(Integer result) {
                if (!isAdded()) return;

                previousQuery = "";
                
                switch (result) {
                    case NO_DATA:
                        Toast.makeText(getContext(), "Keine Belegpositionen verfügbar!", Toast.LENGTH_SHORT).show();
                        break;
                    case NOT_FOUND:
                        Toast.makeText(getContext(), "Keinen passenden Artikel gefunden oder nichts mehr offen!", Toast.LENGTH_SHORT).show();
                        break;
                    case SUCCESS:
                        // 1. Offene Menge im UI aktualisieren (Original-Match aus der Liste)
                        // Da matchedPos im doInBackground evtl getauscht wurde, müssen wir das Original in der Liste wiederfinden/updaten?
                        // Nein, wir müssen die Liste der offenen Positionen updaten.
                        // Wir suchen das Element nochmal in der Liste im UI Thread
                        for (Belegposition bp : belegpositionen) {
                             if ((bp.getArtikelnummer().equalsIgnoreCase(search) || bp.getEannummer().equalsIgnoreCase(search)) && bp.getOffen() > 0) {
                                 bp.setOffen(bp.getOffen() - 1);
                                 break;
                             }
                        }
                        wareneingangBestellbezugViewModel.setBelegposition(belegpositionen);

                        // 2. Queue aktualisieren
                        if (isNewInQueue) {
                            wareneingangBestellbezugViewModel.addQueue(matchedPos);
                            Toast.makeText(getContext(), "Artikel in Queue eingefügt!", Toast.LENGTH_SHORT).show();
                        } else {
                            // TODO: Besser wäre updateQueue im ViewModel
                            // Aktuell reicht Toast, da QueueFragment neu lädt
                            Toast.makeText(getContext(), "Menge in Queue um 1 erhöht!", Toast.LENGTH_SHORT).show();
                            // Optional: ViewModel Queue updaten, falls QueueFragment aktiv ist
                        }
                        break;
                }
                wareneingangBestellbezugViewModel.setSearchArtikel(null);
            }
        }.execute(search);
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
        searchArtikel = (OnSearchArtikel) context;
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.wareneingangbestellbezug_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        if (searchView != null) {
            EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            searchEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
            searchView.setQueryHint("Suchen...");
            searchArtikel.onSearchArtikel(menuItem, searchView);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!query.equals(previousQuery)) {
                        previousQuery = query;
                        if (!query.isEmpty()) {
                            wareneingangBestellbezugViewModel.setSearchArtikel(query);
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
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
