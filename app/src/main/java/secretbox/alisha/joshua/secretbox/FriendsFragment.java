package secretbox.alisha.joshua.secretbox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendsFragment extends Fragment {

    private DatabaseReference mFriendsDB;
    private DatabaseReference mUsersDB;
    private FirebaseAuth mAuth;

    private String mCurUsrID;

    private View mMainView;

    private Query query;

    private TextView mMsg;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //getFragmentManager().beginTransaction().detach(this).attach(this).commit();

        query = FirebaseDatabase.getInstance().getReference();//getting database reference
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mAuth = FirebaseAuth.getInstance();//getting the user's instance

        mCurUsrID = mAuth.getCurrentUser().getUid();//current user

        //Setting up references for friends and users.
        mFriendsDB = FirebaseDatabase.getInstance().getReference().child("friends").child(mCurUsrID);
        mFriendsDB.keepSynced(true);
        mUsersDB = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDB.keepSynced(true);

        mMsg = mMainView.findViewById(R.id.MessageFriends);

        //below code checks condition to display error message or not
        DatabaseReference CheckReq = FirebaseDatabase.getInstance().getReference().child("friends").child(mCurUsrID);
        CheckReq.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    mMsg.setVisibility(View.INVISIBLE);
                } else {
                    mMsg.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FloatingActionButton fab = mMainView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent UserIntent = new Intent(getContext(), UsersActivity.class);
                startActivity(UserIntent);
            }
        });





            return mMainView;

    }

    @Override
    public void onResume() {
        PrefManager prefManager = new PrefManager(getContext());
        if (!prefManager.isFirstTimeLaunch()) {
            if (prefManager.isChatFirst()) {
                new TapTargetSequence(getActivity())
                        .targets(
                                TapTarget.forView(mMainView.findViewById(R.id.fab), "Add New Friend", "You Can find new Friends by Clicking here")
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
                prefManager.setChatFirst(false);
            }
        }
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        //getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        final LayoutInflater inflator = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout single_friend = mMainView.findViewById(R.id.ScrollFriends);
        single_friend.removeAllViews();

        final DatabaseReference CheckFriends = FirebaseDatabase.getInstance().getReference().child("friends").child(mCurUsrID);
        final DatabaseReference userInfo = FirebaseDatabase.getInstance().getReference().child("users");

        CheckFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()){
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            final View view = inflator.inflate(R.layout.single_user_layout, null);
                            final String UID=postSnapshot.getKey();
                            final String LastSeen=postSnapshot.child("date").getValue().toString();
                            //Toast.makeText(getContext(),UID,Toast.LENGTH_LONG).show();
                            userInfo.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    final CircleImageView im=view.findViewById(R.id.user_img);
                                    final TextView name=view.findViewById(R.id.user_name);
                                    final TextView status=view.findViewById(R.id.userSingleStatus);
                                    final ImageView onlinestatus=view.findViewById(R.id.onlineIcon);
                                    name.setText(dataSnapshot.child("name").getValue().toString());
                                    status.setText(LastSeen);
                                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.profile).into(im);

                                    if(dataSnapshot.hasChild("online")){
                                        Boolean online=(Boolean) dataSnapshot.child("online").getValue();
                                        if(online)
                                            onlinestatus.setVisibility(View.VISIBLE);
                                        else
                                            onlinestatus.setVisibility(View.INVISIBLE);
                                    }

                                    view.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            CharSequence options[]=new CharSequence[]{"Open Profile","Send Message"};
                                            AlertDialog.Builder mBuilder=new AlertDialog.Builder(getContext());
                                            mBuilder.setTitle("Select Options");
                                            mBuilder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if(which==0){
                                                        Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                                        profileIntent.putExtra("uid",UID);
                                                        startActivity(profileIntent);
                                                    }

                                                    if(which==1){
                                                        Intent ChatIntent=new Intent(getContext(),ChatActivity.class);
                                                        ChatIntent.putExtra("uid",UID);
                                                        ChatIntent.putExtra("userName",name.getText().toString());
                                                        startActivity(ChatIntent);
                                                    }
                                                }
                                            });
                                            mBuilder.show();

                                        }
                                    });
                                    single_friend.addView(view);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }



                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }
}
