package secretbox.alisha.joshua.secretbox;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {



    private DatabaseReference mConvDB;
    private DatabaseReference mUsersDB;
    private FirebaseAuth mAuth;
    private DatabaseReference mMsgDB;

    private String mCurUsrID;

    private View mMainView;

    private TextView mMessage;

    public ChatsFragment() {
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

        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();

        //fetching intial data to work on
        mCurUsrID = mAuth.getCurrentUser().getUid();
        mUsersDB = FirebaseDatabase.getInstance().getReference().child("users");
        mMsgDB = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurUsrID);
        mConvDB = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurUsrID);
        mMessage = mMainView.findViewById(R.id.MessageChat);

        //below code handles when to show the background message if there are no conversations
        DatabaseReference CheckChat = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurUsrID);
        CheckChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    mMessage.setVisibility(View.INVISIBLE);
                } else {
                    mMessage.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        LinearLayoutManager linear = new LinearLayoutManager(getContext());
        linear.setReverseLayout(true);
        linear.setStackFromEnd(true);

        mUsersDB.keepSynced(true);
        mConvDB.keepSynced(true);
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        final LayoutInflater inflator = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout single_friend = mMainView.findViewById(R.id.chatsList);
        single_friend.removeAllViews();

        mMsgDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    final View view = inflator.inflate(R.layout.single_user_layout, null);
                    final TextView status=view.findViewById(R.id.userSingleStatus);
                    final TextView name=view.findViewById(R.id.user_name);
                    final CircleImageView img=view.findViewById(R.id.user_img);
                    final String UID=postSnapshot.getKey();
                    Query lastMessage = mMsgDB.child(UID).limitToLast(1);
                    lastMessage.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            String Data = dataSnapshot.child("message").getValue().toString();
                            status.setText(Data);
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
                    //Toast.makeText(getContext(),UID,Toast.LENGTH_LONG).show();
                    mUsersDB.child(UID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            name.setText(userName);
                            Picasso.get().load(dataSnapshot.child("thumb_img").getValue().toString()).placeholder(R.drawable.profile).into(img);

                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};

                                    Intent ChatIntent = new Intent(getContext(), ChatActivity.class);
                                    ChatIntent.putExtra("uid", UID);
                                    ChatIntent.putExtra("userName", userName);
                                    startActivity(ChatIntent);


                                }
                            });
                            if(view.getParent()!=null){
                                ((ViewGroup)view.getParent()).removeView(view);
                            }
                            single_friend.addView(view);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

}
