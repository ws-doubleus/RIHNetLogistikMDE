package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.log;

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
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.databinding.FragmentLogBinding;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.LogistikViewModel;


public class LogFragment extends Fragment {
    private FragmentLogBinding binding;
    private LogRecyclerViewAdapter logRecyclerViewAdapter;
    private List<Log> logList = new ArrayList<>();

    // Room
    private LogDAO logDAO;

    // ViewModel (enthält den Executor)
    private LogistikViewModel logistikViewModel;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLogBinding.inflate(inflater, container, false);

        // View-Referenzen
        RecyclerView rv_log = binding.rvLog;
        TextView tv_empty = binding.tvEmpty;
        Button btn_delete = binding.btnDelete;

        // ViewModel holen (wichtig für Executor und LiveData)
        logistikViewModel = new ViewModelProvider(requireActivity()).get(LogistikViewModel.class);

        // Room initialisieren (ohne allowMainThreadQueries!)
        initRoom();

        // Adapter + RecyclerView
        logRecyclerViewAdapter = new LogRecyclerViewAdapter(logList);
        rv_log.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_log.setHasFixedSize(true);
        rv_log.setItemAnimator(new DefaultItemAnimator());
        rv_log.setAdapter(logRecyclerViewAdapter);

        // LiveData-Observer (Log-Liste aus dem ViewModel)
        logistikViewModel.getLog().observe(getViewLifecycleOwner(), l -> {
            logList = l;
            logRecyclerViewAdapter.setLogs(l);
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

        // Button "Alle löschen"
        btn_delete.setOnClickListener(v -> {
            // DB-Löschvorgang
            logistikViewModel.getExecutorService().execute(() -> {
                logDAO.deleteLogByKategorie(Kategorie.WARENAUSGANG);

                requireActivity().runOnUiThread(() -> {
                    logList.clear();
                    logistikViewModel.setLog(logList);
                });
            });
        });

        // Swipe-Funktion (Löschen einzelner Logs)
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

                // Im Hintergrund löschen
                logistikViewModel.getExecutorService().execute(() -> {
                    logDAO.delete(log);

                    // Anschließend UI + ViewModel aktualisieren
                    requireActivity().runOnUiThread(() -> {
                        logList.remove(log);
                        logistikViewModel.setLog(logList);
                    });
                });
            }
        }).attachToRecyclerView(binding.rvLog);

        // Logs einmalig laden (asynchron), damit wir sie ins ViewModel setzen können
        loadLogsFromDb();

        return binding.getRoot();
    }

    /**
     * Baut die Room-Datenbank und holt das LogDAO (ohne allowMainThreadQueries()).
     */
    private void initRoom() {
//        MyDatabase myDatabase = Room.databaseBuilder(requireContext(), MyDatabase.class, "rihnetdatabase")
//                .fallbackToDestructiveMigration()
//                .build();
        MyDatabase myDatabase = MyDatabase.getInstance(requireContext());
        logDAO = myDatabase.getLogDAO();
    }

    /**
     * Lädt sämtliche Logs aus der DB und setzt sie im ViewModel.
     */
    private void loadLogsFromDb() {
       if( Objects.requireNonNull(logistikViewModel.getLog().getValue()).isEmpty()) {
           logistikViewModel.getExecutorService().execute(() -> {
               // DB-Lesevorgang
               List<Log> logs = logDAO.getLogByKategorie(Kategorie.WARENAUSGANG);

               // UI-Thread: ViewModel aktualisieren
               requireActivity().runOnUiThread(() -> {
                   logList.clear();
                   logList.addAll(logs);
                   logistikViewModel.setLog(logList);
               });
           });
       }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
