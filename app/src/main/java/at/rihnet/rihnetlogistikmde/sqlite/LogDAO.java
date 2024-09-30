package at.rihnet.rihnetlogistikmde.sqlite;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.models.Kategorie;

@Dao
public interface LogDAO {
    @Insert
    public void insert(Log... log);

    @Update
    public void update(Log... log);

    @Delete
    public void delete(Log log);

    @Query("DELETE FROM log WHERE kategorie = :kategorie")
    public void deleteLogByKategorie(Kategorie kategorie);

    @Query("SELECT * FROM log WHERE kategorie = :kategorie")
    public List<Log> getLogByKategorie(Kategorie kategorie);
}
