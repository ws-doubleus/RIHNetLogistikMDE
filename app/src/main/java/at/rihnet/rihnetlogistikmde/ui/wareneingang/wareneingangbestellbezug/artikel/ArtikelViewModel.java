package at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.artikel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.models.Artikel;

public class ArtikelViewModel extends ViewModel {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Artikel> mArtikel;
    private final MutableLiveData<String> mSearch;

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public ArtikelViewModel(){
        mArtikel = new MutableLiveData<>();
        mSearch = new MutableLiveData<>();
    }

    public LiveData<Artikel> getArtikel() {
        return mArtikel;
    }

    public void setArtikel(Artikel artikel) {
        mArtikel.setValue(artikel);
    }

    public LiveData<String> getSearch() {
        return mSearch;
    }

    public void setSearch(String search) {
        mSearch.setValue(search);
    }
}
