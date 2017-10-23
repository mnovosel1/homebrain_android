package org.bubulescu.homebrain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity_LOG_";
    final static String SENDMESAGGE = "MSGING";
    final static String DELIMITER = "____";
    final static String CONFIGS = "config.ini";
    final Handler handler = new Handler();

    private EditText emailInput, codeInput;

    private WebView webApp;
    private BroadcastReceiver broadcastReceiver = null;

    private DatabaseHandler db;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context mContext = this;
        super.onCreate(savedInstanceState);

        registerMyReceiver();

        setContentView(R.layout.activity_main);

        user = new User(this);

        webApp = (WebView) findViewById(R.id.webView);
        webApp.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {

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

        if ( userOK() ) {
            handler.postDelayed (new Runnable() {
                public void run() {
                    runOnWebView("go()");
                }
            }, 2000);
        }
    }

    public void runOnWebView(String fnToRun) {

        webApp.loadUrl("javascript:"+ fnToRun);

        Log.d(TAG, "runOnWV - javascript:"+ fnToRun);
    }

    public boolean userOK() {
        return (user.isRegistered() && user.isVerified());
    }

    protected void registerMyReceiver() {

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent arg) {
                // just a message
                if (arg.hasExtra("message")) {
                    Log.d(TAG, "FCM broadcast: " + arg.getStringExtra("message"));

                // run jscript function on WebView
                } else if (arg.hasExtra("runOnWebView")) {
                    webApp.loadUrl("javascript:"+ arg.getStringExtra("runOnWebView"));
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
}
