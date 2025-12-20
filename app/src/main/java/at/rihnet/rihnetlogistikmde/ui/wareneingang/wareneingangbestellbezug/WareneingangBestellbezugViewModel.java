package at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Beleg;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Lagerplatz;
import at.rihnet.rihnetlogistikmde.models.Lieferant;
import at.rihnet.rihnetlogistikmde.models.Log;

public class WareneingangBestellbezugViewModel extends ViewModel {
    private static final String TAG = "RIHNet";
    //private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final MutableLiveData<Artikel> mArtikel;
    private final MutableLiveData<Beleg> mBeleg;
    private final MutableLiveData<String> mSearchArtikel;
    private final MutableLiveData<Boolean> mResetArtikel;
    private final MutableLiveData<List<Belegposition>> mQueueList;
    private final MutableLiveData<List<Belegposition>> mBelegpositionList;
    private final MutableLiveData<String> mSearchQueue;
    private final MutableLiveData<List<Log>> mLogList;
    private final MutableLiveData<List<String>> mLager;
    private final MutableLiveData<List<Lieferant>> mLieferanten;
    private final MutableLiveData<List<Lagerplatz>> mLagerplaetze;

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public WareneingangBestellbezugViewModel(){
        mArtikel = new MutableLiveData<>();
        mBeleg = new MutableLiveData<>();
        mSearchArtikel = new MutableLiveData<>();
        mResetArtikel = new MutableLiveData<>();
        mQueueList = new MutableLiveData<>();
        mQueueList.setValue(new ArrayList<>());
        mBelegpositionList = new MutableLiveData<>();
        mBelegpositionList.setValue(new ArrayList<>());
        mSearchQueue = new MutableLiveData<>();
        mLogList = new MutableLiveData<>();
        mLogList.setValue(new ArrayList<>());
        mLager = new MutableLiveData<>();
        mLager.setValue(new ArrayList<>());
        mLieferanten = new MutableLiveData<>();
        mLieferanten.setValue(new ArrayList<>());
        mLagerplaetze = new MutableLiveData<>();
        mLagerplaetze.setValue(new ArrayList<>());
    }

    public LiveData<Artikel> getArtikel() {
        return mArtikel;
    }

    public void setArtikel(Artikel artikel) {
        mArtikel.setValue(artikel);
    }

    public LiveData<Beleg> getBeleg() {
        return mBeleg;
    }

    public void setBeleg(Beleg beleg) {
        mBeleg.setValue(beleg);
    }

    public LiveData<String> getSearchArtikel() {
        return mSearchArtikel;
    }

    public void setSearchArtikel(String search) {
        mSearchArtikel.setValue(search);
    }

    public LiveData<Boolean> getResetArtikel() {
        return mResetArtikel;
    }

    public void setResetArtikel(Boolean resetArtikel) {
        mResetArtikel.setValue(resetArtikel);
    }

    public LiveData<List<Belegposition>> getQueue() {
        return mQueueList;
    }

    public void setQueue(List<Belegposition> value) {
        mQueueList.setValue(value);
    }

    public void updateQueue(int position, int menge){
        List<Belegposition> belegpositionen = mQueueList.getValue();
        if(belegpositionen != null && position >= 0 && position < belegpositionen.size()){
            belegpositionen.get(position).setMenge(menge);
            mQueueList.setValue(belegpositionen);
        }
    }

    public LiveData<List<Belegposition>> getBelegposition() {
        return mBelegpositionList;
    }

    public void setBelegposition(List<Belegposition> value) {
        mBelegpositionList.setValue(value);
    }

    public void updateBelegposition(int position, int offen){
        List<Belegposition> belegpositionen = mBelegpositionList.getValue();
        if(belegpositionen != null && position >= 0 && position < belegpositionen.size()){
            belegpositionen.get(position).setOffen(offen);
            mBelegpositionList.setValue(belegpositionen);
        }
    }

    public void addQueue(Belegposition belegposition) {
        List<Belegposition> artikels = mQueueList.getValue();
        assert artikels != null;
        artikels.add(belegposition);
        mQueueList.setValue(artikels);
    }

    public void addQueue1(Belegposition belegposition) {
        List<Belegposition> belegpositionen = mQueueList.getValue();
        assert belegpositionen != null;
        android.util.Log.e(TAG, "artikels: " + belegpositionen.size());
        Optional<Belegposition> gefundeneBelegposition = belegpositionen.stream()
                .filter(b -> b.getBelegnummer().equals(belegposition.getBelegnummer())
                        && b.getArtikelnummer().equals(belegposition.getArtikelnummer())
                        && b.getPostext().equals(belegposition.getPostext()))
                .findFirst();
        if (gefundeneBelegposition.isPresent()) {
            android.util.Log.e(TAG, "gefundeneBelegposition: true");
            Belegposition b = gefundeneBelegposition.get();
            b.setMenge(b.getMenge() + belegposition.getMenge());
        } else {
            android.util.Log.e(TAG, "gefundeneBelegposition: false");
            belegpositionen.add(belegposition);
        }
        mQueueList.setValue(belegpositionen);
    }

    public void removeQueue(Belegposition artikel) {
        List<Belegposition> artikels = mQueueList.getValue();
        assert artikels != null;
        artikels.remove(artikel);
        mQueueList.setValue(artikels);
    }

    public LiveData<String> getSearchQueue() {
        return mSearchQueue;
    }

    public void setSearchQueue(String search) {
        mSearchQueue.setValue(search);
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

    public LiveData<List<String>> getLager() {
        return mLager;
    }

    public void setLager(List<String> value) {
        mLager.setValue(value);
    }

    public LiveData<List<Lieferant>> getLieferanten() {
        return mLieferanten;
    }

    public void setLieferanten(List<Lieferant> value) {
        mLieferanten.setValue(value);
    }

    public LiveData<List<Lagerplatz>> getLagerplaetze() {
        return mLagerplaetze;
    }

    public void setLagerplaetze(List<Lagerplatz> value) {
        mLagerplaetze.setValue(value);
    }
}
