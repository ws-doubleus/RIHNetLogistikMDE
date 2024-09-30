package at.rihnet.rihnetlogistikmde.ui.infokorrektur;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

import java.util.Objects;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityInfoKorrekturBinding;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelabbuchen.ArtikelAbbuchenActivity;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelinfo.ArtikelinfoActivity;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelzubuchen.ArtikelZubuchenActivity;
import at.rihnet.rihnetlogistikmde.ui.infokorrektur.lagerplatzinfo.LagerplatzinfoActivity;

public class InfoKorrekturActivity extends AppCompatActivity {
    //private final String TAG = "RIHNet";

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        ActivityInfoKorrekturBinding binding = ActivityInfoKorrekturBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setLogo(R.drawable.ic_edit_note);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialButton btn_artikelinfo = binding.btnArtikelinfo;
        MaterialButton btn_lagerplatzinfo = binding.btnLagerplatzinfo;
        MaterialButton btn_artikelzubuchen = binding.btnArtikelzubuchen;
        MaterialButton btn_artikelabbuchen = binding.btnArtikelabbuchen;

        btn_artikelinfo.setOnClickListener(view -> startActivity(new Intent(InfoKorrekturActivity.this, ArtikelinfoActivity.class)));

        btn_lagerplatzinfo.setOnClickListener(view -> startActivity(new Intent(InfoKorrekturActivity.this, LagerplatzinfoActivity.class)));

        btn_artikelzubuchen.setOnClickListener(view -> startActivity(new Intent(InfoKorrekturActivity.this, ArtikelZubuchenActivity.class)));

        btn_artikelabbuchen.setOnClickListener(view -> startActivity(new Intent(InfoKorrekturActivity.this, ArtikelAbbuchenActivity.class)));
    }
}