package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.UriProcessor;
import com.koolew.mars.view.TitleBarView;

import java.util.HashMap;
import java.util.Map;


public class KoolewWebActivity extends BaseActivity
        implements TitleBarView.OnRightLayoutClickListener {

    public static final String KEY_URL = "url";
    public static final String KEY_TITLE = "title";
    public static final String KEY_TITLE_BAR_BG = "title bar bg";
    public static final String KEY_NO_TITLE_BAR = "no title bar";

    protected static final String PARAM_KEY_IS_SUPPORT_SHARE = "isSupportShare";
    protected static final String PARAM_KEY_TITLE = "title";
    protected static final String PARAM_KEY_THUMB = "thumb";
    protected static final String PARAM_KEY_URL = "url";

    private TitleBarView mTitleBar;
    private WebView mWebView;
    private Map<String, String> mWebParams = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Intent extra = getIntent();

        if (extra.getBooleanExtra(KEY_NO_TITLE_BAR, false)) {
            findViewById(R.id.title_bar).setVisibility(View.GONE);
            findViewById(R.id.shadow).setVisibility(View.INVISIBLE);
            findViewById(R.id.title_bar2).setVisibility(View.VISIBLE);
        }
        else {
            mTitleBar = (TitleBarView) findViewById(R.id.title_bar);
            String title = extra.getStringExtra(KEY_TITLE);
            if (TextUtils.isEmpty(title)) {
                mTitleBar.setTitle(R.string.koolew);
            } else {
                mTitleBar.setTitle(title);
            }
            mTitleBar.setBackgroundColor(extra.getIntExtra(KEY_TITLE_BAR_BG,
                    getResources().getColor(R.color.koolew_light_blue)));
            mTitleBar.setRightLayoutVisibility(View.INVISIBLE);
            mTitleBar.setOnRightLayoutClickListener(this);
        }

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.setWebViewClient(mWebViewClient);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String userAgent = webSettings.getUserAgentString();
        String versionName = null;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        webSettings.setUserAgentString(userAgent + " koolew/" + versionName);

        mWebView.addJavascriptInterface(new JSInterface(), "androidInterface");

        Map<String, String> header = new HashMap<>();
        header.put("Authorization", MyAccountInfo.getToken());
        mWebView.loadUrl(extra.getStringExtra(KEY_URL), header);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mWebView.loadUrl("javascript:androidGetParams()");
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return new WebUriProcessor(KoolewWebActivity.this).process(url);
        }
    };

    @Override
    public void onRightLayoutClick() {
        new ShareUrlWindow(this, mWebParams.get(PARAM_KEY_URL), mWebParams.get(PARAM_KEY_TITLE),
                mWebParams.get(PARAM_KEY_THUMB)).showAtLocation(mTitleBar, Gravity.TOP, 0, 0);
    }

    public static void startThisActivity(Context context, String url) {
        Intent intent = new Intent(context, KoolewWebActivity.class);
        intent.putExtra(KEY_URL, url);
        context.startActivity(intent);
    }

    public static void startThisActivity(Context context, String url,
                                         String title, int titleBarBg) {
        Intent intent = new Intent(context, KoolewWebActivity.class);
        intent.putExtra(KEY_URL, url);
        intent.putExtra(KEY_TITLE, title);
        intent.putExtra(KEY_TITLE_BAR_BG, titleBarBg);
        context.startActivity(intent);
    }

    public static void startThisActivityWithoutTitleBar(Context context, String url) {
        Intent intent = new Intent(context, KoolewWebActivity.class);
        intent.putExtra(KEY_URL, url);
        intent.putExtra(KEY_NO_TITLE_BAR, true);
        context.startActivity(intent);
    }

    class WebUriProcessor extends UriProcessor {
        public WebUriProcessor(Context context) {
            super(context);
        }

        @Override
        protected boolean processUrl(String url) {
            return false;
        }
    }

    class JSInterface {
        @JavascriptInterface
        public void setParam(String key, String value) {
            if (PARAM_KEY_IS_SUPPORT_SHARE.equals(key) && "true".equals(value)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTitleBar.setRightLayoutVisibility(View.VISIBLE);
                    }
                });
            }
            mWebParams.put(key, value);
        }
    }
}
