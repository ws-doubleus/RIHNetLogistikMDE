package at.rihnet.rihnetlogistikmde.models;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Ignore;

@Dao
@Entity(tableName = "queuebelegposition")
public class Belegposition extends Artikel implements Cloneable {
    private String belegtyp;
    private String belegnummer;
    private String postext;
    private String kennung;
    private String vorgaenger;
    private int offen;
    private int originalMenge;
    private float gewicht;
    private float kalkulationspreis;

    @Ignore
    private int paketNummer;

    public Belegposition(String artikelnummer,
                         String bezeichnung,
                         String zusatz,
                         String serieCharge,
                         String seriennummer,
                         String charge,
                         int menge,
                         int bestand,
                         String lager,
                         String hstArtikelnummer,
                         String eannummer,
                         String lagerplatz,
                         int lagerplatzId,
                         String sn,
                         Kategorie kategorie,
                         String belegtyp,
                         String belegnummer,
                         String postext,
                         String kennung,
                         String vorgaenger,
                         int offen,
                         int originalMenge,
                         float gewicht,
                         float kalkulationspreis) {
        super(artikelnummer, bezeichnung, zusatz, serieCharge, seriennummer, charge, menge, bestand, lager, hstArtikelnummer, eannummer, lagerplatz, lagerplatzId, sn, kategorie);
        this.belegtyp = belegtyp;
        this.belegnummer = belegnummer;
        this.postext = postext;
        this.kennung = kennung;
        this.vorgaenger = vorgaenger;
        this.offen = offen;
        this.originalMenge = originalMenge;
        this.gewicht = gewicht;
        this.kalkulationspreis = kalkulationspreis;
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

    public String getPostext() {
        return postext;
    }

    public void setPostext(String postext) {
        this.postext = postext;
    }

    public String getKennung() {
        return kennung;
    }

    public void setKennung(String kennung) {
        this.kennung = kennung;
    }

    public String getVorgaenger() {
        return vorgaenger;
    }

    public void setVorgaenger(String vorgaenger) {
        this.vorgaenger = vorgaenger;
    }

    public int getOffen() {
        return offen;
    }

    public void setOffen(int offen) {
        this.offen = offen;
    }

    public int getOriginalMenge() {
        return originalMenge;
    }

    public void setOriginalMenge(int originalMenge) {
        this.originalMenge = originalMenge;
    }

    public float getGewicht() {
        return gewicht;
    }

    public void setGewicht(float gewicht) {
        this.gewicht = gewicht;
    }

    public float getKalkulationspreis() {
        return kalkulationspreis;
    }

    public void setKalkulationspreis(float kalkulationspreis) {
        this.kalkulationspreis = kalkulationspreis;
    }

    public int getPaketNummer() {
        return paketNummer;
    }

    public void setPaketNummer(int paketNummer) {
        this.paketNummer = paketNummer;
    }

    @NonNull
    @Override
    public Belegposition clone() {
        try {
            return (Belegposition) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported", e);
        }
    }
}
