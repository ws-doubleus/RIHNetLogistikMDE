package at.rihnet.rihnetlogistikmde.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BelegInfo implements Serializable {
    private String belegtyp;
    private String belegnummer;
    private LocalDateTime datum;
    private String adressnummer;
    private String firma;
    private String name;
    private String vorname;
    private String anzeigename;
    private String zusatz;
    private String zusatz2;
    private String strasse;
    private String plz;
    private String ort;
    private String land;
    private String lieferbedingung;
    private float netto;
    private LocalDateTime liefertermin;
    private String ihrAuftrag;
    private Adress abweichendeLieferadresse;
    private Verarbeitungskennzeichen verarbeitungskennzeichen;
    private Kategorie kategorie;

    public BelegInfo(String belegtyp, String belegnummer, LocalDateTime datum, String adressnummer, String firma, String name, String vorname, String anzeigename, String zusatz, String zusatz2, String strasse, String plz, String ort, String land, String lieferbedingung, float netto, LocalDateTime liefertermin, String ihrAuftrag, Adress abweichendeLieferadresse, Verarbeitungskennzeichen verarbeitungskennzeichen, Kategorie kategorie) {
        this.belegtyp = belegtyp;
        this.belegnummer = belegnummer;
        this.datum = datum;
        this.adressnummer = adressnummer;
        this.firma = firma;
        this.name = name;
        this.vorname = vorname;
        this.anzeigename = anzeigename;
        this.zusatz = zusatz;
        this.zusatz2 = zusatz2;
        this.strasse = strasse;
        this.plz = plz;
        this.ort = ort;
        this.land = land;
        this.lieferbedingung = lieferbedingung;
        this.netto = netto;
        this.liefertermin = liefertermin;
        this.ihrAuftrag = ihrAuftrag;
        this.abweichendeLieferadresse = abweichendeLieferadresse;
        this.verarbeitungskennzeichen = verarbeitungskennzeichen;
        this.kategorie = kategorie;
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

    public LocalDateTime getDatum() {
        return datum;
    }

    public void setDatum(LocalDateTime datum) {
        this.datum = datum;
    }

    public String getAdressnummer() {
        return adressnummer;
    }

    public void setAdressnummer(String adressnummer) {
        this.adressnummer = adressnummer;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getAnzeigename() {
        return anzeigename;
    }

    public void setAnzeigename(String anzeigename) {
        this.anzeigename = anzeigename;
    }

    public String getZusatz() {
        return zusatz;
    }

    public void setZusatz(String zusatz) {
        this.zusatz = zusatz;
    }

    public String getZusatz2() {
        return zusatz2;
    }

    public void setZusatz2(String zusatz2) {
        this.zusatz2 = zusatz2;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }

    public String getLieferbedingung() {
        return lieferbedingung;
    }

    public void setLieferbedingung(String lieferbedingung) {
        this.lieferbedingung = lieferbedingung;
    }

    public float getNetto() {
        return netto;
    }

    public void setNetto(float netto) {
        this.netto = netto;
    }

    public LocalDateTime getLiefertermin() {
        return liefertermin;
    }

    public void setLiefertermin(LocalDateTime liefertermin) {
        this.liefertermin = liefertermin;
    }

    public String getIhrAuftrag() {
        return ihrAuftrag;
    }

    public void setIhrAuftrag(String ihrAuftrag) {
        this.ihrAuftrag = ihrAuftrag;
    }

    public Adress getAbweichendeLieferadresse() {
        return abweichendeLieferadresse;
    }

    public void setAbweichendeLieferadresse(Adress abweichendeLieferadresse) {
        this.abweichendeLieferadresse = abweichendeLieferadresse;
    }

    public Verarbeitungskennzeichen getVerarbeitungskennzeichen() {
        return verarbeitungskennzeichen;
    }

    public void setVerarbeitungskennzeichen(Verarbeitungskennzeichen verarbeitungskennzeichen) {
        this.verarbeitungskennzeichen = verarbeitungskennzeichen;
    }

    public Kategorie getKategorie() {
        return kategorie;
    }

    public void setKategorie(Kategorie kategorie) {
        this.kategorie = kategorie;
    }
}
