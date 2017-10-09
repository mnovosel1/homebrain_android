package org.bubulescu.homebrain;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import android.view.LayoutInflater;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityLOG";
    final static String SENDMESAGGE = "passMessage";

    private WebView webApp;
    private ImageView imgLoading;
    private boolean ShowImageSplash = true;
    private TextToSpeech tts;
    private MyReceiver myReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final Context mContext = this;
        registerReceiver();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgLoading = (ImageView)findViewById(R.id.imgLoader);

        webApp = (WebView) findViewById(R.id.webView);
        webApp.addJavascriptInterface(new WebAppInterface(this), "Android");

        webApp.getSettings().setLoadWithOverviewMode(true);
        webApp.getSettings().setUseWideViewPort(true);
        webApp.getSettings().setJavaScriptEnabled(true);
        webApp.getSettings().setAllowUniversalAccessFromFileURLs(true);


        webApp.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //hide loading image
                //show webview
                if (ShowImageSplash)
                {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //hide loading image
                    findViewById(R.id.imgLoader).setVisibility(View.GONE);
                    ShowImageSplash = false;
                }
                //show webview
                findViewById(R.id.webView).setVisibility(View.VISIBLE);
            }
        });

        //webApp.loadUrl("file:///android_asset/index.html");
        webApp.loadUrl("http://homebrain.bubulescu.org/app/home.php");

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });
    }

    protected void onDestroy()
    {
        unregisterReceiver();
        super.onDestroy();
    }


    public class WebAppInterface
    {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) { mContext = c; }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void toast(String toast)
        {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public JSONObject getUpdate() throws JSONException {
            // TODO getUpdates from database
            DatabaseHandler dbh = new DatabaseHandler(mContext);

            JSONObject ret = new JSONObject();

            // Select All Query
            String selectQuery = "SELECT timestamp, statebefore, state, changedto FROM changelog";

            SQLiteDatabase db = dbh.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            int rowNum = 0;

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    JSONObject row = new JSONObject();
                    row.put("timestamp", cursor.getString(0));
                    row.put("statebefore", cursor.getString(1));
                    row.put("state", cursor.getString(2));
                    row.put("changedto", cursor.getString(3));

                    ret.put(Integer.toString(rowNum++), row);
                } while (cursor.moveToNext());
            }

            return  ret;
        }

        @JavascriptInterface
        public void speak (String toSpeak)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsGreater21(toSpeak);
            } else {
                ttsUnder20(toSpeak);
            }
        }

        @SuppressWarnings("deprecation")
        private void ttsUnder20(String text) {
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void ttsGreater21(String text) {
            String utteranceId=this.hashCode() + "";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }
    }

    private void registerReceiver(){
        if ( myReceiver == null )
        {
            myReceiver = new MyReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SENDMESAGGE);
            registerReceiver(myReceiver, intentFilter);
        }
    }

    private void unregisterReceiver()
    {
        if ( myReceiver != null )
            unregisterReceiver(myReceiver);
    }

    // class of receiver, the magic is here...
    private class MyReceiver extends BroadcastReceiver {

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
}
