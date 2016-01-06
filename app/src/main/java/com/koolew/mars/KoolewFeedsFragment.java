package com.koolew.mars;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koolew.mars.blur.DisplayBlurImage;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MovieTopicInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.view.RatioFrameLayout;
import com.koolew.mars.webapi.UrlHelper;
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
        isNeedApiCache = true;
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
    protected int getNoDataViewResId() {
        return R.layout.koolew_feeds_no_data_layout;
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        mAdapter.mData.clear();
        try {
            JSONArray cards = result.getJSONArray("cards");
            int addedCount = addFeedsCards(cards);
            mAdapter.notifyDataSetChanged();
            return addedCount > 0;
        } catch (JSONException e) {
            handleJsonException(result, e);
        }
        return false;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        try {
            JSONArray cards = result.getJSONArray("cards");
            int originCount = mAdapter.mData.size();
            int addedCount = addFeedsCards(cards);
            if (addedCount > 0) {
                mAdapter.notifyItemRangeInserted(originCount, addedCount);
                return true;
            }
        } catch (JSONException e) {
            handleJsonException(result, e);
        }
        return false;
    }

    private int addFeedsCards(JSONArray cards) {
        int count = cards.length();
        int addedCount = 0;
        for (int i = 0; i < count; i++) {
            try {
                FeedsItem feedsItem = new FeedsItem(cards.getJSONObject(i));
                if (feedsItem.videoInfos[0] != null) {
                    mAdapter.mData.add(feedsItem);
                    addedCount++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return addedCount;
    }

    @Override
    protected String getRefreshRequestUrl() {
        return UrlHelper.FEEDS_TOPIC_URL;
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getFeedsTopicUrl(mAdapter.mData.getOldestCardTime());
    }


    private static final int HOLDER_TYPE_SEARCH = 1;
    private static final int HOLDER_TYPE_2ITEMS = 2;
    private static final int HOLDER_TYPE_3ITEMS = 3;

    class FeedsTopicAdapter extends LoadMoreAdapter {

        private FeedsData mData = new FeedsData();

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            if (viewType == HOLDER_TYPE_SEARCH) {
                return new SearchTopicHolder(LayoutInflater.from(getActivity())
                        .inflate(R.layout.search_topic_item_feeds, parent, false));
            }
            else {
                FeedsItemViewHolder holder = new FeedsItemViewHolder(LayoutInflater.from(getActivity())
                        .inflate(R.layout.feeds_card_item, parent, false));

                if (viewType == HOLDER_TYPE_2ITEMS) {
                    LinearLayout.LayoutParams lp =
                            (LinearLayout.LayoutParams) holder.thumbFrame0.getLayoutParams();
                    lp.weight = 4;
                    holder.thumbFrame0.setRatio(8, 3);
                    holder.thumbFrame1.setVisibility(View.GONE);
                }

                return holder;
            }
        }

        @Override
        public int getCustomItemViewType(int position) {
            FeedsItem feedsItem = mData.get(position);
            if (feedsItem == null) {
                return HOLDER_TYPE_SEARCH;
            }
            else {
                return feedsItem.videoInfos[1] == null ? HOLDER_TYPE_2ITEMS : HOLDER_TYPE_3ITEMS;
            }
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            if (getCustomItemViewType(position) == HOLDER_TYPE_SEARCH) {
                ImageLoader.getInstance().displayImage(MyAccountInfo.getAvatar(),
                        ((SearchTopicHolder) viewHolder).avatar);
                return;
            }

            FeedsItemViewHolder holder = (FeedsItemViewHolder) viewHolder;
            FeedsItem item = mData.get(position);

            holder.title.setText(item.topicInfo.getTitle());
            holder.videoCount.setText(getString(R.string.video_count, item.topicInfo.getVideoCount()));
            if (item.topicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_VIDEO)) {
                holder.captureButton.setImageResource(R.mipmap.ic_btn_capture_video);
                holder.movieCategoryImage.setVisibility(View.INVISIBLE);
            }
            else if (item.topicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_MOVIE)) {
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

            String thumbImage2;
            if (item.videoInfos[2] != null) {
                thumbImage2 = item.videoInfos[2].getVideoThumb();
            }
            else {
                thumbImage2 = item.videoInfos[0].getVideoThumb();
            }
            DisplayBlurImage displayBlurImageTask = new DisplayBlurImage(
                    holder.thumbImages[2], thumbImage2);
            displayBlurImageTask.setScaleBeforeBlur(15);
            displayBlurImageTask.execute();
        }

        @Override
        public int getCustomItemCount() {
            return mData.size();
        }
    }

    static class FeedsData {
        private List<FeedsItem> itemList = new ArrayList<>();

        public int size() {
            if (itemList.size() == 0) {
                return 0;
            }
            else {
                return itemList.size() + 1;
            }
        }

        public FeedsItem get(int position) {
            if (position == 0) {
                return null;
            }
            else {
                return itemList.get(position - 1);
            }
        }

        public void add(FeedsItem feedsItem) {
            itemList.add(feedsItem);
        }

        public void clear() {
            itemList.clear();
        }

        private long getOldestCardTime() {
            if (itemList.size() == 0) {
                return Long.MAX_VALUE;
            }
            else {
                return itemList.get(itemList.size() - 1).topicInfo.getUpdateTime();
            }
        }
    }

    class FeedsItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView videoCount;
        private ImageView captureButton;
        private ImageView movieCategoryImage;

        private ImageView[] thumbImages = new ImageView[MAX_VIDEO_COUNT_PER_TOPIC];
        private RatioFrameLayout thumbFrame0;
        private RatioFrameLayout thumbFrame1;

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

            thumbFrame0 = (RatioFrameLayout) itemView.findViewById(R.id.thumb_frame0);
            thumbFrame1 = (RatioFrameLayout) itemView.findViewById(R.id.thumb_frame1);
        }

        @Override
        public void onClick(View v) {
            FeedsItem feedsItem = mAdapter.mData.get(getAdapterPosition());
            BaseTopicInfo topicInfo = feedsItem.topicInfo;
            switch (v.getId()) {
                case R.id.btn_capture:
                    if (topicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_VIDEO)) {
                        VideoShootActivity.startThisActivity(getActivity(), topicInfo);
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

    class SearchTopicHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView avatar;

        public SearchTopicHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
        }

        @Override
        public void onClick(View v) {
            getActivity().startActivity(new Intent(getActivity(), AddTopicActivity.class));
        }
    }

    static class FeedsItem {
        private BaseTopicInfo topicInfo;
        private BaseVideoInfo[] videoInfos = new BaseVideoInfo[MAX_VIDEO_COUNT_PER_TOPIC];

        public FeedsItem(JSONObject jsonObject) {
            topicInfo = BaseTopicInfo.dynamicTopicInfo(JsonUtil.getJSONObjectIfHas(jsonObject, "topic"));
            JSONArray videosJson = JsonUtil.getJSONArrayIfHas(jsonObject, "videos");
            int videoCount = videosJson == null ? 0 : videosJson.length();
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
