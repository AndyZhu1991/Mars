package com.koolew.mars;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.koolew.mars.view.TitleBarView;


public class UserTopicActivity extends FragmentActivity {

    public static final String KEY_TOPIC_ID = BaseVideoListFragment.KEY_TOPIC_ID;
    public static final String KEY_UID = UserVideoListFragment.KEY_UID;
    public static final String KEY_NICKNAME = "nickname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_topic);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new UserVideoListFragment());
        fragmentTransaction.commit();

        String nickname = getIntent().getStringExtra(KEY_NICKNAME);
        ((TitleBarView) findViewById(R.id.title_bar)).setTitle("@" + nickname);
    }
}
