package com.koolew.mars;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseCommentInfo;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.topicmedia.BaseTopicMediaFragment;
import com.koolew.mars.topicmedia.BasicTitleItem;
import com.koolew.mars.topicmedia.CommentItem;
import com.koolew.mars.topicmedia.CommentTitleItem;
import com.koolew.mars.topicmedia.MediaItem;
import com.koolew.mars.topicmedia.MovieItem;
import com.koolew.mars.topicmedia.UniversalMediaAdapter;
import com.koolew.mars.topicmedia.VideoItem;
import com.koolew.mars.topicmedia.VideoKooBriefItem;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by jinchangzhu on 12/8/15.
 */
public class SingleMediaFragment extends BaseTopicMediaFragment<SingleMediaFragment.SingleMediaAdapter> {

    public static final String KEY_VIDEO_ID = BaseVideoInfo.KEY_VIDEO_ID;

    private String mVideoId;

    public SingleMediaFragment() {
        isNeedLoadMore = true;
        isLazyLoad = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVideoId = getActivity().getIntent().getStringExtra(KEY_VIDEO_ID);

        TitleFragmentActivity activity =(TitleFragmentActivity) getActivity();
        if (activity != null) { // 奇葩的锤子手机，这个地方也会NPE
            activity.getTitleBar().setBackgroundColor(getThemeColor());
        }
    }

    @Override
    public void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        mRecyclerView.setPadding(0, 0, 0, 0);
    }

    @Override
    protected SingleMediaAdapter useThisAdapter() {
        return new SingleMediaAdapter(getActivity());
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_blue);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestSingleVideo(mVideoId, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().getVideoComment(mVideoId, mAdapter.getLastUpdateTime(),
                mLoadMoreListener, null);
    }

    public static void startThisFragment(Context context, String videoId) {
        Bundle extras = new Bundle();
        extras.putString(KEY_VIDEO_ID, videoId);
        TitleFragmentActivity.launchFragment(context, SingleMediaFragment.class, extras);
    }


    public static class SingleMediaAdapter extends UniversalMediaAdapter implements View.OnClickListener {

        private VideoInfo mVideoInfo;

        public SingleMediaAdapter(Context context) {
            super(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder = super.onCreateCustomViewHolder(parent, viewType);
            if (holder instanceof BasicTitleItem.ItemViewHolder) {
                holder.itemView.setOnClickListener(this);
            }
            else if (holder instanceof VideoItem.ItemViewHolder) {
                ((VideoItem.ItemViewHolder) holder).disableKooAndComment();
            }
            return holder;
        }

        @Override
        protected JSONObject findTopicJson(JSONObject result) {
            try {
                return result.getJSONObject("video").getJSONObject("topic");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new JSONObject();
        }

        @Override
        protected JSONArray getRefreshArray(JSONObject result) {
            JSONArray refreshArray = new JSONArray();
            try {
                refreshArray.put(result.getJSONObject("video"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return refreshArray;
        }

        @Override
        protected MediaItem fromEveryRefreshObject(JSONObject itemObject) {
            mVideoInfo = new VideoInfo(itemObject);
            if (mVideoInfo.getTopicInfo() == null) {
                mVideoInfo.setTopicInfo(mTopicInfo);
            }
            if (mTopicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_VIDEO)) {
                return new VideoItem(mVideoInfo);
            }
            else if (mTopicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_MOVIE)) {
                return new MovieItem(mVideoInfo);
            }
            else {
                throw new RuntimeException("Unknown category");
            }
        }

        @Override
        protected MediaItem generatorTitleItem(BaseTopicInfo topicInfo) {
            return new BasicTitleItem(topicInfo);
        }

        @Override
        protected void onRefreshResult(JSONObject result, List<MediaItem> data) {
            try {
                VideoKooBriefItem videoKooBriefItem = new VideoKooBriefItem(mVideoInfo,
                        result.getJSONObject("video").getJSONArray("koo_ranks"));
                if (videoKooBriefItem.hasKooRankUser()) {
                    data.add(videoKooBriefItem);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            data.add(new CommentTitleItem(mVideoInfo.getCommentCount()));
        }

        @Override
        protected JSONArray getLoadMoreArray(JSONObject result) {
            try {
                return result.getJSONArray("comments");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new JSONArray();
        }

        @Override
        protected MediaItem fromEveryLoadMoreObject(JSONObject itemObject) {
            return new CommentItem(new BaseCommentInfo(itemObject));
        }

        @Override
        public void onClick(View v) {
            TopicMediaActivity.startThisActivity(mContext, mTopicInfo.getTopicId(),
                    TopicMediaActivity.TYPE_WORLD);
        }


        class VideoInfo extends BaseVideoInfo {
            public VideoInfo(JSONObject jsonObject) {
                super(jsonObject);
            }

            @Override
            public void setKooTotal(int count) {
                super.setKooTotal(count);

                notifyItemChanged(2); // Comment title always at position-2
            }
        }
    }
}
