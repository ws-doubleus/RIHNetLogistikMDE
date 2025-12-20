package at.rihnet.rihnetlogistikmde.sqlite;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import at.rihnet.rihnetlogistikmde.models.Beleg;
import at.rihnet.rihnetlogistikmde.models.Kategorie;

@Dao
public interface BelegDAO {
    @Insert
    void insert(Beleg... beleg);

    @Update
    void update(Beleg... beleg);

    @Delete
    void delete(Beleg... beleg);

    @Query("SELECT * FROM beleg WHERE kategorie = :kategorie LIMIT 1")
    Beleg getBeleg(Kategorie kategorie);

    @Query("DELETE FROM beleg")
    void deleteAllBeleg();

    @Query("DELETE FROM beleg WHERE kategorie = :kategorie")
    void deleteBelegByKategorie(Kategorie kategorie);
}
