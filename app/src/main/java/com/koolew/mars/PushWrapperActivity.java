package com.koolew.mars;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.koolew.mars.view.TitleBarView;


public class PushWrapperActivity extends FragmentActivity {

    public static final String KEY_TAB_TYPE = "tab type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_wrapper);

        String tabType = getIntent().getStringExtra(KEY_TAB_TYPE);
        Fragment fragment;
        String title;
        if (tabType.equals("feeds")) {
            fragment = KoolewNewsFragment.newInstance();
            title = getString(R.string.koolew_news_title);
        }
        else if (tabType.equals("suggestion")) {
            fragment = FriendMeetFragment.newInstance();
            title = getString(R.string.friend_meet_title);
        }
        else {
            fragment = null;
            title = null;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

        ((TitleBarView) findViewById(R.id.title_bar)).setTitle(title);
    }
}
