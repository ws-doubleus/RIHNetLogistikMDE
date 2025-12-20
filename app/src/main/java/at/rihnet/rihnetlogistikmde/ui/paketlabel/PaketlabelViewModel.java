package at.rihnet.rihnetlogistikmde.ui.paketlabel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.models.BelegInfo;
import at.rihnet.rihnetlogistikmde.models.Lieferbedingung;
import at.rihnet.rihnetlogistikmde.models.Log;

public class PaketlabelViewModel extends ViewModel {
    private final MutableLiveData<BelegInfo> mBelegInfo;
    private final MutableLiveData<List<File>> mPhotoList;
    private final MutableLiveData<List<Lieferbedingung>> mLieferbedingungList;
    private final MutableLiveData<Integer> photoCount = new MutableLiveData<>(0);
    private final MutableLiveData<List<Log>> mLogList;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public PaketlabelViewModel() {
        this.mBelegInfo = new MutableLiveData<>();
        mPhotoList = new MutableLiveData<>();
        mPhotoList.setValue(new ArrayList<>());
        this.mLieferbedingungList = new MutableLiveData<>();
        mLieferbedingungList.setValue(new ArrayList<>());
        this.mLogList = new MutableLiveData<>();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    public LiveData<BelegInfo> getBelegInfo() {
        return mBelegInfo;
    }

    public void setBelegInfo(BelegInfo value) {
        mBelegInfo.setValue(value);
    }

    public LiveData<List<File>> getPhotos() {
        return mPhotoList;
    }

    public void setPhotos(List<File> value) {
        mPhotoList.setValue(value);
    }

    public void addPhoto(File file) {
//        List<File> files = mPhotoList.getValue();
//        assert files != null;
//        files.add(file);
//        mPhotoList.setValue(files);

        List<File> current = mPhotoList.getValue();
        if (current == null) current = new ArrayList<>();
        current = new ArrayList<>(current); // Kopie, wichtig für LiveData Updates
        current.add(file);
        mPhotoList.setValue(current);
        updatePhotoCount();
    }

    public LiveData<List<Lieferbedingung>> getLieferbedingungen() {
        return mLieferbedingungList;
    }

    public void setLieferbedingungen(List<Lieferbedingung> value) {
        mLieferbedingungList.setValue(value);
    }

    public LiveData<Integer> getPhotoCount() {
        return photoCount;
    }

    public void increasePhotoCount() {
        Integer current = photoCount.getValue();
        if (current == null) current = 0;
        photoCount.setValue(current + 1);
    }

    private void updatePhotoCount() {
        List<File> current = mPhotoList.getValue();
        photoCount.setValue(current == null ? 0 : current.size());
    }

    public void resetPhotoCount() {
        photoCount.setValue(0);
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

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }
}
