package at.rihnet.rihnetlogistikmde.core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Zentrale Ersatzklasse für AsyncTask.
 *
 * - Verwendet EINEN statischen ExecutorService (SingleThread) für alle Hintergrundjobs.
 * - doInBackground(..) läuft im Hintergrund-Thread.
 * - onPostExecute(..) läuft sicher im Main-Thread.
 *
 * Verwendung:
 *
 * new AsyncTaskExecutorService<ParamType, Void, ResultType>() {
 *     @Override
 *     protected ResultType doInBackground(ParamType... params) {
 *         // lange Operation (SQL, Netzwerk etc.)
 *     }
 *
 *     @Override
 *     protected void onPostExecute(ResultType result) {
 *         // UI aktualisieren
 *     }
 * }.execute(param);
 */


public abstract class AsyncTaskExecutorService<Params, Progress, Result> {

    // EIN globaler Executor-Thread für alle DB/Netzwerk-Operationen.
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactory() {
        private int counter = 0;

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "AsyncTaskExecutorService-" + (++counter));
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    });

    // Handler für den Main-Thread (UI-Thread)
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    protected AsyncTaskExecutorService() {
    }

    /**
     * Wird im Hintergrund-Thread ausgeführt.
     */
    protected abstract Result doInBackground(Params... params);

    /**
     * Wird im UI-Thread ausgeführt, nachdem doInBackground fertig ist.
     */
    protected void onPostExecute(Result result) {
        // optional
    }

    /**
     * Kann verwendet werden, um Fortschritt im UI anzuzeigen.
     */
    protected void onProgressUpdate(Progress... values) {
        // optional
    }

    @SafeVarargs
    public final void execute(final Params... params) {
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                final Result result = doInBackground(params);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute(result);
                    }
                });
            }
        });
    }

    /**
     * Optionale Methode, falls du den Executor beim Beenden der App schließen möchtest.
     * In der Regel NICHT nötig, da der Prozess beim Schließen der App beendet wird.
     */
    public static void shutdownNow() {
        EXECUTOR.shutdownNow();
    }
}
