package net.whispwriting.mantischat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

public class Chat extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public Intent lastIntent;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    public static String CHANNEL_ID = "main";
    private DocumentReference userDoc;
    private FirebaseAuth user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(this, Login.class));
            return;
        }
        setContentView(R.layout.activity_chat);
        Toolbar toolbar1 = (Toolbar) findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar1);
        getSupportActionBar().setTitle("Chat");
        Intent intent = new Intent(this, Chat.class);
        lastIntent = intent;
        createNotificationChannel();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar1, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        try {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
        catch (Exception e){
            startActivity(lastIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            Intent loginSplash = new Intent(this, Login.class);
            startActivity(loginSplash);
        }
        if (item.getItemId() == R.id.action_accounts){
            Intent accountSettings = new Intent(this, AccountSettings.class);
            startActivity(accountSettings);
        }
        if (item.getItemId() == R.id.action_usersList){
            Intent userList = new Intent(this, UserList.class);
            startActivity(userList);
        }

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_chat) {
            onBackPressed();
        }
        if (id == R.id.nav_friends){
            startActivity(new Intent(this, FriendsList.class));
        }
        if (id == R.id.nav_search_users){
            startActivity(new Intent(this, UserList.class));
        }
        if (id == R.id.nav_account_settings){ // Put ad code here
            startActivity(new Intent(this, AccountSettings.class));
        }
        if (id == R.id.nav_requests){
            startActivity(new Intent(this, FriendRequestList.class));
        }

        return true;
    }

    @SuppressLint("WrongConstant")
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.channelName);
            String description = getString(R.string.channelDescription);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_MAX);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
