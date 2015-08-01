package com.koolew.mars;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.android.volley.Response;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;


public class TaskTopicActivity extends FragmentActivity
        implements TitleBarView.OnRightLayoutClickListener, Response.Listener<JSONObject> {

    public static final String KEY_TOPIC_ID = BaseVideoListFragment.KEY_TOPIC_ID;
    public static final String KEY_INVITER = TaskVideoListFragment.KEY_INVITER;

    public static final int RESULT_IGNORE = RESULT_FIRST_USER + 1;

    private String mTopicId;
    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_topic);

        mTopicId = getIntent().getStringExtra(KEY_TOPIC_ID);

        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);
        mProgressDialog = DialogUtil.getConnectingServerDialog(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new TaskVideoListFragment());
        fragmentTransaction.commit();
    }

    @Override
    public void onRightLayoutClick() {
        ApiWorker.getInstance().ignoreInvitation(mTopicId, this, null);
        mProgressDialog.show();
    }

    @Override
    public void onResponse(JSONObject response) {
        mProgressDialog.dismiss();
        try {
            if (response.getInt("code") == 0) {
                setResult(RESULT_IGNORE);
                onBackPressed();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
