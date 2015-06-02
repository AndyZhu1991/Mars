package com.koolew.mars;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        return root;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    class KoolewFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList;

        public KoolewFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            // TODO Auto-generated constructor stub
            fragmentList = new ArrayList<Fragment>();
            fragmentList.add(KoolewNewsFragment.newInstance());
            fragmentList.add(KoolewRelatedMeFragment.newInstance());
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

}
