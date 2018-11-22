package secretbox.alisha.joshua.secretbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class ProfileActivity extends AppCompatActivity {

    private TextView name, status, total_friends;
    private Toolbar mtoolbar;
    private ImageView Profileimage;
    private Button sendRequest, declineRequest;
    private FirebaseAuth mAuth;

    private DatabaseReference mUsersdatabase, mFriendRequestDatabase, mFriendsDatabase, mNotificationDatabase, mRoot, mUserRef;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressBar;

    private String currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //setting up ToolBar
        mtoolbar = findViewById(R.id.profileAppBar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        total_friends = findViewById(R.id.totFriends);

        mAuth = FirebaseAuth.getInstance();

        //checking if the user is logged in by getting the instance
        if (mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        }


        mRoot = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        final String uid = getIntent().getStringExtra("uid");


        mUsersdatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("friend_req");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");


        DatabaseReference FriendsCount = FirebaseDatabase.getInstance().getReference().child("friends").child(uid);
        FriendsCount.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                total_friends.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Profileimage = findViewById(R.id.profileImage);
        name = findViewById(R.id.displayName);
        status = findViewById(R.id.status);


        sendRequest = findViewById(R.id.sendRequest);
        declineRequest = findViewById(R.id.declineRequest);
        if (mCurrentUser.getUid().equals(uid)) {
            sendRequest.setVisibility(View.INVISIBLE);
            declineRequest.setVisibility(View.INVISIBLE);
        }

        declineRequest.setVisibility(View.INVISIBLE);
        declineRequest.setEnabled(false);

        currentState = "Not Friends";

        mProgressBar = new ProgressDialog(this);
        mProgressBar.setTitle("Loading User Data");
        mProgressBar.setMessage("Please wait while we load user Data");
        mProgressBar.setCanceledOnTouchOutside(false);
        mProgressBar.show();

        mUsersdatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final int radius = 100;
                final int margin = 20;
                final Transformation transformation = new RoundedCornersTransformation(radius, margin);

                String DisplayName = dataSnapshot.child("name").getValue().toString();
                String Status = dataSnapshot.child("status").getValue().toString();
                String ImageURL = dataSnapshot.child("image").getValue().toString();
                name.setText(DisplayName);
                status.setText(Status);
                Picasso.get().load(ImageURL).transform(transformation).placeholder(R.drawable.profile).into(Profileimage);


                //check friend state and changing the state accordingly
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(uid)) {
                            String ReqType = dataSnapshot.child(uid).child("request_type").getValue().toString();
                            if (ReqType.equals("received")) {
                                currentState = "RequestReceived";
                                sendRequest.setText("Accept Friend Request");
                                declineRequest.setVisibility(View.VISIBLE);
                                declineRequest.setEnabled(true);
                            } else if (ReqType.equals("sent")) {
                                currentState = "RequestSent";
                                sendRequest.setText("Cancel Friend Request");
                                declineRequest.setVisibility(View.INVISIBLE);
                                declineRequest.setEnabled(false);
                            }
                            mProgressBar.dismiss();
                        } else {
                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(uid)) {
                                        currentState = "Friends";
                                        sendRequest.setText("Unfriend " + name.getText().toString());
                                        declineRequest.setVisibility(View.INVISIBLE);
                                        declineRequest.setEnabled(false);
                                    }
                                    mProgressBar.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressBar.dismiss();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendRequest.setEnabled(false);
                sendRequest.setBackgroundColor(Color.parseColor("#D3D3D3"));

                //SEND REQUEST handler
                if (currentState.equals("Not Friends")) {
                    DatabaseReference newNotRef = mRoot.child("notifications").child(uid).push();
                    String newNotID = newNotRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap<>();
                    requestMap.put("friend_req/" + mCurrentUser.getUid() + "/" + uid + "/request_type", "sent");
                    requestMap.put("friend_req/" + uid + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    //requestMap.put("friend_req/"+mCurrentUser.getUid()+"/"+uid+"/status","notfriends");
                    //requestMap.put("friend_req/"+uid+"/"+mCurrentUser.getUid()+"/status","notfriends");
                    requestMap.put("notifications/" + uid + "/" + newNotID, notificationData);

                    mRoot.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(getApplicationContext(), "Error in Sending Request", Toast.LENGTH_SHORT).show();
                            }
                            sendRequest.setEnabled(true);
                            currentState = "RequestSent";
                            sendRequest.setText("Cancel Friend Request");
                        }
                    });

                }

                //CANCEL REQUEST handler
                if (currentState.equals("RequestSent")) {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    sendRequest.setEnabled(true);
                                    currentState = "Not Friends";
                                    sendRequest.setText("Send Friend Request");

                                    declineRequest.setVisibility(View.INVISIBLE);
                                    declineRequest.setEnabled(false);
                                }
                            });
                        }
                    });

                }
                //Request Received handler
                if (currentState.equals("RequestReceived")) {

                    final String curdate = DateFormat.getDateTimeInstance().format(new Date());
                    Map friendsMap = new HashMap();
                    friendsMap.put("friends/" + mCurrentUser.getUid() + "/" + uid + "/date", curdate);
                    friendsMap.put("friends/" + uid + "/" + mCurrentUser.getUid() + "/date", curdate);

                    friendsMap.put("friend_req/" + mCurrentUser.getUid() + "/" + uid, null);
                    friendsMap.put("friend_req/" + uid + "/" + mCurrentUser.getUid(), null);


                    mRoot.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                sendRequest.setEnabled(true);
                                currentState = "Friends";
                                sendRequest.setText("Unfriend " + name.getText().toString());

                                declineRequest.setVisibility(View.INVISIBLE);
                                declineRequest.setEnabled(false);
                            } else {
                                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                //unfriend handler
                if (currentState.equals("Friends")) {
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("friends/" + mCurrentUser.getUid() + "/" + uid, null);
                    unfriendMap.put("friends/" + uid + "/" + mCurrentUser.getUid(), null);

                    mRoot.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                sendRequest.setEnabled(true);
                                currentState = "Not Friends";
                                sendRequest.setText("Send Friend Request to " + name.getText().toString());

                                declineRequest.setVisibility(View.INVISIBLE);
                                declineRequest.setEnabled(false);
                            } else {
                                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        declineRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map unfriendMap = new HashMap();
                unfriendMap.put("friends_req/" + mCurrentUser.getUid() + "/" + uid, null);
                unfriendMap.put("friends_req/" + uid + "/" + mCurrentUser.getUid(), null);

                mRoot.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            sendRequest.setEnabled(true);
                            currentState = "Not Friends";
                            sendRequest.setText("Send Friend Request to " + name.getText().toString());

                            declineRequest.setVisibility(View.INVISIBLE);
                            declineRequest.setEnabled(false);
                        } else {
                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


    }


    @Override
    public void onStart() {

        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            sendToStart();
        } else {
            mUserRef.child("online").setValue(true);
        }
    }


    private void sendToStart() {
        Intent startIntent = new Intent(getApplicationContext(), startActivity.class);
        startActivity(startIntent);
        finish();
    }
}
