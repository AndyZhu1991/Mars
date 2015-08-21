package com.koolew.mars;

import android.os.Bundle;
import android.webkit.WebView;

import com.koolew.mars.statistics.BaseActivity;

public class PrivacyPolicyActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        ((WebView) findViewById(R.id.web_view)).loadUrl("http://www.koolew.com/agreement.html");
    }
}
