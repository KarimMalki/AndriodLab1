package Karim;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChatMessage {
    @ColumnInfo(name="message")
    protected String message;
    @ColumnInfo(name="timeSent")
    protected String timeSent;
    @ColumnInfo(name="sendOrReceive")
    protected int sendOrReceived;
    private boolean isSentButton;

    public int getId() {
        return (int) id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @PrimaryKey (autoGenerate = true)
    @ColumnInfo(name="id")
    public long id;

    public ChatMessage(String message, String timeSent, boolean isSentButton) {
        this.message = message;
        this.timeSent = timeSent;
        this.isSentButton = isSentButton;
    }

    public String getMessage() {
        return message;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public boolean isSentButton() {
        return isSentButton;
    }

}