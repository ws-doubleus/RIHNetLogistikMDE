package at.rihnet.rihnetlogistikmde.ui.warenausgang;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.models.Beleg;
import at.rihnet.rihnetlogistikmde.models.BelegInfo;

public class WarenausgangViewModel extends ViewModel {
    private final MutableLiveData<String> mSearch;
    private final MutableLiveData<List<Beleg>> mBelegList;
    private final MutableLiveData<List<BelegInfo>> mBelegInfoList;
    private final MutableLiveData<BelegInfo> mBelegInfo;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public WarenausgangViewModel() {
        mSearch = new MutableLiveData<>();
        mBelegList = new MutableLiveData<>();
        mBelegInfoList = new MutableLiveData<>();
        mBelegInfo = new MutableLiveData<>();
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

    public LiveData<List<Beleg>> getBelege(){
        return mBelegList;
    }

    public void setBelege(List<Beleg> value){
        mBelegList.setValue(value);
    }

    public void removeBeleg(Beleg beleg){
        List<Beleg> belege = mBelegList.getValue();
        assert belege != null;
        belege.remove(beleg);
        mBelegList.setValue(belege);
    }

    public LiveData<List<BelegInfo>> getBelegInfos(){
        return mBelegInfoList;
    }

    public void setBelegInfos(List<BelegInfo> value){
        mBelegInfoList.setValue(value);
    }

    public void removeBelegInfo(BelegInfo belegInfo){
        List<BelegInfo> belegInfos = mBelegInfoList.getValue();
        assert belegInfos != null;
        belegInfos.remove(belegInfo);
        mBelegInfoList.setValue(belegInfos);
    }

    public LiveData<BelegInfo> getBelegInfo(){
        return mBelegInfo;
    }

    public void setBelegInfo(BelegInfo value){
        mBelegInfo.setValue(value);
    }
}
