package com.koolew.mars;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;


public class WebActivity extends Activity {

    public static final String KEY_URL = "url";

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        mWebView = (WebView) findViewById(R.id.web_view);

        mWebView.loadUrl(getIntent().getStringExtra(KEY_URL));
    }
}
