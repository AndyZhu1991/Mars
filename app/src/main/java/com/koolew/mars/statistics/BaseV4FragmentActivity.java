package com.koolew.mars.statistics;

import android.support.v4.app.FragmentActivity;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by jinchangzhu on 8/20/15.
 */
public abstract class BaseV4FragmentActivity extends FragmentActivity {

    protected boolean isNeedPageStatistics = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedPageStatistics) {
            MobclickAgent.onPageStart(getClass().getSimpleName());
        }
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isNeedPageStatistics) {
            MobclickAgent.onPageEnd(getClass().getSimpleName());
        }
        MobclickAgent.onPause(this);
    }
}
