package secretbox.alisha.joshua.secretbox;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mtoolbar;
    private ViewPager mViewPager;
    private tabsPagerAdapter mtabsPagerAdapter;
    private DatabaseReference mUserRef;
    private TabLayout mtablyout;
    private Animation an;
    private ImageView error;
    private TextView terror;
    private Button bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            mAuth = FirebaseAuth.getInstance();
            mtoolbar = findViewById(R.id.mainAppBar);
            setSupportActionBar(mtoolbar);
            getSupportActionBar().setTitle("Secret Box");
            an = AnimationUtils.loadAnimation(this, R.anim.blink);
            error = findViewById(R.id.error);
            terror = findViewById(R.id.textError);
            mtablyout = findViewById(R.id.mainTabs);
            bluetooth = findViewById(R.id.bluetoothBtn);

            View contextView = findViewById(R.id.mainv);
            if (mAuth.getCurrentUser() != null) {
                mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
                Snackbar.make(contextView, "Logged in as: " + mAuth.getCurrentUser().getEmail(), Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).setActionTextColor(Color.RED).show();
            }

            mViewPager = findViewById(R.id.tabPager);

            Boolean CheckNetwork = isNetworkAvailable();
            if (CheckNetwork.equals(false)) {
                error.startAnimation(an);
                terror.startAnimation(an);
                Snackbar.make(contextView, "Check Internet Connection", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).setActionTextColor(Color.RED).show();
                bluetooth.setVisibility(View.VISIBLE);
                bluetooth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent bluetoothIntent = new Intent(getApplicationContext(), BluetoothActivity.class);
                        startActivity(bluetoothIntent);
                    }
                });
            } else {

                //setting up headers for the three tabs namely requests,chats and friends
                mtabsPagerAdapter = new tabsPagerAdapter(getSupportFragmentManager());
                mViewPager.setAdapter(mtabsPagerAdapter);
                mtablyout = findViewById(R.id.mainTabs);
                mtablyout.setupWithViewPager(mViewPager);
            }

            //checking the origin of intent
            String checkfrom = getIntent().getStringExtra("from");
            if (checkfrom == "notification") {
                mViewPager.setCurrentItem(0);
            } else {
                mViewPager.setCurrentItem(2);



        }

        PrefManager prefManager = new PrefManager(getApplicationContext());
        if(!prefManager.isFirstTimeLaunch()){
            if(prefManager.isMainFirst()){
                new TapTargetSequence(this)
                        .targets(
                                TapTarget.forView(findViewById(R.id.mainTabs), "Main Tabs","We have Three Tabs one for Request, another for Chats and other for Friends"),
                                TapTarget.forView(findViewById(R.id.mainToolBar), "Action", "On the right Corner of this action bar you will find options for Account Settings\nFind Users\nAnd Logout")
                                        .dimColor(R.color.purple)
                                        .drawShadow(true)
                                        .tintTarget(false)
                                        .outerCircleColor(R.color.colorAccent)
                                        .targetCircleColor(R.color.white)
                                        .transparentTarget(false)
                                        .textColor(android.R.color.white)
                                        .tintTarget(false))
                        .listener(new TapTargetSequence.Listener() {
                            @Override
                            public void onSequenceFinish() {
                            }

                            @Override
                            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                            }

                            @Override
                            public void onSequenceCanceled(TapTarget lastTarget) {
                            }
                        }).start();
                prefManager.setMainFirst(false);
            }
        }






    }


    @Override
    public void onStart() {
        super.onStart();
        PrefManager prefManager = new PrefManager(getApplicationContext());
        if(prefManager.isFirstTimeLaunch()){
            prefManager.setFirstTimeLaunch(false);
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            finish();
        }
        else{
            // Check if user is signed in (non-null) and update UI accordingly. Also sets the user online till he or she logs out or closes the app
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Intent start=new Intent(getApplicationContext(),startActivity.class);
                start.putExtra("firstTime","yes");
                startActivity(start);
                finish();
                //sendToStart();
            } else {
                mUserRef.child("online").setValue(true);
            }
        }




    }


    //this function checks if the application is closed, if its closed we set the user as offline
    public void onTrimMemory(final int level) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            if (currentUser != null) {
                mUserRef.child("online").setValue(false);
            }
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, startActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //setting up what does what when options are selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.logout) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                mUserRef.child("online").setValue(false);
            }
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if (item.getItemId() == R.id.settings) {
            Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settingsIntent);
        }

        if (item.getItemId() == R.id.users) {
            Intent usersIntent = new Intent(getApplicationContext(), UsersActivity.class);
            startActivity(usersIntent);
        }

        if (item.getItemId() == R.id.dev_info) {
            Intent infoIntent = new Intent(getApplicationContext(), aboutus.class);
            startActivity(infoIntent);
        }

        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
