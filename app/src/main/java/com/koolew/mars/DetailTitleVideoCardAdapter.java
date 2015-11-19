package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.KooCountUserInfo;
import com.koolew.mars.infos.MovieTopicInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.view.KoolewVideoView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 9/1/15.
 */
public class DetailTitleVideoCardAdapter extends VideoCardAdapter implements View.OnClickListener {

    private TopicTitleDetail topicTitleDetail;
    protected String mTopicId;

    private MovieDetailInfo mMovieDetailInfo;
    private OnTitleVideoListener mTitleVideoListener;
    private KoolewVideoView mVideoView;
    private View mPlayImage;
    private KoolewVideoView.VideoDownloader mDownloader;


    public DetailTitleVideoCardAdapter(Context context, String topicId) {
        super(context);
        mTopicId = topicId;
        mDownloader = new KoolewVideoView.VideoDownloaderImpl(context);
    }

    @Override
    protected View getTitleView(View convertView) {
        if (TextUtils.isEmpty(category)) {
            return super.getTitleView(convertView);
        }
        else if (category.equals("video")) {
            return getVideoTitle(convertView);
        }
        else if (category.equals("movie")) {
            return getMovieTitle(convertView);
        }
        else {
            return super.getTitleView(convertView);
        }
    }

    private View getVideoTitle(View convertView) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.topic_detail_title, null);
        }

        TextView summary = (TextView) convertView.findViewById(R.id.summary);
        if (topicTitleDetail.type == TYPE_TASK) {
            summary.setTextColor(mContext.getResources().getColor(R.color.koolew_light_green));
            summary.setText(mContext.getString(R.string.invited_label, topicTitleDetail.inviter));
        }
        else {
            summary.setVisibility(View.GONE);
        }

        ((TextView) convertView.findViewById(R.id.title)).setText(topicTitleDetail.title);

        TextView description = ((TextView) convertView.findViewById(R.id.description));
        if (TextUtils.isEmpty(topicTitleDetail.description)) {
            description.setVisibility(View.GONE);
        }
        else {
            description.setText(topicTitleDetail.description);
            description.setVisibility(View.VISIBLE);
        }

        ((TextView) convertView.findViewById(R.id.video_count)).setText(
                mContext.getString(R.string.video_count_label, topicTitleDetail.videoCount));

        KooCountUserInfo[] users = topicTitleDetail.kooRankUsers;
        if (users == null || users.length == 0) {
            convertView.findViewById(R.id.stars).setVisibility(View.GONE);
        }
        else {
            convertView.findViewById(R.id.stars).setOnClickListener(onStarsClickListener);

            ((TextView) convertView.findViewById(R.id.stars_rank_title)).setText(
                    mContext.getString(R.string.stars_rank, users.length));

            int[] avatarIds = new int[] {
                    R.id.first_koo,
                    R.id.second_koo,
                    R.id.third_koo,
                    R.id.forth_koo,
                    R.id.fifth_koo,
            };
            for (int i = 0; i < avatarIds.length && i < users.length; i++) {
                ImageLoader.getInstance().displayImage(users[i].getAvatar(),
                        (ImageView) convertView.findViewById(avatarIds[i]),
                        ImageLoaderHelper.avatarLoadOptions);
            }

            int avatarCrowns[] = new int[]{
                    R.id.first_crown,
                    R.id.second_crown,
                    R.id.third_crown,
            };
            for (int i = 0; i < avatarCrowns.length; i++) {
                int visibility = (topicTitleDetail.type == TYPE_WORLD && i < users.length)
                        ? View.VISIBLE
                        : View.INVISIBLE;
                convertView.findViewById(avatarCrowns[i]).setVisibility(visibility);
            }

            convertView.findViewById(R.id.topic_manager).setVisibility(
                    topicTitleDetail.type == TYPE_WORLD ? View.VISIBLE : View.INVISIBLE);

            View editTopicDesc = convertView.findViewById(R.id.edit_topic_desc);
            if (topicTitleDetail.type == TYPE_WORLD &&
                    users[0].getUid().equals(MyAccountInfo.getUid())) {
                editTopicDesc.setVisibility(View.VISIBLE);
                editTopicDesc.setOnClickListener(onEditTopicDescListener);
            }
            else {
                editTopicDesc.setVisibility(View.INVISIBLE);
            }
        }

        return convertView;
    }

    protected View getMovieTitle(View convertView) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.movie_header, null);
        }

        mVideoView = (KoolewVideoView) convertView.findViewById(R.id.video_view);
        setupVideoView();
        mVideoView.setOnClickListener(this);
        mPlayImage = convertView.findViewById(R.id.play_image);

        ((TextView) convertView.findViewById(R.id.title)).setText(mMovieDetailInfo.getTitle());
        ((TextView) convertView.findViewById(R.id.video_count)).setText(
                mContext.getString(R.string.video_count_label, mMovieDetailInfo.getVideoCount()));

        View kooTopLayout = convertView.findViewById(R.id.koo_top_layout);
        kooTopLayout.setOnClickListener(onStarsClickListener);

        if (mMovieDetailInfo.topStars == null || mMovieDetailInfo.topStars.length == 0) {
            kooTopLayout.setVisibility(View.GONE);
        }
        else {
            ((TextView) convertView.findViewById(R.id.stars_rank_title)).setText(
                    mContext.getString(R.string.stars_rank, mMovieDetailInfo.topStars.length));

            int starsAvatarRes[] = new int[]{
                    R.id.first_koo,
                    R.id.second_koo,
                    R.id.third_koo,
                    R.id.forth_koo,
                    R.id.fifth_koo,
            };
            for (int i = 0; i < starsAvatarRes.length; i++) {
                ImageView avatar = (ImageView) convertView.findViewById(starsAvatarRes[i]);
                if (i < mMovieDetailInfo.topStars.length) {
                    ImageLoader.getInstance().displayImage(mMovieDetailInfo.topStars[i].getAvatar(),
                            avatar, ImageLoaderHelper.avatarLoadOptions);
                } else {
                    avatar.setVisibility(View.INVISIBLE);
                }
            }
        }

        return convertView;
    }

    private View.OnClickListener onEditTopicDescListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, EditTopicDescActivity.class);
            intent.putExtra(EditTopicDescActivity.KEY_TOPIC_ID, mTopicId);
            mContext.startActivity(intent);
        }
    };

    private View.OnClickListener onStarsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, TopicKooRankActivity.class);
            intent.putExtra(TopicKooRankActivity.KEY_KOO_COUNT_USER_INFO, "movie".equals(category) ?
                    mMovieDetailInfo.topStars : topicTitleDetail.kooRankUsers);
            mContext.startActivity(intent);
        }
    };

    public void setTopicTitleDetail(TopicTitleDetail topicTitleDetail) {
        this.topicTitleDetail = topicTitleDetail;
    }

    public void setMovieDetail(MovieDetailInfo movieDetailInfo) {
        super.setMovieInfo(movieDetailInfo);
        mMovieDetailInfo = movieDetailInfo;
        setupVideoView();
    }

    private void setupVideoView() {
        if (mMovieDetailInfo != null && mVideoView != null) {
            mVideoView.setVideoInfo(mMovieDetailInfo.getVideoUrl(), mMovieDetailInfo.getThumbnail());
        }
    }

    public void setTitleVideoListener(OnTitleVideoListener listener) {
        mTitleVideoListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_view:
                if (mPlayImage.getVisibility() == View.VISIBLE) {
                    startTitleVideo();
                }
                else {
                    stopTitleVideo();
                }
                break;
        }
    }

    private void startTitleVideo() {
        mVideoView.startPlay(mDownloader);
        mPlayImage.setVisibility(View.INVISIBLE);
        if (mTitleVideoListener != null) {
            mTitleVideoListener.onTitleVideoStart();
        }
    }

    public void stopTitleVideoByOther() {
        if ("movie".equals(category)) {
            mVideoView.stop();
            mPlayImage.setVisibility(View.VISIBLE);
        }
    }

    private void stopTitleVideo() {
        mVideoView.stop();
        mPlayImage.setVisibility(View.VISIBLE);
        if (mTitleVideoListener != null) {
            mTitleVideoListener.onTitleVideoStop();
        }
    }

    @Override
    protected void onVideoItemStartPlay() {
        if ("movie".equals(category)) {
            stopTitleVideo();
        }
    }

    public static final int TYPE_FEEDS = 0;
    public static final int TYPE_WORLD = 1;
    public static final int TYPE_TASK  = 2;
    public static final int TYPE_USER  = 3;

    public static class TopicTitleDetail {

        private int type;
        private String inviter;
        private String title;
        private String description;
        private int videoCount;
        private KooCountUserInfo[] kooRankUsers;

        public void setType(int type) {
            this.type = type;
        }

        public void setInviter(String inviter) {
            this.inviter = inviter;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setKooRankUsers(KooCountUserInfo[] kooRankUsers) {
            this.kooRankUsers = kooRankUsers;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setVideoCount(int videoCount) {
            this.videoCount = videoCount;
        }
    }

    public static class MovieDetailInfo extends MovieTopicInfo {

        private KooCountUserInfo[] topStars;

        public MovieDetailInfo(JSONObject jsonObject) {
            super(jsonObject);

            JSONArray kooRanks = JsonUtil.getJSONArrayIfHas(jsonObject, "koo_ranks",
                    new JSONArray());
            int length = kooRanks.length();
            topStars = new KooCountUserInfo[length];
            for (int i = 0; i < length; i++) {
                try {
                    topStars[i] = new KooCountUserInfo(kooRanks.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface OnTitleVideoListener {
        void onTitleVideoStart();
        void onTitleVideoStop();
    }
}
