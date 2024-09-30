package at.rihnet.rihnetlogistikmde.models;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "beleg")
public class Beleg {
    @PrimaryKey(autoGenerate = true)
    private  Long id;
    private String lieferscheinnummer;
    @Embedded
    private Lieferant lieferant;
    private String lager;
    @Embedded
    private Lagerplatz lagerplatz;

    public Beleg(String lieferscheinnummer, Lieferant lieferant, String lager, Lagerplatz lagerplatz) {
        this.lieferscheinnummer = lieferscheinnummer;
        this.lieferant = lieferant;
        this.lager = lager;
        this.lagerplatz = lagerplatz;
    }

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
}
