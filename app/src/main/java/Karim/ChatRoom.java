package Karim;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.cst3104.Vishnu.R;
import com.cst3104.Vishnu.databinding.ActivityChatRoomBinding;
import com.cst3104.Vishnu.databinding.SentMessageBinding;
import com.cst3104.Vishnu.databinding.ReceivedMessageBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class ChatRoom extends AppCompatActivity {

    ActivityChatRoomBinding binding;
    ArrayList<ChatMessage> messages = new ArrayList<>();
    ChatRoomViewModel chatModel;
    RecyclerView.Adapter<MyRowHolder> myAdapter;
    ChatMessageDAO mDAO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        chatModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        messages = chatModel.messages.getValue();

        MessageDatabase db = Room.databaseBuilder(getApplicationContext(), MessageDatabase.class, "Chat-database").build();
        mDAO = db.cmDAO();

        if (messages == null) {
            chatModel.messages.postValue(messages = new ArrayList<ChatMessage>());
        }
        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));

        binding.recycleView.setAdapter(myAdapter = new RecyclerView.Adapter<MyRowHolder>() {
            @NonNull
            @Override
            public MyRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == 0) {
                    SentMessageBinding sentBinding = SentMessageBinding.inflate(getLayoutInflater(), parent, false);
                    return new MyRowHolder(sentBinding.getRoot());
                } else {
                    ReceivedMessageBinding receivedBinding = ReceivedMessageBinding.inflate(getLayoutInflater(), parent, false);
                    return new MyRowHolder(receivedBinding.getRoot());
                }
            }

            @Override
            public void onBindViewHolder(@NonNull MyRowHolder holder, int position) {
                ChatMessage chatMessage = messages.get(position);
                holder.messageText.setText(chatMessage.getMessage());
                holder.timeText.setText(chatMessage.getTimeSent());
            }

            @Override
            public int getItemCount() {
                return messages.size();
            }

            @Override
            public int getItemViewType(int position) {
                ChatMessage chatMessage = messages.get(position);
                return chatMessage.isSentButton() ? 0 : 1;
            }
        });

        if (chatModel.messages.getValue() == null) {
            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() -> {
                List<ChatMessage> dbMessages = mDAO.getAllMessages();
                runOnUiThread(() -> {
                    messages.addAll(dbMessages);
                    chatModel.messages.setValue(messages);
                    myAdapter.notifyDataSetChanged();
                });
            });
        } else {
            messages.addAll(chatModel.messages.getValue());
        }


        binding.sendBtn.setOnClickListener(click -> {
            String messageText = binding.editText.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd-MMMM-yy hh-mm a");
            String currentTime = sdf.format(new Date());
            boolean isSentButton = true;

            ChatMessage newMessage = new ChatMessage(messageText, currentTime, isSentButton);
            messages.add(newMessage);
            myAdapter.notifyItemInserted(messages.size() - 1);

            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() -> {
                long messageId = mDAO.insertMessage(newMessage);

                runOnUiThread(() -> {
                    newMessage.setId(messageId);
                    binding.editText.setText("");
                });
            });
        });


        binding.receiveBtn.setOnClickListener(click -> {
            String messageText = binding.editText.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EE, dd-MMM-yy hh-mm a");
            String currentTime = sdf.format(new Date());
            boolean isSentButton = false;

            ChatMessage newMessage = new ChatMessage(messageText, currentTime, isSentButton);
            messages.add(newMessage);
            myAdapter.notifyItemInserted(messages.size() - 1);

            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() -> {
                long messageId = mDAO.insertMessage(newMessage);

                runOnUiThread(() -> {
                    newMessage.setId(messageId);
                    binding.editText.setText("");
                });

            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.deleteBtn) {
            if (!messages.isEmpty()) {
                int messageIndex = messages.size() - 1; // Assuming last message is to be deleted
                ChatMessage removedMessage = messages.get(messageIndex);

                AlertDialog.Builder alertMsg = new AlertDialog.Builder(ChatRoom.this);
                String dialogMessage = "Are you sure you want to delete this message?\n\n" + removedMessage.getMessage();
                alertMsg.setTitle("Confirm Deletion");
                alertMsg.setMessage(dialogMessage);

                alertMsg.setPositiveButton("Yes", (dialog, which) -> {
                    Executor thread = Executors.newSingleThreadExecutor();
                    thread.execute(() -> {
                        mDAO.deleteMessageById(removedMessage.getId());
                        runOnUiThread(() -> {
                            messages.remove(messageIndex);
                            myAdapter.notifyItemRemoved(messageIndex);
                            Snackbar.make(binding.getRoot(), "Message deleted", Snackbar.LENGTH_LONG).show();
                        });
                    });
                });
                alertMsg.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
                alertMsg.create().show();
            } else {
                Toast.makeText(this, "No messages to delete", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.aboutBtn) {
            Toast.makeText(this, "Version 1.0, created by Karim Al Malki", Toast.LENGTH_LONG).show();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    class MyRowHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        public MyRowHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(clk ->{
                int position = getAbsoluteAdapterPosition();
                AlertDialog.Builder alertMsg = new AlertDialog.Builder(ChatRoom.this);
                alertMsg.setTitle("Question :");
                alertMsg.setMessage(getString(R.string.delete_message) + messageText.getText());

                alertMsg.setPositiveButton("Yes", (dialog, cl)-> {
                    ChatMessage removedMessage = messages.get(position);

                    Executor thread = Executors.newSingleThreadExecutor();
                    thread.execute(() -> {
                        mDAO.deleteMessageById(removedMessage.getId());
                        runOnUiThread(() -> {
                            messages.remove(position);
                            myAdapter.notifyItemRemoved(position);

                            Snackbar.make(messageText, "You deleted this message #" + position, Snackbar.LENGTH_LONG)
                                    .setAction("Undo", undoClick -> {
                                        messages.add(position, removedMessage);
                                        myAdapter.notifyItemInserted(position);

                                        Executor reinsertThread = Executors.newSingleThreadExecutor();
                                        reinsertThread.execute(() -> {
                                            mDAO.insertMessage(removedMessage);
                                        });
                                    })
                                    .show();
                        });
                    });
                });
                alertMsg.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertMsg.create().show();
            });

            messageText = itemView.findViewById(R.id.message);
            timeText = itemView.findViewById(R.id.time);
        }
    }
}