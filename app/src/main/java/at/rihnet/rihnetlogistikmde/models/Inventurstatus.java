package at.rihnet.rihnetlogistikmde.models;

import androidx.annotation.NonNull;

public enum Inventurstatus {
    ANGELEGT("Angelegt"),
    BEGONNEN("Begonnen"),
    ABGESCHLOSSEN("Abgeschlossen");

    private final String value;

    Inventurstatus(String value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return this.value;
    }
}
