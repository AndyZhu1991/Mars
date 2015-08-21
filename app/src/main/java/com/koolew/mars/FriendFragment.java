package com.koolew.mars;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koolew.mars.view.KoolewViewPagerIndicator;

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

    private ViewPager mViewPager;
    private FriendFragmentPagerAdapter mAdapter;
    private KoolewViewPagerIndicator mViewPagerIndicator;


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

        mViewPager = (ViewPager) root.findViewById(R.id.view_pager);
        mAdapter = new FriendFragmentPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPagerIndicator = (KoolewViewPagerIndicator) root.findViewById(R.id.indicator);
        mViewPagerIndicator.setViewPager(mViewPager);
        mViewPagerIndicator.setOnBackgroundColorChangedListener(
                new KoolewViewPagerIndicator.OnBackgroundColorChangedListener() {
                    @Override
                    public void onBackgroundColorChanged(int color) {
                        mToolbarInterface.setToolbarColor(color);
                    }
                }
        );

        mToolbarInterface.setToolbarColor(getResources().getColor(R.color.koolew_light_blue));

        mToolbarInterface.setTopIconCount(1);
        mToolbarInterface.setTopIconImageResource(0, R.mipmap.ic_search);

        return root;
    }

    @Override
    public void onTopIconClick(int position) {
        new SearchUserWindow(getActivity()).showAtLocation(
                getActivity().findViewById(R.id.my_toolbar), Gravity.TOP, 0, 0);
    }

    class FriendFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<android.support.v4.app.Fragment> fragmentList;
        private List<String> fragmentTitles;

        public FriendFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            // TODO Auto-generated constructor stub
            fragmentList = new ArrayList<android.support.v4.app.Fragment>();
            fragmentTitles = new ArrayList<String>();

            fragmentList.add(FriendMeetFragment.newInstance());
            fragmentTitles.add(getString(R.string.friend_meet_title));

            fragmentList.add(FriendContactFragment.newInstance());
            fragmentTitles.add(getString(R.string.friend_contact_title));

            //fragmentList.add(FriendWeiboFragment.newInstance());
            //fragmentTitles.add(getString(R.string.friend_weibo_title));

            fragmentList.add(FriendCurrentFragment.newInstance());
            fragmentTitles.add(getString(R.string.friend_current_title));
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
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
