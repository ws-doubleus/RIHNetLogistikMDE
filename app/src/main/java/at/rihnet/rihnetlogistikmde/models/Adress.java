package at.rihnet.rihnetlogistikmde.models;

import java.io.Serializable;

public class Adress implements Serializable {
    private int adresse;
    private String adresstyp;
    private String firma;
    private String vorname;
    private String name;
    private String anzeigename;
    private String zusatz;
    private String zusatz2;
    private String strasse;
    private String plz;
    private String ort;
    private String land;

    public Adress(int adresse, String adresstyp, String firma, String vorname, String name, String anzeigename, String zusatz, String zusatz2, String strasse, String plz, String ort, String land) {
        this.adresse = adresse;
        this.adresstyp = adresstyp;
        this.firma = firma;
        this.vorname = vorname;
        this.name = name;
        this.anzeigename = anzeigename;
        this.zusatz = zusatz;
        this.zusatz2 = zusatz2;
        this.strasse = strasse;
        this.plz = plz;
        this.ort = ort;
        this.land = land;
    }

    public int getAdresse() {
        return adresse;
    }

    public void setAdresse(int adresse) {
        this.adresse = adresse;
    }

    public String getAdresstyp() {
        return adresstyp;
    }

    public void setAdresstyp(String adresstyp) {
        this.adresstyp = adresstyp;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
