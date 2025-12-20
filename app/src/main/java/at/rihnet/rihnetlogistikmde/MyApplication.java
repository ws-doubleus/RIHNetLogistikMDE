package at.rihnet.rihnetlogistikmde;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.preference.PreferenceManager;

import at.rihnet.rihnetlogistikmde.audio.SoundPoolManager;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;

// Implement the listener interface
public class MyApplication extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override public void onCreate() {
        super.onCreate();

        // Register the listener
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Set initial theme
        setDarkMode(prefs);

        try {
            SoundPoolManager.getInstance(this);

            String ipadresse = prefs.getString("ipadresse", "");
            String port = prefs.getString("port", "");
            String datenbank = prefs.getString("datenbank", "");
            String instance = prefs.getString("instance", "");
            String benutzername = prefs.getString("benutzername", "");
            String kennwort = prefs.getString("kennwort", "");

            SqlServerData sqlServerData = new SqlServerData(ipadresse, port, datenbank, instance, benutzername, kennwort);

            Db.open(sqlServerData);   // einmal öffnen
//            new Thread(() -> {
//                try {
//                    Db.open(sqlServerData);               // baut die Connection auf
//                    Log.i("RIHNet", "DB ready");
//                } catch (Exception e) {
//                    Log.e("RIHNet", "DB‑Init‑Fehler: " + e.getMessage());
//                }
//            }).start();
        } catch (Exception e) {
            Log.e("RIHNet", "DB‑Startfehler: " + e.getMessage());
        }
    }

    @Override public void onTerminate() {
        super.onTerminate();
        SoundPoolManager.getInstance(this).release();
        Db.close();     // sauber schließen
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    // This method is called when a preference changes
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("dark_mode".equals(key)) {
            setDarkMode(sharedPreferences);
        }
    }

    private void setDarkMode(SharedPreferences sharedPreferences) {
        boolean useDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        if (useDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
