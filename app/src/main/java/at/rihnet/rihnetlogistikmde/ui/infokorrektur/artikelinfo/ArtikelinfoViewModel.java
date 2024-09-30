package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelinfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import at.rihnet.rihnetlogistikmde.models.Artikel;

public class ArtikelinfoViewModel extends ViewModel {
    //private static final String TAG = "RIHNet";
    private final MutableLiveData<String> mSearch;
    private final MutableLiveData<Artikel> mArtikel;

    public ArtikelinfoViewModel(){
        mSearch = new MutableLiveData<>();
        mArtikel = new MutableLiveData<>();
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
