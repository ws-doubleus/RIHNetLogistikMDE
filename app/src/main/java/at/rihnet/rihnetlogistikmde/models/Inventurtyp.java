package at.rihnet.rihnetlogistikmde.models;

public enum Inventurtyp {
    VOLLSTAENDIG_ALLE_ARTIKEL_IN_ALLEN_LAEGERN,
    AUSGEWAEHLTE_ARTIKEL_IN_ALLEN_LAEGERN,
    ALLE_ARTIKEL_IN_AUSGEWAEHLTEN_LAEGERN,
    AUSGEWAEHLTE_ARTIKEL_IN_AUSGEWAEHLTEN_LAEGERN;

    public String beschreibung() {
        switch (this) {
            case VOLLSTAENDIG_ALLE_ARTIKEL_IN_ALLEN_LAEGERN:
                return "Vollständig (Alle Artikel in allen Lägern)";
            case AUSGEWAEHLTE_ARTIKEL_IN_ALLEN_LAEGERN:
                return "Ausgewählte Artikel in allen Lägern";
            case ALLE_ARTIKEL_IN_AUSGEWAEHLTEN_LAEGERN:
                return "Alle Artikel in ausgewählten Lägern";
            case AUSGEWAEHLTE_ARTIKEL_IN_AUSGEWAEHLTEN_LAEGERN:
                return  "Ausgewählte Artikel in ausgewählten Lägern";
            default:
                return "Unbekannter Inventurtyp";
        }
    }
}
