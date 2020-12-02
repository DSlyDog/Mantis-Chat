package net.whispwriting.mantischat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationActivity extends AppCompatActivity {

    private String userID, name, image;
    private DocumentReference userDoc;
    private CircleImageView profileIcon;
    private FloatingActionButton sendMessageButton;
    private EditText messageBox;
    private FirebaseUser currentUser;
    private DatabaseReference rootRef;
    private RecyclerView messagesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        final Toolbar convToolbar = findViewById(R.id.conversation_bar);
        setSupportActionBar(convToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileIcon = (CircleImageView) findViewById(R.id.profile_image_conv);
        sendMessageButton = (FloatingActionButton) findViewById(R.id.sendMessage);
        messageBox = (EditText) findViewById(R.id.message);

        userID = getIntent().getStringExtra("userID");
        name = getIntent().getStringExtra("name");
        image = getIntent().getStringExtra("image");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        Picasso.get().load(image).placeholder(R.drawable.avatar).into(profileIcon);
        getSupportActionBar().setTitle(name);

        messagesView = (RecyclerView) findViewById(R.id.conversation_recycler);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                messageBox.setText("");
            }
        });
    }

    private void sendMessage(){
        String message = messageBox.getText().toString();

        if (!TextUtils.isEmpty(message)){
            String currentUserRef = "messages/" + currentUser.getUid() + "/" + userID;
            String chatUserRef = "messages/" + userID + "/" + currentUser.getUid();

            DatabaseReference userMessagePush = rootRef.child("messages").child(currentUser.getUid())
                    .child(userID).push();
            String pushID = userMessagePush.getKey();

            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("message", message);
            messageMap.put("type", "text");

            Map<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(currentUserRef + "/" + pushID, messageMap);
            messageUserMap.put(chatUserRef + "/" + pushID, messageMap);

            rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if (error != null){
                        Log.w("CHAT", error.getMessage());
                    }
                }
            });
        }
    }
}