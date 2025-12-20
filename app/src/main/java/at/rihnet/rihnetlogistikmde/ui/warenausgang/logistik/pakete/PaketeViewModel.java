package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.pakete;

import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaketeViewModel extends ViewModel {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public PaketeViewModel(){

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }
}
