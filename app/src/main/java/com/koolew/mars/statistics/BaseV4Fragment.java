package com.koolew.mars.statistics;

import android.support.v4.app.Fragment;

import com.tendcloud.tenddata.TCAgent;

/**
 * Created by jinchangzhu on 8/20/15.
 */
public abstract class BaseV4Fragment extends Fragment {

    protected boolean isNeedPageStatistics = true;

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            onPageStart();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            onPageEnd();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            onPageStart();
        }
        else if (!isVisibleToUser && isResumed()) {
            onPageEnd();
        }
    }

    protected void onPageStart() {
        if (isNeedPageStatistics && StatisticsUtil.NEED_STATISTICS) {
            TCAgent.onPageStart(getActivity(), getClass().getSimpleName());
        }
    }

    protected void onPageEnd() {
        if (isNeedPageStatistics && StatisticsUtil.NEED_STATISTICS) {
            TCAgent.onPageEnd(getActivity(), getClass().getSimpleName());
        }
    }
}
