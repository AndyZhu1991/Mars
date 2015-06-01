package com.koolew.mars;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.koolew.mars.infos.MyAccountInfo;


public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        String token = MyAccountInfo.getToken();
        Intent intent = null;
        if (token == null || token.equals("")) {
            intent = new Intent(this, FirstLoginActivity.class);
        }
        else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }

}
