package com.koolew.mars;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.blur.DisplayBlurImage;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MovieTopicInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 12/10/15.
 */
public class KoolewFeedsFragment extends RecyclerListFragmentMould<KoolewFeedsFragment.FeedsTopicAdapter> {

    private static final int MAX_VIDEO_COUNT_PER_TOPIC = 3;

    public KoolewFeedsFragment() {
        isNeedLoadMore = true;
        isLazyLoad = true;
    }

    @Override
    protected FeedsTopicAdapter useThisAdapter() {
        return new FeedsTopicAdapter();
    }

    @Override
    public int getThemeColor() {
        return getActivity().getResources().getColor(R.color.koolew_light_orange);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        mAdapter.mData.clear();
        try {
            JSONArray cards = response.getJSONObject("result").getJSONArray("cards");
            int addedCount = addFeedsCards(cards);
            mAdapter.notifyDataSetChanged();
            return addedCount > 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        try {
            JSONArray cards = response.getJSONObject("result").getJSONArray("cards");
            int originCount = mAdapter.mData.size();
            int addedCount = addFeedsCards(cards);
            if (addedCount > 0) {
                mAdapter.notifyItemRangeInserted(originCount, addedCount);
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int addFeedsCards(JSONArray cards) {
        int count = cards.length();
        int addedCount = 0;
        for (int i = 0; i < count; i++) {
            try {
                mAdapter.mData.add(new FeedsItem(cards.getJSONObject(i)));
                addedCount++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return addedCount;
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestFeedsTopic(mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().requestFeedsTopic(
                mAdapter.getOldestCardTime(), mLoadMoreListener, null);
    }

    class FeedsTopicAdapter extends LoadMoreAdapter {

        private List<FeedsItem> mData = new ArrayList<>();

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new FeedsItemViewHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.feeds_card_item, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            FeedsItemViewHolder holder = (FeedsItemViewHolder) viewHolder;
            FeedsItem item = mData.get(position);

            holder.title.setText(item.topicInfo.getTitle());
            holder.videoCount.setText(getString(R.string.video_count_label, item.topicInfo.getVideoCount()));
            if (item.topicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_VIDEO)) {
                holder.captureButton.setImageResource(R.mipmap.ic_btn_capture_video);
                holder.movieCategoryImage.setVisibility(View.INVISIBLE);
            }
            else if (item.topicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_VIDEO)) {
                holder.captureButton.setImageResource(R.mipmap.ic_btn_capture_movie);
                holder.movieCategoryImage.setVisibility(View.VISIBLE);
            }
            else {
                // WTF
            }

            ImageLoader.getInstance().displayImage(item.videoInfos[0].getVideoThumb(),
                    holder.thumbImages[0], ImageLoaderHelper.topicThumbLoadOptions);
            if (item.videoInfos[1] != null) {
                ImageLoader.getInstance().displayImage(item.videoInfos[1].getVideoThumb(),
                        holder.thumbImages[1], ImageLoaderHelper.topicThumbLoadOptions);
            }
            else {
                holder.thumbImages[1].setImageDrawable(null);
            }
            if (item.videoInfos[2] != null) {
                DisplayBlurImage displayBlurImageTask = new DisplayBlurImage(
                        holder.thumbImages[2], item.videoInfos[2].getVideoThumb());
                displayBlurImageTask.setScaleBeforeBlur(15);
                displayBlurImageTask.execute();
            }
            else {
                holder.thumbImages[2].setImageDrawable(null);
            }
        }

        @Override
        public int getCustomItemCount() {
            return mData.size();
        }

        private long getOldestCardTime() {
            if (mData.size() == 0) {
                return Long.MAX_VALUE;
            }
            else {
                return mData.get(mData.size() - 1).topicInfo.getUpdateTime();
            }
        }
    }

    class FeedsItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView videoCount;
        private ImageView captureButton;
        private ImageView movieCategoryImage;

        private ImageView[] thumbImages = new ImageView[MAX_VIDEO_COUNT_PER_TOPIC];

        public FeedsItemViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            title = (TextView) itemView.findViewById(R.id.topic_title);
            videoCount = (TextView) itemView.findViewById(R.id.video_count);
            captureButton = (ImageView) itemView.findViewById(R.id.btn_capture);
            captureButton.setOnClickListener(this);
            movieCategoryImage = (ImageView) itemView.findViewById(R.id.corner_icon);

            thumbImages[0] = (ImageView) itemView.findViewById(R.id.first_thumb);
            thumbImages[0].setOnClickListener(this);
            thumbImages[1] = (ImageView) itemView.findViewById(R.id.second_thumb);
            thumbImages[1].setOnClickListener(this);
            thumbImages[2] = (ImageView) itemView.findViewById(R.id.third_thumb);
            thumbImages[2].setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            FeedsItem feedsItem = mAdapter.mData.get(getAdapterPosition());
            BaseTopicInfo topicInfo = feedsItem.topicInfo;
            switch (v.getId()) {
                case R.id.btn_capture:
                    if (topicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_VIDEO)) {
                        VideoShootActivity.startThisActivity(getActivity(), topicInfo.getTopicId(),
                                topicInfo.getTitle());
                    }
                    else if (topicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_MOVIE)) {
                        MovieStudioActivity.startThisActivity(getActivity(), (MovieTopicInfo) topicInfo);
                    }
                    else {
                    }
                    break;
                case R.id.first_thumb:
                    TopicMediaActivity.startThisActivity(getActivity(), topicInfo.getTopicId(),
                            TopicMediaActivity.TYPE_FEEDS, feedsItem.videoInfos[0].getVideoId());
                    break;
                case R.id.second_thumb:
                    if (feedsItem.videoInfos[1] != null) {
                        TopicMediaActivity.startThisActivity(getActivity(), topicInfo.getTopicId(),
                                TopicMediaActivity.TYPE_FEEDS, feedsItem.videoInfos[1].getVideoId());
                    }
                    else {
                        startFeedsMediaActivity(topicInfo.getTopicId());
                    }
                    break;
                default:
                    startFeedsMediaActivity(topicInfo.getTopicId());
            }
        }

        private void startFeedsMediaActivity(String topicId) {
            TopicMediaActivity.startThisActivity(getActivity(), topicId, TopicMediaActivity.TYPE_FEEDS);
        }
    }

    static class FeedsItem {
        private BaseTopicInfo topicInfo;
        private BaseVideoInfo[] videoInfos = new BaseVideoInfo[MAX_VIDEO_COUNT_PER_TOPIC];

        public FeedsItem(JSONObject jsonObject) {
            topicInfo = BaseTopicInfo.dynamicTopicInfo(JsonUtil.getJSONObjectIfHas(jsonObject, "topic"));
            JSONArray videosJson = JsonUtil.getJSONArrayIfHas(jsonObject, "videos");
            int videoCount = videosJson.length();
            for (int i = 0; i < videoCount && i < MAX_VIDEO_COUNT_PER_TOPIC; i++) {
                try {
                    videoInfos[i] = new BaseVideoInfo(videosJson.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
