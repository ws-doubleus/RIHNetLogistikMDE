package at.rihnet.rihnetlogistikmde.models;

public class SeriennummerCharge {
    private String artikelnummer;
    private int bestand;
    private String nummer;
    private String serieCharge;
    private String lager;

    public SeriennummerCharge(String artikelnummer, int bestand, String nummer, String serieCharge, String lager) {
        this.artikelnummer = artikelnummer;
        this.bestand = bestand;
        this.nummer = nummer;
        this.serieCharge = serieCharge;
        this.lager = lager;
    }

    public String getArtikelnummer() {
        return artikelnummer;
    }

    public void setArtikelnummer(String artikelnummer) {
        this.artikelnummer = artikelnummer;
    }

    public int getBestand() {
        return bestand;
    }

    public void setBestand(int bestand) {
        this.bestand = bestand;
    }

    public String getNummer() {
        return nummer;
    }

    public void setNummer(String nummer) {
        this.nummer = nummer;
    }

    public String getSerieCharge() {
        return serieCharge;
    }

    public void setSerieCharge(String serieCharge) {
        this.serieCharge = serieCharge;
    }

    public String getLager() {
        return lager;
    }

    public void setLager(String lager) {
        this.lager = lager;
    }
}
