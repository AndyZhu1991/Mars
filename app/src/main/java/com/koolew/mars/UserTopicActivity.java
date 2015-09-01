package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.view.TitleBarView;


public class UserTopicActivity extends BaseV4FragmentActivity
        implements TitleBarView.OnRightLayoutClickListener,
        BaseVideoListFragment.TopicInfoInterface {

    public static final String KEY_TOPIC_ID = BaseVideoListFragment.KEY_TOPIC_ID;
    public static final String KEY_UID = UserVideoListFragment.KEY_UID;
    public static final String KEY_NICKNAME = "nickname";

    protected TitleBarView mTitleBar;
    protected UserVideoListFragment mFragment;

    protected String topicId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_topic);

        mTitleBar = (TitleBarView) findViewById(R.id.title_bar);
        mTitleBar.setOnRightLayoutClickListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mFragment = new UserVideoListFragment();
        fragmentTransaction.add(R.id.fragment_container, mFragment);
        fragmentTransaction.commit();

        Intent intent = getIntent();
        topicId = intent.getStringExtra(KEY_TOPIC_ID);
        String nickname = intent.getStringExtra(KEY_NICKNAME);
        if (!TextUtils.isEmpty(nickname)) {
            mTitleBar.setTitle("@" + nickname);
        }
    }

    @Override
    public void onRightLayoutClick() {
        Intent intent = new Intent(this, WorldTopicActivity.class);
        intent.putExtra(WorldTopicActivity.KEY_TOPIC_ID, mFragment.getTopicId());
        intent.putExtra(WorldTopicActivity.KEY_TOPIC_TITLE, mFragment.getTopicTitle());
        startActivity(intent);
    }

    @Override
    public String getTopicId() {
        return topicId;
    }
}
