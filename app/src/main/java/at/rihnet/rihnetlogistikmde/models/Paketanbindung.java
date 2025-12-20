package at.rihnet.rihnetlogistikmde.models;

public class Paketanbindung {
    private String belegtyp;
    private String belegnummer;
    private int anzahl;
    private int drucker;
    private String angelegtVon;

    public Paketanbindung(String belegtyp, String belegnummer, int anzahl, int drucker, String angelegtVon) {
        this.belegtyp = belegtyp;
        this.belegnummer = belegnummer;
        this.anzahl = anzahl;
        this.drucker = drucker;
        this.angelegtVon = angelegtVon;
    }

    public String getBelegtyp() {
        return belegtyp;
    }

    public void setBelegtyp(String belegtyp) {
        this.belegtyp = belegtyp;
    }

    public String getBelegnummer() {
        return belegnummer;
    }

    public void setBelegnummer(String belegnummer) {
        this.belegnummer = belegnummer;
    }

    public int getAnzahl() {
        return anzahl;
    }

    public void setAnzahl(int anzahl) {
        this.anzahl = anzahl;
    }

    public int getDrucker() {
        return drucker;
    }

    public void setDrucker(int drucker) {
        this.drucker = drucker;
    }

    public String getAngelegtVon() {
        return angelegtVon;
    }

    public void setAngelegtVon(String angelegtVon) {
        this.angelegtVon = angelegtVon;
    }
}
