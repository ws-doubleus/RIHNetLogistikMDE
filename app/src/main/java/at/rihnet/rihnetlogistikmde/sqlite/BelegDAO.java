package at.rihnet.rihnetlogistikmde.sqlite;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import at.rihnet.rihnetlogistikmde.models.Beleg;

@Dao
public interface BelegDAO {
    @Insert
    void insert(Beleg... beleg);

    @Update
    void update(Beleg... beleg);

    @Delete
    void delete(Beleg... beleg);

    @Query("SELECT * FROM beleg LIMIT 1")
    Beleg getBeleg();

    @Query("DELETE FROM beleg")
    void deleteAllBeleg();
}
