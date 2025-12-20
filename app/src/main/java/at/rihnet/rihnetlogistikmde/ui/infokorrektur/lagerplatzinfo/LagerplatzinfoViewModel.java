package at.rihnet.rihnetlogistikmde.ui.infokorrektur.lagerplatzinfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.models.Lagerplatzinfo;

public class LagerplatzinfoViewModel extends ViewModel {
    //private static final String TAG = "RIHNet";
    private final MutableLiveData<String> mSearch;
    private final MutableLiveData<List<Lagerplatzinfo>> mLagerplatzinfoList;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LagerplatzinfoViewModel() {
        mSearch = new MutableLiveData<>();
        mLagerplatzinfoList = new MutableLiveData<>();
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

    public LiveData<List<Lagerplatzinfo>> getLagerplatzinfo(){
        return mLagerplatzinfoList;
    }

    public void setLagerplatzinfo(List<Lagerplatzinfo> value){
        mLagerplatzinfoList.setValue(value);
    }
}
