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
            final String email = "mail";

            new Thread(new Runnable() { @Override public void run() {

                try {
                    URL url = new URL("http://homebrain.bubulescu.org/api/fcm/reg/" + token + "/" + email);
                    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                    httpCon.setReadTimeout(10000);
                    httpCon.setConnectTimeout(15000);
                    httpCon.setRequestMethod("POST");
                    httpCon.setDoInput(true);
                    httpCon.setDoOutput(true);

                    int tstamp = (int) ((System.currentTimeMillis()/1000)/20);
                    Uri.Builder builder = new Uri.Builder().appendQueryParameter("token", md5("H" + String.valueOf(tstamp)));
                    String query = builder.build().getEncodedQuery();

                    OutputStream os = httpCon.getOutputStream();

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    os.close();

                    httpCon.getInputStream();

                    Log.d(TAG, "Registered: http://homebrain.bubulescu.org/api/fcm/reg/" + token + "/" + email);
                }
                catch (MalformedURLException ex) {
                    Log.d(TAG, Log.getStackTraceString(ex));
                }
                catch (IOException ex) {
                    Log.d(TAG, Log.getStackTraceString(ex));
                }
            } }).start();
        }
    }

    @Nullable
    private String md5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return null;
    }
}
