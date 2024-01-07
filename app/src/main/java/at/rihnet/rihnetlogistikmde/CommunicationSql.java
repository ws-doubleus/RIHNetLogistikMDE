package at.rihnet.rihnetlogistikmde;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Grund;
import at.rihnet.rihnetlogistikmde.models.LagerLagerplatz;
import at.rihnet.rihnetlogistikmde.models.LagerplatzBestand;
import at.rihnet.rihnetlogistikmde.models.Lagerplatzinfo;
import at.rihnet.rihnetlogistikmde.models.SeriennummerCharge;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.ui.login.data.model.LoggedInUser;

public class CommunicationSql {
    private static final String TAG = "RIHNet";
    public static SqlServerData sqlServerData;
    public static Connection connection;

    public static Connection getConnection() {
        if (connection != null) {
            return connection;
        }
        if (sqlServerData == null) {
            return null;
        }
        String classes = "net.sourceforge.jtds.jdbc.Driver";
        String url = "jdbc:jtds:sqlserver://" + sqlServerData.getIpadresse() + ":" + sqlServerData.getPort() + "/" + sqlServerData.getDatenbank() + ";instance=" + sqlServerData.getInstance() + ";";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName(classes);
            connection = DriverManager.getConnection(url, sqlServerData.getBenutzername(), sqlServerData.getKennwort());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            connection = null;
        }
        return connection;
    }

    public static LoggedInUser getMitarbw(String benutzer, String kennwort) {
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT Benutzer, " +
                        "ISNULL(_APPKENNWORT, '') AS _APPKENNWORT, " +
                        "LTRIM(ISNULL(Vorname, '') + ' ' + ISNULL(Name, '')) AS Anzeigename " +
                        "FROM MITARBW " +
                        "WHERE UPPER(Benutzer) = '" + benutzer + "' " +
                        "AND _CANUSEAPP = 1 " +
                        "AND _APPKENNWORT = '" + kennwort + "'";
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    //DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", new Locale("de"));
                    return new LoggedInUser(
                            resultSet.getString("Benutzer"),
                            resultSet.getString("Anzeigename"),
                            resultSet.getString("_APPKENNWORT")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }

    public static Artikel getArtikel(String search, String standort) {
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT rs.Search, " +
                        "rs.Artikelnummer, " +
                        "rs.Bezeichnung, " +
                        "rs.SerieCharge, " +
                        "rs.SN, " +
                        "ISNULL(a.Zusatz, '') AS Zusatz, " +
                        "ISNULL(a.HstArtikelnummer, '') AS HstArtikelnummer, " +
                        "ISNULL(a.EANNummer, '') AS EANNummer, " +
                        "SUM(lb.Bestand) AS Bestand " +
                        "FROM dbo.RI_MDE_Search rs " +
                        "LEFT JOIN ART a ON rs.Artikelnummer = a.Artikelnummer " +
                        "LEFT JOIN LAGERBESTAND lb ON a.Artikelnummer = lb.Artikelnummer " +
                        "LEFT JOIN LAGERPLATZ lp ON lb.LagerplatzId = lp.Id " +
                        "LEFT JOIN LAGER l ON lp.Lager = l.Lager " +
                        "LEFT JOIN SERIE s on s.Id = lb.SerieId " +
                        "WHERE rs.Search = '" + search + "' " +
                        "AND l.Standort = '" + standort + "' " +
                        "AND ( " +
                        "ISNULL(s.SerieCharge, '') = CASE " +
                        "WHEN rs.SN IN ('S') " +
                        "THEN rs.Search " +
                        "ELSE '' " +
                        "END " +
                        "OR rs.SerieCharge = 'C' " +
                        ") " +
                        "GROUP BY rs.Search, " +
                        "rs.Artikelnummer, " +
                        "rs.Bezeichnung, " +
                        "rs.SerieCharge, " +
                        "rs.SN, " +
                        "a.Zusatz, " +
                        "a.HstArtikelnummer, " +
                        "a.EANNummer";
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    String sn = resultSet.getString("SN");
                    String seriennummer = "";
                    if (sn.equals("S")) {
                        seriennummer = resultSet.getString("Search");
                    }
                    return new Artikel(
                            resultSet.getString("Artikelnummer"),
                            resultSet.getString("Bezeichnung"),
                            resultSet.getString("Zusatz"),
                            resultSet.getString("SerieCharge"),
                            seriennummer,
                            "",
                            1,
                            resultSet.getInt("Bestand"),
                            "",
                            resultSet.getString("HstArtikelnummer"),
                            resultSet.getString("EANNummer"),
                            "",
                            0,
                            sn
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }

    public static Artikel getArtikel(String search){
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT rs.Search, " +
                        "rs.Artikelnummer, " +
                        "rs.Bezeichnung, " +
                        "rs.SerieCharge, " +
                        "rs.SN, " +
                        "ISNULL(a.Zusatz, '') AS Zusatz, " +
                        "ISNULL(a.HstArtikelnummer, '') AS HstArtikelnummer, " +
                        "ISNULL(a.EANNummer, '') AS EANNummer " +
                        "FROM dbo.RI_MDE_Search rs " +
                        "LEFT JOIN ART a ON rs.Artikelnummer = a.Artikelnummer " +
                        "LEFT JOIN LAGERBESTAND lb ON a.Artikelnummer = lb.Artikelnummer " +
                        "LEFT JOIN LAGERPLATZ lp ON lb.LagerplatzId = lp.Id " +
                        "LEFT JOIN LAGER l ON lp.Lager = l.Lager " +
                        "LEFT JOIN SERIE s ON s.Id = lb.SerieId " +
                        "WHERE rs.Search = '" + search + "' " +
                        "AND ( " +
                        "ISNULL(s.SerieCharge, '') = CASE  " +
                        "WHEN rs.SN IN ('S') " +
                        "THEN rs.Search " +
                        "ELSE '' " +
                        "END " +
                        "OR rs.SerieCharge = 'C' " +
                        ") " +
                        "GROUP BY rs.Search, " +
                        "rs.Artikelnummer, " +
                        "rs.Bezeichnung, " +
                        "rs.SerieCharge, " +
                        "rs.SN, " +
                        "a.Zusatz, " +
                        "a.HstArtikelnummer, " +
                        "a.EANNummer";
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    String sn = resultSet.getString("SN");
                    String seriennummer = "";
                    if (sn.equals("S")) {
                        seriennummer = resultSet.getString("Search");
                    }
                    return new Artikel(
                            resultSet.getString("Artikelnummer"),
                            resultSet.getString("Bezeichnung"),
                            resultSet.getString("Zusatz"),
                            resultSet.getString("SerieCharge"),
                            seriennummer,
                            "",
                            1,
                            0,
                            "",
                            resultSet.getString("HstArtikelnummer"),
                            resultSet.getString("EANNummer"),
                            "",
                            0,
                            sn
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }

    public static List<SeriennummerCharge> getSeriennummerCharge(String artikelnummer, String standort) {
        List<SeriennummerCharge> seriennummern = new ArrayList<>();
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT lb.Artikelnummer, " +
                        "SUM(ISNULL(lb.Bestand, 0)) AS Bestand, " +
                        "ISNULL(s.SerieCharge, '') AS Nummer, " +
                        "ISNULL(a.SerieCharge, '') AS SerieCharge, " +
                        "lp.Lager, " +
                        "s.Verfall " +
                        "FROM LAGERBESTAND lb " +
                        "LEFT JOIN ART a ON lb.Artikelnummer = a.Artikelnummer " +
                        "LEFT JOIN SERIE s ON lb.SerieId = s.Id " +
                        "LEFT JOIN LAGERPLATZ lp ON lb.LagerplatzId = lp.Id " +
                        "LEFT JOIN LAGER l ON lp.Lager = l.Lager " +
                        "WHERE Bestand > 0 " +
                        "AND lb.Artikelnummer = '" + artikelnummer + "' " +
                        "AND l.Standort = '" + standort + "' " +
                        "GROUP BY lb.Artikelnummer, " +
                        "ISNULL(s.SerieCharge, ''), " +
                        "ISNULL(a.SerieCharge, ''), " +
                        "lp.Lager, " +
                        "s.Verfall " +
                        "ORDER BY ISNULL(a.SerieCharge, '')";
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    seriennummern.add(new SeriennummerCharge(
                            artikelnummer,
                            resultSet.getInt("Bestand"),
                            resultSet.getString("Nummer"),
                            resultSet.getString("SerieCharge"),
                            resultSet.getString("Lager")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return seriennummern;
    }

    public static List<String> getZiellager(String quellager, String standort) {
        List<String> ziellager = new ArrayList<>();
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT Lager " +
                        "FROM Lager " +
                        "WHERE Standort = '" + standort + "' " +
                        "ORDER BY LEN(Lager), " +
                        "Lager";
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    ziellager.add(resultSet.getString("Lager"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return ziellager;
    }

    public static List<LagerplatzBestand> getLagerplatzBestand(String artikelnummer, String lager, String serieCharge) {
        List<LagerplatzBestand> lb = new ArrayList<>();
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT lp.Lager, " +
                        "lp.Id, " +
                        "ISNULL(_EAN, '') AS _EAN, " +
                        "SUM(lb.Bestand) AS Bestand, " +
                        "CASE " +
                        "WHEN ISNULL(lp.Bezeichnung, '') = '' " +
                        "THEN CONVERT(nvarchar(80), lp.Id) " +
                        "ELSE lp.Bezeichnung " +
                        "END AS Bezeichnung " +
                        "FROM LAGERBESTAND lb " +
                        "LEFT JOIN LAGERPLATZ lp ON lb.LagerplatzId = lp.Id " +
                        "LEFT JOIN SERIE s on lb.SerieId=s.Id " +
                        "WHERE lb.Artikelnummer = '" + artikelnummer + "' " +
                        "AND lp.Lager = '" + lager + "' " +
                        "AND lb.Bestand > 0 " +
                        "AND ISNULL(s.SerieCharge,'') = '" + serieCharge + "'" +
                        "GROUP BY lp.Lager, " +
                        "lp.Id, " +
                        "_EAN, " +
                        "lp.Bezeichnung " +
                        "ORDER BY lp.Bezeichnung";
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    lb.add(new LagerplatzBestand(
                            resultSet.getString("Lager"),
                            resultSet.getInt("Id"),
                            resultSet.getString("Bezeichnung"),
                            resultSet.getString("_EAN"),
                            resultSet.getInt("Bestand")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return lb;
    }

    public static LagerplatzBestand getLagerplatzBestandByEan(String artikelnummer, String ean) {
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT lp.Lager, " +
                        "lp.Id, " +
                        "ISNULL(_EAN, '') AS _EAN, " +
                        "SUM(lb.Bestand) AS Bestand, " +
                        "CASE " +
                        "WHEN ISNULL(lp.Bezeichnung, '') = '' " +
                        "THEN CONVERT(nvarchar(80), lp.Id) " +
                        "ELSE lp.Bezeichnung " +
                        "END AS Bezeichnung " +
                        "FROM LAGERBESTAND lb " +
                        "LEFT JOIN LAGERPLATZ lp ON lb.LagerplatzId = lp.Id " +
                        "WHERE lb.Artikelnummer = '" + artikelnummer + "' " +
                        "AND lb.Bestand > 0 " +
                        "AND _EAN = '" + ean + "' " +
                        "GROUP BY lp.Lager, " +
                        "lp.Id, " +
                        "_EAN, " +
                        "lp.Bezeichnung " +
                        "ORDER BY lp.Bezeichnung";
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    return new LagerplatzBestand(
                            resultSet.getString("Lager"),
                            resultSet.getInt("Id"),
                            resultSet.getString("Bezeichnung"),
                            resultSet.getString("_EAN"),
                            resultSet.getInt("Bestand")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }

    public static String getLagerByEan(String ean){
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT Lager " +
                        "FROM LAGERPLATZ " +
                        "WHERE _EAN = '" + ean + "'";
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    return resultSet.getString("Lager");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }

    public static LagerplatzBestand getLagerplatzBestandByStandortEan(String standort, String ean) {
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT lp.Lager, " +
                        "lp.Id, " +
                        "ISNULL(lp._EAN, '') AS _EAN, " +
                        "CASE " +
                        "WHEN ISNULL(lp.Bezeichnung, '') = '' " +
                        "THEN CONVERT(nvarchar(80), lp.Id) " +
                        "ELSE lp.Bezeichnung " +
                        "END AS Bezeichnung " +
                        "FROM LAGERPLATZ lp  " +
                        "LEFT JOIN Lager l ON lp.Lager = l.Lager " +
                        "WHERE l.Standort = '" + standort + "' " +
                        "AND lp._EAN = '" + ean + "' " +
                        "GROUP BY lp.Lager, " +
                        "lp.Id, " +
                        "lp._EAN, " +
                        "lp.Bezeichnung " +
                        "ORDER BY lp.Bezeichnung";
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    return new LagerplatzBestand(
                            resultSet.getString("Lager"),
                            resultSet.getInt("Id"),
                            resultSet.getString("Bezeichnung"),
                            resultSet.getString("_EAN"),
                            0
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }

    public static List<LagerplatzBestand> getLagerplatzBestandByLager(String lager) {
        List<LagerplatzBestand> lb = new ArrayList<>();
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT lp.Lager, " +
                        "lp.Id, " +
                        "ISNULL(_EAN, '') AS _EAN, " +
                        "CASE " +
                        "WHEN ISNULL(lp.Bezeichnung, '') = '' " +
                        "THEN CONVERT(nvarchar(80), lp.Id) " +
                        "ELSE lp.Bezeichnung " +
                        "END AS Bezeichnung " +
                        "FROM LAGERPLATZ lp  " +
                        "WHERE lp.Lager = '" + lager + "' " +
                        "GROUP BY lp.Lager, " +
                        "lp.Id, " +
                        "_EAN, " +
                        "lp.Bezeichnung " +
                        "ORDER BY lp.Bezeichnung";
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    lb.add(new LagerplatzBestand(
                            resultSet.getString("Lager"),
                            resultSet.getInt("Id"),
                            resultSet.getString("Bezeichnung"),
                            resultSet.getString("_EAN"),
                            0
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return lb;
    }

    public static List<Lagerplatzinfo> getLagerplatzArtikelnummerByLagerplatzId(String standort, Integer lagerplatzId) {
        List<Lagerplatzinfo> lp = new ArrayList<>();
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT ISNULL(lb.Artikelnummer, '') AS Artikelnummer, " +
                        "ISNULL(a.Bezeichnung, '') AS Bezeichnung, " +
                        "SUM(lb.Bestand) AS Bestand " +
                        "FROM LAGERBESTAND lb " +
                        "LEFT JOIN LAGERPLATZ lp ON lb.LagerplatzId = lp.Id " +
                        "LEFT JOIN LAGER l ON lp.Lager = l.Lager " +
                        "LEFT JOIN ART a ON lb.Artikelnummer = a.Artikelnummer " +
                        "WHERE lb.LagerplatzId = " + lagerplatzId + " " +
                        "AND lb.Bestand > 0 " +
                        "AND l.Standort = '" + standort + "' " +
                        "GROUP BY " +
                        "lb.Artikelnummer, " +
                        "a.Bezeichnung";
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    lp.add(new Lagerplatzinfo(
                            resultSet.getString("Artikelnummer"),
                            resultSet.getString("Bezeichnung"),
                            resultSet.getInt("Bestand")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return lp;
    }

    public static List<LagerplatzBestand> getLagerByArtikelnummer(String artikelnummer, String standort) {
        List<LagerplatzBestand> lb = new ArrayList<>();
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT lp.Lager, " +
                        "CASE " +
                        "WHEN ISNULL(lp.Bezeichnung, '') = '' " +
                        "THEN CONVERT(NVARCHAR(80), lp.Id) " +
                        "ELSE lp.Bezeichnung " +
                        "END AS Bezeichnung, " +
                        "SUM(lb.Bestand) AS Bestand " +
                        "FROM LAGERBESTAND lb " +
                        "LEFT JOIN LAGERPLATZ lp ON lb.LagerplatzId = lp.Id " +
                        "LEFT JOIN LAGER l ON lp.Lager = l.Lager " +
                        "WHERE lb.Artikelnummer = '" + artikelnummer + "' " +
                        "AND lb.Bestand > 0 " +
                        "AND l.Standort = '" + standort + "' " +
                        "GROUP BY lp.Lager, " +
                        "lp.Bezeichnung, " +
                        "lp.Id " +
                        "ORDER BY lp.Lager";
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    lb.add(new LagerplatzBestand(
                            resultSet.getString("Lager"),
                            0,
                            resultSet.getString("Bezeichnung"),
                            "",
                            resultSet.getInt("Bestand")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return lb;
    }

    public static LagerLagerplatz getLagerByLagerplatzId(String standort, Integer lagerplatzId) {
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT l.Lager, " +
                        "lp.LAGERPLATZ_ID " +
                        "CASE " +
                        "WHEN ISNULL(lp.Bezeichnung, '') = '' " +
                        "THEN CONVERT(nvarchar(80), lp.Id) " +
                        "ELSE lp.Bezeichnung " +
                        "END AS Bezeichnung" +
                        "FROM LAGERPLATZ lp " +
                        "LEFT JOIN LAGER l ON lp.Lager = l.Lager " +
                        "WHERE lp.Id = " + lagerplatzId + " " +
                        "AND l.Standort = '" + standort + "'";
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    return new LagerLagerplatz(
                            resultSet.getString("Lager"),
                            resultSet.getInt("LAGERPLATZ_ID"),
                            resultSet.getString("Bezeichnung")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }

    public static List<Grund> getXLogistikappGruendeByType(String type) {
        List<Grund> g = new ArrayList<>();
        try {
            Connection connection = getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                String query = "SELECT ISNULL(_GRUND, '') AS _GRUND, " +
                        "ISNULL(_TYPE, '') AS _TYPE, " +
                        "ISNULL(_ORDER, 0) AS _ORDER " +
                        "FROM XLOGISTIKAPPGruende " +
                        "WHERE _TYPE IN (" + type + ") " +
                        "ORDER BY ISNULL(_TYPE, ''), " +
                        "ISNULL(_ORDER, 0)";
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    g.add( new Grund(
                            resultSet.getString("_GRUND"),
                            resultSet.getString("_TYPE"),
                            resultSet.getInt("_ORDER")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return g;
    }
}
