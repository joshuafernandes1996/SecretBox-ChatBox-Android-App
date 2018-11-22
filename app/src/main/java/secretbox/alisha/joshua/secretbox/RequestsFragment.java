package secretbox.alisha.joshua.secretbox;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {
    private RecyclerView mReqList;
    private DatabaseReference mFriendReqDB, mRoot;
    private DatabaseReference mUsersDB;
    private FirebaseAuth mAuth;
    private String mCurUsrID;
    private View mMainView;
    private TextView mMsg;


    public RequestsFragment() {
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
        mMainView = inflater.inflate(R.layout.fragment_request, container, false);
        mAuth = FirebaseAuth.getInstance();
        mCurUsrID = mAuth.getCurrentUser().getUid();

        mUsersDB = FirebaseDatabase.getInstance().getReference().child("users");
        mFriendReqDB = FirebaseDatabase.getInstance().getReference().child("friend_req").child(mCurUsrID);


        mUsersDB.keepSynced(true);
        mFriendReqDB.keepSynced(true);
        mRoot = FirebaseDatabase.getInstance().getReference();


        mMsg = mMainView.findViewById(R.id.MessageRequest);
        final DatabaseReference CheckReq = FirebaseDatabase.getInstance().getReference().child("friend_req").child(mCurUsrID);
        CheckReq.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Query q = CheckReq.orderByChild("request_type");
                    q.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            String req_type = dataSnapshot.child("request_type").getValue().toString();
                            if (req_type.equals("sent")) {
                                mMsg.setVisibility(View.VISIBLE);
                            } else {
                                mMsg.setVisibility(View.INVISIBLE);
                            }

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    mMsg.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        final LayoutInflater inflator = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout single_req = mMainView.findViewById(R.id.reqItem);
        single_req.removeAllViews();

        final DatabaseReference CheckReq = FirebaseDatabase.getInstance().getReference().child("friend_req").child(mCurUsrID);
        CheckReq.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot data) {
                Query q = CheckReq.orderByChild("request_type");
                q.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String fromUID = dataSnapshot.getKey();
                        final String req_type = dataSnapshot.child("request_type").getValue().toString();
                            single_req.removeAllViews();
                            for (DataSnapshot dataSnap : data.getChildren()) {
                                final String fromReq = dataSnap.getKey();
                                final DatabaseReference getFromDetails = FirebaseDatabase.getInstance().getReference().child("users").child(fromUID);
                                getFromDetails.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (!fromReq.equals(mCurUsrID)) {
                                            if (req_type.equals("received")) {
                                                final String Name = dataSnapshot.child("name").getValue().toString();
                                                String pic = dataSnapshot.child("image").getValue().toString();
                                                View view = inflator.inflate(R.layout.request_layout, null);
                                                CircleImageView dp = view.findViewById(R.id.reqImg);
                                                Picasso.get().load(pic).placeholder(R.drawable.profile).into(dp);
                                                TextView NameText = view.findViewById(R.id.fromReq);
                                                NameText.setText(Name);
                                                Button accept = view.findViewById(R.id.reqAccept), reject = view.findViewById(R.id.reqReject);
                                                accept.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        final String curdate = DateFormat.getDateTimeInstance().format(new Date());
                                                        Map friendsMap = new HashMap();
                                                        friendsMap.put("friends/" + mCurUsrID + "/" + fromReq + "/date", curdate);
                                                        friendsMap.put("friends/" + fromReq + "/" + mCurUsrID + "/date", curdate);
                                                        friendsMap.put("friend_req/" + mCurUsrID + "/" + fromReq, null);
                                                        friendsMap.put("friend_req/" + fromReq + "/" + mCurUsrID, null);

                                                        mRoot.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                if (databaseError == null) {
                                                                    Toast.makeText(getContext(), "You are now friends with " + Name, Toast.LENGTH_SHORT).show();
                                                                    getActivity().finish();
                                                                    startActivity(getActivity().getIntent());

                                                                } else {
                                                                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }


                                                });

                                                reject.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Map unfriendMap = new HashMap();
                                                        unfriendMap.put("friend_req/" + mCurUsrID + "/" + fromReq, null);
                                                        unfriendMap.put("friend_req/" + fromReq + "/" + mCurUsrID, null);

                                                        mRoot.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                if (databaseError == null) {
                                                                    Toast.makeText(getContext(), Name + " is Rejected", Toast.LENGTH_SHORT).show();
                                                                    getActivity().finish();
                                                                    startActivity(getActivity().getIntent());
                                                                } else {
                                                                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                                single_req.addView(view);
                                            }


                                        }


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }


}
