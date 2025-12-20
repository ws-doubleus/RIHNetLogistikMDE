package at.rihnet.rihnetlogistikmde.sqlite;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Kategorie;

@Dao
public interface QueueBelegpositionDAO {
    @Insert
    void insert(Belegposition... belegposition);

    @Update
    void update(Belegposition... belegposition);

    @Delete
    void delete(Belegposition... belegposition);

    @Query("SELECT * FROM queuebelegposition WHERE kategorie = :kategorie")
    List<Belegposition> getBelegpositionByKategorie(Kategorie kategorie);

    @Query("UPDATE queuebelegposition SET menge = :menge WHERE id = :id")
    void updateBelegpositionById(long id, int menge);

    @Query("SELECT * FROM queuebelegposition WHERE belegtyp = :belegtyp AND belegnummer = :belegnummer AND postext = :postext AND artikelnummer = :artikelnummer AND kategorie = :kategorie")
    Belegposition getBelegpositionByBelegtypBelegnummerArtikelnummer(String belegtyp, String belegnummer, String postext, String artikelnummer, Kategorie kategorie);

    @Query("DELETE FROM queuebelegposition WHERE kategorie = :kategorie")
    void deleteBelegpositionByKategorie(Kategorie kategorie);
}
