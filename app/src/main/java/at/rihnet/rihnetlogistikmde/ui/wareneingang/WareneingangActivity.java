package at.rihnet.rihnetlogistikmde.ui.wareneingang;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Objects;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityWareneingangBestellbezugBinding;
import at.rihnet.rihnetlogistikmde.databinding.ActivityWareneingangBinding;
import at.rihnet.rihnetlogistikmde.ui.main.MainActivity;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.freierwareneingang.FreierWareneingangActivity;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.WareneingangBestellbezugActivity;

public class WareneingangActivity extends AppCompatActivity {
    // UI
    private MaterialButton btn_freierwareneingang;
    private MaterialButton btn_wareneingangbestellbezug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWareneingangBinding binding = ActivityWareneingangBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar(binding);
        initView(binding);
        initListeners();
    }

    private void initToolbar(ActivityWareneingangBinding binding) {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_exit_to_app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initView(ActivityWareneingangBinding binding){
        btn_freierwareneingang = binding.btnFreierwareneingang;
        btn_wareneingangbestellbezug = binding.btnWareneingangbestellbezug;
    }

    private void initListeners() {
        btn_freierwareneingang.setOnClickListener(view -> startActivity(new Intent(WareneingangActivity.this, FreierWareneingangActivity.class)));
        btn_wareneingangbestellbezug.setOnClickListener(view -> startActivity(new Intent(WareneingangActivity.this, WareneingangBestellbezugActivity.class)));
    }
}