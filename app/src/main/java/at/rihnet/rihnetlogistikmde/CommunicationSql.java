package at.rihnet.rihnetlogistikmde;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Grund;
import at.rihnet.rihnetlogistikmde.models.Invbasis;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Lagerplatz;
import at.rihnet.rihnetlogistikmde.models.LagerplatzBestand;
import at.rihnet.rihnetlogistikmde.models.Lagerplatzinfo;
import at.rihnet.rihnetlogistikmde.models.Lieferant;
import at.rihnet.rihnetlogistikmde.models.SeriennummerCharge;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;

public class CommunicationSql {
    private static final String TAG = "RIHNet";

    public static Connection getConnection(SqlServerData sqlServerData) throws Exception {
        Connection connection;
        String classes = "net.sourceforge.jtds.jdbc.Driver";
        String url = "jdbc:jtds:sqlserver://" + sqlServerData.getIpadresse() + ":" + sqlServerData.getPort() + "/" + sqlServerData.getDatenbank() + ";instance=" + sqlServerData.getInstance() + ";";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName(classes);
            connection = DriverManager.getConnection(url, sqlServerData.getBenutzername(), sqlServerData.getKennwort());
        } catch (ClassNotFoundException | SQLException e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
            //connection = null;
            throw e;
        }
        return connection;
    }

    /*public static LoggedInUser getMitarbw(SqlServerData sqlServerData, String benutzer, String kennwort) {
        try {
            Connection connection = getConnection(sqlServerData);
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
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }*/

    public static Artikel getArtikel(SqlServerData sqlServerData, String search, String standort) {
        try (Connection connection = getConnection(sqlServerData)) {
            String query = "SELECT rs.Search, " +
                    "rs.Artikelnummer, " +
                    "ISNULL(rs.Bezeichnung, '') AS Bezeichnung, " +
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
                    "WHERE rs.Search = ? " +
                    "AND l.Standort = ? " +
                    "GROUP BY rs.Search, " +
                    "rs.Artikelnummer, " +
                    "rs.Bezeichnung, " +
                    "rs.SerieCharge, " +
                    "rs.SN, " +
                    "a.Zusatz, " +
                    "a.HstArtikelnummer, " +
                    "a.EANNummer";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, search);
                preparedStatement.setString(2, standort);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
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
                                sn,
                                Kategorie.UMLAGERUNG
                        );
                    }
                }
            }


            /*
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
                        sn,
                        Kategorie.UMLAGERUNG
                );
            }
            */
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return null;
    }

    public static Artikel getArtikelWE(SqlServerData sqlServerData, String search, String standort) {
        try (Connection connection = getConnection(sqlServerData)) {
            String query = "SELECT rs.Search, " +
                    "rs.Artikelnummer, " +
                    "ISNULL(rs.Bezeichnung, '') AS Bezeichnung, " +
                    "rs.SerieCharge, " +
                    "rs.SN, " +
                    "ISNULL(a.Zusatz, '') AS Zusatz, " +
                    "ISNULL(a.HstArtikelnummer, '') AS HstArtikelnummer, " +
                    "ISNULL(a.EANNummer, '') AS EANNummer, " +
                    "ISNULL(SUM(lb.Bestand), 0) AS Bestand " +
                    "FROM dbo.RI_MDE_Search rs " +
                    "LEFT JOIN ART a ON rs.Artikelnummer = a.Artikelnummer " +
                    "LEFT JOIN LAGERBESTAND lb ON a.Artikelnummer = lb.Artikelnummer " +
                    "LEFT JOIN LAGERPLATZ lp ON lb.LagerplatzId = lp.Id " +
                    "LEFT JOIN LAGER l ON lp.Lager = l.Lager " +
                    "LEFT JOIN SERIE s on s.Id = lb.SerieId " +
                    "WHERE rs.Search = ? " +
                    "GROUP BY rs.Search, " +
                    "rs.Artikelnummer, " +
                    "rs.Bezeichnung, " +
                    "rs.SerieCharge, " +
                    "rs.SN, " +
                    "a.Zusatz, " +
                    "a.HstArtikelnummer, " +
                    "a.EANNummer";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, search);
                preparedStatement.setString(2, standort);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
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
                                sn,
                                Kategorie.UMLAGERUNG
                        );
                    }
                }
            }


            /*
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
                        sn,
                        Kategorie.UMLAGERUNG
                );
            }
            */
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return null;
    }

    public static Artikel getArtikel(SqlServerData sqlServerData, String search) {
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
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
                        sn,
                        Kategorie.UMLAGERUNG
                );
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return null;
    }

    public static List<SeriennummerCharge> getSeriennummerCharge(SqlServerData sqlServerData, String artikelnummer, String standort) {
        List<SeriennummerCharge> seriennummern = new ArrayList<>();
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
            String query = "SELECT lb.Artikelnummer, " +
                    "SUM(ISNULL(lb.Bestand, 0)) AS Bestand, " +
                    "ISNULL(s.SerieCharge, '') AS Nummer, " +
                    "ISNULL(a.SerieCharge, '') AS SerieCharge, " +
                    "lp.Lager " +
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
                    "lp.Lager " +
                    "ORDER BY ISNULL(a.SerieCharge, '')";
            Log.e(TAG, "query: " + query);
            ResultSet resultSet = statement.executeQuery(query);
            Log.e(TAG, "a");
            while (resultSet.next()) {
                seriennummern.add(new SeriennummerCharge(
                        artikelnummer,
                        resultSet.getInt("Bestand"),
                        resultSet.getString("Nummer"),
                        resultSet.getString("SerieCharge"),
                        resultSet.getString("Lager")
                ));
            }
            Log.e(TAG, "b");
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return seriennummern;
    }

    public static List<String> getZiellager(SqlServerData sqlServerData, String standort) {
        List<String> ziellager = new ArrayList<>();
        try (Connection connection = getConnection(sqlServerData)) {
            String query = "SELECT Lager " +
                    "FROM Lager " +
                    "WHERE Standort = ? " +
                    "ORDER BY LEN(Lager), " +
                    "Lager";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, standort);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        ziellager.add(resultSet.getString("Lager"));
                    }
                }
            }


            //ResultSet resultSet = statement.executeQuery(query);
            //while (resultSet.next()) {
            //    ziellager.add(resultSet.getString("Lager"));
            //}
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return ziellager;
    }

    public static List<LagerplatzBestand> getLagerplatzBestand(SqlServerData sqlServerData, String artikelnummer, String lager, String serieCharge) {
        List<LagerplatzBestand> lb = new ArrayList<>();
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
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
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return lb;
    }

    public static LagerplatzBestand getLagerplatzBestandByEan(SqlServerData sqlServerData, String artikelnummer, String ean) {
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
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
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return null;
    }

    public static String getLagerByEan(SqlServerData sqlServerData, String ean) {
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
            String query = "SELECT Lager " +
                    "FROM LAGERPLATZ " +
                    "WHERE _EAN = '" + ean + "'";
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getString("Lager");
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return null;
    }

    public static LagerplatzBestand getLagerplatzBestandByStandortEan(SqlServerData sqlServerData, String standort, String ean) {
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
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

        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return null;
    }

    public static List<Lagerplatz> getLagerplatzByLager(SqlServerData sqlServerData, String lager) {
        List<Lagerplatz> lb = new ArrayList<>();
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
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
                lb.add(new Lagerplatz(
                        resultSet.getString("Lager"),
                        resultSet.getInt("Id"),
                        resultSet.getString("Bezeichnung"),
                        resultSet.getString("_EAN")
                ));
            }

        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return lb;
    }

    public static List<Lagerplatzinfo> getLagerplatzArtikelnummerByLagerplatzId(SqlServerData sqlServerData, String standort, Integer lagerplatzId) {
        List<Lagerplatzinfo> lp = new ArrayList<>();
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
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
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return lp;
    }

    public static List<LagerplatzBestand> getLagerByArtikelnummer(SqlServerData sqlServerData, String artikelnummer, String standort) {
        List<LagerplatzBestand> lb = new ArrayList<>();
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
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
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return lb;
    }

    public static List<Grund> getXLogistikappGruendeByType(SqlServerData sqlServerData, String type) {
        List<Grund> g = new ArrayList<>();
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
            String query = "SELECT ISNULL(_GRUND, '') AS _GRUND, " +
                    "ISNULL(_TYPE, '') AS _TYPE, " +
                    "ISNULL(_ORDER, 0) AS _ORDER " +
                    "FROM XLOGISTIKAPPGruende " +
                    "WHERE _TYPE IN (" + type + ") " +
                    "ORDER BY ISNULL(_TYPE, ''), " +
                    "ISNULL(_ORDER, 0)";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                g.add(new Grund(
                        resultSet.getString("_GRUND"),
                        resultSet.getString("_TYPE"),
                        resultSet.getInt("_ORDER")
                ));
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return g;
    }

    public static List<Lieferant> getLieferanten(SqlServerData sqlServerData) {
        List<Lieferant> lieferanten = new ArrayList<>();
        try (Connection connection = getConnection(sqlServerData)) {
            String query = "SELECT ISNULL(Anzeigename, '') AS Anzeigename, " +
                    "Nummer " +
                    "FROM Liefer " +
                    "WHERE ISNULL(Inaktiv, 0) = 0 " +
                    "AND ISNULL(Anzeigename, '') <> '' " +
                    "AND ISNULL(_ISVISIBLEINMDE, 0) = 1 " +
                    "ORDER BY ISNULL(Anzeigename, '')";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        lieferanten.add(new Lieferant(
                                resultSet.getString("Nummer"),
                                resultSet.getString("Anzeigename")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return lieferanten;
    }

    public static String getStandortBezeichnungByStandort(SqlServerData sqlServerData, String standort) {
        try (Connection connection = getConnection(sqlServerData)) {
            String query = "SELECT Standort, " +
                    "ISNULL(Bezeichnung, '') AS Bezeichnung " +
                    "FROM STANDORTW " +
                    "WHERE Standort = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, standort);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("Bezeichnung");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return null;
    }

    public static List<Invbasis> getInvbasisByBelegnummerKennzeichen(SqlServerData sqlServerData, String belegnummer, String kennzeichen) {
        List<Invbasis> invbases = new ArrayList<>();
        try (Connection connection = getConnection(sqlServerData)) {
            String query = "SELECT ISNULL(Belegnummer, '') AS Belegnummer, " +
                    "ISNULL(Nummer, '') AS Nummer, " +
                    "ISNULL(Bezeichnung, '') AS Bezeichnung " +
                    "FROM INVBASIS " +
                    "WHERE Belegnummer = ? " +
                    "AND Kennzeichen = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, belegnummer);
                preparedStatement.setString(2, kennzeichen);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        invbases.add(new Invbasis(
                                resultSet.getString("Belegnummer"),
                                resultSet.getString("Nummer"),
                                resultSet.getString("Bezeichnung")
                        ));
                    }
                }
            }
//            ResultSet resultSet = statement.executeQuery(query);
//            while (resultSet.next()) {
//                invbases.add(new Invbasis(
//                        resultSet.getString("Belegnummer"),
//                        resultSet.getString("Nummer"),
//                        resultSet.getString("Bezeichnung")
//                ));
//            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return invbases;
    }

    public static boolean ExistsSerieCharge(SqlServerData sqlServerData, String serieCharge, String artikelnummer) {
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
            String query = "SELECT COUNT(*) AS Anzahl " +
                    "FROM SERIE " +
                    "WHERE SerieCharge = '" + serieCharge + "' " +
                    "AND Artikelnummer = '" + artikelnummer + "'";
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getInt("Anzahl") > 0;
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return false;
    }

    public static boolean ExistsInventur(SqlServerData sqlServerData, String belegnummer, String serieCharge) {
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
            String query = "SELECT COUNT(*) AS Anzahl " +
                    "FROM INVEN " +
                    "WHERE Belegnummer = '" + belegnummer + "' " +
                    "AND SerienNr = '" + serieCharge + "' " +
                    "AND Ist = 1";
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getInt("Anzahl") > 0;
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return true;
    }

    public static int updateBelegFreierText1ByBelegtypBelegnummer(SqlServerData sqlServerData, String belegtyp, String belegnummer, String freierText1) {
        try (Connection connection = getConnection(sqlServerData); Statement statement = connection.createStatement()) {
            String query = "UPDATE BELEG " +
                    "SET FreierText1 = '" + freierText1 + "' " +
                    "WHERE Belegtyp = '" + belegtyp + "' " +
                    "AND Belegnummer = '" + belegnummer + "'";
            return statement.executeUpdate(query);
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return 0;
    }
}
