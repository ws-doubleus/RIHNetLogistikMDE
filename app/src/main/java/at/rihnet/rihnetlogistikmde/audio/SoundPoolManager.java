package at.rihnet.rihnetlogistikmde.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import at.rihnet.rihnetlogistikmde.R;

public final class SoundPoolManager {

    private static volatile SoundPoolManager INSTANCE;

    private final SoundPool soundPool;
    private final int errorSoundId;
    private volatile boolean errorLoaded = false;

    /** private, damit nur getInstance(...) das Objekt erzeugen kann */
    private SoundPoolManager(Context ctx) {
//        AudioAttributes attrs = new AudioAttributes.Builder()
//                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
//                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                .build();

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(attrs)
                .build();

        errorSoundId = soundPool.load(ctx, R.raw.error, 1);

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0 && sampleId == errorSoundId) {
                errorLoaded = true;
            }
        });
    }

    /** Thread‑safe lazy initialisation */
    public static SoundPoolManager getInstance(Context ctx) {
        if (INSTANCE == null) {
            synchronized (SoundPoolManager.class) {
                if (INSTANCE == null) {        // double‑checked locking
                    INSTANCE = new SoundPoolManager(ctx.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // ------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------
    public void playError() {
        if (errorLoaded) {
            int streamId = soundPool.play(errorSoundId, 1f, 1f, 1 /*priority*/, 0 /*loop*/, 1f /*rate*/);
            if (streamId == -1) {
                Log.w("RIHNet", "Sound konnte nicht abgespielt werden");
            }
        }
    }

    /** Optional von Application.onTerminate() oder bei globalem Logout aufrufen */
    public void release() {
        soundPool.release();
        INSTANCE = null;
    }
}

