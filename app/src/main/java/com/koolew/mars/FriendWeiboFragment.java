package com.koolew.mars;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koolew.mars.statistics.BaseV4Fragment;


public class FriendWeiboFragment extends BaseV4Fragment {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment FriendWeiboFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendWeiboFragment newInstance() {
        FriendWeiboFragment fragment = new FriendWeiboFragment();
        return fragment;
    }

    public FriendWeiboFragment() {
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
        return inflater.inflate(R.layout.fragment_friend_weibo, container, false);
    }
}
