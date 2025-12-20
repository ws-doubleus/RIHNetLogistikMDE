package at.rihnet.rihnetlogistikmde.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Lagerplatz implements Serializable {
    private String lager0;
    private int lagerplatzId;
    private String bezeichnung;
    private String ean;

    public Lagerplatz(String lager0, int lagerplatzId, String bezeichnung, String ean) {
        this.lager0 = lager0;
        this.lagerplatzId = lagerplatzId;
        this.bezeichnung = bezeichnung;
        this.ean = ean;
    }

    public String getLager0() {
        return lager0;
    }

    public void setLager0(String lager) {
        this.lager0 = lager;
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

    @NonNull
    @Override
    public String toString() {
        return getBezeichnung();
    }
}
