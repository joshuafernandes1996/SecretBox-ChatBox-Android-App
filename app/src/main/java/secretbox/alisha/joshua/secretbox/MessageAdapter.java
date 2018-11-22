package secretbox.alisha.joshua.secretbox;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<messages> mMessageList;
    private FirebaseAuth mAuth;
    String AES = "AES";

    public MessageAdapter(List<messages> mMessageList) {
        this.mMessageList = mMessageList;
    }


    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_msg_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        final String CurUser = mAuth.getCurrentUser().getUid();
        final messages c = mMessageList.get(position);
        final String fromUser = c.getFrom();
        String MsgType = c.getType();

        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("users").child(fromUser);

        //fetching the user information
        mUserDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_img").getValue().toString();
                holder.displayName.setText(name);
                if(dataSnapshot.getKey().equals(CurUser)){
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.img);
                    holder.img.setVisibility(View.VISIBLE);
                    holder.img2.setVisibility(View.INVISIBLE);
                }
                else{
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.img2);
                    holder.img2.setVisibility(View.VISIBLE);
                    holder.img.setVisibility(View.INVISIBLE);
                }

                if (fromUser == CurUser) {

                    holder.messageText.setBackgroundResource(R.drawable.msgback);
                } else {
                    holder.messageText.setBackgroundResource(R.drawable.msgback2);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Double miliTIme = Double.parseDouble(c.getTime().toString());
        final int minutes = (int) ((miliTIme / 1000 / 60) % 60) + 30;
        final int hours = (int) ((miliTIme / 1000 / 60 / 60) % 24) + 5;
        final String clock;
        if (hours > 12 || hours == 12) {
            clock = "PM";
        } else {
            clock = "AM";
        }

        holder.messageText.setText(c.getMessage());

        if (MsgType.equals("text")) {
            holder.msgImage.setVisibility(View.INVISIBLE);
            holder.messageText.setVisibility(View.VISIBLE);
            if (fromUser.equals(CurUser)) {
                final DatabaseReference getPass = FirebaseDatabase.getInstance().getReference().child("users").child(CurUser);
                getPass.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String PassKey = dataSnapshot.child("password").getValue().toString();
                        try {
                            String DecryptedMsg = decrypt(c.getMessage(), PassKey);
                            holder.messageText.setBackgroundResource(R.drawable.msgback);
                            holder.messageText.setText(DecryptedMsg);
                            holder.timeText.setText(String.valueOf(hours) + ":" + String.valueOf(minutes) + clock);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            } else {

                DatabaseReference CheckPass = FirebaseDatabase.getInstance().getReference().child("Chat").child(CurUser).child(fromUser);
                CheckPass.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("passKey")) {
                            String PassKey = dataSnapshot.child("passKey").getValue().toString();
                            if (!PassKey.isEmpty()) {
                                try {
                                    String DecryptedMsg = decrypt(c.getMessage(), PassKey);
                                    holder.messageText.setBackgroundResource(R.drawable.msgback2);
                                    holder.messageText.setText(DecryptedMsg);
                                    holder.timeText.setText(String.valueOf(hours) + ":" + String.valueOf(minutes) + clock);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        } else {

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

        } else {
            holder.messageText.setVisibility(View.INVISIBLE);
            holder.msgImage.setVisibility(View.VISIBLE);
            holder.timeText.setText(String.valueOf(hours) + ":" + String.valueOf(minutes) + clock);
            Picasso.get().load(c.getMessage()).resize(500,650).placeholder(R.drawable.image).into(holder.msgImage);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText, timeText, displayName;
        public CircleImageView img,img2;
        public ImageView msgImage;

        public MessageViewHolder(View view) {
            super(view);

            //fetching elements by ids
            displayName = view.findViewById(R.id.msgName);
            messageText = view.findViewById(R.id.msgTextLay);
            timeText = view.findViewById(R.id.msgTime);
            img = view.findViewById(R.id.msgProfileLay);
            img2 = view.findViewById(R.id.msgProfileLay2);
            msgImage = view.findViewById(R.id.msgImage);
        }
    }

    //function to decrypt the message
    private String decrypt(String outputString, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedVal = Base64.decode(outputString, Base64.DEFAULT);
        byte[] decVal = c.doFinal(decodedVal);
        String decryptedValue = new String(decVal);
        return decryptedValue;
    }

    //function to get the decryption key
    private SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }
}
