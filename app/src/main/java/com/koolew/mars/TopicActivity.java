package com.koolew.mars;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.player.ScrollPlayer;
import com.koolew.mars.view.LoadMoreFooter;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONObject;


public class TopicActivity extends Activity implements LoadMoreFooter.OnLoadListener,
        SwipeRefreshLayout.OnRefreshListener, VideoCardAdapter.OnDanmakuSendListener,
        View.OnClickListener {

    private static final String TAG = "koolew-TopicActivity";

    public static final String KEY_TOPIC_ID = "topic_id";

    private String mTopicId;

    private ListView mListView;
    private ScrollPlayer mScrollPlayer;
    private SwipeRefreshLayout mRefreshLayout;
    private LoadMoreFooter mListFooter;
    private View mCaptureView;
    private View mSendInvitationView;

    private VideoCardAdapter mAdapter;

    private JsonObjectRequest mRefreshRequest;
    private JsonObjectRequest mLoadMoreRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        Intent intent = getIntent();
        mTopicId = intent.getStringExtra(KEY_TOPIC_ID);

        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new VideoCardAdapter(TopicActivity.this);
        mAdapter.setOnDanmakuSendListener(TopicActivity.this);
        mScrollPlayer = new VideoCardAdapter.TopicScrollPlayer(mAdapter, mListView);
        mListFooter = (LoadMoreFooter) getLayoutInflater().inflate(R.layout.load_more_footer, null);
        mListView.addFooterView(mListFooter);
        mListFooter.setup(mListView, mScrollPlayer);
        mListFooter.setOnLoadListener(this);
        mListView.setAdapter(mAdapter);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mCaptureView = findViewById(R.id.capture);
        mCaptureView.setOnClickListener(this);
        mSendInvitationView = findViewById(R.id.send_invitation);
        mSendInvitationView.setOnClickListener(this);

        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScrollPlayer.onActivityResume();
    }

    @Override
    protected void onPause() {
        mScrollPlayer.onActivityPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScrollPlayer.onActivityDestroy();
    }

    private void doRefresh() {
        if (mLoadMoreRequest != null) {
            mLoadMoreRequest.cancel();
        }
        mRefreshRequest = ApiWorker.getInstance().
                requestTopicVideo(mTopicId, mRefreshListener, null);
    }

    private void doLoadMore() {
        if (mRefreshRequest != null) {
            mRefreshRequest.cancel();
        }
        mLoadMoreRequest = ApiWorker.getInstance().requestTopicVideo(
                mTopicId, mAdapter.getOldestVideoTime(), mLoadMoreListener, null);
    }

    private Response.Listener<JSONObject> mRefreshListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mRefreshRequest = null;
            mAdapter.setData(response);
            mAdapter.notifyDataSetChanged();

            mRefreshLayout.setRefreshing(false);
            mListFooter.haveMore(true);

            mScrollPlayer.onListRefresh();
        }
    };

    private Response.Listener<JSONObject> mLoadMoreListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mLoadMoreRequest = null;
            int loadedCount = mAdapter.addData(response);
            mAdapter.notifyDataSetChanged();

            mListFooter.loadComplete();
            if (loadedCount == 0) {
                mListFooter.haveNoMore();
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.capture:
                onCaptureClick();
                break;
            case R.id.send_invitation:
                break;
        }
    }

    private void onCaptureClick() {
        Intent intent = new Intent(this, VideoShootActivity.class);
        intent.putExtra(VideoShootActivity.KEY_TOPIC_ID, mTopicId);
        startActivity(intent);
    }

    // Here to load more
    @Override
    public void onLoad() {
        doLoadMore();
    }

    // Here to refresh
    @Override
    public void onRefresh() {
        doRefresh();
    }

    public void onDanmakuSend(JSONObject videoItem) {
        Intent intent = new Intent(this, SendDanmakuActivity.class);
        intent.putExtra(SendDanmakuActivity.KEY_VIDEO_JSON, videoItem.toString());
        startActivity(intent);
    }
}
