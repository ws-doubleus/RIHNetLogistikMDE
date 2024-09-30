package at.rihnet.rihnetlogistikmde.models;

import androidx.annotation.NonNull;

public class LagerplatzBestand extends Lagerplatz {

    private int bestand;

    public LagerplatzBestand(String lager, int lagerplatzId, String bezeichnung, String ean, int bestand) {
        super(lager, lagerplatzId, bezeichnung, ean);
        this.bestand = bestand;
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
        return getBezeichnung();
    }
}
