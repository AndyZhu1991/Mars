package com.koolew.mars;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koolew.mars.utils.PagerScrollSmoothColorListener;
import com.koolew.mars.utils.Utils;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.ScrollIndicatorView;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;

import java.util.ArrayList;
import java.util.List;


public class KoolewFragment extends MainBaseFragment implements View.OnClickListener {

    private static final String TAG = "koolew-KoolewFragment";

    public enum KoolewTab {
        SQUARE, FEEDS, INVOLVE
    }

    private ViewPager mViewPager;
    private IndicatorViewPager indicatorViewPager;
    private ScrollIndicatorView indicator;

    private View mBtnAddTopic;


    public KoolewFragment() {
        // Required empty public constructor
        isNeedPageStatistics = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarInterface.setToolbarTitle(getString(R.string.title_koolew));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_koolew, container, false);

        mViewPager = (ViewPager) root.findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new PagerScrollListener(
                getResources().getColor(R.color.koolew_black),
                getResources().getColor(R.color.koolew_light_orange),
                getResources().getColor(R.color.koolew_deep_orange)
        ));

        indicator = (ScrollIndicatorView) root.findViewById(R.id.indicator);
        indicator.setScrollBar(new ColorBar(getActivity(), Color.WHITE,
                getResources().getDimensionPixelSize(R.dimen.underline_height)));
        indicator.setOnTransitionListener(new OnTransitionTextListener().setColorId(getActivity(),
                R.color.title_text_color_indicated, R.color.title_text_color_unindicate));

        indicatorViewPager = new IndicatorViewPager(indicator, mViewPager);
        indicatorViewPager.setAdapter(new KoolewFragmentPagerAdapter(getChildFragmentManager()));
        indicatorViewPager.setCurrentItem(mStartTabPosition, false);

        mToolbarInterface.setTopIconCount(0);

        mBtnAddTopic = root.findViewById(R.id.btn_add_topic);
        mBtnAddTopic.setOnClickListener(this);

        return root;
    }

    @Override
    protected ViewPager getViewPager() {
        return mViewPager;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_topic:
                SelectCategoryWindow window = new SelectCategoryWindow(getActivity());
                window.showAtLocation(getView(), Gravity.TOP, 0, 0);
                break;
        }
    }

    class KoolewFragmentPagerAdapter extends IndicatorViewPager.IndicatorFragmentPagerAdapter {

        private List<Fragment> fragmentList;
        private List<String> titleList;

        public KoolewFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            fragmentList = new ArrayList<>();
            titleList = new ArrayList<>();

            fragmentList.add(new KoolewSquareFragment());
            titleList.add(getString(R.string.koolew_square_title));

            fragmentList.add(new KoolewFeedsFragment());
            titleList.add(getString(R.string.koolew_news_title));

            fragmentList.add(new KoolewInvolveFragment());
            titleList.add(getString(R.string.koolew_involve_title));
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public View getViewForTab(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.indicator_text, container, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(titleList.get(position));
            int paddingLR = (int) Utils.dpToPixels(getActivity(), 10);
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
            mToolbarInterface.setToolbarColor(color);
            indicator.setBackgroundColor(color);
        }
    }
}
