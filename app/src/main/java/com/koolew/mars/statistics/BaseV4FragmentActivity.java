package com.koolew.mars.statistics;

import android.support.v4.app.FragmentActivity;

import com.tendcloud.tenddata.TCAgent;

/**
 * Created by jinchangzhu on 8/20/15.
 */
public abstract class BaseV4FragmentActivity extends FragmentActivity {

    protected boolean isNeedPageStatistics = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedPageStatistics) {
            TCAgent.onResume(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isNeedPageStatistics) {
            TCAgent.onPause(this);
        }
    }
}
