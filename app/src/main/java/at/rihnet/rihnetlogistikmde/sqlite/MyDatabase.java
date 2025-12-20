package at.rihnet.rihnetlogistikmde.sqlite;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.Beleg;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Log;

@Database(
        entities = {Artikel.class, Belegposition.class, Log.class, Beleg.class},
        version = 12,
        exportSchema = false
)
public abstract class MyDatabase extends RoomDatabase {

    private static volatile MyDatabase INSTANCE;   // <-- Singleton

    // -------------------------
    //      DAO Zugriff
    // -------------------------
    public abstract QueueDAO getQueueDAO();
    public abstract QueueBelegpositionDAO getQueueBelegpositionDAO();
    public abstract LogDAO getLogDAO();
    public abstract BelegDAO getBelegDAO();


    // -------------------------
    //      Singleton Builder
    // -------------------------
    public static MyDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MyDatabase.class) {   // threadsafe
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    MyDatabase.class,
                                    "rihnetdatabase"
                            )
                            //.fallbackToDestructiveMigration()  // nur falls du möchtest
                            //.allowMainThreadQueries()          // bitte NICHT benutzen!
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
