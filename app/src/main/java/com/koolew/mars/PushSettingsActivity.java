package com.koolew.mars;

import android.os.Bundle;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.android.preference.PreferenceAdapter;
import com.koolew.android.preference.PreferenceGroupTitle;
import com.koolew.mars.preference.PreferenceHelper;
import com.koolew.android.preference.SwitchPreference;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONObject;


public class PushSettingsActivity extends BaseActivity implements Response.ErrorListener,
        Response.Listener<JSONObject> {

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

    @Override
    protected void onPause() {
        super.onPause();
        ApiWorker.getInstance().postPushBit(new PreferenceHelper(this).getPushBit(),
                this, this);
    }

    private void setupAdapter() {
        mAdapter = new PreferenceAdapter(this);

        mAdapter.add(new PreferenceGroupTitle(this, R.string.permission));
        mAdapter.add(new SwitchPreference(this, R.string.new_fan,
                PreferenceHelper.KEY_NEW_FRIEND_APPLY, PreferenceHelper.DEFAULT_NEW_FRIEND_APPLY));
        mAdapter.add(new SwitchPreference(this, R.string.new_video_by_following,
                PreferenceHelper.KEY_NEW_VIDEO_BY_FRIEND, PreferenceHelper.DEFAULT_NEW_VIDEO_BY_FRIEND));
        mAdapter.add(new SwitchPreference(this, R.string.new_danmaku,
                PreferenceHelper.KEY_DANMAKUED_BY_FRIEND, PreferenceHelper.DEFAULT_DANMAKUED_BY_FRIEND));
        mAdapter.add(new SwitchPreference(this, R.string.new_task,
                PreferenceHelper.KEY_INVITED, PreferenceHelper.DEFAULT_INVITED));
        mAdapter.add(new SwitchPreference(this, R.string.invitation_accepted,
                PreferenceHelper.KEY_INVITATION_ACCEPTED, PreferenceHelper.DEFAULT_INVITATION_ACCEPTED));
    }

    @Override
    public void onResponse(JSONObject response) {
        // TODO
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // TODO
    }
}
