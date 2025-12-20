package at.rihnet.rihnetlogistikmde.ui.wareneingang.freierwareneingang.queue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.room.Room;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentFreierWareneingangQueueBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Beleg;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.SelectLine.ArticlePositionItem;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentCreateModel;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentDetailAddress;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentDetailBusinessPartner;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionStoreInformationUpdate;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.sqlite.BelegDAO;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.sqlite.QueueDAO;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.main.MainActivity;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.freierwareneingang.FreierWareneingangViewModel;

public class QueueFragment extends Fragment implements MenuProvider {
    private final String TAG = "RIHNet";
    private SharedPreferences prefs;
    private LoadingDialogFragment loadingDialogFragment;
    private FragmentFreierWareneingangQueueBinding binding;
    private FreierWareneingangViewModel freierWareneingangViewModel;
    private QueueRecyclerViewAdapter queueRecyclerViewAdapter;
    private List<Artikel> queueList = new ArrayList<>();
    private QueueDAO queueDAO;
    private LogDAO logDAO;
    private BelegDAO belegDAO;
    private OnSearchQueue searchQueue;
    private Beleg beleg;

    public interface OnSearchQueue {
        void onSearchQueue(MenuItem menuItem, SearchView searchView);
    }

    @SuppressLint("NotifyDataSetChanged")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFreierWareneingangQueueBinding.inflate(inflater, container, false);

        loadingDialogFragment = LoadingDialogFragment.newInstance("Wareneingang wird angelegt...");
        loadingDialogFragment.setCancelable(false);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        RecyclerView rv_queue = binding.rvQueue;
        TextView tv_empty = binding.tvEmpty;
        Button btn_wareneinganganlegen = binding.btnWareneinganganlegen;

        freierWareneingangViewModel = new ViewModelProvider(requireActivity()).get(FreierWareneingangViewModel.class);
        freierWareneingangViewModel.getQueue().observe(getViewLifecycleOwner(), q -> {
            queueList = q;
            queueRecyclerViewAdapter.setQueue(q);
            queueRecyclerViewAdapter.notifyDataSetChanged();
            if (!queueList.isEmpty()) {
                binding.rvQueue.smoothScrollToPosition(queueList.size() - 1);
                rv_queue.setVisibility(View.VISIBLE);
                tv_empty.setVisibility(View.GONE);
                btn_wareneinganganlegen.setVisibility(View.VISIBLE);
            } else {
                rv_queue.setVisibility(View.GONE);
                tv_empty.setVisibility(View.VISIBLE);
                btn_wareneinganganlegen.setVisibility(View.GONE);
            }
        });

        btn_wareneinganganlegen.setOnClickListener(v -> {
            loadingDialogFragment.show(getChildFragmentManager(), "fragment_loading_dialog");
            new Handler().postDelayed(() -> {
                CreateWareneinganAsyncTask createWareneinganAsyncTask = new CreateWareneinganAsyncTask();
                createWareneinganAsyncTask.execute();
            }, 3000);
        });

        queueRecyclerViewAdapter = new QueueRecyclerViewAdapter(queueList);
        rv_queue.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_queue.setHasFixedSize(true);
        rv_queue.setItemAnimator(new DefaultItemAnimator());
        rv_queue.setAdapter(queueRecyclerViewAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Artikel artikel = queueRecyclerViewAdapter.getQueueAt(viewHolder.getAdapterPosition());
                queueDAO.delete(artikel);
                freierWareneingangViewModel.removeQueue(artikel);
            }
        }).attachToRecyclerView(binding.rvQueue);

        freierWareneingangViewModel.getSearchQueue().observe(getViewLifecycleOwner(), this::doSearch);

        if (queueRecyclerViewAdapter.getItemCount() > 0) {
            rv_queue.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        } else {
            rv_queue.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        }

        MyDatabase myDatabase = Room.databaseBuilder(requireContext(), MyDatabase.class, "rihnetdatabase").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        queueDAO = myDatabase.getQueueDAO();
        logDAO = myDatabase.getLogDAO();
        belegDAO = myDatabase.getBelegDAO();
        queueList.addAll(queueDAO.getArtikelByKategorie(Kategorie.FREIERWARENEINGANG));
        freierWareneingangViewModel.setQueue(queueList);

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

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.freierwareneingang_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setQueryHint("Suchen...");
        searchQueue.onSearchQueue(menuItem, searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                freierWareneingangViewModel.setSearchQueue(newText);
                return false;
            }
        });
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    private void doSearch(String search) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
            String standort = prefs.getString("standort", null);
            String ipadresse = prefs.getString("ipadresse", "");
            String port = prefs.getString("port", "");
            String datenbank = prefs.getString("datenbank", "");
            String instance = prefs.getString("instance", "");
            String benutzername = prefs.getString("benutzername", "");
            String kennwort = prefs.getString("kennwort", "");
            SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

            Artikel a = CommunicationSql.getArtikel(sqlServerData, search, standort);
            if (a != null) {
                if (a.getSn().equals("K")) {
                    search = a.getArtikelnummer();
                }
            }
            queueRecyclerViewAdapter.getFilter().filter(search);
        } catch (IOError | Exception error) {
            Intent i = new Intent(getActivity(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }

    public class CreateWareneinganAsyncTask extends AsyncTaskExecutorService<Void, Artikel, String> {
        private int success = 0;
        private final int all = queueList.size();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void unused) throws Exception {

            beleg = freierWareneingangViewModel.getBeleg().getValue();
            if (beleg != null && beleg.getLieferant() != null && !beleg.getLieferant().getNummer().isEmpty()) {
                String appKey = prefs.getString("appkey", "");
                String baseAddress = prefs.getString("baseaddress", "");
                String userName = prefs.getString("username", "");
                String password = prefs.getString("password", "");

                DocumentCreateModel documentCreateModel = getDocumentCreateModel(beleg);

                // Originaler Login-Aufruf wiederhergestellt
                if (CommunicationSelectLine.login(appKey, baseAddress, userName, password)) {
                    android.util.Log.i(TAG, "Anmeldung erfolgreich!");
                    DocumentCreated documentCreated = CommunicationSelectLine.createDocument(documentCreateModel);
                    if (documentCreated != null) {
                        android.util.Log.i(TAG, "documentCreated.getDocumentKey(): " + documentCreated.getDocumentKey());
                        queueList.sort(Comparator.comparing(Artikel::getArtikelnummer));
                        List<Artikel> list = queueList;
                        Collections.reverse(list);
                        for (int i = list.size() - 1; i >= 0; i--) {
                            try {
                                Artikel a = list.get(i);
                                ArticlePositionItem articlePositionItem = getArticlePositionItem(a);
                                android.util.Log.i(TAG, "a.getSeriennummer(): " + a.getSeriennummer());
                                android.util.Log.i(TAG, "a.getLagerplatzId(): " + a.getLagerplatzId());

                                ObjectMapper objectMapper = new ObjectMapper();
                                try {
                                    String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(articlePositionItem);
                                    android.util.Log.e(TAG, jsonString);
                                } catch (JsonProcessingException e) {
                                    android.util.Log.e(TAG, String.format("%s", e.getMessage()));
                                }

                                DocumentPositionCreated documentPositionCreated = CommunicationSelectLine.createDocumentPositionWithArticleItemByDocumentKey(documentCreated.getDocumentKey(), articlePositionItem);
                                if (documentPositionCreated != null && !documentPositionCreated.getPositionIdentifier().isEmpty()) {
                                    success++;
                                    publishProgress(a);
                                }
                            } catch (Exception e) {
                                android.util.Log.e(TAG, String.format("%s", e.getMessage()));
                            }
                        }
                        return documentCreated.getDocumentNumber();
                    } else {
                        return "";
                    }
                }
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(@NotNull Artikel value) {
            super.onProgressUpdate(value);
            queueList.remove(value);
            freierWareneingangViewModel.setQueue(queueList);
            queueDAO.delete(value);
        }

        @Override
        protected void onPostExecute(String documentNumber) {
            at.rihnet.rihnetlogistikmde.models.Log log;
            if (!documentNumber.isEmpty()) {
                if (success == all) {
                    log = new at.rihnet.rihnetlogistikmde.models.Log("Belegnummer: " + documentNumber + "\nBelegpositionen: " + success + " / " + all + "\nWareneingang erfolgreich!", ContextCompat.getColor(requireContext(), R.color.green_500), Kategorie.FREIERWARENEINGANG);
                    beleg.setLieferscheinnummer("");
                    freierWareneingangViewModel.setBeleg(beleg);
                    Beleg b = belegDAO.getBeleg(Kategorie.FREIERWARENEINGANG);
                    b.setLieferscheinnummer("");
                    belegDAO.update(b);
                } else {
                    log = new at.rihnet.rihnetlogistikmde.models.Log("Belegnummer: " + documentNumber + "\nBelegpositionen: " + success + " / " + all + "\nWareneingang teilweise erfolgreich!", ContextCompat.getColor(requireContext(), R.color.orange_500), Kategorie.FREIERWARENEINGANG);
                }
            } else {
                log = new at.rihnet.rihnetlogistikmde.models.Log("Belegnummer: " + documentNumber + "\nBelegpositionen: " + success + " / " + all + "\nWareneingang fehlerhaft!", ContextCompat.getColor(requireContext(), R.color.red_500), Kategorie.FREIERWARENEINGANG);
            }
            freierWareneingangViewModel.addLog(log);
            logDAO.insert(log);
            if (loadingDialogFragment != null) {
                loadingDialogFragment.dismiss();
            }
        }

        private @NonNull DocumentCreateModel getDocumentCreateModel(Beleg beleg) {
            DocumentDetailAddress documentDetailAddress = new DocumentDetailAddress();
            documentDetailAddress.setNumber(beleg.getLieferant().getNummer());

            DocumentDetailBusinessPartner documentDetailBusinessPartner = new DocumentDetailBusinessPartner();
            documentDetailBusinessPartner.setReferenceAddressNumber(beleg.getLieferant().getNummer());
            documentDetailBusinessPartner.setAddress(documentDetailAddress);

            DocumentCreateModel documentCreateModel = new DocumentCreateModel();
            documentCreateModel.setKindFlag("S");
            documentCreateModel.setBusinessPartner(documentDetailBusinessPartner);
            documentCreateModel.setWarehouseNumber(beleg.getLager());
            documentCreateModel.setDeliveryDocumentNumber(beleg.getLieferscheinnummer());
            return documentCreateModel;
        }

        private @NonNull ArticlePositionItem getArticlePositionItem(Artikel a) {
            ArticlePositionItem articlePositionItem = new ArticlePositionItem();
            articlePositionItem.setArticleNumber(a.getArtikelnummer());
            articlePositionItem.setCalculatedQuantityValue(a.getMenge());
            articlePositionItem.setWarehouseId(a.getLager());
            if (a.getLagerplatzId() > 0) {
                List<DocumentPositionStoreInformationUpdate> storeInformation = new ArrayList<>();
                DocumentPositionStoreInformationUpdate documentPositionStoreInformationUpdate = getDocumentPositionStoreInformationUpdate(a);
                storeInformation.add(documentPositionStoreInformationUpdate);
                articlePositionItem.setStoreInformation(storeInformation);
            }
            return articlePositionItem;
        }

        private @NonNull DocumentPositionStoreInformationUpdate getDocumentPositionStoreInformationUpdate(Artikel a) {
            DocumentPositionStoreInformationUpdate documentPositionStoreInformationUpdate = new DocumentPositionStoreInformationUpdate();
            documentPositionStoreInformationUpdate.setArticleNumber(a.getArtikelnummer());
            documentPositionStoreInformationUpdate.setQuantity(a.getMenge());
            documentPositionStoreInformationUpdate.setSerialNumber(a.getSeriennummer());
            documentPositionStoreInformationUpdate.setStoragePlaceIdentifier(a.getLagerplatzId());
            documentPositionStoreInformationUpdate.setWarehouse(a.getLager());
            return documentPositionStoreInformationUpdate;
        }
    }
}
