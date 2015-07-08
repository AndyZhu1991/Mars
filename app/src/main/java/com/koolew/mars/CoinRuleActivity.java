package com.koolew.mars;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.koolew.mars.infos.MyAccountInfo;


public class CoinRuleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_rule);

        ((TextView) findViewById(R.id.count_coin)).setText(String.valueOf(MyAccountInfo.getCoinNum()));
    }
}
