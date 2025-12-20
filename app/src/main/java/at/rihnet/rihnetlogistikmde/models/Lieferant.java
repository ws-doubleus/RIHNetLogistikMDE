package at.rihnet.rihnetlogistikmde.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Lieferant implements Serializable {
    private String nummer;
    private String anzeigename;

    public Lieferant(String nummer, String anzeigename) {
        this.nummer = nummer;
        this.anzeigename = anzeigename;
    }

    public String getNummer() {
        return nummer;
    }

    public void setNummer(String nummer) {
        this.nummer = nummer;
    }

    public String getAnzeigename() {
        return anzeigename;
    }

    public void setAnzeigename(String anzeigename) {
        this.anzeigename = anzeigename;
    }

    @NonNull
    @Override
    public String toString() {
        return anzeigename;
    }
}
