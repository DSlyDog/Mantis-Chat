package net.whispwriting.mantischat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView displayName;
    private TextView status;
    private CircleImageView userImage;
    private Button sendFriendRequest;
    private Button declineFriendRequest;
    private ProgressDialog progress;
    private int currentState;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private FirebaseDatabase firebase;
    private DatabaseReference friendRequestDatabase;
    private DatabaseReference friendDatabase;
    private DatabaseReference notificationDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar usersToolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(usersToolbar);
        getSupportActionBar().setTitle(" ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String uid = getIntent().getStringExtra("userID");

        firestore = FirebaseFirestore.getInstance();
        firebase = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        displayName = (TextView) findViewById(R.id.DisplayNameText_other);
        status = (TextView) findViewById(R.id.statusText_other);
        userImage = (CircleImageView) findViewById(R.id.profile_image_other);
        sendFriendRequest = (Button) findViewById(R.id.friendRequest);
        declineFriendRequest = (Button) findViewById(R.id.declineFriendRequest);
        declineFriendRequest.setEnabled(false);
        declineFriendRequest.setVisibility(View.INVISIBLE);

        currentState = 0;

        progress = new ProgressDialog(this);
        progress.setTitle("Loading User Data");
        progress.setMessage("Please wait while the user's data is loaded");
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        DocumentReference user = firestore.collection("Users").document(uid);
        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.getString("name");
                        String userStatus = dataSnapshot.getString("status");
                        String image = dataSnapshot.getString("image");

                        displayName.setText(name);
                        status.setText(userStatus);
                        Picasso.get().load(image).placeholder(R.drawable.avatar).into(userImage);
                    }
                }

                friendRequestDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(uid)){
                            String requestType = dataSnapshot.child(uid).child("request_type").getValue().toString();
                            if (requestType.equals("received")){
                                currentState = 2;
                                sendFriendRequest.setText("Accept Friend Request");
                                declineFriendRequest.setEnabled(true);
                                declineFriendRequest.setVisibility(View.VISIBLE);
                            }else if (requestType.equals("sent")){
                                currentState = 1;
                                sendFriendRequest.setText("Cancel Friend Request");
                            }
                        }

                        progress.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                DocumentReference friends = firestore.collection("Users").document(currentUser.getUid());
                friends.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot dataSnapshot = task.getResult();
                            if (dataSnapshot.exists()) {
                                List<String> friends = (ArrayList<String>) dataSnapshot.get("friends");
                                if (friends.contains(uid)) {
                                    currentState = 3;
                                    sendFriendRequest.setText("Unfriend");
                                }
                            }
                        }
                        progress.dismiss();
                    }
                });
            }
        });
        if (currentUser.getUid().equals(uid)){
            sendFriendRequest.setEnabled(false);
            sendFriendRequest.setVisibility(View.INVISIBLE);
        }

        sendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                sendFriendRequest.setEnabled(false);
                if (currentState == 0){
                    friendRequestDatabase.child(currentUser.getUid()).child(uid).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                friendRequestDatabase.child(uid).child(currentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", currentUser.getUid());
                                        notificationData.put("type", "request");

                                        notificationDatabase.child(uid).push().setValue(notificationData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    sendFriendRequest.setEnabled(true);
                                                    currentState = 1;
                                                    sendFriendRequest.setText("Cancel Request");
                                                    Toast.makeText(ProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else if (currentState == 1){
                    friendRequestDatabase.child(currentUser.getUid()).child(uid).child("request_type")
                            .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                friendRequestDatabase.child(uid).child(currentUser.getUid()).child("request_type")
                                        .setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendFriendRequest.setEnabled(true);
                                        currentState = 0;
                                        sendFriendRequest.setText("Send Friend Request");
                                        Toast.makeText(ProfileActivity.this, "Friend request canceled", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed canceling request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else if (currentState == 2){
                    declineFriendRequest.setEnabled(false);
                    declineFriendRequest.setVisibility(View.INVISIBLE);
                    final Date currentDate = new Date();
                    friendDatabase.child(currentUser.getUid()).child(uid).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendDatabase.child(uid).child(currentUser.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    friendRequestDatabase.child(currentUser.getUid()).child(uid).child("request_type")
                                            .setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                friendRequestDatabase.child(uid).child(currentUser.getUid()).child("request_type")
                                                        .setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        final DocumentReference friends = firestore.collection("Users").document(uid);
                                                        friends.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot dataSnapshot = task.getResult();
                                                                    if (dataSnapshot.exists()) {
                                                                        List<String> userFriends = (ArrayList<String>) dataSnapshot.get("friends");
                                                                        userFriends.add(currentUser.getUid());
                                                                        Map<String, Object> friendMap = new HashMap<>();
                                                                        friendMap.put("friends", userFriends);
                                                                        friends.set(friendMap, SetOptions.merge())
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            final DocumentReference selfFriendsDoc = firestore.collection("Users").document(currentUser.getUid());
                                                                                            selfFriendsDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        DocumentSnapshot dataSnapshot = task.getResult();
                                                                                                        if (dataSnapshot.exists()) {
                                                                                                            List<String> selfFriends = (ArrayList<String>) dataSnapshot.get("friends");
                                                                                                            selfFriends.add(uid);
                                                                                                            Map<String, Object> friendMap = new HashMap<>();
                                                                                                            friendMap.put("friends", selfFriends);
                                                                                                            selfFriendsDoc.set(friendMap, SetOptions.merge())
                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                            if (task.isSuccessful()){
                                                                                                                                Toast.makeText(ProfileActivity.this, "Friend request accepted", Toast.LENGTH_SHORT).show();
                                                                                                                                sendFriendRequest.setText("Unfriend");
                                                                                                                                currentState = 3;
                                                                                                                                declineFriendRequest.setEnabled(false);
                                                                                                                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                                                                                                            }else{
                                                                                                                                Toast.makeText(ProfileActivity.this, "Failed to accept friend request", Toast.LENGTH_SHORT).show();
                                                                                                                            }
                                                                                                                        }
                                                                                                                    });
                                                                                                        }else{
                                                                                                            Toast.makeText(ProfileActivity.this, "Failed to accept friend request", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }else{
                                                                                                        Toast.makeText(ProfileActivity.this, "Failed to accept friend request", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                    progress.dismiss();
                                                                                                }
                                                                                            });
                                                                                        }else{
                                                                                            Toast.makeText(ProfileActivity.this, "Failed to accept friend request", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }else{
                                                                        Toast.makeText(ProfileActivity.this, "Failed to accept friend request", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }else{
                                                                    Toast.makeText(ProfileActivity.this, "Failed to accept friend request", Toast.LENGTH_SHORT).show();
                                                                }
                                                                progress.dismiss();
                                                            }
                                                        });
                                                        /*sendFriendRequest.setEnabled(true);
                                                        currentState = 3;
                                                        sendFriendRequest.setText("Unfriend");
                                                        Toast.makeText(ProfileActivity.this, "Friend request accepted", Toast.LENGTH_SHORT).show();*/
                                                    }
                                                });
                                            }else{
                                                Toast.makeText(ProfileActivity.this, "Failed accepting request", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                }else if (currentState == 3){
                    friendDatabase.child(currentUser.getUid()).child(uid).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                friendDatabase.child(uid).child(currentUser.getUid()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        final CollectionReference userRef = firestore.collection("Users");

                                        userRef.document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    final DocumentSnapshot selfDocument = task.getResult();
                                                    if (selfDocument.exists()) {
                                                        userRef.document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()){
                                                                    final DocumentSnapshot userDocument = task.getResult();
                                                                    if (userDocument.exists()){
                                                                        List<String> selfFriends = (ArrayList<String>) selfDocument.get("friends");
                                                                        selfFriends.remove(uid);
                                                                        Map<String, Object> selfFriendMap = new HashMap<>();
                                                                        selfFriendMap.put("friends", selfFriends);
                                                                        userRef.document(currentUser.getUid()).set(selfFriendMap, SetOptions.merge())
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            List<String> userFriends = (ArrayList<String>) userDocument.get("friends");
                                                                                            userFriends.remove(currentUser.getUid());
                                                                                            Map<String, Object> userFriendMap = new HashMap<>();
                                                                                            userFriendMap.put("friends", userFriends);
                                                                                            userRef.document(uid).set(userFriendMap, SetOptions.merge())
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()){
                                                                                                                sendFriendRequest.setText("Send Friend Request");
                                                                                                                sendFriendRequest.setEnabled(true);
                                                                                                                currentState = 0;
                                                                                                                Toast.makeText(ProfileActivity.this, "Successfully unfriended", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }else{
                                                                                            Toast.makeText(ProfileActivity.this, "Failed to unfriend", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }else{
                                                                        Toast.makeText(ProfileActivity.this, "Failed to unfriend", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }else{
                                                                    Toast.makeText(ProfileActivity.this, "Failed to unfriend", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }else{
                                                        Toast.makeText(ProfileActivity.this, "Failed to unfriend", Toast.LENGTH_SHORT).show();
                                                    }
                                                }else{
                                                    Toast.makeText(ProfileActivity.this, "Failed to unfriend", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        declineFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendRequestDatabase.child(currentUser.getUid()).child(uid).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestDatabase.child(uid).child(currentUser.getUid()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendFriendRequest.setEnabled(true);
                                    currentState = 0;
                                    sendFriendRequest.setText("Send Friend Request");
                                    declineFriendRequest.setEnabled(false);
                                    declineFriendRequest.setVisibility(View.INVISIBLE);
                                    Toast.makeText(ProfileActivity.this, "Request denied.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

    }
}