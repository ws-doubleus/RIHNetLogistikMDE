package at.rihnet.rihnetlogistikmde.ui.login.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private final String benutzer;
    private final String kennwort;
    private final String anzeigename;

    public LoggedInUser(String benutzer, String anzeigename, String kennwort) {
        this.benutzer = benutzer;
        this.anzeigename = anzeigename;
        this.kennwort = kennwort;
    }

    public String getBenutzer() {
        return benutzer;
    }

    public String getKennwort() {
        return kennwort;
    }

    public String getAnzeigename() {
        return anzeigename;
    }
}