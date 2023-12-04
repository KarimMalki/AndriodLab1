package Karim;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
@Dao
public interface ChatMessageDAO {
    @Insert
    public long insertMessage(ChatMessage m);
    @Query("Select * from ChatMessage")
    public List<ChatMessage> getAllMessages();

    @Query("DELETE FROM ChatMessage WHERE id = :messageId")
    void deleteMessageById(int messageId);

}
