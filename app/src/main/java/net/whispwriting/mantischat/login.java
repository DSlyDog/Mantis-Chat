package net.whispwriting.mantischat;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    private FirebaseAuth mAuth;
    private Button registryBtn;
    private Button loginBtn;
    private Toolbar rToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        rToolbar = (Toolbar) findViewById(R.id.loginToolbar);
        setSupportActionBar(rToolbar);
        getSupportActionBar().setTitle("Chat");

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        try {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
        }
        catch (Exception e){
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_chat) {
            // Handle the camera action
            Intent intent = new Intent (this, Chat.class);
            startActivity(intent);
        }
        if (id == R.id.nav_chat) {
            Intent intent = new Intent(this, login.class);
            startActivity(intent);
        }

        return true;
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            Intent chat = new Intent(this, Chat.class);
            startActivity(chat);
            finish();
        }
        else {
            registryBtn = (Button) findViewById(R.id.register);
            registryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent regIntent = new Intent(login.this, Registration.class);
                    startActivity(regIntent);
                }
            });
            loginBtn = (Button) findViewById(R.id.login);
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent logIntent = new Intent(login.this, LoggingIn.class);
                    startActivity(logIntent);
                }
            });
        }
    }
}

