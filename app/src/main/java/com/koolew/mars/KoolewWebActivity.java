package com.koolew.mars;

import android.content.Context;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.UriProcessor;

import java.util.HashMap;
import java.util.Map;


public class KoolewWebActivity extends BaseActivity {

    public static final String KEY_URL = "url";

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.setWebViewClient(mWebViewClient);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", MyAccountInfo.getToken());
        mWebView.loadUrl(getIntent().getStringExtra(KEY_URL), header);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return new WebUriProcessor(KoolewWebActivity.this).process(url);
        }
    };

    class WebUriProcessor extends UriProcessor {
        public WebUriProcessor(Context context) {
            super(context);
        }

        @Override
        protected boolean processUrl(String url) {
            return false;
        }
    }
}
