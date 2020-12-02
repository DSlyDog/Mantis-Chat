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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

// AccountSettings should be a mainactivity with an XML
// Chat page is a home page
// include in the README.md we have to
// conversation and chat page
// ConversionActivity lisy of chats in chat class

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
                DocumentReference friendRequests = firestore.collection("Friend_Requests").document(currentUser.getUid());
                friendRequests.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot dataSnapshot = task.getResult();
                            if (dataSnapshot.exists()) {
                                List<String> requests = (ArrayList<String>) dataSnapshot.get("requests");
                                if (requests.contains(uid + "received")){
                                    currentState = 2;
                                    sendFriendRequest.setText("Accept Friend Request");
                                    declineFriendRequest.setEnabled(true);
                                    declineFriendRequest.setVisibility(View.VISIBLE);
                                }else if (requests.contains(uid + "sent")){
                                    currentState = 1;
                                    sendFriendRequest.setText("Cancel Friend Request");
                                }
                            }
                        }
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
                // Current states determines if a friend request s gets sent 0 is hasn't been sent
                if (currentState == 0){
                    final DocumentReference currentUserRef = firestore.collection("Friend_Requests").document(currentUser.getUid());
                    final DocumentReference otherUser = firestore.collection("Friend_Requests").document(uid);

                    currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()){
                                    List<String> requests = (ArrayList<String>) document.get("requests");
                                    requests.add(uid + "sent");
                                    Map<String, Object> selfRequests = new HashMap<>();
                                    selfRequests.put("requests", requests);
                                    currentUserRef.set(selfRequests, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                otherUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()){
                                                            DocumentSnapshot document = task.getResult();
                                                            if (document.exists()){
                                                                List<String> requests = (ArrayList<String>) document.get("requests");
                                                                requests.add(currentUser.getUid() + "received");
                                                                Map<String, Object> otherRequests = new HashMap<>();
                                                                otherRequests.put("requests", requests);
                                                                otherUser.set(otherRequests, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            Toast.makeText(ProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                                                                        }else{
                                                                            Toast.makeText(ProfileActivity.this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            }else{
                                                                Toast.makeText(ProfileActivity.this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }else{
                                                            Toast.makeText(ProfileActivity.this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }else{
                                                Toast.makeText(ProfileActivity.this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }else{
                                    Toast.makeText(ProfileActivity.this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    // 1 is has been sent
                }else if (currentState == 1){
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", null);
                    notificationData.put("type", null);
                    firebase.getReference().child(uid).setValue(notificationData);

                    final Map<String, Object> friendRequests = new HashMap<>();
                    Map<String, Object> selfFriendRequests = new HashMap<>();
                    selfFriendRequests.put(uid + "request_type", FieldValue.delete());
                    friendRequests.put(currentUser.getUid() + "request_type", FieldValue.delete());
                    final CollectionReference friendRequestCollection = firestore.collection("Friend_Requests");
                    friendRequestCollection.document(currentUser.getUid()).set(selfFriendRequests, SetOptions.merge())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                friendRequestCollection.document(uid).set(friendRequests, SetOptions.merge())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(ProfileActivity.this, "Friend request canceled", Toast.LENGTH_SHORT).show();
                                                    sendFriendRequest.setText("Send Friend Request");
                                                    sendFriendRequest.setEnabled(true);
                                                    currentState = 0;
                                                }else{
                                                    Toast.makeText(ProfileActivity.this, "Failed to cancel friend request", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed to cancel friend request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    // 2 a friend request has been received
                }else if (currentState == 2){
                    final CollectionReference userRef = firestore.collection("Friend_Requests");
                    Map<String, Object> requestMap = new HashMap();
                    requestMap.put(currentUser.getUid() + "request_type", FieldValue.delete());
                    final Map<String, Object> selfRequestMap = new HashMap<>();
                    selfRequestMap.put(uid + "request_type", FieldValue.delete());

                    userRef.document(uid).set(requestMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                userRef.document(currentUser.getUid()).set(selfRequestMap, SetOptions.merge());
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
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed to accept friend request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    // users already friends
                }else if (currentState == 3){
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
            }
        });

        /*declineFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> requestMap = new HashMap();
                requestMap.put("Friend_Requests/" + currentUser.getUid() + "/" + uid + "request_type", null);
                requestMap.put("Friend_Requests/" + uid + "/" + currentUser.getUid() + "request_type", null);

                rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null){
                            sendFriendRequest.setEnabled(true);
                            currentState = 0;
                            sendFriendRequest.setText("Send Friend Request");
                            declineFriendRequest.setEnabled(false);
                            declineFriendRequest.setVisibility(View.INVISIBLE);
                            Toast.makeText(ProfileActivity.this, "Friend request declined", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(ProfileActivity.this, "Failed to decline friend request", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });*/

    }
}
