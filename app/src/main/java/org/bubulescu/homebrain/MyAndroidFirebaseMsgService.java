package org.bubulescu.homebrain;

import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyAndroidFirebaseMsgService extends FirebaseMessagingService
{
    private static final String TAG = "MyAndroidFCMService";
    private String msg;
    private WebView wb;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        msg = remoteMessage.getData().get("msg");

        //notification message
        if(msg == null || msg.trim().isEmpty())
        {
            msg = remoteMessage.getNotification().getBody();

            Log.d(TAG, "From: " + remoteMessage.getFrom());
            Log.d(TAG, "Notification Message: " + msg);
        }
        //data message
        else
        {
            Log.d(TAG, "From: " + remoteMessage.getFrom());
            Log.d(TAG, "DATA Message: " + msg);
        }


        //createNotification(msg);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createNotification(String messageBody) {

        //Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_home)
                .setContentTitle("HomeBrain")
                .setContentText(messageBody)
                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notify));
        //.setSound(notificationSoundURI)

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, mNotificationBuilder.build());

    }
}
