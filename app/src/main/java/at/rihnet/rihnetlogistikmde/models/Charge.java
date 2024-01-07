package at.rihnet.rihnetlogistikmde.models;

public class Charge {
    private String nummer;
    private double bestand;
    private String verfallsdatum;

    public Charge(String nummer, double bestand, String verfallsdatum) {
        this.nummer = nummer;
        this.bestand = bestand;
        this.verfallsdatum = verfallsdatum;
    }

    public String getNummer() {
        return nummer;
    }

    public void setNummer(String nummer) {
        this.nummer = nummer;
    }

    public double getBestand() {
        return bestand;
    }

    public void setBestand(double bestand) {
        this.bestand = bestand;
    }

    public String getVerfallsdatum() {
        return verfallsdatum;
    }

    public void setVerfallsdatum(String verfallsdatum) {
        this.verfallsdatum = verfallsdatum;
    }
}
