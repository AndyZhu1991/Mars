package com.koolew.mars;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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

import com.koolew.mars.view.KoolewViewPagerIndicator;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KoolewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KoolewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KoolewFragment extends MainBaseFragment implements View.OnClickListener {

    private static final String TAG = "koolew-KoolewFragment";

    private static int[] subPageColors = null;

    public enum KoolewTab {
        SQUARE, FEEDS, INVOLVE
    }

    private ViewPager mViewPager;
    private KoolewFragmentPagerAdapter mAdapter;
    private KoolewViewPagerIndicator mViewPagerIndicator;

    private FloatingActionButton mFab;

    /**
     * Use this factory method to create a new instance of
     * this fragment // using the provided parameters.
     *
     * @return A new instance of fragment KoolewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KoolewFragment newInstance() {
        KoolewFragment fragment = new KoolewFragment();
        return fragment;
    }

    public KoolewFragment() {
        // Required empty public constructor
        isNeedPageStatistics = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarInterface.setToolbarTitle(getString(R.string.title_koolew));
        initSubPageColors();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_koolew, container, false);

        mViewPager = (ViewPager) root.findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(4);
        mAdapter = new KoolewFragmentPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPagerIndicator = (KoolewViewPagerIndicator) root.findViewById(R.id.indicator);
        mViewPagerIndicator.setViewPager(mViewPager, subPageColors);
        mViewPagerIndicator.setOnBackgroundColorChangedListener(
                new KoolewViewPagerIndicator.OnBackgroundColorChangedListener() {
                    @Override
                    public void onBackgroundColorChanged(int color) {
                        mToolbarInterface.setToolbarColor(color);
                    }
                }
        );
        mViewPager.setCurrentItem(mStartTabPosition, false);

        mToolbarInterface.setToolbarColor(subPageColors[mViewPager.getCurrentItem()]);

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

    private void initSubPageColors() {
        if (subPageColors == null) {
            subPageColors = new int[]{
                    getResources().getColor(R.color.koolew_black),
                    getResources().getColor(R.color.koolew_light_orange),
                    getResources().getColor(R.color.koolew_deep_orange),
            };
        }
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

    class KoolewFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList;
        private List<String> fragmentTitles;

        public KoolewFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            // TODO Auto-generated constructor stub
            fragmentList = new ArrayList<Fragment>();
            fragmentTitles = new ArrayList<String>();

            fragmentList.add(new KoolewSquareFragment());
            fragmentTitles.add(getString(R.string.koolew_square_title));

            fragmentList.add(new KoolewFeedsFragment());
            fragmentTitles.add(getString(R.string.koolew_news_title));

            fragmentList.add(new KoolewInvolveFragment());
            fragmentTitles.add(getString(R.string.koolew_involve_title));
        }

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
