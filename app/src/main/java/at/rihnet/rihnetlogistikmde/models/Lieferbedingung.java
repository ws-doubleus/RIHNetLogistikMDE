package at.rihnet.rihnetlogistikmde.models;

import androidx.annotation.NonNull;

public class Lieferbedingung {
    private String nummer;
    private String bezeichnung;
    private String zusatz;

    public Lieferbedingung(String nummer, String bezeichnung, String zusatz) {
        this.nummer = nummer;
        this.bezeichnung = bezeichnung;
        this.zusatz = zusatz;
    }

    public String getNummer() {
        return nummer;
    }

    public void setNummer(String nummer) {
        this.nummer = nummer;
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

    @NonNull
    @Override
    public String toString() {
        return nummer;
    }
}
