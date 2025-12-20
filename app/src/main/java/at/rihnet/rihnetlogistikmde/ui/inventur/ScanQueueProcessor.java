package at.rihnet.rihnetlogistikmde.ui.inventur;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import at.rihnet.rihnetlogistikmde.core.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.models.ScanJob;

public class ScanQueueProcessor {

    private static final String TAG = "RIHNet|ScanQueueProcessor";

    private final BlockingQueue<ScanJob> scanQueue;
    private final Thread workerThread;
    private final InventurErfassungViewModel viewModel;
    private volatile boolean running = true;

    public ScanQueueProcessor(InventurErfassungViewModel viewModel, int maxQueueSize) {
        this.viewModel = viewModel;
        this.scanQueue = new LinkedBlockingQueue<>(maxQueueSize);

        this.workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                processLoop();
            }
        }, "ScanQueueWorker");
        this.workerThread.start();
    }

    public void enqueueScan(ScanJob job) {
        try {
            // wirft nur Exception bei job == null
            scanQueue.add(job);
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Enqueue des ScanJobs: " + e.getMessage(), e);
        }
    }

    private void processLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                ScanJob job = scanQueue.take();

                new AsyncTaskExecutorService<ScanJob, Void, Void>() {
                    @Override
                    protected Void doInBackground(ScanJob... params) {
                        ScanJob scanJob = params[0];
//                        viewModel.processInventoryScan(
//                                scanJob.getBarcode(),
//                                scanJob.getBelegnummer(),
//                                scanJob.getStandort()
//                        );
                        return null;
                    }
                }.execute(job);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.i(TAG, "Worker-Thread unterbrochen, beende processLoop.");
            }
        }
    }

    public void shutdown() {
        running = false;
        workerThread.interrupt();
    }
}
