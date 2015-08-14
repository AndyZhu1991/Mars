package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;

import com.koolew.mars.view.TitleBarView;


public class WorldTopicActivity extends FragmentActivity
        implements TitleBarView.OnRightLayoutClickListener {

    public static final String KEY_TOPIC_ID = BaseVideoListFragment.KEY_TOPIC_ID;
    public static final String KEY_TOPIC_TITLE = BaseVideoListFragment.KEY_TOPIC_TITLE;

    private String mTopicId;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_topic);

        Intent intent = getIntent();
        mTopicId = intent.getStringExtra(KEY_TOPIC_ID);
        mTitle = intent.getStringExtra(KEY_TOPIC_TITLE);

        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new WorldVideoListFragment());
        fragmentTransaction.commit();
    }

    @Override
    public void onRightLayoutClick() {
        new ShareVideoWindow(this, ShareVideoWindow.TYPE_VIDEO_LIST, mTopicId, mTitle)
                .showAtLocation(findViewById(R.id.fragment_container), Gravity.TOP, 0, 0);
    }
}
