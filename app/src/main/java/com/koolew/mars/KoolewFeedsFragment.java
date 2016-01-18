package com.koolew.mars;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.KoolewVideoView;
import com.koolew.mars.view.RatioFrameLayout;
import com.koolew.mars.view.RatioLinearLayout;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jinchangzhu on 12/10/15.
 */
public class KoolewFeedsFragment extends RecyclerListFragmentMould<KoolewFeedsFragment.FeedsTopicAdapter> {

    private static final int MAX_VIDEO_COUNT_PER_TOPIC = 3;

    private static final List<String> ignoredUser = new ArrayList<>();

    private KoolewVideoView.ScrollPlayer mScrollPlayer;

    public KoolewFeedsFragment() {
        isNeedApiCache = true;
        isNeedLoadMore = true;
        isLazyLoad = true;
    }

    @Override
    protected void initViews() {
        super.initViews();

        mScrollPlayer = new FeedsScrollPlayer(mRecyclerView);
    }

    @Override
    protected void onPageEnd() {
        super.onPageEnd();

        mScrollPlayer.onPause();
    }

    @Override
    protected void onPageStart() {
        super.onPageStart();

        mScrollPlayer.onResume();
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
            JSONArray users = result.getJSONArray("recommend_users");
            if (users != null && users.length() > 0) {
                BaseUserInfo userInfo = new BaseUserInfo(users.getJSONObject(0));
                if (!ignoredUser.contains(userInfo.getUid())) {
                    mAdapter.mData.setRecommendUser(userInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONArray cards = result.getJSONArray("cards");
            int addedCount = addFeedsCards(cards);
            mAdapter.notifyDataSetChanged();
            mScrollPlayer.onRefresh();
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

    class FeedsScrollPlayer extends KoolewVideoView.ScrollPlayer {
        public FeedsScrollPlayer(RecyclerView recyclerView) {
            super(recyclerView);
        }

        @Override
        protected KoolewVideoView getVideoView(RecyclerView.ViewHolder holder) {
            if (holder instanceof FeedsItemViewHolder) {
                return ((FeedsItemViewHolder) holder).videoView;
            }
            else {
                return null;
            }
        }
    }


    private static final int TYPE_SEARCH = 1;
    private static final int TYPE_RECOMMEND = 2;
    private static final int TYPE_1ITEM = 3;
    private static final int TYPE_2ITEMS = 4;
    private static final int TYPE_3ITEMS = 5;

    class FeedsTopicAdapter extends LoadMoreAdapter {

        private FeedsData mData = new FeedsData();

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_SEARCH:
                    return new SearchTopicHolder(LayoutInflater.from(getActivity())
                            .inflate(R.layout.search_topic_item_feeds, parent, false));
                case TYPE_RECOMMEND:
                    return new RecommendUserHolder(LayoutInflater.from(getActivity())
                            .inflate(R.layout.feeds_recommend_user, parent, false));
                case TYPE_1ITEM:
                    FeedsItemViewHolder holder1 = new FeedsItemViewHolder(LayoutInflater.from(getContext())
                            .inflate(R.layout.feeds_card_item, parent, false));
                    holder1.transformTo1VideoLayout();
                    return holder1;
                case TYPE_2ITEMS:
                    FeedsItemViewHolder holder2 = new FeedsItemViewHolder(LayoutInflater.from(getContext())
                            .inflate(R.layout.feeds_card_item, parent, false));
                    holder2.transformTo2VideoLayout();
                    return holder2;
                case TYPE_3ITEMS:
                    return new FeedsItemViewHolder(LayoutInflater.from(getActivity())
                            .inflate(R.layout.feeds_card_item, parent, false));
            }
            return null;
        }

        @Override
        public int getCustomItemViewType(int position) {
            Object itemData = mData.get(position);
            if (itemData instanceof FeedsItem) {
                FeedsItem feedsItem = (FeedsItem) itemData;
                if (feedsItem.videoInfos[1] == null) {
                    return TYPE_1ITEM;
                }
                else if (feedsItem.videoInfos[2] == null) {
                    return TYPE_2ITEMS;
                }
                else {
                    return TYPE_3ITEMS;
                }
            }
            else if (itemData instanceof BaseUserInfo) {
                return TYPE_RECOMMEND;
            }
            else {
                return TYPE_SEARCH;
            }
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            if (getCustomItemViewType(position) == TYPE_SEARCH) {
                ImageLoader.getInstance().displayImage(MyAccountInfo.getAvatar(),
                        ((SearchTopicHolder) viewHolder).avatar);
                return;
            }
            switch (getCustomItemViewType(position)) {
                case TYPE_RECOMMEND:
                    bindRecommendUserHolder((RecommendUserHolder) viewHolder, position);
                    break;
                case TYPE_1ITEM:
                case TYPE_2ITEMS:
                case TYPE_3ITEMS:
                    bindFeedsItemHolder((FeedsItemViewHolder) viewHolder, position);
                    break;
            }
        }

        private void bindRecommendUserHolder(RecommendUserHolder holder, int position) {
            BaseUserInfo userInfo;
            try {
                userInfo = (BaseUserInfo) mData.get(position);
            } catch (ClassCastException cce) {
                if (MarsApplication.DEBUG) {
                    throw cce;
                }
                else {
                    return;
                }
            }

            ImageLoader.getInstance().displayImage(userInfo.getAvatar(), holder.avatar,
                    ImageLoaderHelper.avatarLoadOptions);
            holder.nameView.setUser(userInfo);
        }

        private void bindFeedsItemHolder(FeedsItemViewHolder holder, int position) {
            FeedsItem item;
            try {
                item = (FeedsItem) mData.get(position);
            } catch (ClassCastException cce) {
                if (MarsApplication.DEBUG) {
                    throw cce;
                }
                else {
                    return;
                }
            }

            int newVideoCount = 0;
            for (int i = 0; i < item.videoInfos.length; i++) {
                if (item.videoInfos[i] != null && item.videoInfos[i].isNew) {
                    newVideoCount++;
                }
            }
            if (newVideoCount > 0) {
                holder.newVideoCount.setText(getString(R.string.absolutely_new, newVideoCount));
                holder.newVideoCount.setVisibility(View.VISIBLE);
                holder.title.setPadding(0, 0,
                        getResources().getDimensionPixelSize(R.dimen.new_video_view_width), 0);
            }
            else {
                holder.newVideoCount.setVisibility(View.INVISIBLE);
                holder.title.setPadding(0, 0, 0, 0);
            }
            holder.title.setText(item.topicInfo.getTitle());

            if (item.topicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_VIDEO)) {
                holder.captureButton.setImageResource(R.mipmap.ic_btn_capture_video);
            }
            else if (item.topicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_MOVIE)) {
                holder.captureButton.setImageResource(R.mipmap.ic_btn_capture_movie);
            }
            else {
                // WTF
            }

            holder.videoView.setVideoInfo(item.videoInfos[0]);
            if (item.videoInfos[1] != null) {
                ImageLoader.getInstance().displayImage(item.videoInfos[1].getVideoThumb(),
                        holder.thumbImages[1], ImageLoaderHelper.topicThumbLoadOptions);
            }
            if (item.videoInfos[2] != null) {
                ImageLoader.getInstance().displayImage(item.videoInfos[2].getVideoThumb(),
                        holder.thumbImages[2], ImageLoaderHelper.topicThumbLoadOptions);
            }

            for (int i = 0; i < item.videoInfos.length && item.videoInfos[i] != null; i++) {
                ImageLoader.getInstance().displayImage(item.videoInfos[i].getUserInfo().getAvatar(),
                        holder.avatars[i], ImageLoaderHelper.avatarLoadOptions);
                holder.avatars[i].setBorderColor(getResources().getColor(item.videoInfos[i].isNew ?
                        R.color.koolew_light_green : R.color.avatar_gray_border));
                holder.userNames[i].setText(item.videoInfos[i].getUserInfo().getNickname());
            }
        }

        @Override
        public int getCustomItemCount() {
            return mData.size();
        }
    }

    static class FeedsData {
        private List<Object> search;
        private List<BaseUserInfo> recommendUsers;
        private List<FeedsItem> itemList;

        public FeedsData() {
            search = new ArrayList<>(1);
            search.add(new Object());
            recommendUsers = new ArrayList<>(1);
            itemList = new ArrayList<>();
        }

        public int size() {
            return search.size() + recommendUsers.size() + itemList.size();
        }

        public Object get(int position) {
            return Utils.getItemFromLists(position, search, recommendUsers, itemList);
        }

        public void add(FeedsItem feedsItem) {
            itemList.add(feedsItem);
        }

        public void clear() {
            itemList.clear();
        }

        public void setRecommendUser(BaseUserInfo userInfo) {
            recommendUsers.clear();
            recommendUsers.add(userInfo);
        }

        public void clearRecommendUser() {
            recommendUsers.clear();
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

    private static final int[] AVATAR_IDS = new int[] {
            R.id.avatar0, R.id.avatar1, R.id.avatar2
    };
    private static final int[] NAME_VIEW_IDS = new int[] {
            R.id.name0, R.id.name1, R.id.name2
    };

    class FeedsItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView newVideoCount;
        private ImageView captureButton;

        private RatioLinearLayout videoContainer;
        private RatioFrameLayout firstVideoLayout;
        private KoolewVideoView videoView;
        private LinearLayout rightVideoLayout;
        private FrameLayout thirdVideoLayout;
        private ImageView[] thumbImages = new ImageView[MAX_VIDEO_COUNT_PER_TOPIC];
        private CircleImageView[] avatars = new CircleImageView[MAX_VIDEO_COUNT_PER_TOPIC];
        private TextView[] userNames = new TextView[MAX_VIDEO_COUNT_PER_TOPIC];

        public FeedsItemViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            title = (TextView) itemView.findViewById(R.id.title);
            newVideoCount = (TextView) itemView.findViewById(R.id.new_video_count);
            captureButton = (ImageView) itemView.findViewById(R.id.capture_btn);
            captureButton.setOnClickListener(this);

            videoContainer = (RatioLinearLayout) itemView.findViewById(R.id.video_container);
            firstVideoLayout = (RatioFrameLayout) itemView.findViewById(R.id.first_video_layout);
            videoView = (KoolewVideoView) itemView.findViewById(R.id.video_view);
            rightVideoLayout = (LinearLayout) itemView.findViewById(R.id.right_video_layout);
            thirdVideoLayout = (FrameLayout) itemView.findViewById(R.id.third_video_layout);

            thumbImages[1] = (ImageView) itemView.findViewById(R.id.second_thumb);
            thumbImages[2] = (ImageView) itemView.findViewById(R.id.third_thumb);

            for (int i = 0; i < MAX_VIDEO_COUNT_PER_TOPIC; i++) {
                avatars[i] = (CircleImageView) itemView.findViewById(AVATAR_IDS[i]);
                userNames[i] = (TextView) itemView.findViewById(NAME_VIEW_IDS[i]);
            }
        }

        private void transformTo2VideoLayout() {
            rightVideoLayout.removeView(thirdVideoLayout);
            thirdVideoLayout = null;
            thumbImages[2] = null;
            avatars[2] = null;
            userNames[2] = null;
        }

        private void transformTo1VideoLayout() {
            videoContainer.removeView(rightVideoLayout);
            videoContainer.setRatio(4, 3);
            rightVideoLayout = null;
            thirdVideoLayout = null;
            for (int i = 1; i <= 2; i++) {
                thumbImages[i] = null;
                avatars[i] = null;
                userNames[i] = null;
            }
        }

        @Override
        public void onClick(View v) {
            FeedsItem feedsItem = (FeedsItem) mAdapter.mData.get(getAdapterPosition());
            BaseTopicInfo topicInfo = feedsItem.topicInfo;
            if (v.getId() == R.id.capture_btn) {
                topicInfo.gotoCapture(getContext());
            }
            else {
                TopicMediaActivity.startThisActivity(getActivity(), topicInfo.getTopicId(),
                        TopicMediaActivity.TYPE_FEEDS);
                for (CircleImageView avatar: avatars) {
                    if (avatar != null) {
                        avatar.setBorderColor(getResources().getColor(R.color.avatar_gray_border));
                    }
                }
                newVideoCount.setVisibility(View.INVISIBLE);
                title.setPadding(0, 0, 0, 0);
            }
        }
    }

    class RecommendUserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView avatar;
        private UserNameView nameView;
        private View addUserView;
        private View clearRecommendView;

        private Dialog connectingDialog;

        public RecommendUserHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            nameView = (UserNameView) itemView.findViewById(R.id.name_view);
            addUserView = itemView.findViewById(R.id.add_user);
            addUserView.setOnClickListener(this);
            clearRecommendView = itemView.findViewById(R.id.clear_recommend);
            clearRecommendView.setOnClickListener(this);

            connectingDialog = DialogUtil.getConnectingServerDialog(getContext());
        }

        @Override
        public void onClick(View v) {
            BaseUserInfo userInfo;
            try {
                userInfo = (BaseUserInfo) mAdapter.mData.get(getAdapterPosition());
            } catch (ClassCastException cce) {
                return;
            }

            if (v == itemView) {
                FriendInfoActivity.startThisActivity(getContext(), userInfo.getUid());
            }
            else if ((v == addUserView)) {
                connectingDialog.show();
                ApiWorker.getInstance().followUser(userInfo.getUid(),
                        new FollowListener(), new ErrorListener());
            }
            else if (v == clearRecommendView) {
                mAdapter.mData.clearRecommendUser();
                mAdapter.notifyItemRemoved(getAdapterPosition());
                ignoredUser.add(userInfo.getUid());
            }
        }

        class FollowListener implements Response.Listener<JSONObject> {
            @Override
            public void onResponse(JSONObject response) {
                connectingDialog.dismiss();
                mAdapter.mData.clearRecommendUser();
                mAdapter.notifyItemRemoved(getAdapterPosition());
            }
        }

        class ErrorListener implements Response.ErrorListener {
            @Override
            public void onErrorResponse(VolleyError error) {
                connectingDialog.dismiss();
                if (MarsApplication.DEBUG) {
                    throw new RuntimeException(error.getMessage());
                }
                else {
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }
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
            getActivity().startActivity(new Intent(getActivity(), GlobalSearchActivity.class));
        }
    }

    static class FeedsItem {
        private BaseTopicInfo topicInfo;
        private VideoInfo[] videoInfos = new VideoInfo[MAX_VIDEO_COUNT_PER_TOPIC];

        public FeedsItem(JSONObject jsonObject) {
            topicInfo = BaseTopicInfo.dynamicTopicInfo(JsonUtil.getJSONObjectIfHas(jsonObject, "topic"));
            JSONArray videosJson = JsonUtil.getJSONArrayIfHas(jsonObject, "videos");
            int videoCount = videosJson == null ? 0 : videosJson.length();
            for (int i = 0; i < videoCount && i < MAX_VIDEO_COUNT_PER_TOPIC; i++) {
                try {
                    videoInfos[i] = new VideoInfo(videosJson.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class VideoInfo extends BaseVideoInfo {
        private boolean isNew;

        public VideoInfo(JSONObject jsonObject) {
            super(jsonObject);
            isNew = JsonUtil.getIntIfHas(jsonObject, "new") > 0;
        }
    }
}
