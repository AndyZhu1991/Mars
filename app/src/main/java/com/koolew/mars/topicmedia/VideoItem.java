package com.koolew.mars.topicmedia;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.FirstKooExplainWindow;
import com.koolew.mars.FriendInfoActivity;
import com.koolew.mars.R;
import com.koolew.mars.SendDanmakuActivity;
import com.koolew.mars.ShareVideoWindow;
import com.koolew.mars.SingleMediaFragment;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.utils.FirstHintUtil;
import com.koolew.mars.utils.KooSoundUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.KooAnimationView;
import com.koolew.mars.view.KoolewVideoView;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.ApiErrorCode;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 11/27/15.
 */
public class VideoItem extends MediaItem {

    private static final int TYPE = UniversalMediaAdapter.registerGenerator(
            new UniversalMediaAdapter.ItemViewHolderGenerator() {
                @Override
                protected int layoutResId() {
                    return R.layout.video_item;
                }

                @Override
                protected Class<?> holderClass() {
                    return ItemViewHolder.class;
                }
            }
    );


    protected BaseVideoInfo videoInfo;
    protected boolean needGotoSingleMedia = true;


    public VideoItem(BaseVideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    @Override
    protected int getType() {
        return TYPE;
    }

    @Override
    protected long getUpdateTime() {
        return videoInfo.getCreateTime();
    }

    public BaseVideoInfo getVideoInfo() {
        return videoInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        else if (o instanceof VideoItem) {
            return ((VideoItem) o).videoInfo.getVideoId().equals(this.videoInfo.getVideoId());
        }
        else {
            return false;
        }
    }

    public void setNeedGotoSingleMedia(boolean needGotoSingleMedia) {
        this.needGotoSingleMedia = needGotoSingleMedia;
    }

    public static class ItemViewHolder extends MediaHolder<VideoItem> implements View.OnClickListener,
            ShareVideoWindow.OnVideoOperatedListener {
        protected ImageView avatar;
        protected ImageView followIndicator;
        protected UserNameView userName;
        protected TextView videoDate;
        protected KoolewVideoView videoView;
        protected TextView kooAndComment;

        protected View kooLayout;
        protected View kooIcon;
        protected View danmakuSend;
        protected View moreView;

        protected KooAnimationView kooAnimationView;

        protected KooListener lastKooListener;


        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);

            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            avatar.setOnClickListener(this);
            followIndicator = (ImageView) itemView.findViewById(R.id.follow_indicator);
            userName = (UserNameView) itemView.findViewById(R.id.name_view);
            userName.setOnClickListener(this);
            videoDate = (TextView) itemView.findViewById(R.id.video_date);
            videoView = (KoolewVideoView) itemView.findViewById(R.id.video_view);
            videoView.setOnClickListener(this);
            kooAndComment = (TextView) itemView.findViewById(R.id.koo_and_comment_count);
            kooAndComment.setOnClickListener(this);

            kooLayout = itemView.findViewById(R.id.koo_layout);
            kooLayout.setOnClickListener(this);
            kooIcon = itemView.findViewById(R.id.koo_icon);
            danmakuSend = itemView.findViewById(R.id.danmaku_send);
            danmakuSend.setOnClickListener(this);
            moreView = itemView.findViewById(R.id.more_view);
            moreView.setOnClickListener(this);

            kooAnimationView = (KooAnimationView) itemView.findViewById(R.id.koo_animation_view);
        }

        @Override
        protected void onBindItem() {
            ImageLoader.getInstance().displayImage(mItem.videoInfo.getUserInfo().getAvatar(),
                    avatar, ImageLoaderHelper.avatarLoadOptions);
            userName.setUser(mItem.videoInfo.getUserInfo());
            videoDate.setText(Utils.buildTimeSummary(mContext,
                    mItem.videoInfo.getCreateTime() * 1000));
            videoView.setVideoInfo(mItem.videoInfo);
            setKooAndComment(mItem.videoInfo);

            BaseUserInfo userInfo = mItem.videoInfo.getUserInfo();
            if (userInfo.getType() == BaseUserInfo.TYPE_SELF) {
                followIndicator.setEnabled(false);
                followIndicator.setVisibility(View.INVISIBLE);
            }
            else if (!userInfo.isFollowed()) {
                followIndicator.setEnabled(true);
                followIndicator.setImageResource(R.mipmap.user_follow_indicator_not_followed);
                followIndicator.setVisibility(View.VISIBLE);
            }
            else {
                followIndicator.setEnabled(false);
                followIndicator.setImageResource(R.mipmap.user_follow_indicator_followed);
                followIndicator.setVisibility(View.VISIBLE);
            }
        }

        protected void setKooAndComment(BaseVideoInfo videoInfo) {
            String kooAndCommentText;
            if (videoInfo.getKooTotal() == 0 && videoInfo.getCommentCount() == 0) {
                kooAndCommentText = "";
            }
            else {
                StringBuilder builder = new StringBuilder();
                builder.append(mContext.getString(R.string.receive));
                if (videoInfo.getKooTotal() != 0) {
                    builder.append(mContext.getString(R.string.koo_count, videoInfo.getKooTotal()));
                }
                if (videoInfo.getKooTotal() != 0 && videoInfo.getCommentCount() != 0) {
                    builder.append(mContext.getString(R.string.comma));
                }
                if (videoInfo.getCommentCount() != 0) {
                    builder.append(mContext.getString(R.string.comment_count, videoInfo.getCommentCount()));
                }
                kooAndCommentText = builder.toString();
            }
            kooAndComment.setText(kooAndCommentText);
        }

        @Override
        public void onClick(View v) {
            if (v == kooLayout) {
                onKooClick();
            }
            else if (v == danmakuSend) {
                onDanmakuSendClick();
            }
            else if (v == moreView) {
                onMoreClick();
            }
            else if (v == avatar) {
                onAvatarClick();
            }
            else if (v == userName) {
                onUserClick();
            }
            else if (v == kooAndComment || v == videoView) {
                gotoSingleMedia();
            }
        }

        protected void onKooClick() {
            // Koo icon animation
            PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", 1f, 0.5f, 1f);
            PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", 1f, 3f, 1f);
            PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f, 3f, 1f);
            ObjectAnimator.ofPropertyValuesHolder(kooIcon, pvhX, pvhY, pvhZ)
                    .setDuration(400)
                    .start();

            // Koo boom animation
            kooAnimationView.startAnimation();

            // Play koo sound
            KooSoundUtil.playKooSoundBackground();

            // Pre-add koo count
            mItem.videoInfo.setKooTotal(mItem.videoInfo.getKooTotal() + 1);
            setKooAndComment(mItem.videoInfo);

            // Do api request
            lastKooListener = new KooListener(mItem.videoInfo.getVideoId());
            ApiWorker.getInstance().kooVideo(mItem.videoInfo.getVideoId(), 1, lastKooListener,
                    new ApiWorker.ToastErrorListener(mContext));

            showFirstKooWindowIfdNeed();
        }

        private void showFirstKooWindowIfdNeed() {
            if (!FirstHintUtil.isFirstKoo()) {
                return;
            }

            FirstKooExplainWindow window = new FirstKooExplainWindow(mContext);
            window.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int windowHeight = window.getContentView().getMeasuredHeight();
            window.showAsDropDown(kooIcon, 0, -windowHeight);
            window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    Utils.setWindowAlpha((Activity) mContext, 1.0f);
                }
            });
            Utils.setWindowAlpha((Activity) mContext, 0.3f);
        }

        protected void onDanmakuSendClick() {
            Intent intent = new Intent(mContext, SendDanmakuActivity.class);
            intent.putExtra(SendDanmakuActivity.KEY_VIDEO_INFO, BaseVideoInfo.lightCopy(mItem.videoInfo));
            mContext.startActivity(intent);
        }

        protected void onMoreClick() {
            ShareVideoWindow shareVideoWindow = new ShareVideoWindow((Activity) mContext,
                    mItem.videoInfo, mAdapter.mTopicInfo.getTitle());
            shareVideoWindow.setOnVideoOperatedListener(this);
            shareVideoWindow.showAtLocation(itemView, Gravity.TOP, 0, 0);
        }

        protected void onAvatarClick() {
            if (followIndicator.isEnabled()) {
                followIndicator.setImageResource(R.mipmap.user_follow_indicator_followed);
                followIndicator.setEnabled(false);
                BaseUserInfo userInfo = mItem.videoInfo.getUserInfo();
                ApiWorker.getInstance().followUser(userInfo.getUid(),
                        new FollowListener(userInfo), new FollowErrorListener());
            }
            else {
                onUserClick();
            }
        }

        protected void onUserClick() {
            FriendInfoActivity.startThisActivity(mContext, mItem.videoInfo.getUserInfo().getUid());
        }

        protected void gotoSingleMedia() {
            if (mItem.needGotoSingleMedia) {
                SingleMediaFragment.startThisFragment(mContext, mItem.videoInfo.getVideoId());
            }
        }

        public void disableKooAndComment() {
            kooAndComment.setEnabled(false);
        }

        @Override
        public void onVideoDeleted(String videoId) {
            deleteThisFromAdapter();
        }

        @Override
        public void onVideoAgainst(String videoId) {
            deleteThisFromAdapter();
        }

        private void deleteThisFromAdapter() {
            for (int i = 0; i < mAdapter.mData.size(); i++) {
                if (mAdapter.mData.get(i) == mItem) {
                    mAdapter.mData.remove(i);
                    mAdapter.notifyItemRemoved(i);
                    break;
                }
            }
        }

        class KooListener implements Response.Listener<JSONObject> {

            private String videoId;

            public KooListener(String videoId) {
                this.videoId = videoId;
            }

            @Override
            public void onResponse(JSONObject response) {
                if (this != lastKooListener || !videoId.equals(mItem.videoInfo.getVideoId())) {
                    // Only response last listener and same video id.
                    return;
                }
                try {
                    int code = response.getInt("code");
                    if (code == 0) {
                        MyAccountInfo.setCoinNum(MyAccountInfo.getCoinNum() - 1);
                        JSONObject result = response.getJSONObject("result");
                        int total = result.getInt("koo_total");
                        mItem.videoInfo.setKooTotal(total);
                        setKooAndComment(mItem.videoInfo);
                    }
                    else if (code == ApiErrorCode.COIN_NOT_ENOUGH) {
                        Toast.makeText(mContext, R.string.not_enough_coin_hint, Toast.LENGTH_SHORT)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        class FollowListener implements Response.Listener<JSONObject> {

            private BaseUserInfo userInfo;

            public FollowListener(BaseUserInfo userInfo) {
                this.userInfo = userInfo;
            }

            @Override
            public void onResponse(JSONObject response) {
                try {
                    int code = response.getInt("code");
                    if (code == 0) {
                        userInfo.doFollow();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        class FollowErrorListener implements Response.ErrorListener {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO
            }
        }
    }
}
