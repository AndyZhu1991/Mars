package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.view.KoolewViewPagerIndicator;
import com.koolew.mars.view.TitleBarView;

import java.util.ArrayList;
import java.util.List;

public abstract class TopicVideoActivity extends BaseV4FragmentActivity
        implements KoolewViewPagerIndicator.OnBackgroundColorChangedListener,
        BaseVideoListFragment.TopicInfoInterface {

    public static final String KEY_TOPIC_ID = BaseVideoListFragment.KEY_TOPIC_ID;
    public static final String KEY_TOPIC_TITLE = BaseVideoListFragment.KEY_TOPIC_TITLE;

    protected String topicId;
    protected String topicTitle;

    protected int[] pageColors = null;

    protected TitleBarView mTitleBar;
    protected KoolewViewPagerIndicator mViewPagerIndicator;
    protected ViewPager mViewPager;
    protected TopicVideoPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_video);

        Intent intent = getIntent();
        topicId = intent.getExtras().getString(KEY_TOPIC_ID);
        topicTitle = intent.getExtras().getString(KEY_TOPIC_TITLE);

        initViews();

        getPageColors();
        getPagerAdapter();

        pageColors = getPageColors();
        mAdapter = getPagerAdapter();

        mViewPager.setAdapter(mAdapter);
        mViewPagerIndicator.setViewPager(mViewPager, pageColors);
        mViewPagerIndicator.setOnBackgroundColorChangedListener(this);
    }

    protected void initViews() {
        mTitleBar = (TitleBarView) findViewById(R.id.title_bar);
        mViewPagerIndicator = (KoolewViewPagerIndicator) findViewById(R.id.indicator);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
    }

    protected abstract int[] getPageColors();

    protected abstract TopicVideoPagerAdapter getPagerAdapter();

    @Override
    public void onBackgroundColorChanged(int color) {
        mTitleBar.setBackgroundColor(color);
    }

    @Override
    public String getTopicId() {
        return topicId;
    }

    public void onCapture(View view) {
        Intent intent = new Intent(this, VideoShootActivity.class);
        intent.putExtra(VideoShootActivity.KEY_TOPIC_ID, topicId);
        startActivity(intent);
    }

    public void onInvite(View view) {
        Intent intent = new Intent(this, InviteActivity.class);
        intent.putExtra(InviteActivity.KEY_TOPIC_ID, topicId);
        intent.putExtra(InviteActivity.KEY_TITLE, topicTitle);
        startActivity(intent);
    }

    static abstract class TopicVideoPagerAdapter extends FragmentPagerAdapter {

        protected List<Fragment> fragmentList;
        protected List<String> fragmentTitles;

        public TopicVideoPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentList = new ArrayList<>();
            fragmentTitles = new ArrayList<>();

            initFragmentList();
            initTitleList();
        }

        protected abstract void initFragmentList();

        protected abstract void initTitleList();

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }
}
