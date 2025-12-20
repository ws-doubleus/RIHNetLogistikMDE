package at.rihnet.rihnetlogistikmde.models;

import androidx.annotation.NonNull;

/**
 * Repräsentiert einen einzelnen Scan-Vorgang (z.B. Barcode).
 * Hier kann man noch mehr Metadaten ergänzen (Zeitpunkt, Gerät, User, etc.).
 */
public class ScanJob {

    private final String barcode;
    private final String belegnummer;
    private final String standort;

    public ScanJob(String barcode, String belegnummer, String standort) {
        this.barcode = barcode;
        this.belegnummer = belegnummer;
        this.standort = standort;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getBelegnummer() {
        return belegnummer;
    }

    public String getStandort() {
        return standort;
    }

    @NonNull
    @Override
    public String toString() {
        return "ScanJob{" +
                "barcode='" + barcode + '\'' +
                ", belegnummer='" + belegnummer + '\'' +
                ", standort='" + standort + '\'' +
                '}';
    }
}
