package com.koolew.mars;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

    private FloatingActionButton mFab;


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

        mToolbarInterface.setTopIconCount(1);
        mToolbarInterface.setTopIconImageResource(0, R.mipmap.ic_search);

        mFab = (FloatingActionButton) root.findViewById(R.id.fab);
        mFab.setOnClickListener(this);

        return root;
    }

    @Override
    protected ViewPager getViewPager() {
        return mViewPager;
    }

    public void showSignMessage(int signContinuous, int coinGot) {
        Snackbar snackbar = Snackbar.make(mFab, "", Snackbar.LENGTH_LONG);
        View contentLayout = LayoutInflater.from(getContext()).inflate(R.layout.sign_success_layout, null);

        // Spans
        ForegroundColorSpan whiteSpan = new ForegroundColorSpan(Color.WHITE);
        ForegroundColorSpan signContinuousSpan = new ForegroundColorSpan(0xFF414141);
        ForegroundColorSpan coinGotSpan = new ForegroundColorSpan(0xFFFFF358);

        // sign continuous spannable
        String signContinuousStr = getString(R.string.sign_continuous, signContinuous);
        SpannableStringBuilder signContinuousBuilder = new SpannableStringBuilder(signContinuousStr);
        int daysPosition = signContinuousStr.indexOf(String.valueOf(signContinuous));
        signContinuousBuilder.setSpan(whiteSpan, 0, daysPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        signContinuousBuilder.setSpan(signContinuousSpan, daysPosition, signContinuousStr.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) contentLayout.findViewById(R.id.sign_continuous)).setText(signContinuousBuilder);

        // coin got spannable
        String coinGotStr = getString(R.string.coin_got, coinGot);
        SpannableStringBuilder coinGotBuilder = new SpannableStringBuilder(coinGotStr);
        int coinsPosition = coinGotStr.indexOf(String.valueOf(coinGot));
        coinGotBuilder.setSpan(whiteSpan, 0, coinsPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        coinGotBuilder.setSpan(coinGotSpan, coinsPosition, coinGotStr.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) contentLayout.findViewById(R.id.coin_got)).setText(coinGotBuilder);

        LinearLayout snackView = ((LinearLayout) snackbar.getView());
        snackView.setBackgroundColor(0xFF5FD4E6);
        snackView.addView(contentLayout, 0);
        snackbar.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                SelectCategoryWindow window = new SelectCategoryWindow(getActivity());
                window.showAtLocation(getView(), Gravity.TOP, 0, 0);
                break;
        }
    }

    @Override
    public void onTopIconClick(int position) {
        getContext().startActivity(new Intent(getContext(), GlobalSearchActivity.class));
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
