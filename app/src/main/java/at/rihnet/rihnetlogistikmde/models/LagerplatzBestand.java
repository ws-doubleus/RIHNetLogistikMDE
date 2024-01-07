package at.rihnet.rihnetlogistikmde.models;

import androidx.annotation.NonNull;

public class LagerplatzBestand {
    private String lager;
    private int lagerplatzId;
    private String bezeichnung;
    private String ean;
    private int bestand;

    public LagerplatzBestand(String lager, int lagerplatzId, String bezeichnung, String ean, int bestand) {
        this.lager = lager;
        this.lagerplatzId = lagerplatzId;
        this.bezeichnung = bezeichnung;
        this.ean = ean;
        this.bestand = bestand;
    }

    public String getLager() {
        return lager;
    }

    public void setLager(String lager) {
        this.lager = lager;
    }

    public int getLagerplatzId() {
        return lagerplatzId;
    }

    public void setLagerplatzId(int lagerplatzId) {
        this.lagerplatzId = lagerplatzId;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public int getBestand() {
        return bestand;
    }

    public void setBestand(int bestand) {
        this.bestand = bestand;
    }

    @NonNull
    @Override
    public String toString() {
        return bezeichnung;
    }
}
