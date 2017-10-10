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

    private static final String TAG = "MyBroadcastReceiverLOG";
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

        //verify if the extra var exist
        //System.out.println(arg1.hasExtra("message")); // true or false
        //another example...
        //System.out.println(arg1.getExtras().containsKey("message")); // true or false
        //if var exist only print or do some stuff
        if (arg1.hasExtra("message")) {
            //do what you want to
            //webApp.evaluateJavascript("toast('Hello World!');", null);
            System.out.println(arg1.getStringExtra("message"));
        }

        if (arg1.hasExtra("token"))
        {
            final String token = arg1.getStringExtra("token");
            final String email = "me@mail.com";

            httpReq.sendReq("fcm/reg/" + email + "____" + token);
        }
    }

}
