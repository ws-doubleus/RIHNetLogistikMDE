package at.rihnet.rihnetlogistikmde.models;

import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.List;

@Dao
@Entity(tableName = "queue")
public class Artikel implements Serializable {
    @PrimaryKey
    private  Long id;
    private String artikelnummer;
    private String bezeichnung;
    private String zusatz;
    private String serieCharge;
    private String seriennummer;
    private String charge;
    private int menge;
    private int bestand;
    private String lager;
    private String lagerplatz;
    private int lagerplatzId;
    private String hstArtikelnummer;
    private String sn;
    private String eannummer;

    @Ignore
    private List<LagerplatzBestand> lagerBestandList;

    public Artikel(String artikelnummer, String bezeichnung, String zusatz, String serieCharge, String seriennummer, String charge, int menge, int bestand, String lager, String hstArtikelnummer, String eannummer, String lagerplatz, int lagerplatzId, String sn) {
        this.artikelnummer = artikelnummer;
        this.bezeichnung = bezeichnung;
        this.zusatz = zusatz;
        this.serieCharge = serieCharge;
        this.seriennummer = seriennummer;
        this.charge = charge;
        this.menge = menge;
        this.bestand = bestand;
        this.lager = lager;
        this.hstArtikelnummer = hstArtikelnummer;
        this.eannummer = eannummer;
        this.lagerplatz = lagerplatz;
        this.lagerplatzId = lagerplatzId;
        this.sn = sn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArtikelnummer() {
        return artikelnummer;
    }

    public void setArtikelnummer(String artikelnummer) {
        this.artikelnummer = artikelnummer;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getZusatz() {
        return zusatz;
    }

    public void setZusatz(String zusatz) {
        this.zusatz = zusatz;
    }

    public String getSerieCharge() {
        return serieCharge;
    }

    public void setSerieCharge(String serieCharge) {
        this.serieCharge = serieCharge;
    }

    public String getSeriennummer() {
        return seriennummer;
    }

    public void setSeriennummer(String seriennummer) {
        this.seriennummer = seriennummer;
    }

    public String getCharge() {
        return charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public int getMenge() {
        return menge;
    }

    public void setMenge(int menge) {
        this.menge = menge;
    }

    public int getBestand() {
        return bestand;
    }

    public void setBestand(int bestand) {
        this.bestand = bestand;
    }

    public String getLager() {
        return lager;
    }

    public void setLager(String lager) {
        this.lager = lager;
    }

    public String getHstArtikelnummer() {
        return hstArtikelnummer;
    }

    public void setHstArtikelnummer(String hstArtikelnummer) {
        this.hstArtikelnummer = hstArtikelnummer;
    }

    public String getLagerplatz() {
        return lagerplatz;
    }

    public void setLagerplatz(String lagerplatz) {
        this.lagerplatz = lagerplatz;
    }

    public int getLagerplatzId() {
        return lagerplatzId;
    }

    public void setLagerplatzId(int lagerplatzId) {
        this.lagerplatzId = lagerplatzId;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getEannummer() {
        return eannummer;
    }

    public void setEannummer(String eannummer) {
        this.eannummer = eannummer;
    }

    public List<LagerplatzBestand> getLagerBestandList() {
        return lagerBestandList;
    }

    public void setLagerBestandList(List<LagerplatzBestand> lagerBestandList) {
        this.lagerBestandList = lagerBestandList;
    }
}
