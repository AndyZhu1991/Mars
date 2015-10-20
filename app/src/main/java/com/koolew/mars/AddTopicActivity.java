package com.koolew.mars;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.statistics.StatisticsEvent;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class AddTopicActivity extends BaseActivity implements TitleBarView.OnRightLayoutClickListener,
        SwipeRefreshLayout.OnRefreshListener, TextWatcher {

    private TitleBarView mTitleBar;
    private EditText mTitleEdit;
    private SwipeRefreshLayout mRefreshLayout;
    private View mRecommendationFrame;
    private RecyclerView mRecommendationRecyler;
    private View mAssociationFrame;
    private RecyclerView mAssociationRecycler;

    private TopicItemAdapter mRecommendationAdapter;
    private TopicItemAdapter mAssociationAdapter;

    private JsonObjectRequest mRecommendationRequest;
    private JsonObjectRequest mAssociationRequest;

    private ProgressDialog mConnectingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_topic);

        initMembers();

        initViews();

        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                refreshRecommendation();
            }
        });
    }

    private void initMembers() {
        mConnectingDialog = DialogUtil.getConnectingServerDialog(this);
    }

    private void initViews() {
        mTitleBar = (TitleBarView) findViewById(R.id.title_bar);
        mTitleBar.setOnRightLayoutClickListener(this);

        mTitleEdit = (EditText) findViewById(R.id.edit_text);
        mTitleEdit.addTextChangedListener(this);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setColorSchemeColors(0xFFF4D288);
        mRefreshLayout.setOnRefreshListener(this);

        mRecommendationFrame = findViewById(R.id.recommendation_frame);

        mRecommendationRecyler = (RecyclerView) findViewById(R.id.recommendation_recycler);
        mRecommendationRecyler.setLayoutManager(new LinearLayoutManager(this));
        mRecommendationAdapter = new RecommendationAdapter();
        mRecommendationRecyler.setAdapter(mRecommendationAdapter);

        mAssociationRecycler = (RecyclerView) findViewById(R.id.association_recycler);
        mAssociationFrame = mAssociationRecycler;
        mAssociationRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAssociationAdapter = new AssociationAdapter();
        mAssociationRecycler.setAdapter(mAssociationAdapter);
    }

    private void refreshRecommendation() {
        mRecommendationRequest = ApiWorker.getInstance()
                .requestWorldHotTopic(mRecommendationListener, null);
    }

    private void refreshAssociation(String keyWord) {
        if (mAssociationRequest != null) {
            mAssociationRequest.cancel();
            mAssociationRequest = null;
        }
        mAssociationRequest = ApiWorker.getInstance()
                .searchTopic(keyWord, mAssociationListener, null);
    }

    public void onClearEditText(View v) {
        mTitleEdit.setText("");
    }

    @Override
    public void onRightLayoutClick() {
        if (mTitleEdit.getText().length() == 0) {
            Toast.makeText(this, R.string.no_title_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        mConnectingDialog.show();
        ApiWorker.getInstance().addTopic(mTitleEdit.getText().toString(), mAddTopicListener, null);
    }

    @Override
    public void onRefresh() {
        refreshRecommendation();
    }

    private Response.Listener<JSONObject> mRecommendationListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mRefreshLayout.setRefreshing(false);
            try {
                if (response.getInt("code") == 0) {
                    mRecommendationAdapter.setData(response.getJSONObject("result").getJSONArray("topics"));
                    mRecommendationAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.Listener<JSONObject> mAssociationListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mAssociationRequest = null;
            try {
                if (response.getInt("code") == 0) {
                    mAssociationAdapter.setData(response.getJSONObject("result").getJSONArray("topics"));
                    mAssociationAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.Listener<JSONObject> mAddTopicListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mConnectingDialog.dismiss();
            try {
                if (response.getInt("code") == 0) {
                    MobclickAgent.onEvent(AddTopicActivity.this, StatisticsEvent.EVENT_ADD_TOPIC);

                    String tid = response.getJSONObject("result").getString("uid");
                    Intent intent = new Intent(AddTopicActivity.this, FeedsTopicActivity.class);
                    intent.putExtra(FeedsTopicActivity.KEY_TOPIC_TITLE, mTitleEdit.getText().toString());
                    intent.putExtra(FeedsTopicActivity.KEY_TOPIC_ID, tid);
                    startActivity(intent);

                    finish();
                }
                else {
                    Toast.makeText(AddTopicActivity.this,
                            R.string.connect_server_failed, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (s.length() == 0) {
            mRecommendationFrame.setVisibility(View.INVISIBLE);
            mAssociationFrame.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        refreshAssociation(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 0) {
            mRecommendationFrame.setVisibility(View.VISIBLE);
            mAssociationFrame.setVisibility(View.INVISIBLE);
        }
    }


    abstract class TopicItemAdapter extends RecyclerView.Adapter<TopicItemAdapter.ViewHolder> {

        private List<BaseTopicInfo> mData;

        public TopicItemAdapter() {
            mData = new ArrayList<>();
        }

        public void setData(JSONArray jsonArray) {
            mData.clear();
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                try {
                    mData.add(new BaseTopicInfo(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BaseTopicInfo topicItem = mData.get(position);
            if (holder.mOrderText != null) {
                holder.mOrderText.setText(String.format("%02d", position + 1));
            }
            holder.mTitleText.setText(topicItem.getTitle());
            holder.mVideoCountText.setText(getString(
                    R.string.little_video_count, topicItem.getVideoCount()));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView mOrderText;
            private TextView mTitleText;
            private TextView mVideoCountText;

            public ViewHolder(View itemView) {
                super(itemView);

                mOrderText = (TextView) itemView.findViewById(R.id.order);
                mTitleText = (TextView) itemView.findViewById(R.id.title);
                mVideoCountText = (TextView) itemView.findViewById(R.id.video_count);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int position = getLayoutPosition();
                BaseTopicInfo topicInfo = mData.get(position);
                Intent intent = new Intent(AddTopicActivity.this, FeedsTopicActivity.class);
                intent.putExtra(FeedsTopicActivity.KEY_TOPIC_ID, topicInfo.getTopicId());
                intent.putExtra(FeedsTopicActivity.KEY_TOPIC_TITLE, topicInfo.getTitle());
                intent.putExtra(FeedsTopicActivity.KEY_DEFAULT_SHOW_POSITION,
                        FeedsTopicActivity.POSITION_WORLD);
                startActivity(intent);
                finish();
            }
        }
    }

    class RecommendationAdapter extends TopicItemAdapter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.add_topic_recommendation_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(itemView);
            return viewHolder;
        }
    }

    class AssociationAdapter extends TopicItemAdapter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.add_topic_association_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(itemView);
            return viewHolder;
        }
    }
}
