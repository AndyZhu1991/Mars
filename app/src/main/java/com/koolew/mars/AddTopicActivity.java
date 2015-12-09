package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.MaxLengthWatcher;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class AddTopicActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener {

    private static final int REQUEST_CODE_CREATE_TOPIC = 1;

    private EditText mTitleEdit;
    private SwipeRefreshLayout mRefreshLayout;
    private View mRecommendationFrame;
    private RecyclerView mRecommendationRecyler;
    private View mAssociationFrame;
    private RecyclerView mAssociationRecycler;
    private View mAddTopicText;

    private TopicItemAdapter mRecommendationAdapter;
    private TopicItemAdapter mAssociationAdapter;

    private JsonObjectRequest mRecommendationRequest;
    private JsonObjectRequest mAssociationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_topic);

        initViews();

        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                refreshRecommendation();
            }
        });
    }

    private void initViews() {
        mTitleEdit = (EditText) findViewById(R.id.edit_text);
        mTitleEdit.addTextChangedListener(
                new TextWatcher(AppProperty.TOPIC_TITLE_MAX_WORDS, mTitleEdit));

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setColorSchemeColors(0xFFF4D288);
        mRefreshLayout.setOnRefreshListener(this);

        mRecommendationFrame = findViewById(R.id.recommendation_frame);

        mRecommendationRecyler = (RecyclerView) findViewById(R.id.recommendation_recycler);
        mRecommendationRecyler.setLayoutManager(new LinearLayoutManager(this));
        mRecommendationAdapter = new RecommendationAdapter();
        mRecommendationRecyler.setAdapter(mRecommendationAdapter);

        mAssociationFrame = findViewById(R.id.association_frame);

        mAssociationRecycler = (RecyclerView) findViewById(R.id.association_recycler);
        mAssociationRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAssociationAdapter = new AssociationAdapter();
        mAssociationRecycler.setAdapter(mAssociationAdapter);

        mAddTopicText = findViewById(R.id.add_topic_text);
        mAddTopicText.setOnClickListener(this);
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

    public void onNewTopicClick() {
        Intent intent = new Intent(this, CreateTopicActivity.class);
        intent.putExtra(CreateTopicActivity.KEY_TOPIC_TITLE, mTitleEdit.getText().toString());
        startActivityForResult(intent, REQUEST_CODE_CREATE_TOPIC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CREATE_TOPIC:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
        }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_topic_text:
                onNewTopicClick();
                break;
        }
    }

    class TextWatcher extends MaxLengthWatcher {
        public TextWatcher(int maxLen, EditText editText) {
            super(maxLen, editText);
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                mRecommendationFrame.setVisibility(View.VISIBLE);
                mAssociationFrame.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (s.length() == 0) {
                mRecommendationFrame.setVisibility(View.INVISIBLE);
                mAssociationFrame.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            super.onTextChanged(s, start, before, count);
            refreshAssociation(s.toString());
        }

        @Override
        public void onTextOverInput() {
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
            if (topicItem.getCategory().equals("movie")) {
                holder.mMovieCategoryLable.setVisibility(View.VISIBLE);
            }
            else {
                holder.mMovieCategoryLable.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView mOrderText;
            private TextView mTitleText;
            private TextView mVideoCountText;
            private TextView mMovieCategoryLable;

            public ViewHolder(View itemView) {
                super(itemView);

                mOrderText = (TextView) itemView.findViewById(R.id.order);
                mTitleText = (TextView) itemView.findViewById(R.id.title);
                mVideoCountText = (TextView) itemView.findViewById(R.id.video_count);
                mMovieCategoryLable = (TextView) itemView.findViewById(R.id.category_lable);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                BaseTopicInfo topicInfo = mData.get(getAdapterPosition());
                TopicMediaActivity.startThisActivity(AddTopicActivity.this, topicInfo.getTopicId(),
                        TopicMediaActivity.TYPE_WORLD);
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
