package com.koolew.mars;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.view.LoadMoreFooter;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class FriendTaskActivity extends BaseActivity
        implements SwipeRefreshLayout.OnRefreshListener, LoadMoreFooter.OnLoadListener {

    public static final String KEY_UID = "uid";
    public static final String KEY_NICKNAME = "nickname";

    private static final int REQUEST_CAPTURE = 1;
    private static final int REQUEST_CHECK_TASK = 2;

    private String mUid;
    private String mNickname;

    private SwipeRefreshLayout mRefreshLayout;
    private ListView mListView;
    private FriendTaskAdapter mAdapter;
    private LoadMoreFooter mListFooter;

    private Dialog mProgressDialog;
    private String mOperatingTopicId;

    private JsonObjectRequest mRefreshRequest;
    private JsonObjectRequest mLoadMoreRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_task);

        initViews();

        mProgressDialog = DialogUtil.getConnectingServerDialog(this);

        Intent intent = getIntent();
        mUid = intent.getStringExtra(KEY_UID);
        mNickname = intent.getStringExtra(KEY_NICKNAME);
        ((TitleBarView) findViewById(R.id.title_bar)).setTitle("@" + mNickname);

        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                doRefresh();
            }
        });
    }

    private void initViews() {
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setColorSchemeResources(R.color.koolew_light_green);
        mRefreshLayout.setOnRefreshListener(this);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(mItemClickListener);

        mListFooter = (LoadMoreFooter) LayoutInflater.from(this).inflate(R.layout.load_more_footer, null);
        mListFooter.setTextColor(getResources().getColor(R.color.koolew_light_gray));
        mListView.addFooterView(mListFooter, null, false);
        mListFooter.setup(mListView);
        mListFooter.setOnLoadListener(this);
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    private void doRefresh() {
        if (mLoadMoreRequest != null) {
            mLoadMoreRequest.cancel();
            mLoadMoreRequest = null;
        }
        mRefreshRequest = ApiWorker.getInstance().requestTaskDetail(mUid, mRefreshListener, null);
    }

    @Override
    public void onLoad() {
        if (mRefreshRequest != null) {
            mRefreshRequest.cancel();
            mRefreshRequest = null;
        }
        mLoadMoreRequest = ApiWorker.getInstance().requestTaskDetail(
                mUid, mAdapter.getLastTaskTime(), mLoadMoreListener, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAPTURE:
                onCaptureRequestResult(resultCode);
                break;
            case REQUEST_CHECK_TASK:
                onCheckTaskResult(resultCode);
                break;
        }
    }

    private void onCaptureRequestResult(int result) {
        if (result == RESULT_OK) {
            removeTopic(mOperatingTopicId);
        }
    }

    private void onCheckTaskResult(int result) {
        if (result == RESULT_OK || result == TaskTopicActivity.RESULT_IGNORE) {
            removeTopic(mOperatingTopicId);
        }
    }

    private Response.Listener<JSONObject> mRefreshListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            mRefreshLayout.setRefreshing(false);
            if (mLoadMoreRequest != null) {
                mLoadMoreRequest.cancel();
                mLoadMoreRequest = null;
            }

            try {
                if (jsonObject.getInt("code") == 0) {
                    JSONArray cards = jsonObject.getJSONObject("result").getJSONArray("cards");
                    if (mAdapter == null) {
                        mAdapter = new FriendTaskAdapter();
                        mListView.setAdapter(mAdapter);
                    }
                    mAdapter.setData(cards);
                    mAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.Listener<JSONObject> mLoadMoreListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            if (mRefreshRequest != null) {
                mRefreshRequest.cancel();
                mRefreshRequest = null;
            }

            try {
                if (jsonObject.getInt("code") == 0) {
                    JSONArray cards = jsonObject.getJSONObject("result").getJSONArray("cards");
                    if (cards.length() > 0) {
                        mAdapter.addData(cards);
                        mAdapter.notifyDataSetChanged();
                        mListFooter.loadComplete();
                    }
                    else {
                        mListFooter.haveNoMore();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private AbsListView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BaseTopicInfo topicInfo = (BaseTopicInfo) mAdapter.getItem(position);
            String topicId = topicInfo.getTopicId();
            mOperatingTopicId = topicId;
            Intent intent = new Intent(FriendTaskActivity.this, TaskTopicActivity.class);
            intent.putExtra(TaskTopicActivity.KEY_TOPIC_ID, topicId);
            intent.putExtra(TaskTopicActivity.KEY_INVITER, mNickname);
            startActivityForResult(intent, REQUEST_CHECK_TASK);
        }
    };

    class RemoveResponseListener implements Response.Listener<JSONObject> {
        private String topicId;

        public RemoveResponseListener(String topicId) {
            this.topicId = topicId;
        }

        @Override
        public void onResponse(JSONObject response) {
            mProgressDialog.dismiss();
            try {
                if (response.getInt("code") == 0) {
                    removeTopic(topicId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeTopic(String topicId) {
        int count = mAdapter.mData.size();
        for (int i = 0; i < count; i++) {
            if (mAdapter.mData.get(i).getTopicId().equals(topicId)) {
                mAdapter.mData.remove(i);
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private View.OnClickListener mRemoveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String topicId = (String) v.getTag();
            mProgressDialog.show();
            ApiWorker.getInstance().ignoreInvitation(
                    topicId, new RemoveResponseListener(topicId), null);
        }
    };

    private View.OnClickListener mAcceptListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String topicId = (String) v.getTag();
            mOperatingTopicId = topicId;
            Intent intent = new Intent(FriendTaskActivity.this, VideoShootActivity.class);
            intent.putExtra(VideoShootActivity.KEY_TOPIC_ID, topicId);
            startActivityForResult(intent, REQUEST_CAPTURE);
        }
    };


    class FriendTaskAdapter extends BaseAdapter {

        private List<BaseTopicInfo> mData;

        public FriendTaskAdapter() {
            mData = new ArrayList<BaseTopicInfo>();
        }

        public void setData(JSONArray jsonArray) {
            mData.clear();
            addData(jsonArray);
        }

        public void addData(JSONArray jsonArray) {
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                try {
                    mData.add(new BaseTopicInfo(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public long getLastTaskTime() {
            if (mData.size() > 0) {
                return mData.get(mData.size() - 1).getUpdateTime();
            }

            return 0;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(FriendTaskActivity.this)
                        .inflate(R.layout.invitation_card_item, null);
                ViewHolder holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.topic_title);
                holder.videoCount = (TextView) convertView.findViewById(R.id.video_count);
                holder.removeBtn = (ImageView) convertView.findViewById(R.id.btn_remove);
                holder.removeBtn.setOnClickListener(mRemoveListener);
                holder.acceptBtn = (ImageView) convertView.findViewById(R.id.btn_accept);
                holder.acceptBtn.setOnClickListener(mAcceptListener);
                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            BaseTopicInfo info = (BaseTopicInfo) getItem(position);
            holder.title.setText(info.getTitle());
            holder.videoCount.setText(getString(R.string.video_count_label, info.getVideoCount()));
            holder.removeBtn.setTag(info.getTopicId());
            holder.acceptBtn.setTag(info.getTopicId());

            return convertView;
        }
    }

    class ViewHolder {
        TextView title;
        TextView videoCount;
        ImageView removeBtn;
        ImageView acceptBtn;
    }
}
