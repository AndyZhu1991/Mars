package com.koolew.mars;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class FirstLoginActivity extends Activity {

    private static final String TAG = "koolew-FirstLoginA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_login);
    }

    public void onLoginClick(View v) {

        switch (v.getId()) {
            case R.id.login_by_phone:
                startActivity(new Intent(this, PhoneLoginActivity.class));
                break;
            case R.id.login_by_weibo:
                break;
            case R.id.login_by_qq:
                break;
            case R.id.login_by_wechat:
                break;
        }
    }
}
