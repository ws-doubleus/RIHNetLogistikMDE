package at.rihnet.rihnetlogistikmde.ui.infokorrektur.lagerplatzinfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import at.rihnet.rihnetlogistikmde.models.Lagerplatzinfo;
import at.rihnet.rihnetlogistikmde.models.Log;

public class LagerplatzinfoViewModel extends ViewModel {
    private static final String TAG = "RIHNet";
    private final MutableLiveData<String> mSearch;
    private final MutableLiveData<List<Lagerplatzinfo>> mLagerplatzinfoList;

    public LagerplatzinfoViewModel() {
        mSearch = new MutableLiveData<>();
        mLagerplatzinfoList = new MutableLiveData<>();
    }

    public LiveData<String> getSearch() {
        return mSearch;
    }

    public void setSearch(String search) {
        mSearch.setValue(search);
    }

    public LiveData<List<Lagerplatzinfo>> getLagerplatzinfo(){
        return mLagerplatzinfoList;
    }

    public void setLagerplatzinfo(List<Lagerplatzinfo> vakue){
        mLagerplatzinfoList.setValue(vakue);
    }
}
