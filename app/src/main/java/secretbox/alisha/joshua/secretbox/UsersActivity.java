package secretbox.alisha.joshua.secretbox;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mUserRef;
    private Query query;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatabase.keepSynced(true);

        if (mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        }


        //setting toolbar
        mtoolbar = findViewById(R.id.usersAppBar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Adding List using recylerView
        mUsersList = findViewById(R.id.usersList);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        //Setting Layout view for each user
        query = FirebaseDatabase.getInstance().getReference();
        FirebaseListOptions<users> options = new FirebaseListOptions.Builder<users>()
                .setLayout(R.layout.single_user_layout)
                .setQuery(query, users.class)
                .build();
    }

    private void sendToStart() {
        Intent startIntent = new Intent(getApplicationContext(), startActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {

            sendToStart();
        } else {
            mUserRef.child("online").setValue(true);
        }

        FirebaseRecyclerOptions<users> options = new FirebaseRecyclerOptions.Builder<users>()
                .setQuery(mUsersDatabase, users.class)
                .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<users, usersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull usersViewHolder holder, int position, @NonNull users model) {
                final String Uid = getRef(position).getKey();
                if (!currentUser.equals(Uid)) {
                    holder.setName(model.getName());
                    holder.setStatus(model.getStatus());
                    holder.setThumbImage(model.getThumb_img(), getApplicationContext());


                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                            profileIntent.putExtra("uid", Uid);
                            startActivity(profileIntent);
                        }
                    });
                } else {

                }

            }


            @Override
            public usersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);

                return new usersViewHolder(view);
            }
        };
        mUsersList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class usersViewHolder extends RecyclerView.ViewHolder {

        static View mView;

        public usersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public static void setName(String name) {
            TextView mUserName = mView.findViewById(R.id.user_name);
            mUserName.setText(name);

        }

        public static void setStatus(String status) {
            TextView mUserStatus = mView.findViewById(R.id.userSingleStatus);
            mUserStatus.setText(status);

        }

        public static void setThumbImage(String thumbImage, Context SingleUserContext) {
            CircleImageView mUserImage = mView.findViewById(R.id.user_img);
            Picasso.get().load(thumbImage).placeholder(R.drawable.profile).into(mUserImage);

        }
    }
}
