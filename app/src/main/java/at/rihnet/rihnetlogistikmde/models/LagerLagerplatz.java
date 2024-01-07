package at.rihnet.rihnetlogistikmde.models;

public class LagerLagerplatz {
    private String lager;
    private int lagerplatzId;
    private String bezeichnung;

    public LagerLagerplatz(String lager, int lagerplatzId, String bezeichnung) {
        this.lager = lager;
        this.lagerplatzId = lagerplatzId;
        this.bezeichnung = bezeichnung;
    }

    public String getLager() {
        return lager;
    }

    public void setLager(String lager) {
        this.lager = lager;
    }

    public int getLagerplatzId() {
        return lagerplatzId;
    }

    public void setLagerplatzId(int lagerplatzId) {
        this.lagerplatzId = lagerplatzId;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }
}
