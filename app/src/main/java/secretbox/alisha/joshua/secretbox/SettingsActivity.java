package secretbox.alisha.joshua.secretbox;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jackandphantom.circularprogressbar.CircleProgressbar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import io.saeid.fabloading.LoadingView;

public class SettingsActivity extends AppCompatActivity {
    //initialising Variables
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private TextView mName, mStatus;
    private Button mChangeImage, mChangeStatus, mChangePassword;
    private CircleImageView mImage;
    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStore;
    private ProgressDialog mProgressD;
    private byte[] thumbnail_byte;
    private Bitmap thumbnail;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference mUserRef;
    CircleProgressbar progress;
    TextView percent;
    ImageView menu;
    Animation an;
    LoadingView fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        fab=findViewById(R.id.loading_view);


        an=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoom_in);
        progress=findViewById(R.id.profileProgress);
        percent=findViewById(R.id.percent);
        menu=findViewById(R.id.settingsMenu);
        final View contextView=findViewById(R.id.settingView);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference mDef=FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
                PopupMenu popup = new PopupMenu(SettingsActivity.this, menu);
                popup.getMenuInflater().inflate(R.menu.setting_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.removeimage){
                            mDef.child("image").setValue("default");
                            mDef.child("thumb_img").setValue("default");
                            Snackbar.make(contextView, "Profile Picture Removed", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).setActionTextColor(Color.RED).show();
                        }
                        if(item.getItemId()==R.id.removestatus){
                            mDef.child("status").setValue("I am using SecretBox");
                            Snackbar.make(contextView, "Status Removed", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).setActionTextColor(Color.RED).show();
                        }
                        return true;
                    }
                });

                popup.show();
            }
        });


        //setting the users online
        if (mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
            mUserRef.child("online").setValue(true);

        }

        //fetching the image database
        mImageStore = FirebaseStorage.getInstance().getReference();

        //finding elements required
        mChangeImage = findViewById(R.id.chg_image);
        mChangeStatus = findViewById(R.id.chg_status);
        mChangePassword = findViewById(R.id.chg_password);
        mName = findViewById(R.id.profile_name);
        mStatus = findViewById(R.id.profile_status);
        mImage = findViewById(R.id.profile_image);

        //getting the initial data form firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();


                if(status.equals("I am using SecretBox")|| image.equals("default")){
                    if(status.equals("I am using SecretBox") & image.equals("default")){
                        progress.setProgressWithAnimation(50, 2000);
                        percent.setText("50%\nComplete");
                    }
                    if(status.equals("I am using SecretBox") & !image.equals("default")){
                        progress.setProgressWithAnimation(75, 2000);
                        percent.setText("75%\nComplete");
                    }
                    if(!status.equals("I am using SecretBox") & image.equals("default")){
                        progress.setProgressWithAnimation(75, 2000);
                        percent.setText("75%\nComplete");
                    }
                }
                else{
                    percent.setText("100%\nComplete");
                    progress.setProgressWithAnimation(100, 2000);
                }

                mName.setText(name);
                mStatus.setText(status);

                if (!image.equals("default")) {

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            mImage.setAnimation(an);
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(mImage);
                            mImage.setAnimation(an);

                        }
                    });
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //opens activity to change status
        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status = mStatus.getText().toString();
                Intent changeStatusIntent = new Intent(getApplicationContext(), StatusActivity.class);
                changeStatusIntent.putExtra("status", status);
                startActivity(changeStatusIntent);

            }
        });

        //opens image intent
        mChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageChange = new Intent();
                imageChange.setType("image/*");
                imageChange.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(imageChange, "Select Image"), GALLERY_PICK);


            }
        });

        //opens activity to change encryption password
        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final String[] m_Text = {""};
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Please Enter Your Old Encryption Password to Verify.");
                final EditText input = new EditText(SettingsActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);
                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text[0] = input.getText().toString();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String pass = dataSnapshot.child("password").getValue().toString();
                                if (m_Text[0].equals(pass)) {
                                    Intent changePasswordIntent = new Intent(getApplicationContext(), PasswordActivity.class);
                                    startActivity(changePasswordIntent);
                                } else {
                                    View contextView = findViewById(R.id.settingView);
                                    Snackbar.make(contextView, "Wrong Password.", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                        }
                                    }).setActionTextColor(Color.RED).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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

        }
    }


    private void sendToStart() {
        Intent startIntent = new Intent(getApplicationContext(), startActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final View contextView = findViewById(R.id.settingView);
        int marvel_1 =   R.drawable.marvel_1;
        int marvel_2 =   R.drawable.marvel_2;
        int marvel_3 =   R.drawable.marvel_3;
        int marvel_4 =   R.drawable.marvel_4;
        fab.addAnimation(Color.parseColor("#FFD200"), marvel_1, LoadingView.FROM_LEFT);
        fab.addAnimation(Color.parseColor("#2F5DA9"), marvel_2, LoadingView.FROM_TOP);
        fab.addAnimation(Color.parseColor("#FF4218"), marvel_3, LoadingView.FROM_RIGHT);
        fab.addAnimation(Color.parseColor("#C7E7FB"), marvel_4, LoadingView.FROM_BOTTOM);
        fab.addAnimation(Color.parseColor("#2F5DA9"), marvel_2, LoadingView.FROM_LEFT);
        fab.addAnimation(Color.parseColor("#FF4218"), marvel_3, LoadingView.FROM_LEFT);
        fab.addAnimation(Color.parseColor("#FFD200"), marvel_1, LoadingView.FROM_RIGHT);
        fab.addAnimation(Color.parseColor("#C7E7FB"), marvel_4, LoadingView.FROM_RIGHT);
        fab.addAnimation(Color.parseColor("#FF4218"), marvel_3, LoadingView.FROM_TOP);
        fab.addAnimation(Color.parseColor("#C7E7FB"), marvel_4, LoadingView.FROM_BOTTOM);
        fab.addAnimation(Color.parseColor("#FF4218"), marvel_3, LoadingView.FROM_TOP);
        fab.addAnimation(Color.parseColor("#C7E7FB"), marvel_4, LoadingView.FROM_BOTTOM);


        if (requestCode == GALLERY_PICK & resultCode == RESULT_OK) {
            Uri img_uri = data.getData();
            CropImage.activity(img_uri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressD = new ProgressDialog(this);
                mProgressD.setTitle("Uploading Image");
                mProgressD.setMessage("Please wait");
                mProgressD.setCanceledOnTouchOutside(false);
                //mProgressD.show();
                fab.startAnimation();
                fab.resumeAnimation();
                fab.setVisibility(View.VISIBLE);

                Uri resultUri = result.getUri();
                final File thumbnailPath = new File(resultUri.getPath());
                String uid = mCurrentUser.getUid();
                try {
                    thumbnail = new Compressor(this).setMaxHeight(200).setMaxWidth(200).setQuality(75).compressToBitmap(thumbnailPath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumbnail_byte = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final StorageReference path = mImageStore.child("profile_images").child(uid + ".jpg");
                final StorageReference thumbnailpath = mImageStore.child("profile_images").child("thumnails").child(uid + "jpg");
                path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final Uri downloadURL = uri;
                                    UploadTask uploadTask = thumbnailpath.putBytes(thumbnail_byte);
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                thumbnailpath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        final Uri ThumbURL = uri;
                                                        Map update = new HashMap();
                                                        update.put("image", downloadURL.toString());
                                                        update.put("thumb_img", ThumbURL.toString());

                                                        mUserDatabase.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                //mProgressD.dismiss();
                                                                fab.pauseAnimation();
                                                                fab.setVisibility(View.INVISIBLE);
                                                                Snackbar.make(contextView, "Profile Picture Updated!", Snackbar.LENGTH_LONG).setAction("Close", new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {

                                                                    }
                                                                }).setActionTextColor(Color.RED).show();
                                                            }
                                                        });
                                                    }
                                                });


                                            } else {
                                                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });


                                }
                            });

                        } else {
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                            mProgressD.dismiss();
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }



}
