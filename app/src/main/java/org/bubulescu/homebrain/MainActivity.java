package org.bubulescu.homebrain;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

    private WebView wb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wb = (WebView) findViewById(R.id.webView);
        wb.getSettings().setLoadWithOverviewMode(true);
        wb.getSettings().setUseWideViewPort(true);

        wb.getSettings().setJavaScriptEnabled(true);
        wb.getSettings().setAllowUniversalAccessFromFileURLs(true);

        //wb.loadUrl("file:///android_asset/index.html");
        wb.loadUrl("http://homebrain.bubulescu.org/app");
        wb.setWebViewClient(new WebViewClient());
    }

    @Override
    public void onBackPressed() {
        if (wb.canGoBack()) {
            wb.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
