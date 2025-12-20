package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelinfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.models.Artikel;

public class ArtikelinfoViewModel extends ViewModel {
    //private static final String TAG = "RIHNet";
    private final MutableLiveData<String> mSearch;
    private final MutableLiveData<Artikel> mArtikel;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ArtikelinfoViewModel(){
        mSearch = new MutableLiveData<>();
        mArtikel = new MutableLiveData<>();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public LiveData<String> getSearch() {
        return mSearch;
    }

    public void setSearch(String search) {
        mSearch.setValue(search);
    }

    public LiveData<Artikel> getArtikel() {
        return mArtikel;
    }

    public void setArtikel(Artikel artikel) {
        mArtikel.setValue(artikel);
    }
}
