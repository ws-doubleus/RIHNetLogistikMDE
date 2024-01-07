package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelzubuchen.lager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LagerzViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public LagerzViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Lager fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}