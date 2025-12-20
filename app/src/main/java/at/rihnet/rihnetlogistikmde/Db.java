package at.rihnet.rihnetlogistikmde;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import at.rihnet.rihnetlogistikmde.models.SqlServerData;

/**
 * Verwaltet exakt eine geöffnete Verbindung zum SQL‑Server (Singleton).
 * – Call  Db.open(cfg)   einmal zu Beginn (z.B. in Application.onCreate())
 * – Call  Db.close()     bei Logout / App‑Shutdown (z.B. in Application.onTerminate())
 * – Hole die Connection überall mit  Db.conn()
 */
public final class Db {

    private static final String TAG = "RIHNet";
    private static final String DRIVER = "net.sourceforge.jtds.jdbc.Driver";

    /** volatile, damit auch bei Multi‑Thread‑Zugriff korrekt synchronisiert */
    private static volatile Connection conn;

    private Db() {}  // keine Instanz erlaubt

    /**
     * baut (falls nötig) eine neue Verbindung auf und gibt diese zurück
     */
    public static synchronized void open(SqlServerData cfg) throws SQLException, ClassNotFoundException {

        if (conn != null && !conn.isClosed()) {
            return;                 // schon offen
        }

        String url = "jdbc:jtds:sqlserver://" + cfg.getIpadresse() + ":" + cfg.getPort() + "/"
                + cfg.getDatenbank() + ";instance=" + cfg.getInstance()
                + ";useCursors=true;buffered=false;prepareSQL=2;"
                + ";tcpKeepAlive=true";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Class.forName(DRIVER);
        conn = DriverManager.getConnection(url, cfg.getBenutzername(), cfg.getKennwort());
        Log.i(TAG, "SQL‑Verbindung aufgebaut");

    }

    /** liefert die bereits geöffnete Verbindung, wirft sonst IllegalStateException */
    public static Connection conn() {
        if (conn == null) {
            throw new IllegalStateException("Db.open(...) zuerst aufrufen!");
        }
        return conn;
    }

    /** sauber schließen – sollte in onTerminate() oder beim Logout aufgerufen werden */
    public static synchronized void close() {
        if (conn != null) {
            try {
                conn.close();
                Log.i(TAG, "SQL‑Verbindung geschlossen");
            } catch (SQLException e) {
                Log.e(TAG, "Fehler beim Schließen: " + e.getMessage());
            } finally {
                conn = null;
            }
        }
    }
}
