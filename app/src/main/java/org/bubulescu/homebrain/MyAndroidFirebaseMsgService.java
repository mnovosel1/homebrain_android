package org.bubulescu.homebrain;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MyAndroidFirebaseMsgService extends FirebaseMessagingService {
    private static final String TAG = "FCM_LOG_";
    private String msgTitle;
    private String msgBody;
    private String msgData;
    private int countRec;

    private void passMessageToMainActivity(String message) {
        passMessageToMainActivity("message", message);
    }

    private void passMessageToMainActivity(String key, String message) {
        Intent intent = new Intent();
        intent.setAction(MainActivity.SENDMESAGGE);
        intent.putExtra(key, message);
        sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //notification message
        if (remoteMessage.getNotification() != null) {
            msgTitle = remoteMessage.getNotification().getTitle();
            msgBody = remoteMessage.getNotification().getBody();
            createNotification(msgTitle, msgBody);

            Log.d(TAG, "Notification from: " + remoteMessage.getFrom() + " " + msgTitle + ": " + msgBody);
        }
        //data message
        else {
            DatabaseHandler db = new DatabaseHandler(this);
            msgTitle = remoteMessage.getData().get("title");
            msgBody = remoteMessage.getData().get("msg");

            createNotification(msgTitle, msgBody);

            if (remoteMessage.getData().get("configs") != null) {
                MainActivity.savePreferences(remoteMessage.getData().get("configs"));

            } else if (remoteMessage.getData().get("data") != null) {
                try {
                    JSONObject data = new JSONObject(remoteMessage.getData().get("data"));

                    db.updateDb(data);
                } catch (JSONException e) {
                    Log.d(TAG, "DATA msg - no JSON " + e.toString());
                }
                /*
                msgData = remoteMessage.getData().get("data");
                passMessageToMainActivity(msgData);

                String[] msgDataArray = msgData.split("\\|");
                //String[] msgDataArray = {"first", "second"};
                Log.d(TAG, "msgDataArray.length = " + msgDataArray.length);
                db.updateDb(msgDataArray);
                */
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

        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage("org.bubulescu.homebrain");
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);

        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_home)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        switch (messageTitle) {
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

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, mNotificationBuilder.build());

    }
}
