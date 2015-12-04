package com.koolew.mars.topicmedia;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.R;
import com.koolew.mars.SendDanmakuActivity;
import com.koolew.mars.ShareVideoWindow;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.KooAnimationView;
import com.koolew.mars.view.KoolewVideoView;
import com.koolew.mars.view.UserNameView;
import com.nostra13.universalimageloader.core.ImageLoader;

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


    public VideoItem(BaseVideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    @Override
    protected int getType() {
        return TYPE;
    }


    static class ItemViewHolder extends MediaHolder<VideoItem> implements View.OnClickListener,
            ShareVideoWindow.OnVideoOperatedListener {
        protected ImageView avatar;
        protected UserNameView userName;
        protected TextView videoDate;
        protected KoolewVideoView videoView;
        protected TextView kooAndComment;

        protected View kooLayout;
        protected View kooIcon;
        protected View danmakuSend;
        protected View moreView;

        protected KooAnimationView kooAnimationView;

        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);

            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            userName = (UserNameView) itemView.findViewById(R.id.name_view);
            videoDate = (TextView) itemView.findViewById(R.id.video_date);
            videoView = (KoolewVideoView) itemView.findViewById(R.id.video_view);
            kooAndComment = (TextView) itemView.findViewById(R.id.koo_and_comment_count);

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
            setKooAndComment(mContext, mItem.videoInfo);
        }

        protected void setKooAndComment(Context context, BaseVideoInfo videoInfo) {
            kooAndComment.setVisibility(View.VISIBLE);
            StringBuilder builder = new StringBuilder();
            builder.append(context.getString(R.string.receive));
            if (videoInfo.getKooTotal() != 0) {
                builder.append(context.getString(R.string.koo_count, videoInfo.getKooTotal()));
            }
            if (videoInfo.getKooTotal() != 0 && videoInfo.getCommentCount() != 0) {
                builder.append(context.getString(R.string.comma));
            }
            if (videoInfo.getCommentCount() != 0) {
                builder.append(context.getString(R.string.comment_count, videoInfo.getCommentCount()));
            }
            kooAndComment.setText(builder.toString());
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
            mAdapter.mSoundPool.play(mAdapter.mKooSound, 1, 1, 0, 0, 1);
        }

        protected void onDanmakuSendClick() {
            Intent intent = new Intent(mContext, SendDanmakuActivity.class);
            intent.putExtra(SendDanmakuActivity.KEY_VIDEO_INFO, mItem.videoInfo);
            mContext.startActivity(intent);
        }

        protected void onMoreClick() {
            ShareVideoWindow shareVideoWindow = new ShareVideoWindow((Activity) mContext,
                    mItem.videoInfo, mItem.videoInfo.getTopicInfo().getTitle());
            shareVideoWindow.setOnVideoOperatedListener(this);
            shareVideoWindow.showAtLocation(itemView, Gravity.TOP, 0, 0);
        }

        public void hideKooAndComment() {
            kooAndComment.setVisibility(View.GONE);
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
    }
}
