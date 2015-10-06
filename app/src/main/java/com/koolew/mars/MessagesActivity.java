package com.koolew.mars;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.utils.PagerScrollSmoothColorListener;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.TitleBarView;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.ScrollIndicatorView;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;

import java.util.ArrayList;
import java.util.List;


public class MessagesActivity extends BaseV4FragmentActivity {

    private TitleBarView titleBar;
    private IndicatorViewPager indicatorViewPager;
    private ScrollIndicatorView indicator;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        titleBar = (TitleBarView) findViewById(R.id.title_bar);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        indicator = (ScrollIndicatorView) findViewById(R.id.indicator);

        indicator.setScrollBar(new ColorBar(this, Color.WHITE,
                getResources().getDimensionPixelSize(R.dimen.underline_height)));
        indicator.setOnTransitionListener(new OnTransitionTextListener().setColorId(this,
                R.color.title_text_color_indicated, R.color.title_text_color_unindicate));

        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new PagerScrollListener(
                getResources().getColor(R.color.koolew_light_blue),
                getResources().getColor(R.color.koolew_deep_blue)
        ));
        indicatorViewPager = new IndicatorViewPager(indicator, viewPager);
        indicatorViewPager.setAdapter(new MessageFragmentPagerAdapter(getSupportFragmentManager()));
    }


    class MessageFragmentPagerAdapter extends IndicatorViewPager.IndicatorFragmentPagerAdapter {

        private List<Fragment> fragmentList;
        private List<String> titleList;

        public MessageFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            fragmentList = new ArrayList<>();
            titleList = new ArrayList<>();

            fragmentList.add(new DanmakuTabFragment());
            titleList.add(getString(R.string.danmaku));

            fragmentList.add(new NotificationTabFragment());
            titleList.add(getString(R.string.notification));
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public View getViewForTab(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MessagesActivity.this)
                        .inflate(R.layout.indicator_text, container, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(titleList.get(position));
            int paddingLR = (int) Utils.dpToPixels(MessagesActivity.this, 10);
            textView.setPadding(paddingLR, 0, paddingLR, 0);
            return convertView;
        }

        @Override
        public Fragment getFragmentForPage(int position) {
            return fragmentList.get(position);
        }
    }

    class PagerScrollListener extends PagerScrollSmoothColorListener {

        public PagerScrollListener(int... colors) {
            super(colors);
        }

        @Override
        public void onColorChanged(int color) {
            titleBar.setBackgroundColor(color);
            indicator.setBackgroundColor(color);
        }
    }
}
