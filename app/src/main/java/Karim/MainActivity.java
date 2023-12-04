package Karim;

import android.content.DialogInterface;
import android.os.Bundle;
import com.cst3104.Vishnu.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;



import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ChatAdapter theAdapter;
    private ArrayList<Message> messages;

    @Override
    protected void onCreate(Bundle p) {
        super.onCreate(p);
        setContentView(R.layout.activity_main);

        Button send = findViewById(R.id.sendBtn);
        Button receive = findViewById(R.id.receiveBtn);
        EditText edit = findViewById(R.id.editText);
        ListView lView = findViewById(R.id.listView);

        messages = new ArrayList<>();
        theAdapter = new ChatAdapter();
        lView.setAdapter( theAdapter ) ;

        send.setOnClickListener( click ->{
            String msgTyped = edit.getText().toString();
            boolean sent = true;

            if ( !msgTyped.isEmpty()) {
                messages.add(new Message(msgTyped, sent));
                edit.setText("");
                theAdapter.notifyDataSetChanged();
            }
        });

        lView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder alertMsg = new AlertDialog.Builder(MainActivity.this);
                alertMsg.setTitle(getString(R.string.delete_message));
                alertMsg.setMessage(getString(R.string.select_row) + position);

                alertMsg.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (position >= 0 && position < messages.size()) {
                            messages.remove(position);
                            theAdapter.notifyDataSetChanged();
                        }
                    }
                });

                alertMsg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertMsg.create().show();
                return true;
            }
        });

        receive.setOnClickListener( click ->{
            String msgTyped = edit.getText().toString();
            boolean received = false;

            if ( !msgTyped.isEmpty()) {
                messages.add(new Message(msgTyped, received));
                edit.setText("");
                theAdapter.notifyDataSetChanged();
            }
        });
    }

    public class ChatAdapter extends BaseAdapter {
        @Override
        public int getCount() {return messages.size(); }

        @Override
        public Object getItem(int position) { return "This is row: " + position; }

        @Override
        public long getItemId(int position) { return position;   }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            Message thisRow = messages.get(position);

            View newView;
            if (thisRow.isSent()) {
                newView = inflater.inflate(R.layout.sent_message, parent, false);
            } else {
                newView = inflater.inflate(R.layout.received_message, parent, false);
            }
            TextView msgView = newView.findViewById(R.id.message);
            msgView.setText( thisRow.getText() );
            return newView;
        }
    }

    public class Message {
        private String text; private boolean isSent;
        public Message(String text, boolean isSent) { this.text = text; this.isSent = isSent; }
        public String getText() { return text; }
        public boolean isSent() { return isSent; }
    }

}