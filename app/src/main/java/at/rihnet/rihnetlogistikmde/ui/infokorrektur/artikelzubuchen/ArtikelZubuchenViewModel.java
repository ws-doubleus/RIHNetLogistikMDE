package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelzubuchen;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Log;

public class ArtikelZubuchenViewModel extends ViewModel {
    //private static final String TAG = "RIHNet";
    private final MutableLiveData<Artikel> mArtikel;
    private final MutableLiveData<String> mSearchArtikel;
    private final MutableLiveData<String> mSearchLager;
    private final MutableLiveData<Boolean> mResetArtikel;
    private final MutableLiveData<List<Log>> mLogList;
    private final MutableLiveData<Double> mMenge;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ArtikelZubuchenViewModel() {
        mArtikel = new MutableLiveData<>();
        mSearchArtikel = new MutableLiveData<>();
        mSearchLager = new MutableLiveData<>();
        mResetArtikel = new MutableLiveData<>();
        mLogList = new MutableLiveData<>();
        mLogList.setValue(new ArrayList<>());
        mMenge = new MutableLiveData<>();
        mMenge.setValue(1.0);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    public ExecutorService getExecutorService() {
        if (executorService.isShutdown() || executorService.isTerminated()) {
            executorService = Executors.newSingleThreadExecutor();   // Pool neu starten
        }
        return executorService;
    }

    public LiveData<Artikel> getArtikel() {
        return mArtikel;
    }

    public void setArtikel(Artikel artikel) {
        mArtikel.setValue(artikel);
    }

    public LiveData<Double> getMenge() {
        return mMenge;
    }

    public void setMenge(double menge) {
        mMenge.setValue(menge);
    }

    public void resetArtikel() {
        mArtikel.setValue(new Artikel("", "", "", "", "", "", 1, 1, "", "", "","", 0, "", Kategorie.ARTIKELZUBUCHEN));
        mMenge.setValue(1.0);
    }

    public LiveData<String> getSearchArtikel() {
        return mSearchArtikel;
    }

    public void setSearchArtikel(String search) {
        mSearchArtikel.setValue(search);
    }

    public LiveData<String> getSearchLager() {
        return mSearchLager;
    }

    public void setSearchLager(String search) {
        mSearchLager.setValue(search);
    }

    public LiveData<Boolean> getResetArtikel() {
        return mResetArtikel;
    }

    public void setResetArtikel(Boolean resetArtikel) {
        mResetArtikel.setValue(resetArtikel);
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
}
