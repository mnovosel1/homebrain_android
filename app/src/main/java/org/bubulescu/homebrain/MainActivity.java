package org.bubulescu.homebrain;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityLOG";
    final static String SENDMESAGGE = "passMessage";

    private WebView webApp;
    private ImageView imgLoading;
    private boolean ShowImageSplash = true;
    private MyBroadcastReceiver myBroadcastReceiver = MyBroadcastReceiver.getInstance();

    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final Context mContext = this;
        super.onCreate(savedInstanceState);

        // registering MyBroadcastReceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SENDMESAGGE);
        registerReceiver(myBroadcastReceiver, intentFilter);

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

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }
}
