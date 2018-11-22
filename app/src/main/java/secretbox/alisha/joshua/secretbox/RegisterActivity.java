package secretbox.alisha.joshua.secretbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mUsername, mEmail, mPassword, mMsgEnPass;
    private Button mregister;
    private FirebaseAuth mAuth, mGuest;
    private Toolbar mToolbar;
    private DatabaseReference mDBRemoveUser;
    private ProgressDialog mRegProgress;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final View contextView = findViewById(R.id.registerView);

        mUsername = findViewById(R.id.Uname);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mMsgEnPass = findViewById(R.id.mpassword);
        mregister = findViewById(R.id.submit_reg_btn);
        mToolbar = findViewById(R.id.reg_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);


        mDBRemoveUser = FirebaseDatabase.getInstance().getReference().child("users");


        mregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean CheckNetwork = isNetworkAvailable();
                if (CheckNetwork.equals(false)) {
                    Snackbar.make(contextView, "Check Internet Connection", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).setActionTextColor(Color.RED).show();
                } else {
                    String DisplayName = mUsername.getEditText().getText().toString();
                    String email = mEmail.getEditText().getText().toString();
                    String password = mPassword.getEditText().getText().toString();
                    String mpassword = mMsgEnPass.getEditText().getText().toString();
                    mAuth = FirebaseAuth.getInstance();

                    if (!TextUtils.isEmpty(DisplayName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(mpassword)) {

                        if (password.length() < 8) {
                            Toast.makeText(getApplicationContext(), "Enter a Password of 8 Characters Long", Toast.LENGTH_LONG).show();
                        } else {
                            mRegProgress.setTitle("Registering your Account");
                            mRegProgress.setMessage("Please wait while your account is being created");
                            mRegProgress.setCanceledOnTouchOutside(false);
                            mRegProgress.show();
                            registerUser(DisplayName, email, password, mpassword);
                        }
                    } else {
                        if (TextUtils.isEmpty(DisplayName))
                            Toast.makeText(getApplicationContext(), "Enter Display Name", Toast.LENGTH_LONG).show();
                        else if (TextUtils.isEmpty(email))
                            Toast.makeText(getApplicationContext(), "Enter an Email to Register Account on", Toast.LENGTH_LONG).show();
                        else if (TextUtils.isEmpty(password))
                            Toast.makeText(getApplicationContext(), "Enter a Password of 8 Characters Long", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "Enter Encryption Password to Encrypt your messages", Toast.LENGTH_LONG).show();

                    }
                }

            }

        });
    }

    //function to register user
    private void registerUser(final String displayName, String email, String password, final String mpassword) {
        final View contextView = findViewById(R.id.registerView);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {

                            }
                            String uid = user.getUid();
                            String token = FirebaseInstanceId.getInstance().getToken();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", displayName);
                            userMap.put("status", "I am using SecretBox");
                            userMap.put("image", "default");
                            userMap.put("thumb_img", "default");
                            userMap.put("token", token);
                            userMap.put("password", mpassword);

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mRegProgress.dismiss();
                                        //Sign in success, update UI with the signed-in user's information
                                        final MediaPlayer mp = MediaPlayer.create(RegisterActivity.this, R.raw.login);
                                        mp.start();
                                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });


                            Snackbar.make(contextView, "Authentication Successful.", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).setActionTextColor(Color.RED).show();
                        } else {
                            mRegProgress.hide();
                            Boolean CheckNetwork = isNetworkAvailable();
                            if (CheckNetwork.equals(false)) {
                                Snackbar.make(contextView, "Check Internet Connection", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).setActionTextColor(Color.RED).show();
                            } else {
                                // If sign in fails, display a message to the user.
                                Snackbar.make(contextView, "Registration failed, Please check form and try again.", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).setActionTextColor(Color.RED).show();
                            }
                        }

                    }
                });
    }

    //checking for internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
