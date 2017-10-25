package org.bubulescu.homebrain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    public Context context;

    public final static String SENDMESAGGE = "MSGING";
    public final static String CONFIGS = "config.ini";
    private final static  String TAG = "MA_LOG_";

    private Handler handler = new Handler();
    private EditText emailInput, codeInput;
    private WebView webApp = null;
    private BroadcastReceiver broadcastReceiver = null;
    private DatabaseHandler db;
    private User user;
    private boolean webAppLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = HbApp.getAppContext();
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
        webApp.addJavascriptInterface(new WebAppInterface(), "Android");
        webApp.getSettings().setAllowUniversalAccessFromFileURLs(true);

        //webApp.loadDataWithBaseURL();

        webApp.loadUrl("file:///android_asset/index.html");
        //webApp.loadUrl("http://homebrain.bubulescu.org/app/home.php");

        showLogin();
    }

    protected void showLogin() {
        if (!user.isRegistered()) {
            showEmailInput();
        } else if (!user.isVerified()) {
            showCodeInput();
        } else {
            show("main");
            startWebApp();
        }
    }

    protected void showEmailInput() {

        show("email");

        emailInput = (EditText) findViewById(R.id.email);
        emailInput.requestFocus();
        emailInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handeld = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {

                    user.setEmail(emailInput.getText().toString());

                    InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(emailInput.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    show("wait");

                    String regData = "{\"email\": \"" + user.email() + "\", \"token\": \"" + FirebaseInstanceId.getInstance().getToken() + "\"}";
                    new HttpReqHelper(context).sendReq("fcm", "reg", regData, "registration");

                }
                return handeld;
            }
        });
    }

    protected void showCodeInput() {

        show("code");

        codeInput = (EditText) findViewById(R.id.code);
        codeInput.requestFocus();
        codeInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handeld = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {

                    user.verify(codeInput.getText().toString());

                    InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(codeInput.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    show("wait");
                }
                return handeld;
            }
        });

        TextView notice = (TextView) findViewById(R.id.codeText);
        notice.setText(getString(R.string.codeSentTo) + " " + user.email());

    }

    protected void show(String show) {

        findViewById(R.id.email).setVisibility(View.GONE);
        findViewById((R.id.wait)).setVisibility(View.GONE);
        findViewById((R.id.code)).setVisibility(View.GONE);
        findViewById((R.id.codeText)).setVisibility(View.GONE);
        findViewById((R.id.webView)).setVisibility(View.GONE);

        switch (show) {

            case "email":
                findViewById((R.id.email)).setVisibility(View.VISIBLE);
                break;

            case "wait":
                findViewById((R.id.wait)).setVisibility(View.VISIBLE);
                break;

            case "code":
                findViewById((R.id.code)).setVisibility(View.VISIBLE);
                findViewById((R.id.codeText)).setVisibility(View.VISIBLE);
                break;

            case "main":
                findViewById((R.id.webView)).setVisibility(View.VISIBLE);
                break;
        }
    }

    private void startWebApp() {
        if ( webAppLoaded && user.isVerified() ) {
            runOnWebView("go(null, " + user.pages() + ")");
        } else {
            Log.d(TAG + "startWebApp", "waiting...");
            handler.postDelayed (new Runnable() {
                public void run() {
                    startWebApp();
                }
            }, 1024);
        }
    }

    public void runOnWebView(String fnToRun) {

        String log = "";

        if ( webApp != null ) {
            webApp.loadUrl("javascript:" + fnToRun);
        } else log = "!NOTRUNNED! ";

        Log.d(TAG + "runOnWV: ", log + fnToRun);
    }

    protected void registerMyReceiver() {

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent arg) {
                // just a message
                if ( arg.hasExtra("message") ) {
                    Log.d(TAG + "regMyReceiver", "FCM broadcast: " + arg.getStringExtra("message"));

                    // registration
                } else if ( arg.hasExtra("registration") ) {

                    if (("200").equals(arg.getStringExtra("registration"))) {

                        String regData = "{\"token\": \"" + FirebaseInstanceId.getInstance().getToken() + "\"}";

                        saveConfigs(regData);
                        showCodeInput();
                    } else {
                        showEmailInput();
                        Toast.makeText(context, context.getString(R.string.tryAgain), Toast.LENGTH_LONG).show();
                    }

                    // verification
                } else if ( arg.hasExtra("verification") ) {

                    if (("200").equals(arg.getStringExtra("verification"))) {
                        show("main");
                    } else {
                        showCodeInput();
                        Toast.makeText(context, context.getString(R.string.tryAgain), Toast.LENGTH_LONG).show();
                    };

                    // configs
                } else if ( arg.hasExtra("configss") ) {


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

    public static void sendBcastMsg(String bcasts) {

        Intent intent = new Intent();
        intent.setAction(MainActivity.SENDMESAGGE);

        try {
            JSONObject bcMessages = new JSONObject(bcasts);
            for (int i = 0; i<bcMessages.names().length(); i++) {
                intent.putExtra(bcMessages.names().getString(i), bcMessages.get(bcMessages.names().getString(i)).toString());
                Log.d(TAG + "sendBcastMsg", bcMessages.names().getString(i) + ", " + bcMessages.get(bcMessages.names().getString(i)).toString());

                HbApp.getAppContext().sendBroadcast(intent);
            }
        } catch (JSONException e) {
            Log.d(TAG + "sendBcastMsg", e.toString());
        }
    }

    public static void saveConfigs(String configs) {

        SharedPreferences.Editor edit = HbApp.getAppContext().getSharedPreferences(MainActivity.CONFIGS, Context.MODE_PRIVATE).edit();

        try {
            JSONObject cfgs = new JSONObject(configs);

            for (int i = 0; i<cfgs.names().length(); i++) {
                edit.putString(cfgs.names().getString(i), cfgs.get(cfgs.names().getString(i)).toString());
                Log.d(TAG + "saveConfigs", cfgs.names().getString(i) + ", " + cfgs.get(cfgs.names().getString(i)).toString());
            }
            edit.commit();
            Log.d(TAG + "saveConfigs", "configs saved..");

        } catch (JSONException e) {
            Log.d(TAG + "saveConfigs", e.toString());
        }
    }

    public static String getConfig(Context ctx, String cfgValue) {
        SharedPreferences configs = ctx.getSharedPreferences(MainActivity.CONFIGS, Context.MODE_PRIVATE);
        return configs.getString(cfgValue, null);
    }
}

