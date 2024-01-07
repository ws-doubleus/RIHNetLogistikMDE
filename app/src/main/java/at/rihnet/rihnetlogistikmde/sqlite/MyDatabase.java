package at.rihnet.rihnetlogistikmde.sqlite;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Log;

@Database(entities = {Artikel.class, Log.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {
    public abstract QueueDAO getQueueDAO();
    public  abstract LogDAO getLogDAO();
}
