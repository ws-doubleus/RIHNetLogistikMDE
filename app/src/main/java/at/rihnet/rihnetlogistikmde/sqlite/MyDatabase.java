package at.rihnet.rihnetlogistikmde.sqlite;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Beleg;
import at.rihnet.rihnetlogistikmde.models.Log;

@Database(entities = {Artikel.class, Log.class, Beleg.class}, version = 3, exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {
    public abstract QueueDAO getQueueDAO();
    public  abstract LogDAO getLogDAO();
    public  abstract  BelegDAO getBelegDAO();
}
