package secretbox.alisha.joshua.secretbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mloginTool;


    private TextInputLayout mEmail, mPassword;
    private Button login;

    private ProgressDialog loginAct;

    private FirebaseAuth mAuth;

    private DatabaseReference mLoginDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mloginTool = findViewById(R.id.loginAppBar);
        setSupportActionBar(mloginTool);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginAct = new ProgressDialog(this);
        loginAct.setTitle("Logging In");
        loginAct.setMessage("Please wait while you are being Verified. ");
        loginAct.setCanceledOnTouchOutside(false);

        mLoginDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        login = findViewById(R.id.login_btn);

        //login button function
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean CheckNetwork = isNetworkAvailable();
                if (CheckNetwork.equals(null)) {
                    Toast.makeText(getApplicationContext(), "Check Internet Connection", Toast.LENGTH_SHORT).show();
                } else {
                    String email = mEmail.getEditText().getText().toString();
                    String password = mPassword.getEditText().getText().toString();

                    if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                        loginAct.show();
                        loginUser(email, password);
                    } else {
                        if (TextUtils.isEmpty(email)) {
                            Toast.makeText(getApplicationContext(), "Please Enter Your Email.", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getApplicationContext(), "Please Enter Your Password.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
        });
    }

    //function for login
    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loginAct.dismiss();

                    String curUser = mAuth.getCurrentUser().getUid();
                    String token = FirebaseInstanceId.getInstance().getToken();

                    mLoginDatabase.child(curUser).child("token").setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
                    final MediaPlayer mp = MediaPlayer.create(LoginActivity.this, R.raw.login);
                    mp.start();
                    Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                } else {
                    loginAct.hide();
                    Boolean CheckNetwork = isNetworkAvailable();
                    if (CheckNetwork.equals(false)) {
                        Toast.makeText(getApplicationContext(), "Check Internet Connection", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Login failed, Please check form and try again.",
                                Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    //function to check for internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
