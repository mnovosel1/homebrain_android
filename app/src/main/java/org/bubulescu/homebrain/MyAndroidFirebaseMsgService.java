package org.bubulescu.homebrain;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyAndroidFirebaseMsgService extends FirebaseMessagingService
{
    final static String SENDMESAGGE = "passMessage";

    public static Boolean serviceStatus = false;

    private static final String TAG = "MyAndroidFCMService";
    private String msgTitle;
    private String msgBody;
    private String msgData;
    private int countRec;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceStatus=true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //passMessageToActivity("The service is finished, This is going to be more cooler than the heart of your ex...");
        //System.out.println("onDestroy");
        serviceStatus=false;
    }

    private void passMessageToActivity(String message){
        Intent intent = new Intent();
        intent.setAction(SENDMESAGGE);
        intent.putExtra("message",message);
        sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //notification message
        if (remoteMessage.getNotification() != null)
        {
            msgBody = remoteMessage.getNotification().getBody();
            createNotification(msgBody);

            Log.d(TAG, "From: " + remoteMessage.getFrom());
            Log.d(TAG, "Notification Message: " + msgBody);
        }
        //data message
        else
        {
            DatabaseHandler db = new DatabaseHandler(this);
            msgTitle = remoteMessage.getData().get("title");
            msgBody = remoteMessage.getData().get("msg");

            createNotification(msgTitle, msgBody);

            if ( remoteMessage.getData().get("data") != null )
            {
                msgData = remoteMessage.getData().get("data");
                passMessageToActivity(msgData);

                String[] msgDataArray = msgData.split("|");
                db.updateDb(msgDataArray);
            }

            countRec = db.getCount();

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    //Toast.makeText(getApplicationContext(), msgTitle + ": " + msgBody, Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), "Br. zapisa: " + countRec, Toast.LENGTH_LONG).show();
                }
            });

            //Log.d(TAG, "From: " + remoteMessage.getFrom());
            //Log.d(TAG, "DATA Message: " + msgBody);
        }
    }

    private void createNotification(String messageBody) {
        createNotification("HomeBrain", messageBody);
    }

    private void createNotification(String messageTitle, String messageBody) {

        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_home)
                .setContentTitle(messageTitle)
                .setContentText(messageBody);

        switch (messageTitle)
        {
            case "HomeBrain":
                mNotificationBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.homebrain));
                break;

            case "HomeServer":
                mNotificationBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.homeserver));
                break;

            case "KODI":
                mNotificationBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.kodi));
                break;

            case "MPD":
                mNotificationBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mpd));
                break;

            default:
                mNotificationBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notify));
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, mNotificationBuilder.build());

    }
}
