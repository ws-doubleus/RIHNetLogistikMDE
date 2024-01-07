package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelabbuchen.lager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LageraViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public LageraViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Lager fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}