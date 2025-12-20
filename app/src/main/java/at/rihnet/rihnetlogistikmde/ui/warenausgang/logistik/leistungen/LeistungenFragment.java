package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.leistungen;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.databinding.FragmentWarenausgangLeistungenBinding;
import at.rihnet.rihnetlogistikmde.models.BelegInfo;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.LogistikViewModel;
import at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.gebucht.GebuchtRecyclerViewAdapter;

public class LeistungenFragment extends Fragment {
    private final String TAG = "RIHNet";

    private FragmentWarenausgangLeistungenBinding binding;
    private LogistikViewModel logistikViewModel;
    private LoadingDialogFragment loadingDialogFragment;
    private RecyclerView rv_leistungen;
    private LeistungenRecyclerViewAdapter leistungenRecyclerViewAdapter;
    private List<Belegposition> leistungenList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWarenausgangLeistungenBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Loading-Dialog
        loadingDialogFragment = LoadingDialogFragment.newInstance("Daten werden geladen...");
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");

        initViewModel();
        initRecyclerView();
        loadData();


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initViewModel() {
        logistikViewModel = new ViewModelProvider(requireActivity()).get(LogistikViewModel.class);

        logistikViewModel.getLeistungen().observe(getViewLifecycleOwner(), new Observer<List<Belegposition>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(List<Belegposition> belegpositions) {
                leistungenList = belegpositions;
                leistungenRecyclerViewAdapter.setBelegpositionen(leistungenList);
                leistungenRecyclerViewAdapter.notifyDataSetChanged();
                updateEmptyView();
            }
        });
    }

    private void initRecyclerView() {
        rv_leistungen = binding.rvLeistungen;
        leistungenRecyclerViewAdapter = new LeistungenRecyclerViewAdapter(leistungenList);
        rv_leistungen.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_leistungen.setHasFixedSize(true);
        rv_leistungen.setItemAnimator(new DefaultItemAnimator());
        rv_leistungen.setAdapter(leistungenRecyclerViewAdapter);
        updateEmptyView();

    }

    private void updateEmptyView() {
        TextView tv_empty = binding.tvEmpty;
        if (!leistungenList.isEmpty()) {
            rv_leistungen.smoothScrollToPosition(leistungenList.size() - 1);
            rv_leistungen.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
        } else {
            rv_leistungen.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        logistikViewModel.getExecutorService().execute(() -> {
            try {
                if (Objects.requireNonNull(logistikViewModel.getLeistungen().getValue()).isEmpty()) {
                    BelegInfo belegInfo = logistikViewModel.getBelegInfo().getValue();
                    leistungenList.clear();
                    assert belegInfo != null;
                    leistungenList = CommunicationSql.getBelegpByZeilentypLagerartikel(belegInfo.getBelegtyp(), belegInfo.getBelegnummer());
                    requireActivity().runOnUiThread(() -> {
                        logistikViewModel.setLeistungen(leistungenList);
                    });
                }
                if (loadingDialogFragment != null) {
                    loadingDialogFragment.dismiss();
                }
            } catch (Exception e) {
                Log.e(TAG, "loadData(): " + e.getMessage());
            }
        });
    }
}