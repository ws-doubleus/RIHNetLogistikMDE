
package at.rihnet.rihnetlogistikmde;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.models.Adress;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Beleg;
import at.rihnet.rihnetlogistikmde.models.BelegInfo;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Grund;
import at.rihnet.rihnetlogistikmde.models.Invbasis;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Lagerplatz;
import at.rihnet.rihnetlogistikmde.models.LagerplatzBestand;
import at.rihnet.rihnetlogistikmde.models.Lagerplatzinfo;
import at.rihnet.rihnetlogistikmde.models.Lieferant;
import at.rihnet.rihnetlogistikmde.models.Lieferbedingung;
import at.rihnet.rihnetlogistikmde.models.Paketanbindung;
import at.rihnet.rihnetlogistikmde.models.SeriennummerCharge;
import at.rihnet.rihnetlogistikmde.models.SqlServerData;
import at.rihnet.rihnetlogistikmde.models.Verarbeitungskennzeichen;

public class CommunicationSql {
    private static final String TAG = "RIHNet";

    public static Artikel getArtikel(SqlServerData sqlServerData, String search, String standort) {
        try {
            Connection connection = Db.conn();
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


//            ResultSet resultSet = statement.executeQuery(query);
//            if (resultSet.next()) {
//                String sn = resultSet.getString("SN");
//                String seriennummer = "";
//                if (sn.equals("S")) {
//                    seriennummer = resultSet.getString("Search");
//                }
//                return new Artikel(
//                        resultSet.getString("Artikelnummer"),
//                        resultSet.getString("Bezeichnung"),
//                        resultSet.getString("Zusatz"),
//                        resultSet.getString("SerieCharge"),
//                        seriennummer,
//                        "",
//                        1,
//                        resultSet.getInt("Bestand"),
//                        "",
//                        resultSet.getString("HstArtikelnummer"),
//                        resultSet.getString("EANNummer"),
//                        "",
//                        0,
//                        sn,
//                        Kategorie.UMLAGERUNG
//                );
//            }


        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return null;
    }

    public static Artikel getArtikelWE(SqlServerData sqlServerData, String search) {
        try {
            Connection connection = Db.conn();
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
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return null;
    }

    public static Artikel getArtikel(SqlServerData sqlServerData, String search) {
        try {
            Connection connection = Db.conn();
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
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
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
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
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
        try {
            Connection connection = Db.conn();
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
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return lb;
    }

    public static LagerplatzBestand getLagerplatzBestandByEan(SqlServerData sqlServerData, String artikelnummer, String ean) {
        try {
            Connection connection = Db.conn();
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
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return null;
    }

    public static String getLagerByEan(SqlServerData sqlServerData, String ean) {
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
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
        try {
            Connection connection = Db.conn();
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

        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return null;
    }

    public static List<Lagerplatz> getLagerplatzByLager(SqlServerData sqlServerData, String lager) {
        List<Lagerplatz> lb = new ArrayList<>();
        try {
            Connection connection = Db.conn();
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
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
            String query = "SELECT ISNULL(a.Artikelnummer, '') AS Artikelnummer, " +
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
                    "a.Artikelnummer, " +
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
        try {
            Connection connection = Db.conn();
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
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return lb;
    }

    public static List<Grund> getXLogistikappGruendeByType(SqlServerData sqlServerData, String type) {
        List<Grund> g = new ArrayList<>();
        try {
            Connection connection = Db.conn();
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
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
            String query = "SELECT LTRIM(ISNULL(Anzeigename, '')) AS Anzeigename, " +
                    "Nummer " +
                    "FROM Liefer " +
                    "WHERE ISNULL(Inaktiv, 0) = 0 " +
                    "AND ISNULL(Anzeigename, '') <> '' " +
                    "AND ISNULL(_ISVISIBLEINMDE, 0) = 1 " +
                    "ORDER BY LTRIM(ISNULL(Anzeigename, ''))";
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

    public static List<Lieferant> getLieferantenBelegByStatus(SqlServerData sqlServerData) {
        List<Lieferant> lieferanten = new ArrayList<>();
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
            String query = "SELECT DISTINCT LTRIM(ISNULL(l.Anzeigename, '')) AS Anzeigename,  " +
                    "Nummer  " +
                    "FROM Liefer l " +
                    "LEFT JOIN BELEG b ON l.Nummer = b.Adressnummer " +
                    "WHERE ISNULL(l.Inaktiv, 0) = 0   " +
                    "AND ISNULL(l.Anzeigename, '') <> ''  " +
                    "AND ISNULL(l._ISVISIBLEINMDE, 0) = 1  " +
                    "AND b.Belegtyp = 'B' " +
                    "AND b.[Status] = 0" +
                    "ORDER BY LTRIM(ISNULL(l.Anzeigename, ''))";
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

    public static List<Beleg> getBelegByBelegtypAdressnummer(SqlServerData sqlServerData, String belegtyp, String adressnummer) {
        List<Beleg> belege = new ArrayList<>();
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
            String query = "SELECT b.Belegtyp, ISNULL(b.Belegnummer, '') AS Belegnummer, " +
                    "b.Datum, " +
                    "LTRIM(ISNULL(l.Anzeigename, '')) AS Anzeigename, " +
                    "ISNULL(b.Adressnummer, '') AS Adressnummer, " +
                    "ISNULL(b.LieferBelegNr, '') AS LieferBelegNr " +
                    "FROM BELEG b " +
                    "LEFT JOIN Liefer l ON l.Nummer = b.Adressnummer " +
                    "WHERE b.[Status] = '0' " +
                    "AND b.Belegtyp = ? " +
                    "AND b.Adressnummer = ? " +
                    "ORDER BY b.Belegnummer";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, belegtyp);
                preparedStatement.setString(2, adressnummer);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        belege.add(new Beleg(
                                "", //TODO LieferBelegNr
                                resultSet.getString("Belegtyp"),
                                resultSet.getString("Belegnummer"),
                                new Lieferant(
                                        resultSet.getString("Adressnummer"),
                                        resultSet.getString("Anzeigename")
                                ),
                                "",
                                null,
                                Kategorie.WARENEINGANGBESTELLBEZUG
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return belege;
    }

    public static String getStandortBezeichnungByStandort(SqlServerData sqlServerData, String standort) {
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
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


    public static int updateBelegFreierText1ByBelegtypBelegnummer(SqlServerData sqlServerData, String belegtyp, String belegnummer, String freierText1) {
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
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

    public static List<Belegposition> getBelegpByBelegtypBelegnummer(SqlServerData sqlServerData, String belegtyp, String belegnummer) {
        List<Belegposition> belegpositionen = new ArrayList<>();
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
            String query = "SELECT bp.Belegtyp, " +
                    "bp.Belegnummer, " +
                    "ISNULL(bp.Postext, '') AS Postext, " +
                    "bp.Artikelnummer, " +
                    "ISNULL(bp.Bezeichnung, '') AS Bezeichnung, " +
                    "ISNULL(bp.Menge, 0) AS Menge, " +
                    "(ISNULL(bp.Menge, 0) - ISNULL(bp.Verwendet, 0)) AS Offen," +
                    "ISNULL(bp.Kalkulationspreis, 0) AS Kalkulationspreis, " +
                    "ISNULL(bp.Gewicht, 0) AS Gewicht, " +
                    "bp.Kennung, " +
                    "bp.Vorgaenger, " +
                    "ISNULL(a.SerieCharge, 'O') AS SerieCharge " +
                    "FROM BELEGP bp " +
                    "LEFT JOIN ART a ON bp.Artikelnummer = a.Artikelnummer " +
                    "WHERE bp.Zeilentyp IN ('A') " +
                    "AND bp.Belegtyp = ? " +
                    "AND bp.Belegnummer = ?" +
                    "AND bp.[Status] IN ('0', '1')" +
                    "AND bp.Artikelnummer IS NOT NULL";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, belegtyp);
                preparedStatement.setString(2, belegnummer);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        belegpositionen.add(new Belegposition(
                                resultSet.getString("Artikelnummer"),
                                resultSet.getString("Bezeichnung"),
                                "",
                                resultSet.getString("SerieCharge"),
                                "",
                                "",
                                resultSet.getInt("Menge"),
                                0,
                                "",
                                "",
                                "",
                                "",
                                0,
                                "",
                                Kategorie.WARENEINGANGBESTELLBEZUG,
                                resultSet.getString("Belegtyp"),
                                resultSet.getString("Belegnummer"),
                                resultSet.getString("Postext"),
                                resultSet.getString("Kennung"),
                                resultSet.getString("Vorgaenger"),
                                resultSet.getInt("Offen"),
                                resultSet.getInt("Menge"),
                                resultSet.getFloat("Gewicht"),
                                resultSet.getFloat("Kalkulationspreis")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return belegpositionen;
    }

    public static List<Belegposition> getBelegpByBelegtypAdressnummer(SqlServerData sqlServerData, String belegtyp, String adressnummer) {
        List<Belegposition> belegpositionen = new ArrayList<>();
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
            String query = "SELECT bp.Belegtyp, " +
                    "bp.Belegnummer, " +
                    "ISNULL(bp.Postext, '') AS Postext, " +
                    "bp.Artikelnummer, " +
                    "ISNULL(bp.Bezeichnung, '') AS Bezeichnung, " +
                    "ISNULL(a.EANNummer, '') AS EANNummer, " +
                    "ISNULL(bp.Menge, 0) AS Menge, " +
                    "(ISNULL(bp.Menge, 0) - ISNULL(bp.Verwendet, 0)) AS Offen, " +
                    "ISNULL(bp.Kalkulationspreis, 0) AS Kalkulationspreis, " +
                    "ISNULL(bp.Gewicht, 0) AS Gewicht, " +
                    "bp.Kennung, " +
                    "bp.Vorgaenger, " +
                    "ISNULL(a.SerieCharge, 'O') AS SerieCharge " +
                    "FROM BELEGP bp " +
                    "LEFT JOIN BELEG b ON bp.Belegtyp = b.Belegtyp AND bp.Belegnummer = b.Belegnummer " +
                    "LEFT JOIN ART a ON bp.Artikelnummer = a.Artikelnummer " +
                    "WHERE bp.Zeilentyp IN ('A') " +
                    "AND bp.Belegtyp = ? " +
                    "AND bp.[Status] IN ('0', '1') " +
                    "AND b.[Status] = 0 " +
                    "AND b.Adressnummer = ? " +
                    "AND bp.Artikelnummer IS NOT NULL  ";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, belegtyp);
                preparedStatement.setString(2, adressnummer);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        belegpositionen.add(new Belegposition(
                                resultSet.getString("Artikelnummer"),
                                resultSet.getString("Bezeichnung"),
                                "",
                                resultSet.getString("SerieCharge"),
                                "",
                                "",
                                resultSet.getInt("Menge"),
                                0,
                                "",
                                "",
                                resultSet.getString("EANNummer"),
                                "",
                                0,
                                "",
                                Kategorie.WARENEINGANGBESTELLBEZUG,
                                resultSet.getString("Belegtyp"),
                                resultSet.getString("Belegnummer"),
                                resultSet.getString("Postext"),
                                resultSet.getString("Kennung"),
                                resultSet.getString("Vorgaenger"),
                                resultSet.getInt("Offen"),
                                resultSet.getInt("Menge"),
                                resultSet.getFloat("Gewicht"),
                                resultSet.getFloat("Kalkulationspreis")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return belegpositionen;
    }

    /// Warenausgang
    public static List<BelegInfo> getBelegInfoByBelegtyp(String belegtypen, String standort) {
        List<BelegInfo> belegInfos = new ArrayList<>();
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
            String query = "SELECT ISNULL(b.Belegtyp, '') AS Belegtyp, " +
                    "ISNULL(b.Belegnummer, '') AS Belegnummer, " +
                    "b.Datum, " +
                    "ISNULL(b.Adressnummer, '') AS Adressnummer, " +
                    "ISNULL(b.[Name], '') AS [Name], " +
                    "ISNULL(b.Firma, '') AS Firma, " +
                    "ISNULL(b.Vorname, '') AS Vorname, " +
                    "ISNULL(b.Zusatz, '') AS Zusatz, " +
                    "ISNULL(b.Zusatz2, '') AS Zusatz2, " +
                    "ISNULL(b.Strasse, '') AS Strasse, " +
                    "ISNULL(b.Land, '') AS Land, " +
                    "ISNULL(b.Plz, '') AS Plz, " +
                    "ISNULL(b.Ort, '') AS Ort, " +
                    "ISNULL(b.Lieferbedingung, '') AS Lieferbedingung, " +
                    "ISNULL(b.Netto, 0) AS Netto, " +
                    "b.Liefertermin, " +
                    "ISNULL(b.IhrAuftrag, '') AS IhrAuftrag, " +
                    "CASE " +
                    "WHEN ISNULL(b.Firma, '') = '' " +
                    "THEN LTRIM((ISNULL(b.Vorname, '') + ' ' + ISNULL(b.[Name], ''))) " +
                    "ELSE a.Firma " +
                    "END AS Anzeigename, " +
                    "ISNULL(Adresse, 0) AS AAdresse, " +
                    "ISNULL(a.Adresstyp, '') AS AAdresstyp, " +
                    "ISNULL(a.Vorname, '') AS AVorname, " +
                    "ISNULL(a.[Name], '') AS AName, " +
                    "ISNULL(a.Zusatz, '') AS AZusatz, " +
                    "ISNULL(a.Zusatz2, '') AS AZusatz2, " +
                    "ISNULL(a.Strasse, '') AS AStrasse, " +
                    "ISNULL(a.Land, '') AS ALand, " +
                    "ISNULL(a.PLZ, '') AS APLZ, " +
                    "ISNULL(a.Ort, '') AS AOrt, " +
                    "ISNULL(a.Firma, '') AS AFirma, " +
                    "CASE " +
                    "WHEN ISNULL(a.Firma, '') = '' " +
                    "THEN LTRIM((ISNULL(a.Vorname, '') + ' ' + ISNULL(a.[Name], ''))) " +
                    "ELSE a.Firma " +
                    "END AS AAnzeigename, " +
                    "ISNULL(b._VERBKENZ, 0) AS _VERBKENZ, " +
                    "ISNULL(v._ANZLIEFERSCHEIN, 0) AS _ANZLIEFERSCHEIN, " +
                    "ISNULL(v._ANZRECHNUNG, 0) AS _ANZRECHNUNG, " +
                    "ISNULL(v._AUTORECHNUNG, 0) AS _AUTORECHNUNG " +
                    "FROM BELEG b " +
                    "LEFT JOIN ADRESS a ON a.Adresstyp = 'Q' + b.Belegtyp + b.Belegnummer " +
                    "LEFT JOIN XVerarbeitungskennzeichen v ON v.ID = b._VERBKENZ " +
                    "WHERE b.Belegtyp IN ( " + belegtypen + " ) " +
                    "AND b.[Status] = 0 " +
                    "AND b.Standort = ? " +
                    "ORDER BY b.Belegtyp, " +
                    "b.Belegnummer";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, standort);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        // Timestamp für 'Datum'
                        Timestamp datumTs = resultSet.getTimestamp("Datum");
                        LocalDateTime datum = null;
                        if (datumTs != null) {
                            datum = datumTs.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime();
                        }
                        // Timestamp für 'Liefertermin'
                        Timestamp lieferterminTs = resultSet.getTimestamp("Datum");
                        LocalDateTime liefertermin = null;
                        if (lieferterminTs != null) {
                            liefertermin = lieferterminTs.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime();
                        }
                        belegInfos.add(new BelegInfo(
                                resultSet.getString("Belegtyp"),
                                resultSet.getString("Belegnummer"),
                                datum,
                                resultSet.getString("Adressnummer"),
                                resultSet.getString("Firma"),
                                resultSet.getString("Name"),
                                resultSet.getString("Vorname"),
                                resultSet.getString("Anzeigename"),
                                resultSet.getString("Zusatz"),
                                resultSet.getString("Zusatz2"),
                                resultSet.getString("Strasse"),
                                resultSet.getString("Plz"),
                                resultSet.getString("Ort"),
                                resultSet.getString("Land"),
                                resultSet.getString("Lieferbedingung"),
                                resultSet.getFloat("Netto"),
                                liefertermin,
                                resultSet.getString("IhrAuftrag"),
                                new Adress(resultSet.getInt("AAdresse"),
                                        resultSet.getString("AAdresstyp"),
                                        resultSet.getString("AFirma"),
                                        resultSet.getString("AVorname"),
                                        resultSet.getString("AName"),
                                        resultSet.getString("AAnzeigename"),
                                        resultSet.getString("AZusatz"),
                                        resultSet.getString("AZusatz2"),
                                        resultSet.getString("AStrasse"),
                                        resultSet.getString("APLZ"),
                                        resultSet.getString("AOrt"),
                                        resultSet.getString("ALand")
                                ),
                                new Verarbeitungskennzeichen(resultSet.getInt("_VERBKENZ"),
                                        resultSet.getInt("_ANZLIEFERSCHEIN"),
                                        resultSet.getInt("_ANZRECHNUNG"),
                                        resultSet.getBoolean("_AUTORECHNUNG")
                                ),
                                Kategorie.WARENAUSGANG
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return belegInfos;
    }

    public static List<Lieferbedingung> getLieferbed() {
        List<Lieferbedingung> lieferbedingungen = new ArrayList<>();
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
            String query = "SELECT Nummer, " +
                    "ISNULL(Bezeichnung, '') AS Bezeichnung, " +
                    "ISNULL(Zusatz, '') AS Zusatz " +
                    "FROM LIEFERBED";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        lieferbedingungen.add(new Lieferbedingung(
                                resultSet.getString("Nummer"),
                                resultSet.getString("Bezeichnung"),
                                resultSet.getString("Zusatz")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return lieferbedingungen;
    }

    public static List<Verarbeitungskennzeichen> getVerarbeitungskennzeichen() {
        List<Verarbeitungskennzeichen> verarbeitungskennzeichen = new ArrayList<>();
        try {
            Connection connection = Db.conn();
            Statement statement = connection.createStatement();
            String query = "SELECT ID, " +
                    "ISNULL(_ANZLIEFERSCHEIN, 0) AS _ANZLIEFERSCHEIN, " +
                    "ISNULL(_ANZRECHNUNG, 0) AS _ANZRECHNUNG, " +
                    "ISNULL(_AUTORECHNUNG, 0) AS _AUTORECHNUNG " +
                    "FROM XVerarbeitungskennzeichen  ";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        verarbeitungskennzeichen.add(new Verarbeitungskennzeichen(
                                resultSet.getInt("ID"),
                                resultSet.getInt("_ANZLIEFERSCHEIN"),
                                resultSet.getInt("_ANZRECHNUNG"),
                                resultSet.getBoolean("_AUTORECHNUNG")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return verarbeitungskennzeichen;
    }

    public static List<Belegposition> getBelegpByZeilentypLagerartikel(String belegtyp, String belegnummer) {
        List<Belegposition> belegpositionen = new ArrayList<>();
        try {
            Connection connection = Db.conn();
            String query = "SELECT ISNULL(Menge, 0) AS Menge, " +
                    "ISNULL(Bezeichnung, '') AS Bezeichnung, " +
                    "ISNULL(Artikelnummer, '') AS Artikelnummer " +
                    "FROM BELEGP " +
                    "WHERE Belegnummer = ? " +
                    "AND Belegtyp = ? " +
                    "AND Zeilentyp IN ( " +
                    "'A', " +
                    "'E', " +
                    "'K' " +
                    ") " +
                    "AND ISNULL(Lagerartikel, 0) = 0 " +
                    "AND (ISNULL(Menge, 0) - ISNULL(Verwendet, 0)) > 0";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, belegnummer);
                preparedStatement.setString(2, belegtyp);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        belegpositionen.add(new Belegposition(
                                resultSet.getString("Artikelnummer"),
                                resultSet.getString("Bezeichnung"),
                                "",
                                "",
                                "",
                                "",
                                resultSet.getInt("Menge"),
                                0,
                                "",
                                "",
                                "",
                                "",
                                0,
                                "",
                                Kategorie.WARENAUSGANG,
                                belegtyp,
                                belegnummer,
                                "",
                                "",
                                "",
                                0,
                                resultSet.getInt("Menge"),
                                0,
                                0
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return belegpositionen;
    }

    public static List<Belegposition> getBelegpByBelegtypBelegnummer0(String belegtyp, String belegnummer) {
        List<Belegposition> belegpositionen = new ArrayList<>();
        try {
            Connection connection = Db.conn();
            String query = "SELECT bp.Belegtyp, " +
                    "bp.Belegnummer, " +
                    "ISNULL(bp.Postext, '') AS Postext, " +
                    "bp.Artikelnummer, " +
                    "ISNULL(bp.Bezeichnung, '') AS Bezeichnung, " +
                    "ISNULL(bp.Menge, 0) AS Menge, " +
                    "(ISNULL(bp.Menge, 0) - ISNULL(bp.Verwendet, 0)) AS Offen, " +
                    "ISNULL(bp.Lager, '') AS Lager, " +
                    "ISNULL(bp.Kalkulationspreis, 0) AS Kalkulationspreis, " +
                    "ISNULL(bp.Gewicht, 0) AS Gewicht, " +
                    "bp.Kennung, " +
                    "bp.Vorgaenger, " +
                    "ISNULL(a.SerieCharge, 'O') AS SerieCharge " +
                    "FROM BELEGP bp " +
                    "LEFT JOIN ART a ON bp.Artikelnummer = a.Artikelnummer " +
                    "WHERE bp.Zeilentyp IN ('A', 'G', 'H') " +
                    "AND bp.Belegtyp = ? " +
                    "AND bp.Belegnummer = ?" +
                    "AND bp.Artikelnummer IS NOT NULL " +
                    "AND ISNULL(a.Lagerartikel, 0) = 1 " +
                    "AND (ISNULL(bp.Menge, 0) - ISNULL(bp.Verwendet, 0)) > 0";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, belegtyp);
                preparedStatement.setString(2, belegnummer);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        belegpositionen.add(new Belegposition(
                                resultSet.getString("Artikelnummer"),
                                resultSet.getString("Bezeichnung"),
                                "",
                                resultSet.getString("SerieCharge"),
                                "",
                                "",
                                resultSet.getInt("Menge"),
                                0,
                                resultSet.getString("Lager"),
                                "",
                                "",
                                "",
                                0,
                                "",
                                Kategorie.WARENEINGANGBESTELLBEZUG,
                                resultSet.getString("Belegtyp"),
                                resultSet.getString("Belegnummer"),
                                resultSet.getString("Postext"),
                                resultSet.getString("Kennung"),
                                resultSet.getString("Vorgaenger"),
                                resultSet.getInt("Offen"),
                                resultSet.getInt("Menge"),
                                resultSet.getFloat("Gewicht"),
                                resultSet.getFloat("Kalkulationspreis")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return belegpositionen;
    }

    public static boolean existsSerieCharge(String artikelnummer, String serieCharge) {
        try {
            Connection connection = Db.conn();
            String query = "SELECT  COUNT(*) AS Anzahl " +
                    "FROM  LAGERBESTAND l " +
                    "LEFT JOIN SERIE s ON s.Id = l.SerieId " +
                    "LEFT JOIN ART a ON a.Artikelnummer = l.Artikelnummer " +
                    "WHERE a.Artikelnummer = ? " +
                    "AND s.SerieCharge = ? " +
                    "AND l.Bestand > 0";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, artikelnummer);
                preparedStatement.setString(2, serieCharge);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("Anzahl") <= 0;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return true;
    }

    public static float getArtikelBestand(String artikelnummer, String standort, String lager) {
        try {
            Connection connection = Db.conn();
            String query = "Select dbo.SL_fnGetArtikelBestand( ?, ?, ?) AS Bestand";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, artikelnummer);
                preparedStatement.setString(2, standort);
                preparedStatement.setString(3, lager);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getFloat("Bestand");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("%s", e.getMessage()));
        }
        return 0;
    }

    public static int insertPaketanbindung(Paketanbindung paketanbindung) {
        String query =
                "INSERT INTO XPaketanbindung (" +
                        "  AngelegtAm, " +
                        "  AngelegtVon, " +
                        "  _ANZAHL, " +
                        "  _BELEGNUMMER, " +
                        "  _BELEGTYP, " +
                        "  _DRUCKER, " +
                        "  _ANGELEGTVON" +
                        ") VALUES (" +
                        "  GETDATE(), " +
                        "  ?, ?, ?, ?, ?, ? " +
                        ")";
        Connection connection = null;
        try {
            connection = Db.conn(); // Hole globale Connection
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setString(1, "RI"); // AngelegtVon
                preparedStatement.setInt(2, paketanbindung.getAnzahl());
                preparedStatement.setString(3, paketanbindung.getBelegnummer());
                preparedStatement.setString(4, paketanbindung.getBelegtyp());
                preparedStatement.setInt(5, paketanbindung.getDrucker());
                preparedStatement.setString(6, paketanbindung.getAngelegtVon());

                return preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL-Fehler bei insertPaketanbindung: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Allgemeiner Fehler bei insertPaketanbindung: " + e.getMessage(), e);
        }
        return 0;
    }

    /// Inventur
    public static List<Invbasis> getInvbasisByBelegnummerKennzeichen(SqlServerData sqlServerData, String belegnummer, String kennzeichen) {
        List<Invbasis> invbases = new ArrayList<>();
        String query =
                "SELECT " +
                        " ISNULL(Belegnummer, '') AS Belegnummer, " +
                        " ISNULL(Nummer, '')      AS Nummer, " +
                        " ISNULL(Bezeichnung, '') AS Bezeichnung " +
                        " FROM INVBASIS " +
                        " WHERE Belegnummer = ? " +
                        " AND Kennzeichen = ?";
        Connection connection = null;
        try {
            connection = Db.conn();
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
        } catch (SQLException e) {
            Log.e(TAG, "SQL-Fehler in getInvbasisByBelegnummerKennzeichen: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Fehler in getInvbasisByBelegnummerKennzeichen: " + e.getMessage(), e);
        }
        return invbases;
    }

    public static boolean existsSerieCharge(SqlServerData sqlServerData, String serieCharge, String artikelnummer) {
        String query =
                "SELECT COUNT(*) AS Anzahl " +
                        " FROM SERIE " +
                        " WHERE SerieCharge = ? " +
                        " AND Artikelnummer = ?";
        Connection connection = null;
        try {
            connection = Db.conn();
            try (PreparedStatement ps = connection.prepareStatement(query)) {

                ps.setString(1, serieCharge);
                ps.setString(2, artikelnummer);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("Anzahl") > 0;
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL-Fehler in existsSerieCharge: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Fehler in existsSerieCharge: " + e.getMessage(), e);
        }
        return false;
    }

    public static boolean existsInventur(String belegnummer, String serieCharge) {
        String query =
                "SELECT 1 " +
                        " FROM INVEN " +
                        " WHERE Belegnummer = ? " +
                        " AND SerienNr = ? " +
                        " AND Ist = 1";
        try {
            Connection connection = Db.conn();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, belegnummer);
                ps.setString(2, serieCharge);
                try (ResultSet rs = ps.executeQuery()) {
                    // Wenn mindestens ein Datensatz existiert → true
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQL-Fehler in existsInventur: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Fehler in existsInventur: " + e.getMessage(), e);
        }
        return false;
    }

}
