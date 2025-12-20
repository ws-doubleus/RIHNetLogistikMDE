package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import at.rihnet.rihnetlogistikmde.models.BelegInfo;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Lieferbedingung;
import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.models.Paket;
import at.rihnet.rihnetlogistikmde.models.Verarbeitungskennzeichen;

public class LogistikViewModel extends ViewModel {
    private final MutableLiveData<BelegInfo> mBelegInfo;
    private final MutableLiveData<List<Lieferbedingung>> mLieferbedingungList;
    private final MutableLiveData<List<Verarbeitungskennzeichen>> mVerarbeitungskennzeichenList;
    private final MutableLiveData<List<Belegposition>> mOffenList;
    private final MutableLiveData<List<Belegposition>> mGebuchtList;
    private final MutableLiveData<List<Belegposition>> mLeistungenList;
    private final MutableLiveData<List<File>> mPhotoList;
    private final MutableLiveData<String> mSearchArtikel;
    private final MutableLiveData<Integer> mPaketeAnzahl = new MutableLiveData<>(1);
    private final MutableLiveData<List<Paket>> mPaketList;
    private final MutableLiveData<List<Log>> mLogList;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LogistikViewModel() {
        this.mBelegInfo = new MutableLiveData<>();
        this.mLieferbedingungList = new MutableLiveData<>();
        mVerarbeitungskennzeichenList = new MutableLiveData<>();
        mLieferbedingungList.setValue(new ArrayList<>());
        mOffenList = new MutableLiveData<>();
        mOffenList.setValue(new ArrayList<>());
        mGebuchtList = new MutableLiveData<>();
        mGebuchtList.setValue(new ArrayList<>());
        mLeistungenList = new MutableLiveData<>();
        mLeistungenList.setValue(new ArrayList<>());
        mPhotoList = new MutableLiveData<>();
        mPhotoList.setValue(new ArrayList<>());
        mSearchArtikel = new MutableLiveData<>();
        mPaketList = new MutableLiveData<>();
        mPaketList.setValue(new ArrayList<>());
        mLogList = new MutableLiveData<>();
        mLogList.setValue(new ArrayList<>());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public LiveData<BelegInfo> getBelegInfo() {
        return mBelegInfo;
    }

    public void setBelegInfo(BelegInfo value) {
        mBelegInfo.setValue(value);
    }

    public LiveData<List<Lieferbedingung>> getLieferbedingungen() {
        return mLieferbedingungList;
    }

    public void setLieferbedingungen(List<Lieferbedingung> value) {
        mLieferbedingungList.setValue(value);
    }

    public LiveData<List<Verarbeitungskennzeichen>> getVerarbeitungskennzeichen() {
        return mVerarbeitungskennzeichenList;
    }

    public void setVerarbeitungskennzeichen(List<Verarbeitungskennzeichen> value) {
        mVerarbeitungskennzeichenList.setValue(value);
    }

    public LiveData<List<Belegposition>> getOffen() {
        return mOffenList;
    }

    public void setOffen(List<Belegposition> value) {
        mOffenList.setValue(value);
    }

    public LiveData<List<Belegposition>> getGebucht() {
        return mGebuchtList;
    }

    public void setGebucht(List<Belegposition> value) {
        mGebuchtList.setValue(value);
    }

    public LiveData<List<Belegposition>> getLeistungen() {
        return mLeistungenList;
    }

    public void setLeistungen(List<Belegposition> value) {
        mLeistungenList.setValue(value);
    }

    public void updateOffen(int position, int offen) {
        List<Belegposition> belegpositionen = mOffenList.getValue();
        if (belegpositionen != null && position >= 0 && position < belegpositionen.size()) {
            belegpositionen.get(position).setOffen(offen);
            mOffenList.setValue(belegpositionen);
        }
    }

    public void removeOffen(int position) {
        List<Belegposition> belegpositionen = mOffenList.getValue();
        if (belegpositionen != null && position >= 0 && position < belegpositionen.size()) {
            belegpositionen.remove(belegpositionen.get(position));
            mOffenList.setValue(belegpositionen);
        }
    }

    public void addOffen(Belegposition belegposition) {
        List<Belegposition> belegpositionen = mOffenList.getValue();
        assert belegpositionen != null;
        belegpositionen.add(belegposition);
        mOffenList.setValue(belegpositionen);
    }

    public void addGebucht(Belegposition belegposition) {
        List<Belegposition> belegpositionen = mGebuchtList.getValue();
        assert belegpositionen != null;
        Optional<Belegposition> gefundeneBelegposition = belegpositionen.stream()
                .filter(b -> b.getBelegnummer().equals(belegposition.getBelegnummer())
                        && b.getArtikelnummer().equals(belegposition.getArtikelnummer())
                        && b.getPostext().equals(belegposition.getPostext())
                        && b.getPaketNummer() == belegposition.getPaketNummer())
                .findFirst();
        if (gefundeneBelegposition.isPresent() && !belegposition.getSerieCharge().equals("S")) {
            Belegposition b = gefundeneBelegposition.get();
            b.setMenge(b.getMenge() + belegposition.getMenge());

        } else {
            belegpositionen.add(belegposition);

        }
        mGebuchtList.setValue(belegpositionen);




        // 1) Positionen nach Paketnummer gruppieren
        Map<Integer, List<Belegposition>> byPaket = belegpositionen.stream()
                .collect(Collectors.groupingBy(Belegposition::getPaketNummer));

        // 2) Für jedes Paket die Summen ermitteln und Paket‑Objekt erzeugen
        List<Paket> pakete = byPaket.entrySet()
                .stream()
                .map(entry -> {
                    int paketNr = entry.getKey();
                    List<Belegposition> pos = entry.getValue();

                    // Gesamtgewicht
                    float gesGewicht = (float) pos.stream()
                            .mapToDouble(p -> p.getGewicht() * p.getMenge())
                            .sum();

                    // Gesamtwert
                    float gesWert = (float) pos.stream()
                            .mapToDouble(p -> p.getKalkulationspreis() * p.getMenge())
                            .sum();

                    Paket paket = new Paket(paketNr,
                            "Paket #" + paketNr,
                            gesGewicht,
                            gesWert);
                    paket.setPositionen(pos);
                    return paket;
                })
                .sorted(Comparator.comparingInt(Paket::getNummer))
                .collect(Collectors.toList());

        setPakete(pakete);
    }

    public void removeGebucht(Belegposition belegposition) {
        List<Belegposition> gebucht = mGebuchtList.getValue();
        assert gebucht != null;
        gebucht.remove(belegposition);
        mGebuchtList.setValue(gebucht);
    }


    public LiveData<String> getSearchArtikel() {
        return mSearchArtikel;
    }

    public void setSearchArtikel(String search) {
        mSearchArtikel.setValue(search);
    }


    public LiveData<Integer> getPaketeAnzahl() {
        return mPaketeAnzahl;
    }

    public void setPaketeAnzahl(int pakete) {
        mPaketeAnzahl.setValue(pakete);
    }

    public void addPaketAnzahl() {
        mPaketeAnzahl.setValue(Objects.requireNonNull(mPaketeAnzahl.getValue()) + 1);
    }

    public LiveData<List<Paket>> getPakete() {
        return mPaketList;
    }

    public void setPakete(List<Paket> pakete) {
        mPaketList.setValue(pakete);
    }

    public void addPaket(Paket paket) {
        List<Paket> pakete = mPaketList.getValue();
        assert pakete != null;
        pakete.add(paket);
        mPaketList.setValue(pakete);
    }

    public LiveData<List<File>> getPhotos() {
        return mPhotoList;
    }

    public void setPhotos(List<File> value) {
        mPhotoList.setValue(value);
    }

    public void addPhoto(File file) {
        List<File> files = mPhotoList.getValue();
        assert files != null;
        files.add(file);
        mPhotoList.setValue(files);
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
