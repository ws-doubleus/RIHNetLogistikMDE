package at.rihnet.rihnetlogistikmde.models;

import androidx.annotation.NonNull;

public class Invbasis {
    private String belegnummer;
    private String nummer;
    private String bezeichnung;

    public Invbasis(String belegnummer, String nummer, String bezeichnung) {
        this.belegnummer = belegnummer;
        this.nummer = nummer;
        this.bezeichnung = bezeichnung;
    }

    public String getBelegnummer() {
        return belegnummer;
    }

    public void setBelegnummer(String belegnummer) {
        this.belegnummer = belegnummer;
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

    @NonNull
    @Override
    public String toString() {
        return getNummer();
    }
}
