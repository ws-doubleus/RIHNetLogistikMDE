package at.rihnet.rihnetlogistikmde.sqlite;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Log;
import at.rihnet.rihnetlogistikmde.models.LogKategorie;

@Dao
public interface QueueDAO {
    @Insert
    public void insert(Artikel... artikel);

    @Update
    public void update(Artikel... artikel);

    @Delete
    public void delete(Artikel artikel);

   @Query("SELECT * FROM queue")
    public List<Artikel> getArtikle();
}
