package com.koolew.mars.statistics;

import android.app.Activity;

import com.tendcloud.tenddata.TCAgent;

/**
 * Created by jinchangzhu on 8/20/15.
 */
public abstract class BaseActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        TCAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        TCAgent.onPause(this);
    }
}
