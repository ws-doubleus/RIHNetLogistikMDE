package at.rihnet.rihnetlogistikmde.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import at.rihnet.rihnetlogistikmde.databinding.ActivitySplashBinding;
import at.rihnet.rihnetlogistikmde.ui.main.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler().postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }, 2500);

        // Nach Ende der Animation (4 s) zur Haupt‑Activity wechseln
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        }, 4000);
    }
}