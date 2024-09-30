package at.rihnet.rihnetlogistikmde.ui.umlagerung.artikel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Kategorie;

public class ArtikelViewModel extends ViewModel {
    //private static final String TAG = "RIHNet";
    private final MutableLiveData<Artikel> mArtikel;

    public ArtikelViewModel() {
        mArtikel = new MutableLiveData<>();
    }

    public LiveData<Artikel> getArtikel() {
        return mArtikel;
    }

    public void resetArtikel() {
        mArtikel.setValue(new Artikel("", "", "", "", "", "", 1, 1, "", "", "","", 0, "", Kategorie.UMLAGERUNG));
    }

    public void setArtikel(Artikel artikel) {
        mArtikel.setValue(artikel);
    }
}