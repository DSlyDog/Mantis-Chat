package net.whispwriting.mantischat;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class MantisChat extends Application {

    public static MainEnabled mainEnabled;
    private FirebaseUser currentUser;
    private DatabaseReference database;

    @Override
    public void onCreate(){
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        mainEnabled = new MainEnabled();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance()
                .getReference().child("Users").child(currentUser.getUid());

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                database.child("online").onDisconnect().setValue(false);
                database.child("online").setValue(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
