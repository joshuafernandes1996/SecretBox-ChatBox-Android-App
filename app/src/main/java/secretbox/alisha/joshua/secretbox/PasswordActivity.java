package secretbox.alisha.joshua.secretbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PasswordActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private TextInputLayout mPassword;
    private Button mSave;

    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mPD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        //fetching initial details required
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        //setting up the toolbar
        mtoolbar = findViewById(R.id.PasswordAppBar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Encryption Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPD = new ProgressDialog(this);
        //getting the elements needed
        mPassword = findViewById(R.id.passwordChange);
        mSave = findViewById(R.id.savePassword);


        //saving the password onclick to firebase
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPD.setTitle("Saving Changes");
                mPD.setMessage("Please wait while changes are being reflected.");
                mPD.show();
                String password = mPassword.getEditText().getText().toString();
                mDatabase.child("password").setValue(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mPD.dismiss();
                            Intent profileIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                            startActivity(profileIntent);
                            Toast.makeText(getApplicationContext(), "Encryption Password Changed!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error in saving changes", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
