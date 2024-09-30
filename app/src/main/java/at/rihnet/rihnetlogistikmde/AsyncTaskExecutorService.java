package at.rihnet.rihnetlogistikmde;

import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AsyncTaskExecutorService<Params, Progress, Result> {
    private final ExecutorService executor;
    private Handler handler;

    protected AsyncTaskExecutorService() {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public Handler getHandler() {
        if (handler == null) {
            synchronized (AsyncTaskExecutorService.class) {
                handler = new Handler(Looper.getMainLooper());
            }
        }
        return handler;
    }

    protected void onPreExecute() {

    }

    protected abstract Result doInBackground(Params params) throws Exception;

    protected abstract void onPostExecute(Result result);

    protected void onProgressUpdate(@NotNull Progress value) {

    }

    public void publishProgress(@NotNull Progress value) {
        getHandler().post(() -> onProgressUpdate(value));
    }

    public void execute() {
        execute(null);
    }

    public void execute(Params params) {
        getHandler().post(() -> {
            onPreExecute();
            executor.execute(() -> {
                Result result;
                try {
                    result = doInBackground(params);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                Result finalResult = result;
                getHandler().post(() -> onPostExecute(finalResult));
            });
        });
    }

    public void shutDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public boolean isCancelled() {
        return executor == null || executor.isTerminated() || executor.isShutdown();
    }
}
