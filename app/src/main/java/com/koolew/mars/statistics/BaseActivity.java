package com.koolew.mars.statistics;

import android.app.Activity;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by jinchangzhu on 8/20/15.
 */
public abstract class BaseActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getSimpleName());
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getSimpleName());
        MobclickAgent.onPause(this);
    }
}
