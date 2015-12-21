package com.koolew.mars;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.utils.UriProcessor;
import com.koolew.mars.view.TitleBarView;


public class PushWrapperActivity extends BaseV4FragmentActivity {

    public static final String KEY_TAB_TYPE = "tab type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_wrapper);

        String tabType = getIntent().getStringExtra(KEY_TAB_TYPE);
        Fragment fragment;
        int themeColor;
        String title;
        if (tabType.equals(UriProcessor.TAB_FEEDS)) {
            fragment = new KoolewFeedsFragment();
            themeColor = getResources().getColor(R.color.koolew_light_orange);
            title = getString(R.string.koolew_news_title);
        }
        else if (tabType.equals(UriProcessor.TAB_SUGGESTION)) {
            fragment = new FriendMeetFragment();
            themeColor = getResources().getColor(R.color.koolew_light_blue);
            title = getString(R.string.friend_meet_title);
        }
        else if (tabType.equals(UriProcessor.TAB_SQUARE)) {
            fragment = new KoolewSquareFragment();
            themeColor = getResources().getColor(R.color.koolew_black);
            title = getString(R.string.koolew_square_title);
        }
        else {
            fragment = null;
            themeColor = 0xFF000000;
            title = null;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

        TitleBarView titleBar = ((TitleBarView) findViewById(R.id.title_bar));
        titleBar.setBackgroundColor(themeColor);
        titleBar.setTitle(title);
    }
}
