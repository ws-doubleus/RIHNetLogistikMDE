package at.rihnet.rihnetlogistikmde.ui.paketlabel.log;

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
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.databinding.FragmentLogBinding;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.ui.paketlabel.PaketlabelViewModel;


public class LogFragment extends Fragment {
    private FragmentLogBinding binding;
    private LogRecyclerViewAdapter logRecyclerViewAdapter;
    private List<Log> logList = new ArrayList<>();

    // Room
    private LogDAO logDAO;

    // ViewModel (enthält den Executor)
    private PaketlabelViewModel paketlabelViewModel;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLogBinding.inflate(inflater, container, false);

        // View-Referenzen
        RecyclerView rv_log = binding.rvLog;
        TextView tv_empty = binding.tvEmpty;
        Button btn_delete = binding.btnDelete;

        // ViewModel holen
        paketlabelViewModel = new ViewModelProvider(requireActivity()).get(PaketlabelViewModel.class);

        // Room initialisieren
        initRoom();

        // Adapter + RecyclerView
        logRecyclerViewAdapter = new LogRecyclerViewAdapter(logList);
        rv_log.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_log.setHasFixedSize(true);
        rv_log.setItemAnimator(new DefaultItemAnimator());
        rv_log.setAdapter(logRecyclerViewAdapter);

        // LiveData-Observer (Log-Liste aus dem ViewModel)
        paketlabelViewModel.getLog().observe(getViewLifecycleOwner(), l -> {
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
            paketlabelViewModel.getExecutorService().execute(() -> {
                // KORREKTUR 1: Hier stand vorher WARENAUSGANG -> muss PAKETLABEL sein!
                logDAO.deleteLogByKategorie(Kategorie.PAKETLABEL);

                // KORREKTUR 2: Prüfen ob Fragment noch da ist, bevor wir die UI anfassen
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        logList.clear();
                        paketlabelViewModel.setLog(logList);
                    });
                }
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
                int position = viewHolder.getAdapterPosition();

                // Sicherheitscheck: Position gültig?
                if (position == RecyclerView.NO_POSITION) return;

                // Geswipten Log-Eintrag ermitteln
                Log log = logRecyclerViewAdapter.getLogAt(position);

                // Im Hintergrund löschen
                paketlabelViewModel.getExecutorService().execute(() -> {
                    logDAO.delete(log);

                    // KORREKTUR: Sicherheitscheck statt requireActivity()
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            logList.remove(log);
                            paketlabelViewModel.setLog(logList);
                        });
                    }
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
        MyDatabase myDatabase = MyDatabase.getInstance(requireContext());
        logDAO = myDatabase.getLogDAO();
    }


    private void loadLogsFromDb() {
        // Hole den aktuellen Wert sicher (verhindert NullPointerException beim Start)
        List<Log> currentLogs = paketlabelViewModel.getLog().getValue();

        // Lade neu, wenn die Liste NULL ist ODER wenn sie leer ist
        if (currentLogs == null || currentLogs.isEmpty()) {
            paketlabelViewModel.getExecutorService().execute(() -> {

                // DB-Lesevorgang (Richtige Kategorie!)
                List<Log> logs = logDAO.getLogByKategorie(Kategorie.PAKETLABEL);

                // KORREKTUR: Prüfen, ob Fragment noch "attached" ist
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Logik: Wir setzen die neuen Daten direkt ins ViewModel.
                        // Der Observer in onCreateView kümmert sich um das UI-Update.
                        if (logList != null) {
                            logList.clear();
                            if (logs != null) {
                                logList.addAll(logs);
                            }
                            paketlabelViewModel.setLog(logList);
                        }
                    });
                }
            });
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
