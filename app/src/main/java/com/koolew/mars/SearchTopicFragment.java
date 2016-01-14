package com.koolew.mars;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 1/14/16.
 */
public class SearchTopicFragment extends RecyclerListFragmentMould<SearchTopicFragment.TopicAdapter> {

    private static final String KEY_KEYWORD = "keyword";

    private String keyword;
    private int page;

    public SearchTopicFragment() {
        isNeedLoadMore = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keyword = getArguments().getString(KEY_KEYWORD);
        TitleBarView titleBar = ((TitleFragmentActivity) getActivity()).getTitleBar();
        titleBar.setBackgroundColor(getThemeColor());
        titleBar.setTitle(R.string.search_topic);
    }

    @Override
    public void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        mRecyclerView.setBackgroundColor(Color.BLACK);
    }

    @Override
    protected TopicAdapter useThisAdapter() {
        return new TopicAdapter();
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_black);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        page = 0;
        return super.doRefreshRequest();
    }

    @Override
    protected String getRefreshRequestUrl() {
        return UrlHelper.getSearchTopicUrl(keyword, page);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        page++;
        return super.doLoadMoreRequest();
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getSearchTopicUrl(keyword, page);
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        try {
            JSONArray topics = result.getJSONArray("topics");
            int addedCount = mAdapter.set(topics);
            mAdapter.notifyDataSetChanged();
            return addedCount > 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        try {
            JSONArray topics = result.getJSONArray("topics");
            int originalCount = mAdapter.topics.size();
            int addedCount = mAdapter.add(topics);
            mAdapter.notifyItemRangeInserted(originalCount, addedCount);
            return addedCount > 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    class TopicAdapter extends LoadMoreAdapter {

        private List<BaseTopicInfo> topics = new ArrayList<>();

        public int set(JSONArray topicJson) {
            topics.clear();
            return add(topicJson);
        }

        public int add(JSONArray topicJson) {
            int addedCount = 0;
            for (int i = 0; i < topicJson.length(); i++) {
                try {
                    topics.add(BaseTopicInfo.dynamicTopicInfo(topicJson.getJSONObject(i)));
                    addedCount++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return addedCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new TopicHolder(LayoutInflater.from(getContext())
                    .inflate(R.layout.global_search_topic, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TopicHolder) holder).bindTopicInfo(topics.get(position));
        }

        @Override
        public int getCustomItemCount() {
            return topics.size();
        }
    }

    class TopicHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView thumb;
        private TextView title;
        private ImageView captureBtn;

        private BaseTopicInfo topicInfo;

        public TopicHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            thumb = (ImageView) itemView.findViewById(R.id.thumb);
            title = (TextView) itemView.findViewById(R.id.title);
            captureBtn = (ImageView) itemView.findViewById(R.id.capture_btn);
            captureBtn.setOnClickListener(this);
        }

        public void bindTopicInfo(BaseTopicInfo topicInfo) {
            this.topicInfo = topicInfo;
            ImageLoader.getInstance().displayImage(topicInfo.getThumb(), thumb);
            title.setText(topicInfo.getTitle());
            if (topicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_VIDEO)) {
                captureBtn.setImageResource(R.mipmap.ic_btn_capture_video);
            }
            else {
                captureBtn.setImageResource(R.mipmap.ic_btn_capture_movie);
            }
        }

        @Override
        public void onClick(View v) {
            if (v == itemView) {
                TopicMediaActivity.startThisActivity(getContext(),
                        topicInfo.getTopicId(), TopicMediaActivity.TYPE_WORLD);
            }
            else if (v == captureBtn) {
                topicInfo.gotoCapture(getContext());
            }
        }
    }

    public static void startThisFragment(Context context, String keyword) {
        Bundle args = new Bundle();
        args.putString(KEY_KEYWORD, keyword);
        TitleFragmentActivity.launchFragment(context, SearchTopicFragment.class, args);
    }
}
