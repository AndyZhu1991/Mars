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

import com.koolew.mars.utils.Utils;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.ScrollIndicatorView;
import com.shizhefei.view.indicator.slidebar.ColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FriendFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FriendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendFragment extends MainBaseFragment {

    private static final String TAG = "koolew-KoolewFragment";

    private IndicatorViewPager indicatorViewPager;
    private ScrollIndicatorView indicator;
    private ViewPager viewPager;
    private FriendFragmentPagerAdapter mAdapter;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FriendFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendFragment newInstance() {
        FriendFragment fragment = new FriendFragment();
        return fragment;
    }

    public FriendFragment() {
        // Required empty public constructor
        isNeedPageStatistics = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarInterface.setToolbarTitle(getString(R.string.title_friend));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_friend, container, false);

        mToolbarInterface.setToolbarColor(getResources().getColor(R.color.koolew_light_blue));

        mToolbarInterface.setTopIconCount(1);
        mToolbarInterface.setTopIconImageResource(0, R.mipmap.ic_search);

        viewPager = (ViewPager) root.findViewById(R.id.view_pager);
        indicator = (ScrollIndicatorView) root.findViewById(R.id.indicator);
        mAdapter = new FriendFragmentPagerAdapter(getChildFragmentManager());

        indicator.setScrollBar(new ColorBar(getActivity(), Color.WHITE,
                getResources().getDimensionPixelSize(R.dimen.underline_height)));
        indicator.setOnTransitionListener(new OnTransitionTextListener().setColorId(getActivity(),
                R.color.title_text_color_indicated, R.color.title_text_color_unindicate));

        viewPager.setOffscreenPageLimit(5);
        indicatorViewPager = new IndicatorViewPager(indicator, viewPager);
        indicatorViewPager.setAdapter(new FriendFragmentPagerAdapter(getChildFragmentManager()));

        return root;
    }

    @Override
    public void onTopIconClick(int position) {
        new SearchUserWindow(getActivity()).showAtLocation(
                getActivity().findViewById(R.id.my_toolbar), Gravity.TOP, 0, 0);
    }

    class FriendFragmentPagerAdapter extends IndicatorViewPager.IndicatorFragmentPagerAdapter {

        private List<android.support.v4.app.Fragment> fragmentList;
        private List<String> fragmentTitles;

        public FriendFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            // TODO Auto-generated constructor stub
            fragmentList = new ArrayList<>();
            fragmentTitles = new ArrayList<>();

            fragmentList.add(FriendMeetFragment.newInstance());
            fragmentTitles.add(getString(R.string.friend_meet_title));

            //fragmentList.add(FriendWeiboFragment.newInstance());
            //fragmentTitles.add(getString(R.string.friend_weibo_title));

            fragmentList.add(FriendCurrentFragment.newInstance());
            fragmentTitles.add(getString(R.string.friend_current_title));

            fragmentList.add(new FriendFollowsFragment());
            fragmentTitles.add(getString(R.string.friend_follows_title));

            fragmentList.add(new FriendFansFragment());
            fragmentTitles.add(getString(R.string.friend_fans_title));

            fragmentList.add(FriendContactFragment.newInstance());
            fragmentTitles.add(getString(R.string.friend_contact_title));
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
            textView.setText(fragmentTitles.get(position));
            int paddingLR = (int) Utils.dpToPixels(getActivity(), 10);
            textView.setPadding(paddingLR, 0, paddingLR, 0);
            return convertView;
        }

        @Override
        public Fragment getFragmentForPage(int position) {
            return fragmentList.get(position);
        }
    }
}
