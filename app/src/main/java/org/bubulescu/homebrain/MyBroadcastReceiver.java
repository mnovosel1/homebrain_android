package org.bubulescu.homebrain;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BroadcastReceiver_LOG";
    private static MyBroadcastReceiver instance = null;
    Context mContext;

    MyBroadcastReceiver(Context c) { mContext = c; }

    private MyBroadcastReceiver(){
        if ( instance != null ) {
            throw new RuntimeException("Singleton CLASS: use MyBroadcastReceiver.getInstance();");
        }
    };

    public synchronized static MyBroadcastReceiver getInstance() {
        if ( instance == null ) return new MyBroadcastReceiver();
        else return instance;
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        HttpReqHelper httpReq = new HttpReqHelper();

        // just a message
        if (arg1.hasExtra("message")) {
            Log.d(TAG, "FCMessage:: " + arg1.getStringExtra("message"));
        }

        // FCM token received
        if (arg1.hasExtra("token"))
        {
            final String token = arg1.getStringExtra("token");
            final String email = "me@mail.com";

            httpReq.sendReq("fcm/reg/" + email + "____" + token);
        }
    }

}
