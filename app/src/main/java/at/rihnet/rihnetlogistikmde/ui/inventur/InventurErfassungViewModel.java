package at.rihnet.rihnetlogistikmde.ui.inventur;

import android.content.Context;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.audio.SoundPoolManager;
import at.rihnet.rihnetlogistikmde.core.AsyncTaskExecutorService;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.models.SelectLine.Inventory;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class InventurErfassungViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<String> lastScanInfo = new MutableLiveData<>(null);
    private final MutableLiveData<Inventory> mInventory;
    private final MutableLiveData<List<Log>> mLogList;
    private final MutableLiveData<String> mSearchArtikel;
    private final MutableLiveData<Boolean> autoMode = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> pendingTasks = new MutableLiveData<>(0);
    private InventurRepository inventurRepository;
    private final ExecutorService bookingExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public InventurErfassungViewModel() {
        mInventory = new MutableLiveData<>();
        mLogList = new MutableLiveData<>();
        mLogList.setValue(new ArrayList<>());
        mSearchArtikel = new MutableLiveData<>();
    }

    public InventurRepository getInventoryRepository() {
        return inventurRepository;
    }

    public void setInventoryRepository(InventurRepository repository) {
        this.inventurRepository = repository;
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

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getLastScanInfo() {
        return lastScanInfo;
    }

    public void clearError() {
        errorMessage.postValue(null);
    }

    public void loadInvbasis(String belegnummer, String kennzeichen) {
        if (inventurRepository == null) {
            errorMessage.postValue("InventoryRepository ist nicht gesetzt.");
            return;
        }

        isLoading.postValue(true);

        // WICHTIG: Diese Methode sollte in einem Hintergrund-Thread aufgerufen werden.
        // Beispiel (Pseudo):
        // List<Invbasis> list =
        //         inventoryRepository.getInvbasisByBelegnummerKennzeichen(belegnummer, kennzeichen);
        //
        // if (list != null) {
        //     // ggf. eigene LiveData-Liste dafür anlegen (z.B. MutableLiveData<List<Invbasis>>)
        //     errorMessage.postValue(null);
        // } else {
        //     errorMessage.postValue("Fehler beim Laden der Inventur-Basisdaten.");
        // }

        isLoading.postValue(false);
    }


    public void erfasseArtikel(
            Inventory inventory,
            Artikel artikel,
            double menge,
            String lager,
            String seriennummer,
            boolean autoModeFlag,
            String appKey,
            String baseAddress,
            String userName,
            String password,
            LogDAO logDAO,
            Context context
    ) {
        if (inventurRepository == null) {
            // Repository nicht gesetzt -> nichts zu tun, auch kein Zähler
            return;
        }

        // 1) Einen neuen Hintergrund-Job einbuchen
        incPendingTasks();

        // 2) Job auf unseren SingleThreadExecutor legen
        bookingExecutor.execute(() -> {
            boolean ok;
            try {
                // Läuft IMMER seriell (ein Request nach dem anderen)
                ok = inventurRepository.bookInventoryArticle(
                        inventory,
                        artikel,
                        menge,
                        lager,
                        seriennummer,
                        appKey,
                        baseAddress,
                        userName,
                        password
                );
            } catch (Exception e) {
                android.util.Log.e("InventurErfassungVM", "bookInventoryArticle failed", e);
                ok = false;
            }

            // 3) Log-Eintrag vorbereiten (wir sind noch im Hintergrund-Thread)
            at.rihnet.rihnetlogistikmde.models.Log log;
            if (ok) {
                log = new at.rihnet.rihnetlogistikmde.models.Log(
                        "Belegnummer: " + inventory.getNumber() +
                                "\nArtikelnummer: " + artikel.getArtikelnummer() +
                                "\nMenge: " + menge +
                                "\nInventur-Erfassung erfolgreich!",
                        ContextCompat.getColor(context, R.color.green_500),
                        Kategorie.INVENTUR
                );
            } else {
                log = new at.rihnet.rihnetlogistikmde.models.Log(
                        "Belegnummer: " + inventory.getNumber() +
                                "\nArtikelnummer: " + artikel.getArtikelnummer() +
                                "\nMenge: " + menge +
                                "\nInventur-Erfassung fehlerhaft!",
                        ContextCompat.getColor(context, R.color.red_500),
                        Kategorie.INVENTUR
                );
            }

            // 4) Log in SQLite speichern: wir SIND bereits im Hintergrund-Thread,
            //    daher können wir direkt logDAO.insert(log) aufrufen.
            try {
                logDAO.insert(log);
            } catch (Exception e) {
                android.util.Log.e("InventurErfassungVM", "logDAO.insert failed", e);
            }

            // 5) UI-Updates auf den Main-Thread posten
            boolean finalOk = ok;
            mainHandler.post(() -> {
                // a) pendingTasks herunterzählen
                decPendingTasks();

                // b) Log in LiveData-Liste packen (muss am Main-Thread passieren)
                addLog(log);

                // c) Toasts & Sound
                if (finalOk) {
                    if (!autoModeFlag) {
                        Toast.makeText(
                                context,
                                "Artikelnummer: " + artikel.getArtikelnummer() +
                                        "\nMenge: " + menge +
                                        "\nInventur-Erfassung erfolgreich!",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                } else {
                    Toast.makeText(
                            context,
                            "Artikelnummer: " + artikel.getArtikelnummer() +
                                    "\nMenge: " + menge +
                                    "\nInventur-Erfassung fehlerhaft!",
                            Toast.LENGTH_LONG
                    ).show();

                    SoundPoolManager.getInstance(context).playError();
                }
            });
        });
    }



    public LiveData<Boolean> getAutoMode() {
        return autoMode;
    }

    public void setAutoMode(boolean enabled) {
        autoMode.setValue(enabled);
    }

    public LiveData<Integer> getPendingTasks() {
        return pendingTasks;
    }

    private void incPendingTasks() {
        Integer current = pendingTasks.getValue();
        if (current == null) current = 0;
        pendingTasks.postValue(current + 1);
    }

    private void decPendingTasks() {
        Integer current = pendingTasks.getValue();
        if (current == null) current = 0;
        int next = current - 1;
        if (next < 0) next = 0;
        pendingTasks.postValue(next);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Executor ordentlich herunterfahren (Activity/Fragment wird beendet)
        bookingExecutor.shutdownNow();
    }

}
