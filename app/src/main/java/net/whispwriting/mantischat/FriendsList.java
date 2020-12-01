package net.whispwriting.mantischat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsList extends AppCompatActivity {

    private RecyclerView usersListPage;
    private FirebaseFirestore usersDatabase;
    private CircleImageView userListImg;
    private FirebaseUser user;
    private List<String> friends;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        Toolbar usersToolbar = findViewById(R.id.UserListToolbar);
        setSupportActionBar(usersToolbar);
        getSupportActionBar().setTitle("Friends List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usersDatabase = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        usersListPage = (RecyclerView) findViewById(R.id.UserListPage);
        usersListPage.setHasFixedSize(true);
        usersListPage.setLayoutManager(new LinearLayoutManager(this));
        userListImg = (CircleImageView) findViewById(R.id.userListImg);

        query = usersDatabase.collection("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference ref = FirebaseFirestore.getInstance().collection("Users").document(uid);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        friends = (ArrayList<String>) document.get("friends");
                        if (friends.size() > 0) {
                            FirestoreRecyclerOptions<Users> options = new FirestoreRecyclerOptions.Builder<Users>().setQuery(query.whereIn("user_id", friends), Users.class).build();
                            FirestoreRecyclerAdapter<Users, UsersViewHolder> adapter = new FirestoreRecyclerAdapter<Users, UsersViewHolder>(options) {
                                @Override
                                public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                                    View view = LayoutInflater.from(parent.getContext())
                                            .inflate(R.layout.single_user_layout, parent, false);
                                    return new UsersViewHolder(view);
                                }

                                @Override
                                protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int i, @NonNull final Users users) {
                                    if (users != null) {
                                        usersViewHolder.setName(users.name);
                                        usersViewHolder.setStatus(users.status);
                                        usersViewHolder.setImg(users.image);

                                        final String userID = getSnapshots().getSnapshot(i).getId();
                                        usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence[] options = new CharSequence[]{"View Profile", "Send Message"};
                                                AlertDialog.Builder alert = new AlertDialog.Builder(FriendsList.this);
                                                alert.setTitle(users.name);
                                                alert.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (i == 0){
                                                            Intent profilePage = new Intent(FriendsList.this, ProfileActivity.class);
                                                            profilePage.putExtra("userID", userID);
                                                            startActivity(profilePage);
                                                        }else if (i == 1){
                                                            Intent conversationIntent = new Intent(FriendsList.this, ConversationActivity.class);
                                                            conversationIntent.putExtra("userID", userID);
                                                            conversationIntent.putExtra("name", users.name);
                                                            conversationIntent.putExtra("image", users.image);
                                                            startActivity(conversationIntent);
                                                        }
                                                    }
                                                });
                                                AlertDialog dialog =  alert.create();
                                                dialog.show();
                                            }
                                        });
                                    }
                                }
                            };
                            usersListPage.setAdapter(adapter);
                            adapter.startListening();
                        }
                    }
                }
            }
        });
    }
    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setName (String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.userListName);
            userNameView.setText(name);
        }
        public void setStatus (String status) {
            TextView userStatusVIew = (TextView) mView.findViewById(R.id.userListStatus);
            userStatusVIew.setText(status);
        }
        public void setImg (String image){
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.userListImg);
            if (!image.equals("default"))
                Picasso.get().load(image).placeholder(R.drawable.avatar).into(userImageView);
        }
        public void delete(){
            ViewGroup group = (ViewGroup) mView.getParent();
            group.removeView(mView);
        }
    }
}
