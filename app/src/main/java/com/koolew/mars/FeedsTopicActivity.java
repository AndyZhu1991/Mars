package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.view.TitleBarView;


public class FeedsTopicActivity extends BaseV4FragmentActivity
        implements TitleBarView.OnRightLayoutClickListener {

    public static final String KEY_TOPIC_ID = BaseVideoListFragment.KEY_TOPIC_ID;
    public static final String KEY_TOPIC_TITLE = BaseVideoListFragment.KEY_TOPIC_TITLE;

    private CaptureInviteVideoListFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds_topic);

        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mFragment = new CaptureInviteVideoListFragment();
        fragmentTransaction.add(R.id.fragment_container, mFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onRightLayoutClick() {
        Intent intent = new Intent(this, WorldTopicActivity.class);
        intent.putExtra(WorldTopicActivity.KEY_TOPIC_ID, mFragment.getTopicId());
        intent.putExtra(WorldTopicActivity.KEY_TOPIC_TITLE, mFragment.getTopicTitle());
        startActivity(intent);
    }
}
