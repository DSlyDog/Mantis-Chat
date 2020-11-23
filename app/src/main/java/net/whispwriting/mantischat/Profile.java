package net.whispwriting.mantischat;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile {
    enum State{
        NOT_FRIENDS,
        REQUEST_SENT,
        REQUEST_RECEIVED,
        FRIENDS
    }
    private FirebaseFirestore firestore;
    private FirebaseDatabase firebase;
    private String name, status, image, currentUID;
    private boolean success;
    private State currentState = State.NOT_FRIENDS;

    public Profile(String currentUID){
        firestore = FirebaseFirestore.getInstance();
        firebase = FirebaseDatabase.getInstance();
        this.currentUID = currentUID;
    }

    public boolean loadProfile(final TextView displayName, final TextView statusView, final CircleImageView userImage){
        success = false;
        DocumentReference user = firestore.collection("Users").document(currentUID);
        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        name = dataSnapshot.getString("name");
                        status = dataSnapshot.getString("status");
                        image = dataSnapshot.getString("image");
                        displayName.setText(name);
                        statusView.setText(status);
                        Picasso.get().load(image).placeholder(R.drawable.avatar).into(userImage);
                        success = true;
                    }
                }

            }
        });
        return success;
    }

    public void getFriendRequests(final String uid, final Button sendFriendRequest, final Button declineFriendRequest){
        DocumentReference friendRequests = firestore.collection("Friend_Requests").document(uid);
        friendRequests.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        String requestType = dataSnapshot.getString(currentUID + "request_type");
                        if (requestType != null && requestType.equals("received")){
                            currentState = State.REQUEST_RECEIVED;
                            sendFriendRequest.setText("Accept Friend Request");
                            declineFriendRequest.setEnabled(true);
                            declineFriendRequest.setVisibility(View.VISIBLE);
                        }else if (requestType != null && requestType.equals("sent")){
                            currentState = State.REQUEST_SENT;
                            sendFriendRequest.setText("Cancel Friend Request");
                        }
                    }
                }
            }
        });
    }

    public void isFriendsWith(String uid, final Button sendFriendRequest){
        DocumentReference friends = firestore.collection("Users").document(uid);
        friends.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        List<String> friends = (ArrayList<String>) dataSnapshot.get("friends");
                        if (friends.contains(currentUID)) {
                            currentState = State.FRIENDS;
                            sendFriendRequest.setText("Unfriend");
                        }
                    }
                }
            }
        });
    }

    public String getName(){
        return name;
    }

    public String getStatus(){
        return status;
    }

    public String getImage(){
        return image;
    }

    public State getCurrentState(){
        return currentState;
    }
}
