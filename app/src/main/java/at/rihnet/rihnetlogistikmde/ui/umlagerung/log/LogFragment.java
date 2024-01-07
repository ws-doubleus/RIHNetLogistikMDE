package at.rihnet.rihnetlogistikmde.ui.umlagerung.log;

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

import at.rihnet.rihnetlogistikmde.databinding.FragmentLogBinding;
import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.models.LogKategorie;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.UmlagerungViewModel;

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

        RecyclerView rv_log = binding.rvLog;
        TextView tv_empty = binding.tvEmpty;
        Button btn_delete = binding.btnDelete;

        UmlagerungViewModel umlagerungViewModel = new ViewModelProvider(requireActivity()).get(UmlagerungViewModel.class);
        umlagerungViewModel.getLog().observe(getViewLifecycleOwner(), l -> {
            logList = l;
            logRecyclerViewAdapter.setLogs(l);
            logRecyclerViewAdapter.notifyDataSetChanged();
            if (logList.size() > 0) {
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


        btn_delete.setOnClickListener(v -> {
            logList.clear();
            logDAO.deleteLogByKategorie(LogKategorie.UMLAGERUNG);
            umlagerungViewModel.setLog(logList);
        });


        logRecyclerViewAdapter = new LogRecyclerViewAdapter(logList);
        rv_log.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_log.setHasFixedSize(true);
        rv_log.setItemAnimator(new DefaultItemAnimator());
        rv_log.setAdapter(logRecyclerViewAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Log log = logRecyclerViewAdapter.getLogAt(viewHolder.getAdapterPosition());
                logList.remove(log);
                logDAO.delete(log);
                umlagerungViewModel.setLog(logList);
                //logRecyclerViewAdapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(binding.rvLog);

        if (logRecyclerViewAdapter.getItemCount() > 0) {
            rv_log.setVisibility(View.VISIBLE);
            btn_delete.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        } else {
            rv_log.setVisibility(View.GONE);
            btn_delete.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        }

        MyDatabase myDatabase = Room.databaseBuilder(requireContext(), MyDatabase.class, "rihnetdatabase").allowMainThreadQueries().build();
        logDAO = myDatabase.getLogDAO();
        logList.addAll(logDAO.getLogByKategorie(LogKategorie.UMLAGERUNG));

        umlagerungViewModel.setLog(logList);

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}