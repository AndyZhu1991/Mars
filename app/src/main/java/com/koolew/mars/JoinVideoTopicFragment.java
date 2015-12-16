package com.koolew.mars;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.adapters.TagAdapter;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.Tag;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 12/16/15.
 */
public class JoinVideoTopicFragment
        extends RecyclerListFragmentMould<JoinVideoTopicFragment.JoinVideoTopicAdapter>
        implements TagAdapter.OnSelectedTagChangedListener, TitleBarView.OnRightLayoutClickListener {

    private static final int REQUEST_CODE_CREATE_TOPIC = 1;

    private Tag selectedTag = null;

    private RecyclerView mTagRecyclerView;

    public JoinVideoTopicFragment() {
        mLayoutResId = R.layout.fragment_join_video_topic;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TitleBarView titleBarView = ((TitleFragmentActivity) getActivity()).getTitleBar();
        titleBarView.setBackgroundColor(getResources().getColor(R.color.koolew_light_orange));
        titleBarView.setTitle(R.string.join_video_topic_title);
        titleBarView.setRightImage(R.mipmap.ic_add_new_topic);
        titleBarView.setRightLayoutVisibility(View.VISIBLE);
        titleBarView.setOnRightLayoutClickListener(this);
    }

    @Override
    public void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);

        mTagRecyclerView = (RecyclerView) findViewById(R.id.tag_recycler);
        mTagRecyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.HORIZONTAL, false));
        TagAdapter tagAdapter = new TagAdapter(getActivity());
        tagAdapter.initTags(TagAdapter.TAGS_VIDEO, true);
        tagAdapter.setTextColorSelected(0xFFD3AB64);
        tagAdapter.setTagChangedListener(this);
        mTagRecyclerView.setAdapter(tagAdapter);
    }

    @Override
    protected JoinVideoTopicAdapter useThisAdapter() {
        return new JoinVideoTopicAdapter();
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_orange);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        if (selectedTag == null) {
            return ApiWorker.getInstance().standardGetRequest(
                    UrlHelper.getRecommendTopicUrl(BaseTopicInfo.CATEGORY_VIDEO),
                    mRefreshListener, null);
        }
        else {
            return ApiWorker.getInstance().standardGetRequest(
                    UrlHelper.getTopicUrlByCategoryAndTag(
                            BaseTopicInfo.CATEGORY_VIDEO, selectedTag.getId()),
                    mLoadMoreListener, null);
        }
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return null;
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        try {
            int code = response.getInt("code");
            if (code == 0) {
                JSONArray topics = response.getJSONObject("result").getJSONArray("topics");
                mAdapter.topicInfos.clear();
                int length = topics.length();
                for (int i = 0; i < length; i++) {
                    mAdapter.topicInfos.add(new BaseTopicInfo(topics.getJSONObject(i)));
                }
                mAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        return false;
    }

    @Override
    public void onSelectedTagChanged(Tag tag) {
        selectedTag = tag;
        mRefreshLayout.setRefreshing(true);
        onRefresh();
    }

    public static void startThisFragment(Context context) {
        TitleFragmentActivity.launchFragment(context, JoinVideoTopicFragment.class);
    }

    @Override
    public void onRightLayoutClick() {
        startActivityForResult(new Intent(getActivity(), CreateTopicActivity.class),
                REQUEST_CODE_CREATE_TOPIC);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CREATE_TOPIC:
                if (resultCode == Activity.RESULT_OK) {
                    getActivity().finish();
                }
                break;
        }
    }


    class JoinVideoTopicAdapter extends LoadMoreAdapter {
        private List<BaseTopicInfo> topicInfos = new ArrayList<>();

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new JoinVideoTopicHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.join_video_topic_item, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position) {
            JoinVideoTopicHolder jh = (JoinVideoTopicHolder) holder;
            BaseTopicInfo topicInfo = topicInfos.get(position);
            ImageLoader.getInstance().displayImage(topicInfo.getThumb(), jh.thumb,
                    ImageLoaderHelper.topicThumbLoadOptions);
            jh.title.setText(topicInfo.getTitle());
        }

        @Override
        public int getCustomItemCount() {
            return topicInfos.size();
        }

        class JoinVideoTopicHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private ImageView thumb;
            private TextView title;
            private View captureBtn;

            public JoinVideoTopicHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                thumb = (ImageView) itemView.findViewById(R.id.thumb);
                title = (TextView) itemView.findViewById(R.id.title);
                captureBtn = itemView.findViewById(R.id.capture_btn);
                captureBtn.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                BaseTopicInfo topicInfo = topicInfos.get(getAdapterPosition());
                if (v == itemView) {
                    TopicMediaActivity.startThisActivity(getActivity(), topicInfo.getTopicId(),
                            TopicMediaActivity.TYPE_WORLD);
                }
                else if (v == captureBtn) {
                    VideoShootActivity.startThisActivity(getActivity(), topicInfo.getTopicId(),
                            topicInfo.getTitle());
                }
            }
        }
    }
}
