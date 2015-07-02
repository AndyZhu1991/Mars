package com.koolew.mars;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.koolew.mars.preference.PreferenceAdapter;
import com.koolew.mars.preference.PreferenceGroupTitle;
import com.koolew.mars.preference.PreferenceHelper;
import com.koolew.mars.preference.SwitchPreference;


public class PushSettingsActivity extends Activity {

    private ListView mListView;
    private PreferenceAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_settings);

        mListView = (ListView) findViewById(R.id.list_view);
        setupAdapter();
        mListView.setAdapter(mAdapter);
    }

    private void setupAdapter() {
        mAdapter = new PreferenceAdapter(this);

        mAdapter.add(new PreferenceGroupTitle(this, R.string.permission));
        mAdapter.add(new SwitchPreference(this, R.string.new_friend_apply,
                PreferenceHelper.KEY_NEW_FRIEND_APPLY, PreferenceHelper.DEFAULT_NEW_FRIEND_APPLY));
        mAdapter.add(new SwitchPreference(this, R.string.new_video_by_friend,
                PreferenceHelper.KEY_NEW_VIDEO_BY_FRIEND, PreferenceHelper.DEFAULT_NEW_VIDEO_BY_FRIEND));
        mAdapter.add(new SwitchPreference(this, R.string.danmakued_by_friend,
                PreferenceHelper.KEY_DANMAKUED_BY_FRIEND, PreferenceHelper.DEFAULT_DANMAKUED_BY_FRIEND));
        mAdapter.add(new SwitchPreference(this, R.string.i_got_koo,
                PreferenceHelper.KEY_I_GOT_KOO, PreferenceHelper.DEFAULT_I_GOT_KOO));
    }
}
