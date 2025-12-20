package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.pakete;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityPaketeBinding;
import at.rihnet.rihnetlogistikmde.models.Paket;

public class PaketeActivity extends AppCompatActivity {
    private final String TAG = "RIHNet";
    private ActivityPaketeBinding binding;
    private PaketeViewModel paketeViewModel;
    private PaketeRecyclerViewAdapter paketeRecyclerViewAdapter;
    private List<Paket> paketeList = new ArrayList<>();
    private RecyclerView rv_pakete;
    private FloatingActionButton fab_paket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPaketeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        paketeList = (List<Paket>) getIntent().getSerializableExtra("EXTRA_PAKETE");
        assert paketeList != null;


        initToolbar();
        initUIReferences();
        initViewModel();
        initRecyclerView();
        initListener();

    }

    @Override
    public void onBackPressed() {
        //Intent result = new Intent().putStringArrayListExtra(EXTRA_PHOTO_PATHS, capturedPaths);
        setResult(2);
        super.onBackPressed();
    }

    private void initToolbar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_package_2_white);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            setResult(2);
            finish();
        });
    }

    private void initUIReferences(){
        rv_pakete = binding.rvPakete;
        fab_paket = binding.fabPaket;
    }

    private void initViewModel() {
        paketeViewModel = new ViewModelProvider(this).get(PaketeViewModel.class);
    }

    private void initRecyclerView() {
        //paketeRecyclerViewAdapter = new PaketeRecyclerViewAdapter(List.of(p1, p2, p3));
        paketeRecyclerViewAdapter = new PaketeRecyclerViewAdapter(paketeList);
        rv_pakete.setLayoutManager(new LinearLayoutManager(this));
        rv_pakete.setHasFixedSize(true);
        rv_pakete.setItemAnimator(new DefaultItemAnimator());
        rv_pakete.setAdapter(paketeRecyclerViewAdapter);

        ItemTouchHelper pkgSwipeHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.START | ItemTouchHelper.END) {

                    @Override
                    public int getSwipeDirs(@NonNull RecyclerView rv,
                                            @NonNull RecyclerView.ViewHolder vh) {
                        // 1. Zugehöriges Paket holen
                        Paket p = paketeList.get(vh.getAdapterPosition());

                        // 2. Wenn Karte geöffnet → Swipe deaktivieren
                        return p.isExpanded()
                                ? 0
                                : super.getSwipeDirs(rv, vh);   // sonst Standard‑Flags
                    }

                    @Override public boolean onMove(@NonNull RecyclerView r,
                                                    @NonNull RecyclerView.ViewHolder v1,
                                                    @NonNull RecyclerView.ViewHolder v2) { return false; }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {

                        int idx = vh.getAdapterPosition();
                        Paket deleted = paketeRecyclerViewAdapter.removePaketAndReturn(idx);

                        Snackbar.make(rv_pakete, "Paket gelöscht!", Snackbar.LENGTH_LONG)
                                .setAction("WIEDERHERSTELLEN", v -> {
                                    paketeRecyclerViewAdapter.restorePaketAt(idx, deleted);
                                    rv_pakete.scrollToPosition(idx);
                                }).show();
                    }
                });
        pkgSwipeHelper.attachToRecyclerView(rv_pakete);
        //updateEmptyView();
    }

    private void initListener(){
        fab_paket.setOnClickListener(v -> {
            paketeList.add(new Paket(paketeList.size() + 1, "", 0, 0));
            paketeRecyclerViewAdapter.notifyItemInserted(paketeList.size() - 1);
        });
    }
}