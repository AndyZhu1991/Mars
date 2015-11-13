package com.koolew.mars;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.view.KoolewVideoView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by jinchangzhu on 11/9/15.
 */
public class MovieVideoCardAdapter extends VideoCardAdapter implements View.OnClickListener {

    private MovieInfo mMovieInfo;
    private OnTitleVideoListener mTitleVideoListener;
    private KoolewVideoView mVideoView;
    private View mPlayImage;
    private KoolewVideoView.VideoDownloader mDownloader;

    public MovieVideoCardAdapter(Context context) {
        super(context);
        mDownloader = new KoolewVideoView.VideoDownloaderImpl(context);
    }

    @Override
    protected View getTitleView(View convertView) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.movie_header, null);
        }

        mVideoView = (KoolewVideoView) convertView.findViewById(R.id.video_view);
        setupVideoView();
        mVideoView.setOnClickListener(this);
        mPlayImage = convertView.findViewById(R.id.play_image);

        ((TextView) convertView.findViewById(R.id.title)).setText(mMovieInfo.getTitle());
        ((TextView) convertView.findViewById(R.id.video_count)).setText(
                mContext.getString(R.string.video_count_label, mMovieInfo.getVideoCount()));
        ((TextView) convertView.findViewById(R.id.stars_rank_title)).setText(
                mContext.getString(R.string.stars_rank, mMovieInfo.topStars.length));

        int starsAvatarRes[] = new int[] {
                R.id.first_koo,
                R.id.second_koo,
                R.id.third_koo,
                R.id.forth_koo,
                R.id.fifth_koo,
        };
        for (int i = 0; i < starsAvatarRes.length; i++) {
            ImageView avatar = (ImageView) convertView.findViewById(starsAvatarRes[i]);
            if (i < mMovieInfo.topStars.length) {
                ImageLoader.getInstance().displayImage(mMovieInfo.topStars[i].getAvatar(), avatar,
                        ImageLoaderHelper.avatarLoadOptions);
            }
            else {
                avatar.setVisibility(View.INVISIBLE);
            }
        }

        return convertView;
    }

    public void setMovieInfo(MovieInfo movieInfo) {
        mMovieInfo = movieInfo;
        setupVideoView();
    }

    private void setupVideoView() {
        if (mMovieInfo != null && mVideoView != null) {
            mVideoView.setVideoInfo(mMovieInfo.movieUrl, mMovieInfo.movieThumb);
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

    public void stopTitleVideo() {
        mVideoView.stop();
        mPlayImage.setVisibility(View.VISIBLE);
        if (mTitleVideoListener != null) {
            mTitleVideoListener.onTitleVideoStop();
        }
    }

    public static class MovieInfo extends BaseTopicInfo {

        private BaseUserInfo[] topStars;
        private String movieUrl;
        private String movieThumb;

        public MovieInfo(JSONObject jsonObject) {
            super(jsonObject);

            JSONArray kooRanks = JsonUtil.getJSONArrayIfHas(jsonObject, "koo_ranks");
            int length = kooRanks.length();
            topStars = new BaseUserInfo[length];
            for (int i = 0; i < length; i++) {
                try {
                    topStars[i] = new BaseUserInfo(kooRanks.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            JSONObject movieAttr = JsonUtil.getJSONObjectIfHas(jsonObject, "attri");
            JSONObject movie = JsonUtil.getJSONObjectIfHas(movieAttr, "movie");
            movieUrl = JsonUtil.getStringIfHas(movie, "video_url");
            movieThumb = JsonUtil.getStringIfHas(movie, "thumbnail");
        }
    }

    public interface OnTitleVideoListener {
        void onTitleVideoStart();
        void onTitleVideoStop();
    }
}
