package com.koolew.mars;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.koolew.mars.view.TitleBarView;


public class WorldTopicActivity extends FragmentActivity
        implements TitleBarView.OnRightLayoutClickListener {

    public static final String KEY_TOPIC_ID = BaseVideoListFragment.KEY_TOPIC_ID;
    public static final String KEY_TOPIC_TITLE = BaseVideoListFragment.KEY_TOPIC_TITLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_topic);

        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new WorldVideoListFragment());
        fragmentTransaction.commit();
    }

    @Override
    public void onRightLayoutClick() {
    }
}
