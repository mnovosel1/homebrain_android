package org.bubulescu.homebrain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    public final static String SENDMESAGGE = "MSGING";
    public final static String DELIMITER = "____";
    public final static String CONFIGS = "config.ini";
    private final static  String TAG = "MainActivity_LOG_";
    private static Context mContext;

    private Handler handler = new Handler();
    private EditText emailInput, codeInput;
    private WebView webApp = null;
    private BroadcastReceiver broadcastReceiver = null;
    private DatabaseHandler db;
    private User user;
    private boolean webAppLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);

        registerMyReceiver();

        setContentView(R.layout.activity_main);

        user = new User(this);

        webApp = (WebView) findViewById(R.id.webView);
        webApp.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                webAppLoaded = true;
            }
        });

        webApp.getSettings().setLoadWithOverviewMode(true);
        webApp.getSettings().setUseWideViewPort(true);
        webApp.getSettings().setJavaScriptEnabled(true);
        webApp.addJavascriptInterface(new WebAppInterface(this), "Android");
        webApp.getSettings().setAllowUniversalAccessFromFileURLs(true);

        //webApp.loadDataWithBaseURL();

        webApp.loadUrl("file:///android_asset/index.html");
        //webApp.loadUrl("http://homebrain.bubulescu.org/app/home.php");

        showLogin();
    }

    public static Context getContext(){
        return mContext;
    }

    protected void showLogin() {
        if (!user.isRegistered()) {
            showEmailInput();
        } else if (!user.isVerified()) {
            showCodeInput();
        } else {
            showMain();
        }
    }

    protected void showEmailInput() {

        findViewById(R.id.emailText1).setVisibility(View.VISIBLE);
        findViewById(R.id.email).setVisibility(View.VISIBLE);
        findViewById(R.id.emailButton).setVisibility(View.VISIBLE);

        findViewById((R.id.codeText1)).setVisibility(View.GONE);
        findViewById((R.id.codeText2)).setVisibility(View.GONE);
        findViewById((R.id.code)).setVisibility(View.GONE);
        findViewById((R.id.codeButton)).setVisibility(View.GONE);

        findViewById((R.id.webView)).setVisibility(View.GONE);

        emailInput = (EditText) findViewById(R.id.email);
        Button prijaviBtn = (Button) findViewById(R.id.emailButton);

        prijaviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                user.register(email);
                showCodeInput();
            }
        });
    }

    protected void showCodeInput() {

        findViewById(R.id.emailText1).setVisibility(View.GONE);
        findViewById(R.id.email).setVisibility(View.GONE);
        findViewById(R.id.emailButton).setVisibility(View.GONE);

        findViewById((R.id.codeText1)).setVisibility(View.VISIBLE);
        findViewById((R.id.codeText2)).setVisibility(View.VISIBLE);
        findViewById((R.id.code)).setVisibility(View.VISIBLE);
        findViewById((R.id.codeButton)).setVisibility(View.VISIBLE);

        findViewById((R.id.webView)).setVisibility(View.GONE);

        codeInput = (EditText) findViewById(R.id.code);

        TextView notice = (TextView) findViewById(R.id.codeText2);
        notice.setText("CODE je mailan na: " + user.email() + ".");

        Button codeBtn = (Button) findViewById(R.id.codeButton);
        codeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.verify(codeInput.getText().toString());
                showMain();
            }
        });
    }

    protected void showMain() {

        findViewById(R.id.emailText1).setVisibility(View.GONE);
        findViewById(R.id.email).setVisibility(View.GONE);
        findViewById(R.id.emailButton).setVisibility(View.GONE);

        findViewById((R.id.codeText1)).setVisibility(View.GONE);
        findViewById((R.id.codeText2)).setVisibility(View.GONE);
        findViewById((R.id.code)).setVisibility(View.GONE);
        findViewById((R.id.codeButton)).setVisibility(View.GONE);

        findViewById((R.id.webView)).setVisibility(View.VISIBLE);

        if ( user.OK() ) {
            startWebApp();
        }
    }

    private void startWebApp() {
        if ( webAppLoaded ) {
            runOnWebView("go()");
        } else {
            Log.d(TAG, "waiting for WebApp to load...");
            handler.postDelayed (new Runnable() {
                public void run() {
                    startWebApp();
                }
            }, 500);
        }
    }

    public void runOnWebView(String fnToRun) {

        String log = "javascript:";

        if ( webApp != null ) {
            webApp.loadUrl("javascript:"+ fnToRun);
        } else log = "!NOTRUNNED! " + log;

        Log.d(TAG, log + fnToRun);
    }

    protected void registerMyReceiver() {

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent arg) {
                // just a message
                if ( arg.hasExtra("message") ) {
                    Log.d(TAG, "FCM broadcast: " + arg.getStringExtra("message"));

                    // configs
                } else if ( arg.hasExtra("configs") ) {
                    try {
                        JSONObject configs = new JSONObject(arg.getStringExtra("configs"));
                        user.configure(configs);
                    } catch (JSONException e) {
                        Log.d(TAG, e.toString());
                    }

                    // run jscript function on WebView
                } else if ( arg.hasExtra("runOnWebView") ) {
                    runOnWebView(arg.getStringExtra("runOnWebView"));
                }
            }
        };

        // registering BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SENDMESAGGE);
        registerReceiver(broadcastReceiver, intentFilter);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
       if ( broadcastReceiver != null ) unregisterReceiver(broadcastReceiver);
    }

    public static void sendBcastMsg(Context c, String bcasts) {

        Intent intent = new Intent();
        intent.setAction(MainActivity.SENDMESAGGE);

        try {
            JSONObject bcMessages = new JSONObject(bcasts);
            for (int i = 0; i<bcMessages.names().length(); i++) {
                intent.putExtra(bcMessages.names().getString(i), bcMessages.get(bcMessages.names().getString(i)).toString());
                Log.d(TAG, bcMessages.names().getString(i) + ", " + bcMessages.get(bcMessages.names().getString(i)).toString());

                c.sendBroadcast(intent);
            }
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
        }
    }

    public static void savePreferences(String configs) {

        String keys = "", vals = "";
        SharedPreferences.Editor edit = mContext.getSharedPreferences(MainActivity.CONFIGS, Context.MODE_PRIVATE).edit();

        try {
            JSONObject prefs = new JSONObject(configs);
            for (int i = 0; i<prefs.names().length(); i++) {
                edit.putString(prefs.names().getString(i), prefs.get(prefs.names().getString(i)).toString());
                Log.d(TAG, prefs.names().getString(i) + ", " + prefs.get(prefs.names().getString(i)).toString());
            }
            edit.commit();
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
        }
    }
}
