package at.rihnet.rihnetlogistikmde.models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Verarbeitungskennzeichen implements Serializable {
    private int id;
    private int anzahlLieferscheine;
    private int anzahlRechnungen;
    private boolean autoRechnung;

    public Verarbeitungskennzeichen(int id, int anzahlLieferscheine, int anzahlRechnungen, boolean autoRechnung) {
        this.id = id;
        this.anzahlLieferscheine = anzahlLieferscheine;
        this.anzahlRechnungen = anzahlRechnungen;
        this.autoRechnung = autoRechnung;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAnzahlLieferscheine() {
        return anzahlLieferscheine;
    }

    public void setAnzahlLieferscheine(int anzahlLieferscheine) {
        this.anzahlLieferscheine = anzahlLieferscheine;
    }

    public int getAnzahlRechnungen() {
        return anzahlRechnungen;
    }

    public void setAnzahlRechnungen(int anzahlRechnungen) {
        this.anzahlRechnungen = anzahlRechnungen;
    }

    public boolean isAutoRechnung() {
        return autoRechnung;
    }

    public void setAutoRechnung(boolean autoRechnung) {
        this.autoRechnung = autoRechnung;
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
