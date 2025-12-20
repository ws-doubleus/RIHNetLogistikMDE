package at.rihnet.rihnetlogistikmde.ui.inventur;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.List;

import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Invbasis;
import at.rihnet.rihnetlogistikmde.models.Paketanbindung;
import at.rihnet.rihnetlogistikmde.models.SelectLine.Inventory;
import at.rihnet.rihnetlogistikmde.models.SelectLine.InventoryArticleEdit;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;

/**
 * Repository-Schicht:
 * <p>
 * - Kapselt ALLE direkten DB-/API-Aufrufe (CommunicationSql, CommunicationSelectLine, etc.).
 * - Activity/Fragment/Scanner kennen NUR noch das Repository, nicht die SQL-Details.
 * - Erleichtert Testing und Austausch der Datenquelle.
 * <p>
 * WICHTIG:
 * - Alle Methoden hier SOLLTEN im Hintergrund-Thread aufgerufen werden
 * (z.B. über AsyncTaskExecutorService oder ScanQueueProcessor).
 */
public class InventurRepository {

    private static final String TAG = "InventurRepository";

    private final SqlServerData sqlServerData;

    public InventurRepository(SqlServerData sqlServerData) {
        this.sqlServerData = sqlServerData;
    }




    public boolean insertPaketanbindung(Paketanbindung paketanbindung) {
        try {
            int rows = CommunicationSql.insertPaketanbindung(paketanbindung);
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "insertPaketanbindung Fehler: " + e.getMessage(), e);
            return false;
        }
    }


    public boolean existsInventur(String belegnummer, String seriennummer) {
        try {
            return CommunicationSql.existsInventur(belegnummer, seriennummer);
        } catch (Exception e) {
            Log.e(TAG, "existsInventur Fehler: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Beispiel: Seriell/Charge prüfen.
     */
    public boolean existsSerieCharge(String serieCharge, String artikelnummer) {
        try {
            return CommunicationSql.existsSerieCharge(sqlServerData, serieCharge, artikelnummer);
        } catch (Exception e) {
            Log.e(TAG, "existsSerieCharge Fehler: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Beispiel: Invbasis-Liste bereitstellen (z.B. um Belege für eine Liste zu laden).
     */
    @Nullable
    public List<Invbasis> getInvbasisByBelegnummerKennzeichen(String belegnummer, String kennzeichen) {
        try {
            return CommunicationSql.getInvbasisByBelegnummerKennzeichen(
                    sqlServerData,
                    belegnummer,
                    kennzeichen
            );
        } catch (Exception e) {
            Log.e(TAG, "getInvbasisByBelegnummerKennzeichen Fehler: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Führt die eigentliche Inventur-Erfassung über die SelectLine-API aus.
     *
     * KEINE UI hier (keine Toasts, keine Sounds, kein SQLite)!
     * Nur Business-Logik + API/SQL.
     *
     * @return true = Erfassung erfolgreich,
     *         false = Erfassung nicht durchgeführt (z.B. Artikel nicht erlaubt oder Seriennummer ungültig)
     */
    public boolean bookInventoryArticle(
            Inventory inventory,
            Artikel artikel,
            double menge,
            String lager,
            String seriennummer,
            String appKey,
            String baseAddress,
            String userName,
            String password
    ) {
        try {
            // 1) Prüfen, ob der Artikel in dieser Inventur erlaubt ist
            if (inventory.getKindFlag() == 1 || inventory.getKindFlag() == 3) {
                List<Invbasis> invbasisList = CommunicationSql.getInvbasisByBelegnummerKennzeichen(
                        sqlServerData,
                        inventory.getNumber(),
                        "A"
                );
                Invbasis result = invbasisList.stream()
                        .filter(invbasis -> invbasis.getNummer().equals(artikel.getArtikelnummer()))
                        .findFirst()
                        .orElse(null);

                if (result == null) {
                    // Artikel nicht erlaubt
                    Log.w(TAG, "Artikel " + artikel.getArtikelnummer() + " ist in dieser Inventur nicht erlaubt.");
                    return false;
                }
            }


            //TODO 2) Login bei SelectLine
//            if (!CommunicationSelectLine.login(appKey, baseAddress, userName, password)) {
//                Log.e(TAG, "SelectLine-Login fehlgeschlagen.");
//                return false;
//            }

            // 3) InventoryArticleEdit aufbauen
            InventoryArticleEdit inventoryArticleEdit = new InventoryArticleEdit();
            inventoryArticleEdit.setQuantity(menge);

            // Serien-/Chargenprüfung, falls nötig
            if ("S".equals(artikel.getSerieCharge()) && seriennummer != null && !seriennummer.isEmpty()) {
                boolean res0 = CommunicationSql.existsSerieCharge(
                        sqlServerData,
                        seriennummer,
                        artikel.getArtikelnummer()
                );
                if (res0) {
                    boolean res1 = CommunicationSql.existsInventur(
                            inventory.getNumber(),
                            seriennummer
                    );
                    if (res1) {
                        // Artikel mit dieser Seriennummer schon erfasst
                        Log.w(TAG, "Seriennummer bereits erfasst: " + seriennummer);
                        return false;
                    } else {
                        inventoryArticleEdit.setSerialnumber(seriennummer);
                    }
                } else {
                    // Seriennummer ungültig
                    Log.w(TAG, "Seriennummer ungültig: " + seriennummer);
                    return false;
                }
            }

            // 4) API-Aufruf zur Erhöhung der Menge
            boolean ok = CommunicationSelectLine.updateInventoryRaiseArticleQuantity(
                    inventory.getNumber(),
                    lager,
                    artikel.getArtikelnummer(),
                    inventoryArticleEdit
            );

            if (!ok) {
                Log.e(TAG, "updateInventoryRaiseArticleQuantity fehlgeschlagen.");
            }

            return ok;

        } catch (Exception e) {
            Log.e(TAG, "bookInventoryArticle Fehler: " + e.getMessage(), e);
            return false;
        }
    }

}
