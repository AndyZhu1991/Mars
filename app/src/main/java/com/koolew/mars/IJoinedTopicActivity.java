package com.koolew.mars;

import android.os.Bundle;

/**
 * Created by jinchangzhu on 8/19/15.
 */
public class IJoinedTopicActivity extends UserTopicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitleBar.setTitle(R.string.koolew_involve_title);
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.koolew_deep_orange));
    }
}
