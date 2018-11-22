package secretbox.alisha.joshua.secretbox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private String UserID;

    private DatabaseReference mRootDb;
    StorageReference ImageStore;

    private TextView mName, mLastSeen;
    private CircleImageView Img;
    private String mCurUsrID;

    private FirebaseAuth mAuth;

    //handling message,add and send
    private ImageButton mAddBtn, mSendBtn;
    private EditText mChatMsg;

    private RecyclerView mMsgList;
    private final List<messages> msgsList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_LOAD = 10;
    private int mCurPage = 1;

    private SwipeRefreshLayout mRefresh;

    private int itemPos = 0;
    private String mlastKey = "";

    private static final int GALLERY_PICK = 1;

    String AES = "AES";
    DatabaseReference mUserRef;
    MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mp=new MediaPlayer();

        //fetching the basic data from firebase to work on
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
        mUserRef.child("online").setValue(true);
        UserID = getIntent().getStringExtra("uid");

        //setting up the toolbar(Customized ToolBar)
        mToolbar = findViewById(R.id.chatAppBar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("userName"));
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View ActionBarView = inflater.inflate(R.layout.chat_action_bar, null);
        actionBar.setCustomView(ActionBarView);

        //finding all th elements required to work on by id
        mName = findViewById(R.id.chatName);
        mLastSeen = findViewById(R.id.lastSeen);
        Img = findViewById(R.id.chatActImg);
        mAddBtn = findViewById(R.id.chatAddBtn);
        mSendBtn = findViewById(R.id.chatSendBtn);
        mChatMsg = findViewById(R.id.chatMessageText);
        mAdapter = new MessageAdapter(msgsList);//
        mMsgList = findViewById(R.id.msgLt); //message list Recycler View
        mRefresh = findViewById(R.id.msgSwipe);

        //setting up the paramters for displaying messages and the size,etc
        mLinearLayout = new LinearLayoutManager(this);
        mMsgList.hasFixedSize();
        mMsgList.setLayoutManager(mLinearLayout);
        mMsgList.setAdapter(mAdapter);

        //fetching the reference for the image folder stored on firebase server
        ImageStore = FirebaseStorage.getInstance().getReference();
        loadMessages();//function to load messages

        mName.setText(getIntent().getStringExtra("userName"));

        mRootDb = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurUsrID = mAuth.getCurrentUser().getUid();

        //fetching the current status of users form database
        mRootDb.child("users").child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = dataSnapshot.child("online").getValue().toString();
                String TImg = dataSnapshot.child("thumb_img").getValue().toString();
                String stat;
                if (status == "true") stat = "Online";
                else stat = "Offline";
                mLastSeen.setText(stat);
                Picasso.get().load(TImg).placeholder(R.drawable.profile).into(Img);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //using database reference to set the last seen and time stamp to be displayed
        mRootDb.child("Chat").child(mCurUsrID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(UserID)) {
                    Map ChatAddMapp = new HashMap();
                    ChatAddMapp.put("seen", false);
                    ChatAddMapp.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurUsrID + "/" + UserID, ChatAddMapp);
                    chatUserMap.put("Chat/" + UserID + "/" + mCurUsrID, ChatAddMapp);

                    mRootDb.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("ChatLog", databaseError.getMessage().toString());
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //add,message,send

        //Button that invokes the send message function
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }

        });

        //button that opens up the inent to send image
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image to send"), GALLERY_PICK);
            }
        });

        //every chat will display previous 10 messages, refreshing will load more 10
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            sendToStart();
        } else {
            mUserRef.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void sendToStart() {
        Intent startIntent = new Intent(ChatActivity.this, startActivity.class);
        startActivity(startIntent);
        finish();
    }

    //this handles the image select and upload to firebase
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            final String CurUsrRef = "messages/" + mCurUsrID + "/" + UserID;
            final String ChatUsrRef = "messages/" + UserID + "/" + mCurUsrID;

            DatabaseReference UsrMsgPush = mRootDb.child("mesasges").child(mCurUsrID).child(UserID).push();

            final String pushID = UsrMsgPush.getKey();

            final StorageReference filepath = ImageStore.child("image_msgs").child(pushID + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final Uri downloadURL = uri;
                                Map msgMap = new HashMap();
                                msgMap.put("message", downloadURL.toString());
                                msgMap.put("seen", false);
                                msgMap.put("type", "image");
                                msgMap.put("time", ServerValue.TIMESTAMP);
                                msgMap.put("from", mCurUsrID);


                                Map usrMap = new HashMap();
                                usrMap.put(CurUsrRef + "/" + pushID, msgMap);
                                usrMap.put(ChatUsrRef + "/" + pushID, msgMap);

                                mChatMsg.setText("");

                                mRootDb.updateChildren(usrMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Log.d("ChatLog", databaseError.getMessage().toString());
                                        }
                                    }
                                });
                            }
                        });


                    }
                }
            });

        }
    }

    //this hadles the loading of 10 more messages everytime the users scrolls down to refresh
    private void loadMoreMessages() {
        mRootDb = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurUsrID = mAuth.getCurrentUser().getUid();

        DatabaseReference mref = mRootDb.child("messages").child(mCurUsrID).child(UserID);
        Query mMsgQuery = mref.orderByKey().endAt(mlastKey);
        mMsgQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                messages msg = dataSnapshot.getValue(messages.class);
                msgsList.add(itemPos++, msg);
                if (itemPos == 1) {
                    String msgKey = dataSnapshot.getKey();
                    mlastKey = msgKey;
                }

                mAdapter.notifyDataSetChanged();

                mRefresh.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10, 0);
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


    private void loadMessages() {

        mRootDb = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurUsrID = mAuth.getCurrentUser().getUid();
        DatabaseReference mref = mRootDb.child("messages").child(mCurUsrID).child(UserID);
        Query mMsgQuery = mref.limitToLast(mCurPage * TOTAL_LOAD);


        mMsgQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                messages msg = dataSnapshot.getValue(messages.class);

                itemPos++;

                if (itemPos == 1) {
                    String msgKey = dataSnapshot.getKey();
                    mlastKey = msgKey;
                }

                msgsList.add(msg);
                mAdapter.notifyDataSetChanged();

                mMsgList.scrollToPosition(msgsList.size() - 1);
                mRefresh.setRefreshing(false);
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

    //sending the messages and storing on firebase database
    private void sendMessage() {
        String CUID = mAuth.getCurrentUser().getUid();
        DatabaseReference getPass = FirebaseDatabase.getInstance().getReference().child("users").child(CUID);
        getPass.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String PassKey = dataSnapshot.child("password").getValue().toString();
                String msg = mChatMsg.getText().toString();
                //Encrypting Message
                try {
                    String EncryptedMsg = encrypt(msg, PassKey);
                    if (!TextUtils.isEmpty(msg)) {
                        if (mp != null && mp.isPlaying()) { // check for null value also
                            mp.stop();
                            mp.reset();
                        } else {
                            mp = MediaPlayer.create(ChatActivity.this, R.raw.send);
                        }
                        mp.start();

                        String CurUsrRef = "messages/" + mCurUsrID + "/" + UserID;
                        String ChatUsrRef = "messages/" + UserID + "/" + mCurUsrID;

                        DatabaseReference UsrMsgPush = mRootDb.child("mesasges").child(mCurUsrID).child(UserID).push();
                        String pushId = UsrMsgPush.getKey();
                        Map msgMap = new HashMap();
                        msgMap.put("message", EncryptedMsg);
                        msgMap.put("seen", false);
                        msgMap.put("type", "text");
                        msgMap.put("time", ServerValue.TIMESTAMP);
                        msgMap.put("from", mCurUsrID);


                        Map usrMap = new HashMap();
                        usrMap.put(CurUsrRef + "/" + pushId, msgMap);
                        usrMap.put(ChatUsrRef + "/" + pushId, msgMap);

                        mChatMsg.setText("");


                        mRootDb.updateChildren(usrMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.d("ChatLog", databaseError.getMessage().toString());
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Type a message", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        //to set the others persons Encryption password to reeal message
        if (item.getItemId() == R.id.setpassword) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Password");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);

            // Set up the buttons to set the password
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String CUID = mAuth.getCurrentUser().getUid();
                    DatabaseReference addPass = FirebaseDatabase.getInstance().getReference().child("Chat").child(CUID).child(UserID);

                    String password = input.getText().toString();
                    addPass.child("passKey").setValue(password);
                    finish();
                    startActivity(getIntent());

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        }

        //Option to remove the Encryption password of the other person
        if (item.getItemId() == R.id.removepassword) {
            String CUID = mAuth.getCurrentUser().getUid();
            DatabaseReference addPass = FirebaseDatabase.getInstance().getReference().child("Chat").child(CUID).child(UserID);
            addPass.child("passKey").setValue("notset");
            Toast.makeText(this, "Password Removed", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(getIntent());

        }


        return true;
    }


    //Encryption/Decryption function that encrypts the messge before sending to firebase
    private String encrypt(String Data, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);
        return encryptedValue;

    }

    //this functions generates the key for encryption and decypting the message
    private SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

}
