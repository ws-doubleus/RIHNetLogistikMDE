package at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.queue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentWareneingangBestellbezugQueueBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Beleg;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.SelectLine.ArticlePositionItem;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentCreateModel;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentDetailAddress;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentDetailBusinessPartner;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionStoreInformationUpdate;
import at.rihnet.rihnetlogistikmde.models.SelectLine.PredecessorDocumentData;
import at.rihnet.rihnetlogistikmde.models.SelectLine.SuccessorsDocumentData;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.sqlite.BelegDAO;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.sqlite.QueueBelegpositionDAO;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.WareneingangBestellbezugViewModel;

public class QueueFragment extends Fragment implements MenuProvider {

    private final String TAG = "RIHNet";

    // UI-Binding
    private FragmentWareneingangBestellbezugQueueBinding binding;

    // ViewModel & SharedPreferences
    private WareneingangBestellbezugViewModel wareneingangBestellbezugViewModel;
    private SharedPreferences prefs;

    // Loading-Dialog
    private LoadingDialogFragment loadingDialogFragment;

    // RecyclerView + Daten
    private QueueRecyclerViewAdapter queueRecyclerViewAdapter;
    private List<Belegposition> queueList = new ArrayList<>();

    // Room-DAOs
    private QueueBelegpositionDAO queueBelegpositionDAO;
    private LogDAO logDAO;
    private BelegDAO belegDAO;

    // Interface Callback
    private OnSearchQueue searchQueue;

    // Aktueller Beleg
    private Beleg beleg;

    // Counter für Erfolge beim Buchen
    private int successCounter = 0;

    // Interface
    public interface OnSearchQueue {
        void onSearchQueue(MenuItem menuItem, SearchView searchView);
    }

    // ---------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWareneingangBestellbezugQueueBinding.inflate(inflater, container, false);

        initLoadingDialog();
        initSharedPreferences();
        initRoom();
        initViewModel();
        initRecyclerView();
        initQueueObserver();
        initCreateWareneingangButton();
        initItemTouchHelper();

        // Suche-Observer (LiveData)
        wareneingangBestellbezugViewModel.getSearchQueue().observe(getViewLifecycleOwner(), this::doSearch);

        // Prüfen, ob Queue leer ist
        updateEmptyView();

        // Queue-Einträge asynchron laden
        loadQueueFromDb();

        return binding.getRoot();
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        searchQueue = (OnSearchQueue) context;
    }

    // ---------------------------------------------------------
    // Menü / Suchfeld (MenuProvider)
    // ---------------------------------------------------------
    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.freierwareneingang_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        if (searchView != null) {
            searchView.setQueryHint("Suchen...");
            // Callback, um das SearchView an Activity zu übergeben
            searchQueue.onSearchQueue(menuItem, searchView);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    wareneingangBestellbezugViewModel.setSearchQueue(newText);
                    return false;
                }
            });
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    // ---------------------------------------------------------
    // Initialisierung / Setup-Methoden
    // ---------------------------------------------------------
    private void initLoadingDialog() {
        loadingDialogFragment = LoadingDialogFragment.newInstance("Wareneingang wird angelegt...");
        loadingDialogFragment.setCancelable(false);
    }

    private void initSharedPreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    private void initRoom() {
        MyDatabase myDatabase = MyDatabase.getInstance(requireContext());
        queueBelegpositionDAO = myDatabase.getQueueBelegpositionDAO();
        logDAO = myDatabase.getLogDAO();
        belegDAO = myDatabase.getBelegDAO();
    }

    private void initViewModel() {
        wareneingangBestellbezugViewModel = new ViewModelProvider(requireActivity()).get(WareneingangBestellbezugViewModel.class);
    }

    private void initRecyclerView() {
        RecyclerView rv_queue = binding.rvQueue;
        queueRecyclerViewAdapter = new QueueRecyclerViewAdapter(queueList);

        rv_queue.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_queue.setHasFixedSize(true);
        rv_queue.setItemAnimator(new DefaultItemAnimator());
        rv_queue.setAdapter(queueRecyclerViewAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initQueueObserver() {
        wareneingangBestellbezugViewModel.getQueue().observe(getViewLifecycleOwner(), q -> {
            queueList = q;
            queueRecyclerViewAdapter.setQueue(q);
            queueRecyclerViewAdapter.notifyDataSetChanged();
            updateEmptyView();
        });
    }

    private void initCreateWareneingangButton() {
        Button btn_wareneinganganlegen = binding.btnWareneinganganlegen;
        btn_wareneinganganlegen.setOnClickListener(v -> {
            loadingDialogFragment.show(getChildFragmentManager(), "fragment_loading_dialog");
            new Handler().postDelayed(() -> {
                CreateWareneinganAsyncTask task = new CreateWareneinganAsyncTask();
                task.execute();
            }, 300);
        });
    }

    private void initItemTouchHelper() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                handleSwipedItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(binding.rvQueue);
    }

    private void loadQueueFromDb() {
        new AsyncTaskExecutorService<Void, Void, List<Belegposition>>() {
            @Override
            protected List<Belegposition> doInBackground(Void params) {
                if (queueBelegpositionDAO != null) {
                    return queueBelegpositionDAO.getBelegpositionByKategorie(Kategorie.WARENEINGANGBESTELLBEZUG);
                }
                return new ArrayList<>();
            }

            @Override
            protected void onPostExecute(List<Belegposition> result) {
                if (isAdded() && result != null) {
                    queueList.clear();
                    queueList.addAll(result);
                    wareneingangBestellbezugViewModel.setQueue(queueList);
                }
            }
        }.execute();
    }

    private void updateEmptyView() {
        RecyclerView rv_queue = binding.rvQueue;
        TextView tv_empty = binding.tvEmpty;
        Button btn_wareneinganganlegen = binding.btnWareneinganganlegen;

        if (!queueList.isEmpty()) {
            rv_queue.smoothScrollToPosition(queueList.size() - 1);
            rv_queue.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
            btn_wareneinganganlegen.setVisibility(View.VISIBLE);
        } else {
            rv_queue.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
            btn_wareneinganganlegen.setVisibility(View.GONE);
        }
    }

    private void handleSwipedItem(int adapterPosition) {
        Belegposition belegposition = queueRecyclerViewAdapter.getQueueAt(adapterPosition);
        if (belegposition == null) return;

        new AsyncTaskExecutorService<Belegposition, Void, Void>() {
            @Override
            protected Void doInBackground(Belegposition params) {
                if (queueBelegpositionDAO != null) {
                    queueBelegpositionDAO.delete(params);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (isAdded()) {
                    wareneingangBestellbezugViewModel.removeQueue(belegposition);
                    
                    // Optional: Offene Menge in Artikelliste zurücksetzen
                    List<Belegposition> belegpositionen = wareneingangBestellbezugViewModel.getBelegposition().getValue();
                    if (belegpositionen != null) {
                        Optional<Belegposition> gefundeneBelegposition = belegpositionen.stream()
                                .filter(b -> b.getArtikelnummer().equals(belegposition.getArtikelnummer())
                                        && b.getBelegnummer().equals(belegposition.getBelegnummer())
                                        && b.getPostext().equals(belegposition.getPostext()))
                                .findFirst();
                        if (gefundeneBelegposition.isPresent()) {
                            Belegposition bp = gefundeneBelegposition.get();
                            bp.setOffen(bp.getOffen() + belegposition.getMenge());
                            wareneingangBestellbezugViewModel.setBelegposition(belegpositionen);
                        }
                    }
                }
            }
        }.execute(belegposition);
    }

    private void doSearch(String search) {
        if (search == null) search = "";
        
        if (!search.isEmpty()) {
            final String searchQuery = search;
            new AsyncTaskExecutorService<String, Void, String>() {
                @Override
                protected String doInBackground(String params) {
                    try {
                        String standort = prefs.getString("standort", null);
                        String ipadresse = prefs.getString("ipadresse", "");
                        String port = prefs.getString("port", "");
                        String datenbank = prefs.getString("datenbank", "");
                        String instance = prefs.getString("instance", "");
                        String benutzer = prefs.getString("benutzername", "");
                        String kennwort = prefs.getString("kennwort", "");

                        SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzer, kennwort);
                        Artikel a = CommunicationSql.getArtikel(sqlServerData, params, standort);
                        if (a != null && "K".equals(a.getSn())) {
                            return a.getArtikelnummer();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Fehler bei Artikelsuche: " + e.getMessage());
                    }
                    return params;
                }

                @Override
                protected void onPostExecute(String result) {
                    if (isAdded() && queueRecyclerViewAdapter != null) {
                        queueRecyclerViewAdapter.getFilter().filter(result);
                    }
                }
            }.execute(searchQuery);
        } else {
            if (queueRecyclerViewAdapter != null) {
                queueRecyclerViewAdapter.getFilter().filter("");
            }
        }
    }

    // ---------------------------------------------------------
    // ASYNC TASK: Wareneingang erstellen
    // ---------------------------------------------------------
    public class CreateWareneinganAsyncTask extends AsyncTaskExecutorService<Void, Belegposition, String> {
        private int totalCount = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (queueList != null) {
                totalCount = queueList.size();
            }
            successCounter = 0; // Reset für neuen Durchlauf
        }

        @Override
        protected String doInBackground(Void unused) throws Exception {
            beleg = wareneingangBestellbezugViewModel.getBeleg().getValue();

            // Lokale Kopie erstellen und sortieren
            List<Belegposition> workingList = new ArrayList<>();
            if (wareneingangBestellbezugViewModel.getQueue().getValue() != null) {
                workingList.addAll(wareneingangBestellbezugViewModel.getQueue().getValue());
            }
            // Sortierung: Erst nach Belegnummer, dann nach Artikelnummer
            workingList.sort(Comparator.comparing(Belegposition::getBelegnummer)
                    .thenComparing(Belegposition::getArtikelnummer));
            Collections.reverse(workingList);

            if (beleg == null || beleg.getLieferant() == null || beleg.getLieferant().getNummer().isEmpty()) {
                return "Fehler: Beleg oder Lieferant nicht gesetzt!";
            }

            // Zentraler Login wird vorausgesetzt (wie bei Inventur)!
            // Optional: Ein schneller Check, ob wir "drin" sind (z.B. Test-Call) könnte hier stehen,
            // aber wir vertrauen auf den App-Flow.
            
            // Liste der beteiligten Belegnummern
            List<String> distinctBelegnummern = workingList.stream()
                    .map(Belegposition::getBelegnummer)
                    .distinct()
                    .collect(Collectors.toList());

            DocumentCreated documentCreated = null;
            String finalDocumentNumber = "";

            for (String belegnummer : distinctBelegnummern) {
                if (belegnummer != null && !belegnummer.replace("-", "").isEmpty()) {
                    // -> Fall 1: Bestellbezug
                    documentCreated = handleDocumentSuccessor(workingList, belegnummer, documentCreated);
                } else {
                    // -> Fall 2: Keine Belegnummer (frei)
                    if (documentCreated == null) {
                        DocumentCreateModel dcm = getDocumentCreateModel(beleg);
                        documentCreated = CommunicationSelectLine.createDocument(dcm);
                    }
                    if (documentCreated != null) {
                        handleDocumentWithoutBelegnummer(workingList, documentCreated);
                    }
                }
            }

            if (documentCreated != null) {
                finalDocumentNumber = documentCreated.getDocumentNumber();
            }

            return finalDocumentNumber;
        }

        @Override
        protected void onProgressUpdate(@NotNull Belegposition value) {
            // UI-Update: Element aus der ViewModel-Queue entfernen
            wareneingangBestellbezugViewModel.removeQueue(value);
        }

        @Override
        protected void onPostExecute(String documentNumber) {
            if (!isAdded()) return;

            at.rihnet.rihnetlogistikmde.models.Log logEntry;
            boolean isError = documentNumber == null || documentNumber.startsWith("Fehler:") || documentNumber.isEmpty();

            if (!isError && successCounter > 0) {
                if (successCounter == totalCount) {
                    logEntry = new at.rihnet.rihnetlogistikmde.models.Log(
                            "Belegnummer: " + documentNumber + "\nPositionen: " + successCounter + " / " + totalCount + "\nWareneingang erfolgreich!",
                            ContextCompat.getColor(requireContext(), R.color.green_500),
                            Kategorie.WARENEINGANGBESTELLBEZUG
                    );
                    
                    // Beleg zurücksetzen
                    if (beleg != null) {
                        beleg.setLieferscheinnummer("");
                        wareneingangBestellbezugViewModel.setBeleg(beleg);
                        
                        // Async DB Update
                        new AsyncTaskExecutorService<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void params) {
                                if (belegDAO != null) {
                                    Beleg bDb = belegDAO.getBeleg(Kategorie.WARENEINGANGBESTELLBEZUG);
                                    if (bDb != null) {
                                        bDb.setLieferscheinnummer("");
                                        belegDAO.update(bDb);
                                    }
                                }
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void result) {}
                        }.execute();
                    }

                } else {
                    logEntry = new at.rihnet.rihnetlogistikmde.models.Log(
                            "Belegnummer: " + documentNumber + "\nPositionen: " + successCounter + " / " + totalCount + "\nWareneingang teilweise erfolgreich!",
                            ContextCompat.getColor(requireContext(), R.color.orange_500),
                            Kategorie.WARENEINGANGBESTELLBEZUG
                    );
                }
            } else {
                String msg = (documentNumber != null && !documentNumber.isEmpty()) ? documentNumber : "Unbekannter Fehler";
                logEntry = new at.rihnet.rihnetlogistikmde.models.Log(
                        msg + "\nPositionen: " + successCounter + " / " + totalCount + "\nWareneingang fehlerhaft!",
                        ContextCompat.getColor(requireContext(), R.color.red_500),
                        Kategorie.WARENEINGANGBESTELLBEZUG
                );
            }

            wareneingangBestellbezugViewModel.addLog(logEntry);

            // Log speichern Async
            final at.rihnet.rihnetlogistikmde.models.Log finalLog = logEntry;
            new AsyncTaskExecutorService<at.rihnet.rihnetlogistikmde.models.Log, Void, Void>() {
                @Override
                protected Void doInBackground(at.rihnet.rihnetlogistikmde.models.Log params) {
                    if (logDAO != null) logDAO.insert(params);
                    return null;
                }
                @Override
                protected void onPostExecute(Void result) {}
            }.execute(finalLog);

            if (loadingDialogFragment != null) {
                loadingDialogFragment.dismiss();
            }
        }

        // ---------------------------------------------------------
        // Hilfsmethoden innerhalb des Tasks
        // ---------------------------------------------------------

        private DocumentCreated handleDocumentSuccessor(List<Belegposition> list, String belegnummer, DocumentCreated currentDocument) throws Exception {
            
            // 1. Alle Positionen für diese Belegnummer filtern
            List<Belegposition> currentList = list.stream()
                    .filter(f -> f.getBelegnummer().equals(belegnummer))
                    .collect(Collectors.toList());

            if (currentList.isEmpty()) return currentDocument;

            List<DocumentPositionStoreInformationUpdate> positions = new ArrayList<>();

            for (Belegposition bp : currentList) {
                DocumentPositionStoreInformationUpdate docPos = new DocumentPositionStoreInformationUpdate();
                docPos.setIdentifier(bp.getKennung()); // Identifier für die Zuordnung
                docPos.setArticleNumber(bp.getArtikelnummer());
                docPos.setQuantity(bp.getMenge());
                
                if (beleg.getLagerplatz() != null) {
                    docPos.setStoragePlaceIdentifier(beleg.getLagerplatz().getLagerplatzId());
                }
                docPos.setWarehouse(beleg.getLager());
                
                positions.add(docPos);
            }

            if (currentDocument == null) {
                // FALL 1: Noch kein Wareneingang -> Neu anlegen via createDocumentSuccessor
                SuccessorsDocumentData successorsData = new SuccessorsDocumentData();
                successorsData.setDocumentKindDestination("S");
                successorsData.setPositions(positions);

                currentDocument = CommunicationSelectLine.createDocumentSuccessor(
                        "B" + belegnummer,
                        successorsData
                );
                
                if (currentDocument != null) {
                    // Alle Positionen als erfolgreich markieren und löschen
                    for (Belegposition bp : currentList) {
                        successCounter++;
                        if (queueBelegpositionDAO != null) queueBelegpositionDAO.delete(bp);
                        publishProgress(bp);
                    }
                }

            } else {
                // FALL 2: Wareneingang existiert -> Belegübernahme
                PredecessorDocumentData predecessorData = new PredecessorDocumentData();
                predecessorData.setPositions(positions);

                boolean result = CommunicationSelectLine.belegUebernahme(
                        currentDocument.getDocumentKey(),
                        "B" + belegnummer,
                        predecessorData
                );
                
                if (result) {
                    for (Belegposition bp : currentList) {
                        successCounter++;
                        if (queueBelegpositionDAO != null) queueBelegpositionDAO.delete(bp);
                        publishProgress(bp);
                    }
                }
            }
            return currentDocument;
        }

        private void handleDocumentWithoutBelegnummer(List<Belegposition> list, DocumentCreated documentCreated) throws Exception {
            List<Belegposition> currentList = list.stream()
                    .filter(f -> f.getBelegnummer().replace("-", "").isEmpty())
                    .collect(Collectors.toList());

            for (Belegposition bp : currentList) {
                ArticlePositionItem item = getArticlePositionItem(bp);
                
                DocumentPositionCreated posCreated = CommunicationSelectLine.createDocumentPositionWithArticleItemByDocumentKey(
                        documentCreated.getDocumentKey(), 
                        item
                );

                if (posCreated != null && !posCreated.getPositionIdentifier().isEmpty()) {
                    successCounter++;
                    if (queueBelegpositionDAO != null) queueBelegpositionDAO.delete(bp);
                    publishProgress(bp);
                }
            }
        }

        private DocumentCreateModel getDocumentCreateModel(Beleg beleg) {
            DocumentDetailAddress addr = new DocumentDetailAddress();
            addr.setNumber(beleg.getLieferant().getNummer());

            DocumentDetailBusinessPartner partner = new DocumentDetailBusinessPartner();
            partner.setReferenceAddressNumber(beleg.getLieferant().getNummer());
            partner.setAddress(addr);

            DocumentCreateModel model = new DocumentCreateModel();
            model.setKindFlag("S");
            model.setBusinessPartner(partner);
            model.setWarehouseNumber(beleg.getLager());
            model.setDeliveryDocumentNumber(beleg.getLieferscheinnummer());
            return model;
        }

        private ArticlePositionItem getArticlePositionItem(Belegposition bp) {
            ArticlePositionItem item = new ArticlePositionItem();
            item.setArticleNumber(bp.getArtikelnummer());
            item.setCalculatedQuantityValue(bp.getMenge());
            item.setWarehouseId(beleg.getLager());

            if (beleg.getLagerplatz() != null && beleg.getLagerplatz().getLagerplatzId() > 0) {
                DocumentPositionStoreInformationUpdate info = new DocumentPositionStoreInformationUpdate();
                info.setArticleNumber(bp.getArtikelnummer());
                info.setQuantity(bp.getMenge());
                info.setWarehouse(beleg.getLager());
                info.setStoragePlaceIdentifier(beleg.getLagerplatz().getLagerplatzId());

                List<DocumentPositionStoreInformationUpdate> infos = new ArrayList<>();
                infos.add(info);
                item.setStoreInformation(infos);
            }
            return item;
        }
    }
}
