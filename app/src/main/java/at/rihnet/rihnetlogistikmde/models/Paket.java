package at.rihnet.rihnetlogistikmde.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Paket implements Serializable {
    private int nummer;
    private String bezeichnung;
    private float gewicht;
    private float wert;
    private  List<Belegposition> positionen ;
    private boolean expanded;

    public Paket(int nummer, String bezeichnung, float gewicht, float wert) {
        this.nummer = nummer;
        this.bezeichnung = bezeichnung;
        this.gewicht = gewicht;
        this.wert = wert;
        this.positionen = new ArrayList<>();
    }

    public int getNummer() {
        return nummer;
    }

    public void setNummer(int nummer) {
        this.nummer = nummer;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public float getGewicht() {
        return gewicht;
    }

    public void setGewicht(float gewicht) {
        this.gewicht = gewicht;
    }

    public float getWert() {
        return wert;
    }

    public void setWert(float wert) {
        this.wert = wert;
    }

    public List<Belegposition> getPositionen() {
        return positionen;
    }

    public void setPositionen(List<Belegposition> positionen) {
        this.positionen = positionen;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
