package at.rihnet.rihnetlogistikmde.ui.umlagerung;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.models.SeriennummerCharge;

public class UmlagerungViewModel extends ViewModel {
    private static final String TAG = "RIHNet";
    private final MutableLiveData<Artikel> mArtikel;
    private final MutableLiveData<String> mSearchArtikel;
    private final MutableLiveData<String> mSearchLager;
    private final MutableLiveData<String> mSearchQueue;
    private final MutableLiveData<Boolean> mResetArtikel;
    private final MutableLiveData<List<Log>> mLogList;
    private final MutableLiveData<List<Artikel>> mQueueList;

    public UmlagerungViewModel() {
        mArtikel = new MutableLiveData<>();
        mSearchArtikel = new MutableLiveData<>();
        mSearchLager = new MutableLiveData<>();
        mSearchQueue = new MutableLiveData<>();
        mResetArtikel = new MutableLiveData<>();
        mLogList = new MutableLiveData<>();
        mLogList.setValue(new ArrayList<>());
        mQueueList = new MutableLiveData<>();
        mQueueList.setValue(new ArrayList<>());
    }

    public LiveData<Artikel> getArtikel() {
        return mArtikel;
    }

    public void setArtikel(Artikel artikel) {
        mArtikel.setValue(artikel);
    }

    public LiveData<String> getSearchArtikel() {
        return mSearchArtikel;
    }

    public void setSearchArtikel(String search) {
        mSearchArtikel.setValue(search);
    }

    public LiveData<String> getSearchQueue() {
        return mSearchQueue;
    }

    public void setSearchQueue(String search) {
        mSearchQueue.setValue(search);
    }

    public LiveData<String> getSearchLager() {
        return mSearchLager;
    }

    public void setSearchLager(String search) {
        mSearchLager.setValue(search);
    }

    public LiveData<Boolean> getResetArtikel(){
        return mResetArtikel;
    }

    public void setResetArtikel(Boolean resetArtikel){
        mResetArtikel.setValue(resetArtikel);
    }

    public LiveData<List<Log>> getLog() {
        return mLogList;
    }

    public void resetArtikel() {
        mArtikel.setValue(new Artikel("", "", "", "", "", "", 1, 1, "", "", "", "", 0, ""));
    }

    public void setLog(List<Log> value) {
        mLogList.setValue(value);
    }

    public void addLog(Log log){
        List<Log> logs = mLogList.getValue();
        assert logs != null;
        logs.add(log);
        mLogList.setValue(logs);
    }

    public LiveData<List<Artikel>> getQueue() {
        return mQueueList;
    }

    public void setQueue(List<Artikel> value) {
        mQueueList.setValue(value);
    }

    public void addQueue(Artikel artikel){
        List<Artikel> artikels = mQueueList.getValue();
        assert artikels != null;
        artikels.add(artikel);
        mQueueList.setValue(artikels);
    }

    public void removeQueue(Artikel artikel){
        List<Artikel> artikels = mQueueList.getValue();
        assert artikels != null;
        artikels.remove(artikel);
        mQueueList.setValue(artikels);
    }
}
