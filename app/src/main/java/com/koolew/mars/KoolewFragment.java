package com.koolew.mars;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class KoolewFragment extends MainBaseFragment {

    private static final String TAG = "koolew-KoolewFragment";

    private ViewPager mViewPager;
    private KoolewFragmentPagerAdapter mAdapter;
    private KoolewViewPagerIndicator mViewPagerIndicator;

    private MainColorChangedListener mMainColorChangedListener;

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_koolew, container, false);

        mViewPager = (ViewPager) root.findViewById(R.id.view_pager);
        mAdapter = new KoolewFragmentPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPagerIndicator = (KoolewViewPagerIndicator) root.findViewById(R.id.indicator);
        mViewPagerIndicator.setViewPager(mViewPager, new int[] {
                getResources().getColor(R.color.koolew_light_orange),
                getResources().getColor(R.color.koolew_deep_orange),
        });
        mViewPagerIndicator.setOnBackgroundColorChangedListener(
                new KoolewViewPagerIndicator.OnBackgroundColorChangedListener() {
                    @Override
                    public void onBackgroundColorChanged(int color) {
                        mMainColorChangedListener.onMainColorChanged(color);
                    }
                }
        );

        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mMainColorChangedListener = (MainColorChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MainColorChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMainColorChangedListener = null;
    }

    class KoolewFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList;
        private List<String> fragmentTitles;

        public KoolewFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            // TODO Auto-generated constructor stub
            fragmentList = new ArrayList<Fragment>();
            fragmentTitles = new ArrayList<String>();

            fragmentList.add(KoolewNewsFragment.newInstance());
            fragmentTitles.add(getString(R.string.koolew_news_title));

            fragmentList.add(KoolewRelatedMeFragment.newInstance());
            fragmentTitles.add(getString(R.string.koolew_related_me_title));
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

    public interface MainColorChangedListener {
        public void onMainColorChanged(int color);
    }

}
