package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;


public class TopicActivity extends FragmentActivity {

    public static final String KEY_TOPIC_ID = TopicListVideoFragment.KEY_TOPIC_ID;
    public static final String KEY_TOPIC_TITLE = TopicListVideoFragment.KEY_TOPIC_TITLE;

    private TopicListVideoFragment mTopicListVideoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mTopicListVideoFragment = new TopicListVideoFragment();
        fragmentTransaction.add(R.id.fragment_container, mTopicListVideoFragment);
        fragmentTransaction.commit();
    }

    public void onCaptureClick(View v) {
        Intent intent = new Intent(this, VideoShootActivity.class);
        intent.putExtra(VideoShootActivity.KEY_TOPIC_ID, mTopicListVideoFragment.getTopicId());
        startActivity(intent);
    }

    public void onInviteClick(View v) {
        Intent intent = new Intent(this, InviteActivity.class);
        intent.putExtra(InviteActivity.KEY_TOPIC_ID, mTopicListVideoFragment.getTopicId());
        intent.putExtra(InviteActivity.KEY_TITLE, mTopicListVideoFragment.getTopicTitle());
        startActivity(intent);
    }
}
