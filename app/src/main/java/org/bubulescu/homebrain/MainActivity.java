package org.bubulescu.homebrain;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity_LOG_";
    final static String SENDMESAGGE = "passMessage";
    final static String DELIMITER = "____";
    final static String CONFIGS = "config.ini";

    private EditText emailInput, codeInput;

    private WebView webAppLoading;
    private WebView webApp;
    private ImageView imgLoading;
    private boolean ShowImageSplash = true;
    private MyBroadcastReceiver myBroadcastReceiver = MyBroadcastReceiver.getInstance();

    private DatabaseHandler db;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final Context mContext = this;
        super.onCreate(savedInstanceState);

        user = new User(this);

        if ( !user.isRegistered() )
        {
            showEmailInput();
        }
        else if ( !user.isVerified() )
        {
            showCodeInput();
        }
            else
        {
            // registering MyBroadcastReceiver
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SENDMESAGGE);
            registerReceiver(myBroadcastReceiver, intentFilter);
            showMain();
        }

    }

    protected void showEmailInput()
    {
        setContentView(R.layout.email_input);

        emailInput = (EditText)findViewById(R.id.email);

        Button prijaviBtn = (Button)findViewById(R.id.button);
        prijaviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String email = emailInput.getText().toString();
                user.register(email);
                showCodeInput();
            }
        });
    }

    protected void showCodeInput()
    {
        setContentView(R.layout.code_input);

        codeInput = (EditText)findViewById(R.id.code);
        TextView notice = (TextView)findViewById(R.id.textView2);
        notice.setText("CODE je mailan na: " + user.email() + ".");
        Button codeBtn = (Button)findViewById(R.id.button);
        codeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                user.verify(codeInput.getText().toString());
                showMain();
            }
        });
    }

    protected void showMain()
    {
        setContentView(R.layout.activity_main);

        webAppLoading = (WebView) findViewById(R.id.webViewLoading);
        webAppLoading.getSettings().setLoadWithOverviewMode(true);
        webAppLoading.getSettings().setUseWideViewPort(true);
        webAppLoading.loadUrl("file:///android_asset/loading.html");

        webApp = (WebView) findViewById(R.id.webView);
        webApp.addJavascriptInterface(new WebAppInterface(this), "Android");

        webApp.getSettings().setLoadWithOverviewMode(true);
        webApp.getSettings().setUseWideViewPort(true);
        webApp.getSettings().setJavaScriptEnabled(true);
        webApp.getSettings().setAllowUniversalAccessFromFileURLs(true);

        webApp.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                if (ShowImageSplash)
                {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //hide loading image
                    findViewById(R.id.webViewLoading).setVisibility(View.GONE);
                    ShowImageSplash = false;
                }
                //show webview
                findViewById(R.id.webView).setVisibility(View.VISIBLE);
            }
        });

        //webApp.loadDataWithBaseURL();
        //webApp.loadUrl("http://homebrain.bubulescu.org/app/home.php");

        //webApp.loadUrl("file:///android_asset/index.html");
        webApp.loadUrl("http://homebrain.bubulescu.org/app/home.php");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }
}
