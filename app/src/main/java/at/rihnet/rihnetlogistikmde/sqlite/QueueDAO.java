package at.rihnet.rihnetlogistikmde.sqlite;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Kategorie;

@Dao
public interface QueueDAO {
    @Insert
    void insert(Artikel... artikel);

    @Update
    void update(Artikel... artikel);

    @Delete
    void delete(Artikel artikel);

    @Query("SELECT * FROM queue WHERE kategorie = :kategorie")
    List<Artikel> getArtikelByKategorie(Kategorie kategorie);

    @Query("UPDATE queue SET menge = :menge WHERE kategorie = :kategorie AND artikelnummer = :artikelnummer AND lager = :lager AND lagerplatz = :lagerplatz")
    void updateArtikelByKategorieArtikelnummerLager(Kategorie kategorie, String artikelnummer, String lager, String lagerplatz, int menge);
}
