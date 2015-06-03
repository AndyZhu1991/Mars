package com.koolew.mars;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class KoolewMeetFragment extends Fragment {

    public static KoolewMeetFragment newInstance() {
        KoolewMeetFragment fragment = new KoolewMeetFragment();
        return fragment;
    }

    public KoolewMeetFragment() {
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
        return inflater.inflate(R.layout.fragment_koolew_meet, container, false);
    }

}
