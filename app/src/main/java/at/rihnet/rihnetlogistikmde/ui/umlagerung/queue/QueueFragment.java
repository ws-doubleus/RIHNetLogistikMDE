package at.rihnet.rihnetlogistikmde.ui.umlagerung.queue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import java.io.IOError;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentQueueBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.sqlite.QueueDAO;
import at.rihnet.rihnetlogistikmde.ui.main.MainActivity;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.UmlagerungViewModel;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.buchung.BuchungActivity;

public class QueueFragment extends Fragment implements MenuProvider {
    private final String TAG = "RIHNet";
    private FragmentQueueBinding binding;
    private UmlagerungViewModel umlagerungViewModel;
    private QueueRecyclerViewAdapter queueRecyclerViewAdapter;
    private List<Artikel> queueList = new ArrayList<>();
    private QueueDAO queueDAO;
    private LogDAO logDAO;
    private OnSearchQueue searchQueue;

    public interface OnSearchQueue {
        void onSearchQueue(MenuItem menuItem, SearchView searchView);
    }

    @SuppressLint("NotifyDataSetChanged")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQueueBinding.inflate(inflater, container, false);

        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == 1 && result.getData() != null) {
                        Bundle bundle = result.getData().getExtras();
                        assert bundle != null;
                        int menge = bundle.getInt("menge");
                        int position = bundle.getInt("position");
                        Artikel a = queueRecyclerViewAdapter.getQueueAt(position);
                        if (menge > 0) {
                            queueList.remove(a);
                            //new Thread(() -> {
                                queueDAO.delete(a);
                            //}).start();
                            umlagerungViewModel.setQueue(queueList);
                            if (a.getMenge() > menge) {
                                a.setMenge(a.getMenge() - menge);
                                queueList.add(a);
                                umlagerungViewModel.setQueue(queueList);
                            }
                            Log log = new at.rihnet.rihnetlogistikmde.models.Log("Artikelnummer: " + a.getArtikelnummer() + "\nMenge: " + menge + "\nUmlagerung erfolgreich!", ContextCompat.getColor(requireContext(), R.color.green_500), Kategorie.UMLAGERUNG);
                            umlagerungViewModel.addLog(log);
                            //new Thread(() -> {
                                logDAO.insert(log);
                            //}).start();
                        } else {
                            Log log = new at.rihnet.rihnetlogistikmde.models.Log("Artikelnummer: " + a.getArtikelnummer() + "\nMenge: " + menge + "\nUmlagerung fehlerhaft!", ContextCompat.getColor(requireContext(), R.color.red_500), Kategorie.UMLAGERUNG);
                            umlagerungViewModel.addLog(log);
                            //new Thread(() -> {
                                logDAO.insert(log);
                            //}).start();
                        }
                    } //else {
                    //TODO WS nicht nötig
                    //Log log = new at.rihnet.rihnetlogistikmde.models.Log("Artikelnummer: ---\nMenge: 0\nUmlagerung fehlerhaft!", ContextCompat.getColor(requireContext(), R.color.red_500), LogKategorie.UMLAGERUNG);
                    //umlagerungViewModel.addLog(log);
                    //logDAO.insert(log);
                    //}
                }
        );

        RecyclerView rv_queue = binding.rvQueue;
        TextView tv_empty = binding.tvEmpty;

        umlagerungViewModel = new ViewModelProvider(requireActivity()).get(UmlagerungViewModel.class);
        umlagerungViewModel.getQueue().observe(getViewLifecycleOwner(), q -> {
            queueList = q;
            queueRecyclerViewAdapter.setQueue(q);
            queueRecyclerViewAdapter.notifyDataSetChanged();
            if (!queueList.isEmpty()) {
                binding.rvQueue.smoothScrollToPosition(queueList.size() - 1);
                rv_queue.setVisibility(View.VISIBLE);
                tv_empty.setVisibility(View.GONE);
            } else {
                rv_queue.setVisibility(View.GONE);
                tv_empty.setVisibility(View.VISIBLE);
            }
        });

        queueRecyclerViewAdapter = new QueueRecyclerViewAdapter(queueList);
        rv_queue.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_queue.setHasFixedSize(true);
        rv_queue.setItemAnimator(new DefaultItemAnimator());
        rv_queue.setAdapter(queueRecyclerViewAdapter);
        queueRecyclerViewAdapter.setOnItemClickListener(position -> {
            Artikel artikel = queueRecyclerViewAdapter.getQueueAt(position);
            Bundle bundle = new Bundle();
            bundle.putSerializable("artikel", artikel);
            bundle.putInt("position", position);
            Intent intent = new Intent(getActivity(), BuchungActivity.class);
            intent.putExtras(bundle);
            someActivityResultLauncher.launch(intent);
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Artikel artikel = queueRecyclerViewAdapter.getQueueAt(viewHolder.getAdapterPosition());
                umlagerungViewModel.removeQueue(artikel);
                //new Thread(() -> {
                    queueDAO.delete(artikel);
                //}).start();
            }
        }).attachToRecyclerView(binding.rvQueue);

        umlagerungViewModel.getSearchQueue().observe(getViewLifecycleOwner(), this::doSearch);

        if (queueRecyclerViewAdapter.getItemCount() > 0) {
            rv_queue.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        } else {
            rv_queue.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        }

        MyDatabase myDatabase = Room.databaseBuilder(requireContext(), MyDatabase.class, "rihnetdatabase").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        //new Thread(() -> {
            queueDAO = myDatabase.getQueueDAO();
            logDAO = myDatabase.getLogDAO();
            queueList.addAll(queueDAO.getArtikelByKategorie(Kategorie.UMLAGERUNG));
            //new Handler(Looper.getMainLooper()).post(() -> {
                umlagerungViewModel.setQueue(queueList);
               android.util.Log.e(TAG, "umlagerungViewModel.getQueue(): " + Objects.requireNonNull(umlagerungViewModel.getQueue().getValue()).size());
            //});
        //}).start();

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
        menuInflater.inflate(R.menu.umlagerung_menu, menu);
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
                umlagerungViewModel.setSearchQueue(newText);
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
            //CommunicationCommon. hideKeyboard(requireActivity());
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
}