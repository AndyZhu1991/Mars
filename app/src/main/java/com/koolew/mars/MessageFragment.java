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

import com.koolew.mars.redpoint.RedPointManager;
import com.koolew.mars.redpoint.RedPointView;
import com.koolew.mars.utils.PagerScrollSmoothColorListener;
import com.koolew.android.utils.Utils;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.ScrollIndicatorView;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 12/25/15.
 */
public class MessageFragment extends MainBaseFragment {

    public enum MessageTab {
        DANMAKU, KOO, NOTIFICATION, TASK
    }

    public MessageFragment() {
        isNeedPageStatistics = false;
    }

    private IndicatorViewPager indicatorViewPager;
    private ScrollIndicatorView indicator;
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarInterface.setToolbarTitle(getString(R.string.title_message));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_message, container, false);

        mToolbarInterface.setToolbarColor(getResources().getColor(R.color.koolew_light_blue));
        mToolbarInterface.setTopIconCount(0);

        viewPager = (ViewPager) root.findViewById(R.id.view_pager);
        indicator = (ScrollIndicatorView) root.findViewById(R.id.indicator);

        indicator.setScrollBar(new ColorBar(getActivity(), Color.WHITE,
                getResources().getDimensionPixelSize(R.dimen.underline_height)));
        indicator.setOnTransitionListener(new TransitionTextListener().setColorId(getActivity(),
                R.color.title_text_color_indicated, R.color.title_text_color_unindicate));

        viewPager.setOffscreenPageLimit(4);
        viewPager.addOnPageChangeListener(new PagerScrollListener(
                getResources().getColor(R.color.koolew_light_blue),
                getResources().getColor(R.color.koolew_light_red),
                getResources().getColor(R.color.koolew_deep_blue),
                getResources().getColor(R.color.koolew_light_green)
        ));
        indicatorViewPager = new IndicatorViewPager(indicator, viewPager);
        indicatorViewPager.setAdapter(new MessageFragmentPagerAdapter(getChildFragmentManager()));
        indicatorViewPager.setCurrentItem(mStartTabPosition, false);

        return root;
    }

    @Override
    protected ViewPager getViewPager() {
        return viewPager;
    }

    class MessageFragmentPagerAdapter extends IndicatorViewPager.IndicatorFragmentPagerAdapter {

        private List<Fragment> fragmentList;
        private List<String> titleList;
        private List<String> redPointPathList;

        public MessageFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            fragmentList = new ArrayList<>();
            titleList = new ArrayList<>();
            redPointPathList = new ArrayList<>();

            fragmentList.add(new DanmakuTabFragment());
            titleList.add(getString(R.string.danmaku));
            redPointPathList.add(RedPointManager.PATH_DANMAKU);

            fragmentList.add(new KooTabFragment());
            titleList.add(getString(R.string.musang));
            redPointPathList.add(RedPointManager.PATH_KOO);

            fragmentList.add(new NotificationTabFragment());
            titleList.add(getString(R.string.notification));
            redPointPathList.add(RedPointManager.PATH_NOTIFICATION);

            fragmentList.add(new TaskTabFragment());
            titleList.add(getString(R.string.task));
            redPointPathList.add(RedPointManager.PATH_TASK);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public View getViewForTab(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.indicator_with_red_point, container, false);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(titleList.get(position));
            int paddingLR = (int) Utils.dpToPixels(10);
            textView.setPadding(paddingLR, 0, paddingLR, 0);
            RedPointView redPoint = (RedPointView) convertView.findViewById(R.id.red_point);
            redPoint.registerPath(redPointPathList.get(position));
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
            mToolbarInterface.setToolbarColor(color);
            indicator.setBackgroundColor(color);
        }
    }

    class TransitionTextListener extends OnTransitionTextListener {
        @Override
        public TextView getTextView(View tabItemView, int position) {
            return (TextView) tabItemView.findViewById(R.id.text);
        }
    }
}
