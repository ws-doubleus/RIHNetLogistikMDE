package at.rihnet.rihnetlogistikmde.models;

public class Lagerplatzinfo {
    private String artikelnummer;
    private String bezeichnung;
    private int menge;

    public Lagerplatzinfo(String artikelnummer, String bezeichnung, int bestand) {
        this.artikelnummer = artikelnummer;
        this.bezeichnung = bezeichnung;
        this.menge = bestand;
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

    public int getMenge() {
        return menge;
    }

    public void setMenge(int menge) {
        this.menge = menge;
    }
}
