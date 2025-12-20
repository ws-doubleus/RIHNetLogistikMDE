package at.rihnet.rihnetlogistikmde.models;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(tableName = "beleg")
public class Beleg implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private  Long id;
    private String lieferscheinnummer;
    @Embedded
    private Lieferant lieferant;
    private String lager;
    @Embedded
    private Lagerplatz lagerplatz;
    @Ignore
    private LocalDateTime datum;
    private String belegnummer;
    private String belegtyp;
    private Kategorie kategorie;

    public Beleg(String lieferscheinnummer, String belegtyp, String belegnummer, Lieferant lieferant, String lager, Lagerplatz lagerplatz, Kategorie kategorie) {
        this.lieferscheinnummer = lieferscheinnummer;
        this.belegtyp = belegtyp;
        this.belegnummer = belegnummer;
        this.lieferant = lieferant;
        this.lager = lager;
        this.lagerplatz = lagerplatz;
        this.kategorie = kategorie;
    }

//    @Ignore
//    public Beleg(String belegnummer, Lieferant lieferant, String lieferscheinnummer, Kategorie kategorie){
//        this.belegnummer = belegnummer;
//        this.lieferant = lieferant;
//        this.lieferscheinnummer = lieferscheinnummer;
//        this.kategorie = kategorie;
//    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLieferscheinnummer() {
        return lieferscheinnummer;
    }

    public void setLieferscheinnummer(String lieferscheinnummer) {
        this.lieferscheinnummer = lieferscheinnummer;
    }

    public Lieferant getLieferant() {
        return lieferant;
    }

    public void setLieferant(Lieferant lieferant) {
        this.lieferant = lieferant;
    }

    public String getLager() {
        return lager;
    }

    public void setLager(String lager) {
        this.lager = lager;
    }

    public Lagerplatz getLagerplatz() {
        return lagerplatz;
    }

    public void setLagerplatz(Lagerplatz lagerplatz) {
        this.lagerplatz = lagerplatz;
    }

    public LocalDateTime getDatum() {
        return datum;
    }

    public void setDatum(LocalDateTime datum) {
        this.datum = datum;
    }

    public String getBelegnummer() {
        return belegnummer;
    }

    public void setBelegnummer(String belegnummer) {
        this.belegnummer = belegnummer;
    }

    public String getBelegtyp() {
        return belegtyp;
    }

    public void setBelegtyp(String belegtyp) {
        this.belegtyp = belegtyp;
    }

    public Kategorie getKategorie() {
        return kategorie;
    }

    public void setKategorie(Kategorie kategorie) {
        this.kategorie = kategorie;
    }

    @NonNull
    @Override
    public String toString() {
        return belegnummer;
    }
}
