package org.bubulescu.homebrain;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WebView wb;
    private TextToSpeech tts;
    private MyReceiver myReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        registerReceiver();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });

        wb = (WebView) findViewById(R.id.webView);
        wb.addJavascriptInterface(new WebAppInterface(this), "Android");
        wb.getSettings().setLoadWithOverviewMode(true);
        wb.getSettings().setUseWideViewPort(true);

        wb.getSettings().setJavaScriptEnabled(true);
        wb.getSettings().setAllowUniversalAccessFromFileURLs(true);

        wb.loadUrl("file:///android_asset/index.html");
        //wb.loadUrl("http://homebrain.bubulescu.org/app");

        wb.setWebViewClient(new WebViewClient());
    }
    //When the activity resume, the receiver is going to register...
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }
    //when the activity stop, the receiver is going to unregister...
    @Override
    protected void onStop() {
        unregisterReceiver(myReceiver); //unregister my receiver...
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (wb.canGoBack()) {
            wb.goBack();
        } else {
            super.onBackPressed();
        }
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
            intentFilter.addAction(MyAndroidFirebaseMsgService.SENDMESAGGE);
            registerReceiver(myReceiver, intentFilter);
        }
    }

    // class of receiver, the magic is here...
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            //verify if the extra var exist
            //System.out.println(arg1.hasExtra("message")); // true or false
            //another example...
            //System.out.println(arg1.getExtras().containsKey("message")); // true or false
            //if var exist only print or do some stuff
            if (arg1.hasExtra("message")) {
                //do what you want to
                //wb.evaluateJavascript("toast('Hello World!');", null);
                System.out.println(arg1.getStringExtra("message"));
            }
        }
    }
}
