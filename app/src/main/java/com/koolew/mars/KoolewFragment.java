package com.koolew.mars;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.koolew.mars.redpoint.RedPointManager;
import com.koolew.mars.utils.Utils;
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

    private ViewPager mViewPager;
    private KoolewFragmentPagerAdapter mAdapter;
    private KoolewViewPagerIndicator mViewPagerIndicator;

    private View mBtnAddTopic;

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

        mToolbarInterface.setTopRedPointPath(0, RedPointManager.PATH_MESSAGE);
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

        mToolbarInterface.setToolbarColor(subPageColors[mViewPager.getCurrentItem()]);

        mToolbarInterface.setTopIconCount(0);

        mBtnAddTopic = root.findViewById(R.id.btn_add_topic);
        mBtnAddTopic.setOnClickListener(this);

        return root;
    }

    private void initSubPageColors() {
        if (subPageColors == null) {
            subPageColors = new int[]{
                    getResources().getColor(R.color.koolew_light_orange),
                    getResources().getColor(R.color.koolew_black),
                    getResources().getColor(R.color.koolew_light_blue),
                    getResources().getColor(R.color.koolew_deep_orange),
            };
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_topic:
                SelectCategoryWindow window = new SelectCategoryWindow(getActivity());
                window.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int windowWidth = window.getContentView().getMeasuredWidth();
                int windowHeight = window.getContentView().getMeasuredHeight();
                int btnWidth = mBtnAddTopic.getWidth();
                int btnHeight = mBtnAddTopic.getHeight();
                window.showAsDropDown(mBtnAddTopic,
                        (int) (btnWidth - windowWidth - Utils.dpToPixels(getActivity(), 10)),
                        - (btnHeight + windowHeight));
                window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Utils.setWindowAlpha(getActivity(), 1.0f);
                    }
                });
                Utils.setWindowAlpha(getActivity(), 0.5f);
                break;
        }
    }

    class KoolewFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList;
        private List<String> fragmentTitles;

        public KoolewFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            // TODO Auto-generated constructor stub
            fragmentList = new ArrayList<Fragment>();
            fragmentTitles = new ArrayList<String>();

            fragmentList.add(new KoolewFeedsFragment());
            fragmentTitles.add(getString(R.string.koolew_news_title));

            fragmentList.add(new KoolewHotsFragment());
            fragmentTitles.add(getString(R.string.koolew_hot_title));

            fragmentList.add(new KoolewRecommendFragment());
            fragmentTitles.add(getString(R.string.koolew_recommend_title));

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
