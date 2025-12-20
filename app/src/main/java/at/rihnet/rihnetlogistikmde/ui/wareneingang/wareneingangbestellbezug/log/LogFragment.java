package at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.log;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.databinding.FragmentLogBinding;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.WareneingangBestellbezugViewModel;

public class LogFragment extends Fragment {

    private FragmentLogBinding binding;
    private LogRecyclerViewAdapter logRecyclerViewAdapter;
    private List<Log> logList = new ArrayList<>();

    // Room
    private LogDAO logDAO;

    // ViewModel
    private WareneingangBestellbezugViewModel wareneingangBestellbezugViewModel;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLogBinding.inflate(inflater, container, false);

        // View-Referenzen
        RecyclerView rv_log = binding.rvLog;
        TextView tv_empty = binding.tvEmpty;
        Button btn_delete = binding.btnDelete;

        // ViewModel holen
        wareneingangBestellbezugViewModel = new ViewModelProvider(requireActivity())
                .get(WareneingangBestellbezugViewModel.class);

        // Room initialisieren (Singleton)
        MyDatabase myDatabase = MyDatabase.getInstance(requireContext());
        logDAO = myDatabase.getLogDAO();

        // Adapter + RecyclerView
        logRecyclerViewAdapter = new LogRecyclerViewAdapter(new ArrayList<>());
        rv_log.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_log.setHasFixedSize(true);
        rv_log.setItemAnimator(new DefaultItemAnimator());
        rv_log.setAdapter(logRecyclerViewAdapter);

        // LiveData-Observer (Log-Liste aus dem ViewModel)
        wareneingangBestellbezugViewModel.getLog().observe(getViewLifecycleOwner(), l -> {
            logList = (l != null) ? l : new ArrayList<>();
            logRecyclerViewAdapter.setLogs(logList);
            logRecyclerViewAdapter.notifyDataSetChanged();

            if (!logList.isEmpty()) {
                binding.rvLog.smoothScrollToPosition(logList.size() - 1);
                rv_log.setVisibility(View.VISIBLE);
                btn_delete.setVisibility(View.VISIBLE);
                tv_empty.setVisibility(View.GONE);
            } else {
                rv_log.setVisibility(View.GONE);
                btn_delete.setVisibility(View.GONE);
                tv_empty.setVisibility(View.VISIBLE);
            }
        });

        // Button "Alle löschen" (Async)
        btn_delete.setOnClickListener(v -> {
            // UI sofort leeren
            if (logList == null) logList = new ArrayList<>();
            logList.clear();
            wareneingangBestellbezugViewModel.setLog(logList);

            // DB im Hintergrund bereinigen
            new AsyncTaskExecutorService<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void params) {
                    if (logDAO != null) {
                        logDAO.deleteLogByKategorie(Kategorie.WARENEINGANGBESTELLBEZUG);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    // Nichts zu tun
                }
            }.execute(null);
        });

        // Swipe-Funktion (Async)
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Geswipten Log-Eintrag ermitteln
                Log log = logRecyclerViewAdapter.getLogAt(viewHolder.getAdapterPosition());
                
                if (log != null) {
                    // UI Update sofort
                    logList.remove(log);
                    wareneingangBestellbezugViewModel.setLog(logList);

                    // Im Hintergrund löschen
                    new AsyncTaskExecutorService<Log, Void, Void>() {
                        @Override
                        protected Void doInBackground(Log params) {
                            if (logDAO != null) {
                                logDAO.delete(params);
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            // Nichts zu tun
                        }
                    }.execute(log);
                }
            }
        }).attachToRecyclerView(binding.rvLog);

        // Logs einmalig laden (asynchron)
        loadLogsFromDb();

        return binding.getRoot();
    }

    private void loadLogsFromDb() {
        // Nur laden, wenn ViewModel leer ist
        List<Log> current = wareneingangBestellbezugViewModel.getLog().getValue();
        if (current == null || current.isEmpty()) {
            new AsyncTaskExecutorService<Void, Void, List<Log>>() {
                @Override
                protected List<Log> doInBackground(Void params) {
                    if (logDAO != null) {
                        return logDAO.getLogByKategorie(Kategorie.WARENEINGANGBESTELLBEZUG);
                    }
                    return new ArrayList<>();
                }

                @Override
                protected void onPostExecute(List<Log> logs) {
                    if (isAdded() && logs != null && !logs.isEmpty()) {
                        List<Log> current = wareneingangBestellbezugViewModel.getLog().getValue();
                        if (current == null || current.isEmpty()) {
                            wareneingangBestellbezugViewModel.setLog(new ArrayList<>(logs));
                        }
                    }
                }
            }.execute(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
