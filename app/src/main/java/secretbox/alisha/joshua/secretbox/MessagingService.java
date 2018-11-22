package secretbox.alisha.joshua.secretbox;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;

public class MessagingService extends FirebaseMessagingService {
    String image;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //handling push notification
        //this class is to set up and customize the notification as received from Firebase payload

        String Not_title = remoteMessage.getNotification().getTitle();
        String Not_msg = remoteMessage.getNotification().getBody();
        String Click_act = remoteMessage.getNotification().getClickAction();
        String FromUserID = remoteMessage.getData().get("frmUID");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.chatbubble)
                        .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.chatbubble))
                        .setContentTitle(Not_title)
                        .setContentText(Not_msg)
                        .setAutoCancel(true)
                        .setVibrate(new long[]{100, 1000, 100, 1000, 100})
                        .setLights(Color.RED, 3000, 3000)
                        .setSound(Uri.parse("android.resource://" + "secretbox.alisha.joshua.secretbox" + "/" + R.raw.not_tone))
                        .setBadgeIconType(R.drawable.chatbubble)
                        .setDefaults(1);

        Intent resultIntent = new Intent(Click_act);
        resultIntent.putExtra("uid", FromUserID);
        resultIntent.putExtra("from", "notification");

        int mNotificationID = (int) System.currentTimeMillis();
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.addAction(R.drawable.chatbubble, "View", resultPendingIntent);
        NotificationManager mNotify = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotify.notify(mNotificationID, mBuilder.build());


    }
}
