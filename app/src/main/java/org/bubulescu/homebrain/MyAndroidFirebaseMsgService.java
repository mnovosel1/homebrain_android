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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyAndroidFirebaseMsgService extends FirebaseMessagingService {
    private static final String TAG = "FCM_LOG_";
    private String msgTitle;
    private String msgBody;
    private String msgData;
    private int countRec;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //notification message
        if (remoteMessage.getNotification() != null) {
            msgTitle = remoteMessage.getNotification().getTitle();
            msgBody = remoteMessage.getNotification().getBody();

            Log.d(TAG + "onMsgRec", "Notification: " + remoteMessage.getFrom() + " " + msgTitle + ": " + msgBody);
        }
        //data message
        else {
            DatabaseHandler db = new DatabaseHandler();
            boolean notify = false;

            msgTitle = remoteMessage.getData().get("title");
            msgBody = remoteMessage.getData().get("msg");

            Log.d(TAG + "onMsgRec", "DATA msg received..");

            // CONFIGS data message
            if (remoteMessage.getData().get("configs") != null) {
                //passMessageToMainActivity("configs", remoteMessage.getData().get("configs"));

                String received = remoteMessage.getData().get("configs");

                // CONFIGS update
                if ( !received.contains("{\"updates\":") ) {

                    MainActivity.saveConfigs(remoteMessage.getData().get("configs"));
                    Log.d(TAG + "cfgsReceived:", remoteMessage.getData().get("configs"));
                }

                // WEBAPP update
                else {

                    try {
                        JSONArray updates = (JSONArray) new JSONObject(remoteMessage.getData().get("configs")).get("updates");

                        for (int i = 0; i < updates.length(); i++) {

                            String updFile = updates.getString(i);
                            String dlDir = MainActivity.WEBAPP_DIR;

                            HttpReqHelper.downloadFile(updFile, dlDir);
                        }
                        MainActivity.sendBcastMsg("{'update': 'finished'}", MainActivity.UPDATE_FINISHED);

                    } catch (JSONException ex) {
                        Log.d(TAG + "_AppUpdateEX", Log.getStackTraceString(ex));
                    }
                }
                //MainActivity.sendBcastMsg("{'runOnWebView': 'reloadWebApp'}");
            }

            // DB UPDATE data message
            else if (remoteMessage.getData().get("data") != null) {
                try {
                    JSONObject data = new JSONObject(remoteMessage.getData().get("data"));
                    db.updateDb(data);
                    notify = true;

                    Log.d(TAG + "onMsgRec", "Db records: " + db.getCount());
                } catch (JSONException ex) {
                    Log.d(TAG + "onMsgRec", "DATA msg - no JSON " + Log.getStackTraceString(ex));
                }
            }

            if ( notify ) {

                createNotification(msgTitle, msgBody);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), msgTitle + ": " + msgBody, Toast.LENGTH_LONG).show();
                    }
                });
            }
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
                mNotificationBuilder.setSound(Uri.parse("android.resource://org.bubulescu.homebrain/" + R.raw.homebrain));
                break;

            case "HomeServer":
                mNotificationBuilder.setSound(Uri.parse("android.resource://org.bubulescu.homebrain/" + R.raw.homeserver));
                break;

            case "MPD":
                mNotificationBuilder.setSound(Uri.parse("android.resource://org.bubulescu.homebrain/" + R.raw.mpd));
                break;

            case "KODI":
                mNotificationBuilder.setSound(Uri.parse("android.resource://org.bubulescu.homebrain/" + R.raw.kodi));
                break;

            default:
                mNotificationBuilder.setSound(Uri.parse("android.resource://org.bubulescu.homebrain/" + R.raw.notify));
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, mNotificationBuilder.build());
    }
}
