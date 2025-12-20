package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelzubuchen.artikel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Kategorie;

public class ArtikelViewModel extends ViewModel {
    private final MutableLiveData<Artikel> mArtikel;

    public ArtikelViewModel() {
        mArtikel = new MutableLiveData<>();
    }

    public LiveData<Artikel> getArtikel() {
        return mArtikel;
    }

    public void resetArtikel() {
        mArtikel.setValue(new Artikel("", "", "", "", "", "", 1, 1, "", "", "","", 0, "", Kategorie.ARTIKELZUBUCHEN));
    }

    public void setArtikel(Artikel artikel) {
        mArtikel.setValue(artikel);
    }
}
