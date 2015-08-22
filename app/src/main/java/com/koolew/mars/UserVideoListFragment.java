package com.koolew.mars;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/28/15.
 */
public class UserVideoListFragment extends BaseVideoListFragment {

    public static final String KEY_UID = "uid";

    protected String mUid;
    protected int mUserType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUid = getActivity().getIntent().getStringExtra(KEY_UID);
    }

    @Override
    protected VideoCardAdapter useThisAdapter() {
        return new UserTopicAdapter(getActivity());
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestUserTopic(mUid, mTopicId, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().requestUserTopic(mUid, mTopicId,
                mAdapter.getOldestVideoTime(), mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        try {
            mUserType = response.getJSONObject("result").getInt("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.handleRefresh(response);
    }

    @Override
    public void onDanmakuSend(BaseVideoInfo videoInfo) {
        if (needShowAddFriend(mUserType)) {
        }
        else {
            super.onDanmakuSend(videoInfo);
        }
    }

    private boolean needShowAddFriend(int type) {
        if (type != FriendSimpleAdapter.TYPE_SELF && type != FriendSimpleAdapter.TYPE_FRIEND) {
            return true;
        }
        else {
            return false;
        }
    }

    class UserTopicAdapter extends VideoCardAdapter {

        public UserTopicAdapter(Context context) {
            super(context);
        }

        @Override
        protected View getVideoItemView(int position, View convertView, ViewGroup parent) {
            View root = super.getVideoItemView(position, convertView, parent);

            if (needShowAddFriend(mUserType)) {
                root.findViewById(R.id.danmaku_send_text).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.danmaku_send_ic).setVisibility(View.INVISIBLE);
            }

            return root;
        }
    }
}
