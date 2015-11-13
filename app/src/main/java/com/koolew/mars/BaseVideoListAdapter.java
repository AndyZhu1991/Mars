package com.koolew.mars;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.KooAnimationView;
import com.koolew.mars.view.KoolewVideoView;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.ApiErrorCode;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jinchangzhu on 11/7/15.
 */
public class BaseVideoListAdapter extends LoadMoreAdapter {

    private static final int TYPE_VIDEO_ITEM = 0;
    // 子类在 {@link #getCustomItemViewType(int)} 的返回值必须大于等于 TYPE_FIRST_USEBLE_TYPE
    protected static final int TYPE_FIRST_USEBLE_TYPE = 1;

    protected Context mContext;
    private SoundPool mSoundPool;
    private int mKooSound;
    private List<BaseVideoInfo> mVideoInfoList = new ArrayList<>();

    public BaseVideoListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mSoundPool = new SoundPool(5, AudioManager.STREAM_RING, 0);
        mKooSound = mSoundPool.load(mContext, R.raw.koo, 1);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        if (mSoundPool != null) {
            mSoundPool.release();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        return new VideoItemHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.video_card_item, null));
    }

    @Override
    public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        VideoItemHolder holder = (VideoItemHolder) viewHolder;

        BaseVideoInfo videoInfo = mVideoInfoList.get(position);
        BaseUserInfo userInfo = videoInfo.getUserInfo();
        ImageLoader.getInstance().displayImage(userInfo.getAvatar(),
                holder.avatar, ImageLoaderHelper.avatarLoadOptions);
        holder.nameView.setUser(userInfo);
        holder.videoDate.setText(Utils.buildTimeSummary(mContext, videoInfo.getCreateTime() * 1000));

        holder.videoView.setVideoInfo(videoInfo);

        holder.setKooAndCommentCount(videoInfo);
    }

    @Override
    public int getCustomItemCount() {
        return mVideoInfoList.size();
    }

    @Override
    public int getCustomItemViewType(int position) {
        return TYPE_VIDEO_ITEM;
    }

    private class VideoItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            ShareVideoWindow.OnVideoOperatedListener {

        public CircleImageView avatar;
        public UserNameView nameView;
        public TextView videoDate;

        public KoolewVideoView videoView;

        public TextView kooAndCommentCount;

        public LinearLayout kooLayout;
        public ImageView kooIcon;
        public LinearLayout danmakuSendLayout;
        public View more;
        public KooAnimationView kooAnimationView;


        public VideoItemHolder(View itemView) {
            super(itemView);

            avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            avatar.setOnClickListener(this);
            nameView = (UserNameView) itemView.findViewById(R.id.name_view);
            videoDate = (TextView) itemView.findViewById(R.id.video_date);

            kooAndCommentCount = (TextView) itemView.findViewById(R.id.koo_and_comment_count);
            kooAndCommentCount.setOnClickListener(this);

            kooLayout = (LinearLayout) itemView.findViewById(R.id.koo_layout);
            kooLayout.setOnClickListener(this);
            kooIcon = (ImageView) itemView.findViewById(R.id.koo_icon);
            danmakuSendLayout = (LinearLayout) itemView.findViewById(R.id.danmaku_send_layout);
            danmakuSendLayout.setOnClickListener(this);
            more = itemView.findViewById(R.id.more_layout);
            more.setOnClickListener(this);
            kooAnimationView = (KooAnimationView) itemView.findViewById(R.id.koo_animation_view);
        }

        public void setKooAndCommentCount(BaseVideoInfo videoInfo) {
            StringBuilder builder = new StringBuilder();
            builder.append(mContext.getString(R.string.receive));
            if (videoInfo.getKooTotal() != 0) {
                builder.append(mContext.getString(R.string.koo_count, videoInfo.getKooTotal()));
            }
            if (videoInfo.getKooTotal() != 0 && videoInfo.getCommentCount() != 0) {
                builder.append(mContext.getString(R.string.comma));
            }
            if (videoInfo.getCommentCount() != 0) {
                builder.append(mContext.getString(R.string.comment_count,
                        videoInfo.getCommentCount()));
            }
            kooAndCommentCount.setText(builder.toString());
        }

        private BaseVideoInfo getVideoItem() {
            return mVideoInfoList.get(getAdapterPosition());
        }

        @Override
        public void onClick(View v) {
            if (v == more) {
                onMoreClick();
            }
            else if (v == avatar) {
                onAvatarClick();
            }
            else if (v == kooLayout) {
                onKooClick();
            }
            else if (v == danmakuSendLayout) {
                onDanmakuSendClick();
            }
            else if (v == kooAndCommentCount) {
                onKooAndCommentCountClick();
            }
        }

        private void onMoreClick() {
            ShareVideoWindow shareVideoWindow = new ShareVideoWindow((Activity) mContext,
                    getVideoItem(), getVideoItem().getTopicInfo().getTitle());
            shareVideoWindow.setOnVideoOperatedListener(this);
            shareVideoWindow.showAtLocation(itemView, Gravity.TOP, 0, 0);
        }

        private void onAvatarClick() {
            FriendInfoActivity.startThisActivity(mContext, getVideoItem().getUserInfo().getUid());
        }

        private void onKooClick() {
            // Play sound
            mSoundPool.play(mKooSound, 1, 1, 0, 0, 1);

            // Increase koo total
            getVideoItem().setKooTotal(getVideoItem().getKooTotal() + 1);
            setKooAndCommentCount(getVideoItem());

            // Post koo request
            ApiWorker.getInstance().kooVideo(getVideoItem().getVideoId(), 1, mKooListener, null);

            // Play animation
            PropertyValuesHolder pvhA = PropertyValuesHolder.ofFloat("alpha", 1f, 0.5f, 1f);
            PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 1f, 3f, 1f);
            PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f, 3f, 1f);
            ObjectAnimator.ofPropertyValuesHolder(kooIcon, pvhA, pvhX, pvhZ)
                    .setDuration(400)
                    .start();
            kooAnimationView.startAnimation();
        }

        private void onDanmakuSendClick() {
            Intent intent = new Intent(mContext, SendDanmakuActivity.class);
            intent.putExtra(SendDanmakuActivity.KEY_VIDEO_INFO, getVideoItem());
            mContext.startActivity(intent);
        }

        private void onKooAndCommentCountClick() {
            Intent intent = new Intent(mContext, CheckDanmakuActivity.class);
            intent.putExtra(CheckDanmakuActivity.KEY_VIDEO_ID, getVideoItem().getVideoId());
            mContext.startActivity(intent);
        }

        @Override
        public void onVideoDeleted(String videoId) {
            deleteVideo(videoId);
        }

        @Override
        public void onVideoAgainst(String videoId) {
            deleteVideo(videoId);
        }

        private void deleteVideo(String videoId) {
            for (int i = 0; i < mVideoInfoList.size(); i++) {
                if (mVideoInfoList.get(i).getVideoId().equals(videoId)) {
                    mVideoInfoList.remove(i);
                    notifyItemRemoved(i);
                }
            }
        }
    }

    protected Response.Listener<JSONObject> mKooListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                int code = response.getInt("code");
                if (code == 0) {
                    MyAccountInfo.setCoinNum(MyAccountInfo.getCoinNum() - 1);
                }
                else if (code == ApiErrorCode.COIN_NOT_ENOUGH) {
                    Toast.makeText(mContext, R.string.not_enough_coin_hint, Toast.LENGTH_SHORT)
                            .show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
