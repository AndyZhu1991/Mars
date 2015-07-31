package com.koolew.mars;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jinchangzhu on 7/28/15.
 */
public class TaskVideoListFragment extends BaseVideoListFragment implements View.OnClickListener {

    public TaskVideoListFragment() {
        super();
        mLayoutResId = R.layout.fragment_task_video_list;
    }

    @Override
    public int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_green);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        root.findViewById(R.id.btn_accept).setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_accept:
                capture();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    getActivity().setResult(Activity.RESULT_OK);
                }
                break;
        }
    }
}
