package at.rihnet.rihnetlogistikmde.ui.wareneingang.freierwareneingang.log;

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

import at.rihnet.rihnetlogistikmde.core.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.databinding.FragmentLogBinding;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.freierwareneingang.FreierWareneingangViewModel;

public class LogFragment extends Fragment {
    //private final String TAG = "RIHNet";
    private FragmentLogBinding binding;
    private LogRecyclerViewAdapter logRecyclerViewAdapter;
    private List<Log> logList = new ArrayList<>();
    private LogDAO logDAO;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLogBinding.inflate(inflater, container, false);

        // 1. Datenbank über Singleton holen (Best Practice)
        // allowMainThreadQueries ist hier nicht mehr nötig, da wir Async nutzen!
        MyDatabase myDatabase = MyDatabase.getInstance(requireContext());
        logDAO = myDatabase.getLogDAO();

        // 2. Adapter initialisieren
        logRecyclerViewAdapter = new LogRecyclerViewAdapter(new ArrayList<>());

        RecyclerView rv_log = binding.rvLog;
        TextView tv_empty = binding.tvEmpty;
        Button btn_delete = binding.btnDelete;

        rv_log.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_log.setHasFixedSize(true);
        rv_log.setItemAnimator(new DefaultItemAnimator());
        rv_log.setAdapter(logRecyclerViewAdapter);

        // 3. ViewModel und Observer einrichten
        FreierWareneingangViewModel freierWareneingangViewModel = new ViewModelProvider(requireActivity()).get(FreierWareneingangViewModel.class);

        freierWareneingangViewModel.getLog().observe(getViewLifecycleOwner(), l -> {
            logList = (l != null) ? l : new ArrayList<>();

            if (logRecyclerViewAdapter != null) {
                logRecyclerViewAdapter.setLogs(logList);
                logRecyclerViewAdapter.notifyDataSetChanged();
            }

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

        // 4. Button Delete (Alles löschen) - ASYNC
        btn_delete.setOnClickListener(v -> {
            if (logList == null) logList = new ArrayList<>();
            logList.clear();
            // UI sofort leeren
            freierWareneingangViewModel.setLog(logList);

            // DB im Hintergrund bereinigen
            new AsyncTaskExecutorService<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    if (logDAO != null) {
                        logDAO.deleteLogByKategorie(Kategorie.FREIERWARENEINGANG);
                    }
                    return null;
                }
            }.execute();
        });

        // 5. Swipe Delete (Einzeln löschen) - ASYNC
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (logRecyclerViewAdapter != null) {
                    Log log = logRecyclerViewAdapter.getLogAt(viewHolder.getAdapterPosition());
                    if (log != null && logList != null) {
                        logList.remove(log);
                        // UI sofort aktualisieren
                        freierWareneingangViewModel.setLog(logList);

                        // DB Eintrag im Hintergrund löschen
                        new AsyncTaskExecutorService<Log, Void, Void>() {
                            @Override
                            protected Void doInBackground(Log... logs) {
                                if (logDAO != null) {
                                    logDAO.delete(logs[0]);
                                }
                                return null;
                            }
                        }.execute(log);
                    }
                }
            }
        }).attachToRecyclerView(binding.rvLog);

        // 6. Initiales Laden aus DB - ASYNC
        // Nur laden, wenn ViewModel noch leer ist
        List<Log> currentLogs = freierWareneingangViewModel.getLog().getValue();
        if (currentLogs == null || currentLogs.isEmpty()) {
            new AsyncTaskExecutorService<Void, Void, List<Log>>() {
                @Override
                protected List<Log> doInBackground(Void... voids) {
                    if (logDAO != null) {
                        return logDAO.getLogByKategorie(Kategorie.FREIERWARENEINGANG);
                    }
                    return new ArrayList<>();
                }

                @Override
                protected void onPostExecute(List<Log> dbLogs) {
                    // Prüfen ob Fragment noch aktiv ist, um Abstürze zu vermeiden
                    if (isAdded() && dbLogs != null && !dbLogs.isEmpty()) {
                        // Erneuter Check, um Überschreiben zu verhindern, falls User schnell war
                        List<Log> current = freierWareneingangViewModel.getLog().getValue();
                        if (current == null || current.isEmpty()) {
                            freierWareneingangViewModel.setLog(new ArrayList<>(dbLogs));
                        }
                    }
                }
            }.execute();
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
