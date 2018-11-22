package secretbox.alisha.joshua.secretbox;

import android.app.Application;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class SecretBox extends Application {
    private DatabaseReference mUserDB;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //Picasso Offline Data Store

        Picasso.Builder Builder = new Picasso.Builder(this);
        Builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = Builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            mUserDB = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

            mUserDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null)
                        mUserDB.child("online").onDisconnect().setValue(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

}
