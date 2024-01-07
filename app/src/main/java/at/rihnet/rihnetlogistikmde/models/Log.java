package at.rihnet.rihnetlogistikmde.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity(tableName = "log")
public class Log {
    @PrimaryKey
    private  Long id;
    private String timestamp;
    private String message;
    private int color;
    private LogKategorie kategorie;

    public Log(String message, int color, LogKategorie kategorie) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        this.timestamp = dtf.format(now);
        this.message = message;
        this.color = color;
        this.kategorie = kategorie;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public LogKategorie getKategorie() {
        return kategorie;
    }

    public void setKategorie(LogKategorie kategorie) {
        this.kategorie = kategorie;
    }
}
