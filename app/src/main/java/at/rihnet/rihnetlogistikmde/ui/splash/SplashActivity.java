package at.rihnet.rihnetlogistikmde.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

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
            //startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }, 2500);
    }




}