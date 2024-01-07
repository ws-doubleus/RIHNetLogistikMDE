package at.rihnet.rihnetlogistikmde.ui.umlagerung.buchung;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BuchungViewModel extends ViewModel {
    private static final String TAG = "RIHNet";
    private final MutableLiveData<String> mSearch;

    public BuchungViewModel(){
        mSearch = new MutableLiveData<>();
    }

    public LiveData<String> getSearch(){
        return mSearch;
    }

    public void setSearch(String search){
        mSearch.setValue(search);
    }
}
