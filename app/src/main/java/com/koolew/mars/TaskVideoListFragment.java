package com.koolew.mars;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jinchangzhu on 7/28/15.
 */
public class TaskVideoListFragment extends BaseVideoListFragment implements View.OnClickListener {

    public static final String KEY_INVITER = "inviter";

    private String mInviter;

    public TaskVideoListFragment() {
        super();
        mLayoutResId = R.layout.fragment_task_video_list;
    }

    @Override
    protected VideoCardAdapter useThisAdapter() {
        return new TaskVideoListAdapter(getActivity());
    }

    @Override
    public int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_green);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInviter = getActivity().getIntent().getStringExtra(KEY_INVITER);
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


    class TaskVideoListAdapter extends VideoCardAdapter {
        public TaskVideoListAdapter(Context context) {
            super(context);
        }

        @Override
        protected View getTitleView() {
            View view = mInflater.inflate(R.layout.task_topic_title_layout, null);
            mTitleText = (TextView) view.findViewById(R.id.title);
            mTitleText.setText(mTopicTitle);
            ((TextView) view.findViewById(R.id.inviter_text))
                    .setText(getString(R.string.invited_label, mInviter));
            return view;
        }
    }
}
