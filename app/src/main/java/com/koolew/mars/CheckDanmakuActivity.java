package com.koolew.mars;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;


public class CheckDanmakuActivity extends FragmentActivity {

    public static final String KEY_VIDEO_ID = CheckDanmakuFragment.KEY_VIDEO_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_danmaku);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new CheckDanmakuFragment());
        fragmentTransaction.commit();
    }
}
