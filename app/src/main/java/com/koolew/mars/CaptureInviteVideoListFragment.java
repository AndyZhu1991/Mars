package com.koolew.mars;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koolew.mars.statistics.StatisticsEvent;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by jinchangzhu on 7/27/15.
 */
public class CaptureInviteVideoListFragment extends BaseVideoListFragment
        implements View.OnClickListener {

    public CaptureInviteVideoListFragment() {
        super();
        mLayoutResId = R.layout.fragment_capture_invite_video_list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        root.findViewById(R.id.capture).setOnClickListener(this);
        root.findViewById(R.id.invite).setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.capture:
                MobclickAgent.onEvent(getActivity(), StatisticsEvent.EVENT_TAKE_VIDEO);
                capture();
                break;
            case R.id.invite:
                MobclickAgent.onEvent(getActivity(), StatisticsEvent.EVENT_INVITE_FRIEND);
                invite();
                break;
        }
    }
}
