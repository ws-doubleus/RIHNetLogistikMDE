package at.rihnet.rihnetlogistikmde.ui.inventur;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.models.SelectLine.Inventory;

public class InventurErfassungViewModel extends ViewModel {
    private final MutableLiveData<Inventory> mInventory;
    private final MutableLiveData<List<Log>> mLogList;
    private final MutableLiveData<String> mSearchArtikel;

    public InventurErfassungViewModel() {
        mInventory =  new MutableLiveData<>();
        mLogList = new MutableLiveData<>();
        mLogList.setValue(new ArrayList<>());
        mSearchArtikel = new MutableLiveData<>();
    }

    public LiveData<Inventory> getInventory() {
        return mInventory;
    }

    public void setInventory(Inventory inventory) {
        mInventory.setValue(inventory);
    }

    public LiveData<List<Log>> getLog() {
        return mLogList;
    }

    public void setLog(List<Log> value) {
        mLogList.setValue(value);
    }

    public void addLog(Log log) {
        List<Log> logs = mLogList.getValue();
        assert logs != null;
        logs.add(log);
        mLogList.setValue(logs);
    }

    public LiveData<String> getSearchArtikel() {
        return mSearchArtikel;
    }

    public void setSearchArtikel(String search) {
        mSearchArtikel.setValue(search);
    }
}
