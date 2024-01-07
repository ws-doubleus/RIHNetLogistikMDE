package at.rihnet.rihnetlogistikmde.models;

public class SqlServerData {
    private String ipadresse;
    private String port;
    private String datenbank;
    private String instance;
    private String benutzername;
    private String kennwort;

    public SqlServerData(String ipadresse, String port, String datenbank, String instance, String benutzername, String kennwort) {
        this.ipadresse = ipadresse;
        this.port = port;
        this.datenbank = datenbank;
        this.instance = instance;
        this.benutzername = benutzername;
        this.kennwort = kennwort;
    }

    public String getIpadresse() {
        return ipadresse;
    }

    public void setIpadresse(String ipadresse) {
        this.ipadresse = ipadresse;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDatenbank() {
        return datenbank;
    }

    public void setDatenbank(String datenbank) {
        this.datenbank = datenbank;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getBenutzername() {
        return benutzername;
    }

    public void setBenutzername(String benutzername) {
        this.benutzername = benutzername;
    }

    public String getKennwort() {
        return kennwort;
    }

    public void setKennwort(String kennwort) {
        this.kennwort = kennwort;
    }
}
