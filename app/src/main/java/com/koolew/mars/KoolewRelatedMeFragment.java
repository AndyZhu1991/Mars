package com.koolew.mars;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class KoolewRelatedMeFragment extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment KoolewRelatedMeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KoolewRelatedMeFragment newInstance() {
        KoolewRelatedMeFragment fragment = new KoolewRelatedMeFragment();
        return fragment;
    }

    public KoolewRelatedMeFragment() {
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
        return inflater.inflate(R.layout.fragment_koolew_related_me, container, false);
    }

}
