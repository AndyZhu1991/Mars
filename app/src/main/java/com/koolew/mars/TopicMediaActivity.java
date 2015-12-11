package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MovieTopicInfo;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.utils.PagerScrollSmoothColorListener;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.TitleBarView;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.ScrollIndicatorView;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;

/**
 * Created by jinchangzhu on 12/4/15.
 */
public class TopicMediaActivity extends BaseV4FragmentActivity implements View.OnClickListener,
        CommonMediaFragment.OnTopicInfoUpdateListener {

    public static final String KEY_TOPIC_ID = BaseTopicInfo.KEY_TOPIC_ID;
    public static final String KEY_TYPE = "type";
    public static final String KEY_TARGET_VIDEO_ID = BaseVideoInfo.KEY_VIDEO_ID;

    public static final int TYPE_FEEDS = 0;
    public static final int TYPE_WORLD = 1;
    public static final int TYPE_TASK = 2;

    private String mTopicId;
    private BaseTopicInfo mTopicInfo;
    private int mType;
    private String mTargetVideoId;

    private TitleBarView mTitleBar;
    private IndicatorViewPager mIndicatorViewPager;
    private ScrollIndicatorView mIndicator;
    private ViewPager mViewPager;

    protected View mBottomBtnLayout;
    protected View mCaptureBtn;
    protected TextView mCaptureText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_topic_media);

        mTopicId = getIntent().getStringExtra(KEY_TOPIC_ID);
        mType = getIntent().getIntExtra(KEY_TYPE, TYPE_FEEDS);
        if (TextUtils.isEmpty(mTopicId)) {
            throw new RuntimeException("No topic id ?!");
        }
        mTargetVideoId = getIntent().getStringExtra(KEY_TARGET_VIDEO_ID);

        initViews();
    }

    private void initViews() {
        mTitleBar = (TitleBarView) findViewById(R.id.title_bar);
        mIndicator = (ScrollIndicatorView) findViewById(R.id.indicator);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mIndicator.setScrollBar(new ColorBar(this, Color.WHITE,
                getResources().getDimensionPixelSize(R.dimen.underline_height)));
        mIndicator.setOnTransitionListener(new OnTransitionTextListener().setColorId(this,
                R.color.title_text_color_indicated, R.color.title_text_color_unindicate));

        mViewPager.setOffscreenPageLimit(2);
        int themeColor0 = mType == TYPE_TASK ? getResources().getColor(R.color.koolew_light_green)
                : getResources().getColor(R.color.koolew_light_orange);
        mViewPager.addOnPageChangeListener(new PagerScrollListener(
                themeColor0,
                getResources().getColor(R.color.koolew_light_blue)
        ));

        mIndicatorViewPager = new IndicatorViewPager(mIndicator, mViewPager);
        mIndicatorViewPager.setAdapter(new TopicMediaFragmentPagerAdapter(getSupportFragmentManager()));
        int initialPagerPosition = mType == TYPE_WORLD ? 1 : 0;
        mIndicatorViewPager.setCurrentItem(initialPagerPosition, false);


        mBottomBtnLayout = findViewById(R.id.bottom_button_layout);
        mCaptureBtn = findViewById(R.id.capture);
        mCaptureBtn.setOnClickListener(this);
        mCaptureText = (TextView) findViewById(R.id.capture_text);
        findViewById(R.id.invite).setOnClickListener(this);
    }

    protected void onCapture() {
        if (BaseTopicInfo.CATEGORY_MOVIE.equals(mTopicInfo.getCategory())) {
            startMovieStudioActivity();
        }
        else if (BaseTopicInfo.CATEGORY_VIDEO.equals(mTopicInfo.getCategory())) {
            startVideoShootActivity();
        }
        else {
            if (MarsApplication.DEBUG) {
                throw new RuntimeException("Shen me gui: " + mTopicInfo.getCategory());
            }
        }
    }

    protected void startVideoShootActivity() {
        VideoShootActivity.startThisActivity(this, mTopicId, mTopicInfo.getTitle());
    }

    protected void startMovieStudioActivity() {
        if (mTopicInfo instanceof MovieTopicInfo) {
            MovieStudioActivity.startThisActivity(this, (MovieTopicInfo) mTopicInfo);
        }
        else {
            if (MarsApplication.DEBUG) {
                throw new RuntimeException("No MovieTopicInfo");
            }
            else {
                Toast.makeText(this, R.string.there_is_an_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onInvite() {
        InviteActivity.startThisActivity(this, mTopicInfo);
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

    @Override
    public void onCategoryDetermined(BaseTopicInfo topicInfo) {
        mTopicInfo = topicInfo;

        int bottomLayoutVisibility = View.VISIBLE;
        if (BaseTopicInfo.CATEGORY_MOVIE.equals(mTopicInfo.getCategory())) {
            mCaptureBtn.setBackgroundResource(R.drawable.btn_act_movie_bg);
            mCaptureText.setText(R.string.i_will_act);
            Utils.setTextViewDrawableLeft(mCaptureText, R.mipmap.ic_act);
            mCaptureText.setShadowLayer(mCaptureText.getShadowRadius(),
                    mCaptureText.getShadowDx(), mCaptureText.getShadowDy(), 0xFF7351E8);
        }
        else if (BaseTopicInfo.CATEGORY_VIDEO.equals(mTopicInfo.getCategory())) {
            // Do nothing
        }
        else {
            bottomLayoutVisibility = View.INVISIBLE;
        }
        mBottomBtnLayout.setVisibility(bottomLayoutVisibility);
    }

    public static void startThisActivity(Context context, String topicId, int type) {
        startThisActivity(context, topicId, type, null);
    }

    public static void startThisActivity(Context context, String topicId, int type, String targetVideoId) {
        Intent intent = new Intent(context, TopicMediaActivity.class);
        intent.putExtra(KEY_TOPIC_ID, topicId);
        intent.putExtra(KEY_TYPE, type);
        if (!TextUtils.isEmpty(targetVideoId)) {
            intent.putExtra(KEY_TARGET_VIDEO_ID, targetVideoId);
        }
        context.startActivity(intent);
    }


    private static final int TAB_COUNT = 2; // Feeds/Task and World

    class TopicMediaFragmentPagerAdapter extends IndicatorViewPager.IndicatorFragmentPagerAdapter {

        private CommonMediaFragment[] fragments = new CommonMediaFragment[TAB_COUNT];
        private String[] titles = new String[TAB_COUNT];

        public TopicMediaFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);

            if (mType == TYPE_TASK) {
                fragments[0] = new TaskMediaFragment(mTopicId);
            }
            else {
                if (mTargetVideoId == null) {
                    fragments[0] = new FeedsMediaFragment(mTopicId);
                }
                else {
                    fragments[0] = new FeedsMediaFragment(mTopicId, mTargetVideoId);
                }
            }
            titles[0] = getString(R.string.feeds_title_friend);

            fragments[1] = new WorldMediaFragment(mTopicId);
            titles[1] = getString(R.string.world_title_public);
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public View getViewForTab(int i, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(TopicMediaActivity.this)
                        .inflate(R.layout.indicator_text, container, false);
            }
            ((TextView) convertView).setText(titles[i]);
            return convertView;
        }

        @Override
        public Fragment getFragmentForPage(int i) {
            return fragments[i];
        }
    }

    class PagerScrollListener extends PagerScrollSmoothColorListener {

        public PagerScrollListener(int... colors) {
            super(colors);
        }

        @Override
        public void onColorChanged(int color) {
            mTitleBar.setBackgroundColor(color);
            mIndicator.setBackgroundColor(color);
        }
    }
}
