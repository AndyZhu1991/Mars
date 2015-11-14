package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.infos.MovieTopicInfo;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.view.KoolewViewPagerIndicator;
import com.koolew.mars.view.TitleBarView;

import java.util.ArrayList;
import java.util.List;

public abstract class TopicVideoActivity extends BaseV4FragmentActivity
        implements KoolewViewPagerIndicator.OnBackgroundColorChangedListener,
        BaseVideoListFragment.TopicInfoInterface, View.OnClickListener,
        DetailTitleVideoListFragment.OnTopicCategoryListener {

    public static final String KEY_TOPIC_ID = BaseVideoListFragment.KEY_TOPIC_ID;
    public static final String KEY_TOPIC_TITLE = BaseVideoListFragment.KEY_TOPIC_TITLE;
    public static final String KEY_DEFAULT_SHOW_POSITION = "default show position";
    public static final int POSITION_WORLD = 1;

    protected String topicId;
    protected String topicTitle;

    protected int[] pageColors = null;

    protected TitleBarView mTitleBar;
    protected KoolewViewPagerIndicator mViewPagerIndicator;
    protected ViewPager mViewPager;
    protected TopicVideoPagerAdapter mAdapter;

    protected String mCategory;
    protected MovieTopicInfo mMovieInfo;

    protected View mBottomBtnLayout;
    protected View mCaptureBtn;
    protected ImageView mCaptureIcon;
    protected TextView mCaptureText;

    protected int mLayoutResId = R.layout.activity_topic_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mLayoutResId);

        Intent intent = getIntent();
        topicId = intent.getExtras().getString(KEY_TOPIC_ID);
        topicTitle = intent.getExtras().getString(KEY_TOPIC_TITLE);

        initViews();

        getPageColors();
        getPagerAdapter();

        pageColors = getPageColors();
        mAdapter = getPagerAdapter();

        mViewPager.setAdapter(mAdapter);
        int defaultShowPos = intent.getIntExtra(KEY_DEFAULT_SHOW_POSITION, 0);
        if (defaultShowPos < mAdapter.getCount()) {
            mViewPager.setCurrentItem(defaultShowPos);
        }
        mViewPagerIndicator.setViewPager(mViewPager, pageColors);
        mViewPagerIndicator.setOnBackgroundColorChangedListener(this);

        mBottomBtnLayout = findViewById(R.id.bottom_button_layout);
        mCaptureBtn = findViewById(R.id.capture);
        mCaptureBtn.setOnClickListener(this);
        mCaptureIcon = (ImageView) findViewById(R.id.capture_icon);
        mCaptureText = (TextView) findViewById(R.id.capture_text);
        findViewById(R.id.invite).setOnClickListener(this);
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

    @Override
    public void onCategoryDetermined(String category, Object extra) {
        mCategory = category;
        if ("movie".equals(category)) {
            mCaptureBtn.setBackgroundResource(R.drawable.btn_act_movie_bg);
            mCaptureIcon.setImageResource(R.mipmap.ic_act);
            mCaptureText.setText(R.string.i_will_act);
            mCaptureText.setShadowLayer(mCaptureText.getShadowRadius(),
                    mCaptureText.getShadowDx(), mCaptureText.getShadowDy(), 0xFF7351E8);
            mMovieInfo = (MovieTopicInfo) extra;
        }
        else {
            if (TextUtils.isEmpty(topicTitle)) {
                topicTitle = (String) extra;
            }
        }
        mBottomBtnLayout.setVisibility(View.VISIBLE);
    }

    protected void onCapture() {
        if ("movie".equals(mCategory)) {
            startMovieStudioActivity();
        }
        else {
            startVideoShootActivity();
        }
    }

    protected void startVideoShootActivity() {
        Intent intent = new Intent(this, VideoShootActivity.class);
        intent.putExtra(VideoShootActivity.KEY_TOPIC_ID, topicId);
        intent.putExtra(VideoShootActivity.KEY_TOPIC_TITLE, topicTitle);
        startActivity(intent);
    }

    protected void startMovieStudioActivity() {
        Intent intent = new Intent(this, MovieStudioActivity.class);
        intent.putExtra(MovieStudioActivity.KEY_MOVIE_TOPIC_INFO, mMovieInfo);
        startActivity(intent);
    }

    protected void onInvite() {
        Intent intent = new Intent(this, InviteActivity.class);
        intent.putExtra(InviteActivity.KEY_TOPIC_ID, topicId);
        intent.putExtra(InviteActivity.KEY_TITLE, topicTitle);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.capture:
                onCapture();
                break;
            case R.id.invite:
                onInvite();
                break;
        }
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
