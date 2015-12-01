package com.koolew.mars;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.qiniu.UploadHelper;
import com.koolew.mars.share.ShareManager;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.BgmUtil;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.FileUtil;
import com.koolew.mars.utils.Mp4ParserUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.TitleBarView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;


public class VideoEditActivity extends BaseActivity
        implements TitleBarView.OnRightLayoutClickListener, View.OnClickListener{

    public static final int REQUEST_VIDEO_PRIVACY = 1;

    public static final int RESULT_UPLOADED = RESULT_FIRST_USER + 1;
    public static final int RESULT_BACKGROUND_UPLOAD = RESULT_FIRST_USER + 2;

    public static final String KEY_CONCATED_VIDEO = "concated video";
    public static final String KEY_VIDEO_THUMB = "video thumb";
    public static final String KEY_TOPIC_ID = "topic id";
    public static final String KEY_TOPIC_TITLE = "topic title";
    public static final String KEY_IS_MOVIE = "is movie";
    public static final String KEY_FROM = "from";

    private static final int NO_MUSIC_SELECTED = -1;

    private String mConcatedVideo;
    private String mVideoThumb;
    private String mTopicId;
    private String mTopicTitle;
    private boolean isMovie;
    private String mFrom;
    private BaseVideoInfo mUploadedVideo;

    private String mSelectedBgmPath;

    private int mAuthority;

    private TitleBarView mTitleBar;
    private FrameLayout mVideoFrame;
    private SurfaceView mPlaySurface;
    private ImageView mThumb;
    private ImageView mPlayImage;
    private RelativeLayout mPrivacyLayout;
    private TextView mAuthorityText;
    private ImageView mBgmSwitch;

    private RecyclerView mRecyclerView;
    private MusicSelectAdapter mAdapter;

    private BgmPlayer mBgmPlayer;

    private View mShareItemWechatMoments;
    private View mShareItemWechatFriends;
    private View mShareItemQQ;
    private View mShareItemWeibo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);

        mConcatedVideo = getIntent().getStringExtra(KEY_CONCATED_VIDEO);
        mVideoThumb = getIntent().getStringExtra(KEY_VIDEO_THUMB);
        mTopicId = getIntent().getStringExtra(KEY_TOPIC_ID);
        mTopicTitle = getIntent().getStringExtra(KEY_TOPIC_TITLE);
        isMovie = getIntent().getBooleanExtra(KEY_IS_MOVIE, false);
        mFrom = getIntent().getStringExtra(KEY_FROM);

        initMembers();

        initViews();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .build();
        ImageLoader.getInstance().displayImage("file://" + mVideoThumb, mThumb, options);
    }

    private void initMembers() {
        mAuthority = VideoPrivacyActivity.AUTHORITY_PUBLIC;
        mBgmPlayer = new BgmPlayer();
    }

    private void initViews() {
        mTitleBar = (TitleBarView) findViewById(R.id.title_bar);
        mTitleBar.setOnRightLayoutClickListener(this);

        mVideoFrame = (FrameLayout) findViewById(R.id.video_frame);
        mVideoFrame.setOnClickListener(this);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mVideoFrame.getLayoutParams();
        lp.height = Utils.getScreenWidthPixel(this) / 4 * 3;
        mVideoFrame.setLayoutParams(lp);

        mPlaySurface = (SurfaceView) findViewById(R.id.play_surface);
        mPlaySurface.getHolder().addCallback(mSurfaceCallback);

        mThumb = (ImageView) findViewById(R.id.thumb);

        mPlayImage = (ImageView) findViewById(R.id.play_image);
        mPrivacyLayout = (RelativeLayout) findViewById(R.id.privacy_layout);
        mPrivacyLayout.setOnClickListener(this);
        mAuthorityText = (TextView) findViewById(R.id.authority_text);

        if (isMovie) {
            findViewById(R.id.music_layout).setVisibility(View.GONE);
        }

        mBgmSwitch = (ImageView) findViewById(R.id.bgm_switch);
        mBgmSwitch.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new MusicSelectAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mShareItemWechatMoments = findViewById(R.id.wechat_moments);
        mShareItemWechatFriends = findViewById(R.id.wechat_friends);
        mShareItemQQ = findViewById(R.id.qq);
        mShareItemWeibo = findViewById(R.id.weibo);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBgmPlayer.pausePlay();
        mPlayImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onRightLayoutClick() {
        final String finalVideo;
        if (mSelectedBgmPath == null) {
            finalVideo = mConcatedVideo;
        } else {
            finalVideo = new File(mConcatedVideo).getParent() + "/final.mp4";
            try {
                Mp4ParserUtil.setVideoBgm(mConcatedVideo, mSelectedBgmPath, finalVideo);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        new AsyncTask<String, Void, Boolean>() {
            private ProgressDialog mUploadingDialog;

            @Override
            protected void onPreExecute() {
                mUploadingDialog = DialogUtil.getGeneralProgressDialog
                        (VideoEditActivity.this, R.string.uploading);
                mUploadingDialog.show();
            }

            @Override
            protected Boolean doInBackground(String... params) {
                if (isMovie) {
                    mUploadedVideo = UploadHelper.uploadMovie(params[0], params[1],
                            params[2], mAuthority, mFrom);
                }
                else {
                    mUploadedVideo = UploadHelper.uploadVideo(params[0], params[1],
                            params[2], mAuthority);
                }
                if (mUploadedVideo != null) {
                    BaseUserInfo userInfo = new BaseUserInfo(new JSONObject());
                    userInfo.setNickname(MyAccountInfo.getNickname());
                    userInfo.setUid(MyAccountInfo.getUid());
                    mUploadedVideo.setUserInfo(userInfo);
                    try {
                        saveAndRegisterVideo();
                    }
                    catch (Exception e) {
                        Toast.makeText(VideoEditActivity.this, R.string.save_to_local_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                    shareVideoIfNeed();
                    return true;
                } else {
                    return false;
                }
            }

            private void saveAndRegisterVideo() {
                Uri videoTable = Uri.parse("content://media/external/video/media");

                long timeMillis = System.currentTimeMillis();
                String newFileName = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss")
                        .format(new Date(timeMillis)) + ".mp4";
                String newFilePath = Environment.getExternalStorageDirectory()
                        + "/koolew/" + newFileName;

                FileUtil.copyFile(finalVideo, newFilePath);

                ContentValues values = new ContentValues(7);
                //values.put(MediaStore.Video.Media.TITLE, title);
                values.put(MediaStore.Video.Media.DISPLAY_NAME, newFileName);
                values.put(MediaStore.Video.Media.DATE_TAKEN, timeMillis);
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Video.Media.DATA, newFilePath);

                values.put(MediaStore.Video.Media.SIZE, new File(newFilePath).length());

                getContentResolver().insert(videoTable, values);
            }

            @Override
            protected void onPostExecute(Boolean isSuccess) {
                mUploadingDialog.dismiss();
                if (isSuccess) {
                    setResult(RESULT_UPLOADED);
                    finish();
                } else {
                    Toast.makeText(VideoEditActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(mTopicId, finalVideo, mVideoThumb);
    }

    private void shareVideoIfNeed() {
        ShareManager.ShareChanel shareChanel = null;
        if (mShareItemWechatMoments.isSelected()) {
            shareChanel = ShareManager.ShareChanel.WECHAT_MOMENTS;
        }
        else if (mShareItemWechatFriends.isSelected()) {
            shareChanel = ShareManager.ShareChanel.WECHAT_FRIENDS;
        }
        else if (mShareItemQQ.isSelected()) {
            shareChanel = ShareManager.ShareChanel.QZONE;
        }
        else if (mShareItemWeibo.isSelected()) {
            shareChanel = ShareManager.ShareChanel.WEIBO;
        }

        if (shareChanel != null) {
            ShareManager.ShareListener shareListener;
            if (ShareManager.ShareChanel.WEIBO.equals(shareChanel)) {
                shareListener = new WeiboShareListener();
            } else {
                shareListener = new ShareListener();
            }
            new ShareManager(this, shareListener).
                    shareVideoTo(shareChanel, mUploadedVideo, mTopicTitle);
        }
    }

    class ShareListener extends ShareManager.ShareListener {

        public ShareListener() {
            super(VideoEditActivity.this);
        }

        @Override
        protected void initMessages() {
            mSuccessMessage = mActivity.getString(R.string.share_success);
            mErrorMessage = mActivity.getString(R.string.share_failed);
            mCancelMessage = mActivity.getString(R.string.share_cancel);
        }

        @Override
        public void onCancel(Platform platform, int i) {
            super.onCancel(platform, i);
            shareToWeibo();
        }

        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            super.onComplete(platform, i, hashMap);
            shareToWeibo();
        }

        @Override
        public void onError(Platform platform, int i, Throwable throwable) {
            super.onError(platform, i, throwable);
            shareToWeibo();
        }

        private void shareToWeibo() {
            new ShareManager(VideoEditActivity.this, new WeiboShareListener()).
                    shareVideoTo(ShareManager.ShareChanel.WEIBO, mUploadedVideo, mTopicTitle);
        }
    }

    class WeiboShareListener extends ShareManager.ShareListener {

        public WeiboShareListener() {
            super(VideoEditActivity.this);
        }

        @Override
        protected void initMessages() {
            mSuccessMessage = mActivity.getString(R.string.share_success);
            mErrorMessage = mActivity.getString(R.string.share_failed);
            mCancelMessage = mActivity.getString(R.string.share_cancel);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_frame:
                onVideoPlayClick();
                break;
            case R.id.privacy_layout:
                onPrivacyLayoutClick();
                break;
            case R.id.bgm_switch:
                onBgmSwitchClick();
                break;
        }
    }

    public void onShareItemClick(View v) {
        ShareManager.ShareChanel shareChanel = null;
        switch (v.getId()) {
            case R.id.wechat_moments:
                shareChanel = ShareManager.ShareChanel.WECHAT_MOMENTS;
                break;
            case R.id.wechat_friends:
                shareChanel = ShareManager.ShareChanel.WECHAT_FRIENDS;
                break;
            case R.id.qq:
                shareChanel = ShareManager.ShareChanel.QZONE;
                break;
            case R.id.weibo:
                shareChanel = ShareManager.ShareChanel.WEIBO;
                break;
        }

        if (ShareManager.isAuthValid(shareChanel)) {
            onShareItemClickReal(v);
        }
        else {
            authorizeByChanel(shareChanel, v);
        }
    }

    private void authorizeByChanel(ShareManager.ShareChanel shareChanel, View originView) {
        Toast.makeText(this, R.string.request_authorize, Toast.LENGTH_SHORT).show();
        ShareManager.authorize(shareChanel, new AuthorizeListener(originView));
    }

    class AuthorizeListener extends ShareManager.ShareListener {
        private View originView;

        public AuthorizeListener(View originView) {
            super(VideoEditActivity.this);

            this.originView = originView;
        }

        @Override
        protected void initMessages() {
        }

        @Override
        public void onCancel(Platform platform, int i) {
            onAuthorizeFailed();
        }

        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            onAuthorizeSuccess();
        }

        @Override
        public void onError(Platform platform, int i, Throwable throwable) {
            onAuthorizeFailed();
        }

        private void onAuthorizeFailed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoEditActivity.this, R.string.authorize_failed,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void onAuthorizeSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoEditActivity.this, R.string.authorize_success,
                            Toast.LENGTH_SHORT).show();
                    onShareItemClickReal(originView);
                }
            });
        }
    }

    private void onShareItemClickReal(View v) {
        boolean originalState = v.isSelected();
        switch (v.getId()) {
            case R.id.wechat_moments:
            case R.id.wechat_friends:
            case R.id.qq:
                mShareItemWechatMoments.setSelected(false);
                mShareItemWechatFriends.setSelected(false);
                mShareItemQQ.setSelected(false);
                break;
            case R.id.weibo:
                break;
        }
        v.setSelected(!originalState);
    }

    private void onVideoPlayClick() {
        if (mBgmPlayer.isPlaying()) {
            mPlayImage.setVisibility(View.VISIBLE);
            mBgmPlayer.pausePlay();
        }
        else {
            mBgmPlayer.resumePlay();
            mPlayImage.setVisibility(View.INVISIBLE);
            if (mThumb.getVisibility() == View.VISIBLE) {
                mThumb.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mThumb.setVisibility(View.INVISIBLE);
                    }
                }, 80); // 80ms = 2 frame
            }
        }
    }

    private void onPrivacyLayoutClick() {
        Intent intent = new Intent(this, VideoPrivacyActivity.class);
        startActivityForResult(intent, REQUEST_VIDEO_PRIVACY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_VIDEO_PRIVACY:
                setAuthority(resultCode);
                break;
        }
    }

    private void setAuthority(int authority) {
        mAuthority = authority;
        if (authority == VideoPrivacyActivity.AUTHORITY_PUBLIC) {
            mAuthorityText.setText(R.string.public_visible);
        }
        else if (authority == VideoPrivacyActivity.AUTHORITY_FRIEND_ONLY) {
            mAuthorityText.setText(R.string.only_friend_visible);
        }
    }

    private void onBgmSwitchClick() {
        if (mAdapter.mSelectedPosition == NO_MUSIC_SELECTED) {
            return;
        }

        mBgmSwitch.setImageResource(R.mipmap.music_disable);
        mSelectedBgmPath = null;
        int lastSelectedMusic = mAdapter.mSelectedPosition;
        mAdapter.mSelectedPosition = NO_MUSIC_SELECTED;
        mAdapter.notifyItemChanged(lastSelectedMusic);
        try {
            mBgmPlayer.restart();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mBgmPlayer.setDisplay(mPlaySurface.getHolder());
            try {
                mBgmPlayer.mVideoPlayer.prepareAsync();
            }
            catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {}
    };


    class BgmPlayer implements MediaPlayer.OnSeekCompleteListener,
            MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
        private MediaPlayer mVideoPlayer;
        private MediaPlayer mAudioPlayer;

        private boolean isRestarting;

        private BgmPlayer() {
            mVideoPlayer = new MediaPlayer();
            try {
                mVideoPlayer.setDataSource(mConcatedVideo);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mVideoPlayer.setOnSeekCompleteListener(this);
            mVideoPlayer.setOnPreparedListener(this);
            mVideoPlayer.setOnCompletionListener(this);

            mAudioPlayer = new MediaPlayer();
            mAudioPlayer.setOnPreparedListener(this);
            mAudioPlayer.setOnSeekCompleteListener(this);

            isRestarting = false;
        }

        public boolean isPlaying() {
            return mVideoPlayer.isPlaying();
        }

        public void setDisplay(SurfaceHolder sh) {
            mVideoPlayer.setDisplay(sh);
        }

        public synchronized void restart() throws IOException {
            if (isRestarting) {
                return;
            }

            isRestarting = true;

            if (mVideoPlayer.isPlaying()) {
                mVideoPlayer.pause();
            }

            if (mSelectedBgmPath != null) {
                mVideoPlayer.setVolume(0.0f, 0.0f);
                mAudioPlayer.setVolume(1.0f, 1.0f);
                if (mAudioPlayer.isPlaying()) {
                    mAudioPlayer.stop();
                }
                mAudioPlayer.reset();
                mAudioPlayer.setDataSource(mSelectedBgmPath);
                mAudioPlayer.setLooping(true);
                mAudioPlayer.prepare();
            }
            else {
                mVideoPlayer.setVolume(1.0f, 1.0f);
                mAudioPlayer.setVolume(0.0f, 0.0f);
            }

            mVideoPlayer.seekTo(0);
        }

        public void pausePlay() {
            mVideoPlayer.pause();
            if (mSelectedBgmPath != null) {
                mAudioPlayer.pause();
            }
        }

        public void resumePlay() {
            mVideoPlayer.start();
            if (mSelectedBgmPath != null) {
                mAudioPlayer.start();
            }
        }

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            mVideoPlayer.start();
            mAudioPlayer.start();

            isRestarting = false;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            try {
                restart();
            } catch (IOException e) {
                throw new RuntimeException("IOException in onCompletion: " + e);
            }
        }
    }


    class MusicSelectAdapter extends RecyclerView.Adapter<MusicSelectAdapter.ViewHolder>
            implements View.OnClickListener {

        private int mSelectedPosition = NO_MUSIC_SELECTED;
        private BgmUtil.BgmStyleItem mBgms[] = BgmUtil.ALL_BGMS;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.music_select_item, parent, false);

            ViewHolder holder = new ViewHolder(itemView);
            holder.mImageView.setOnClickListener(this);

            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mImageView.setTag(new Integer(position));
            if (position == mSelectedPosition) {
                holder.mImageView.setImageResource(mBgms[position].getHoverIconResId());
                holder.mImageView.setAlpha(1.0f);
            }
            else {
                holder.mImageView.setImageResource(mBgms[position].getIconResId());
                holder.mImageView.setAlpha(0.8f);
            }
        }

        @Override
        public int getItemCount() {
            return mBgms.length;
        }

        @Override
        public void onClick(View v) {
            int lastSelectedPosition = mSelectedPosition;
            mSelectedPosition = (Integer) v.getTag();
            notifyItemChanged(lastSelectedPosition);
            notifyItemChanged(mSelectedPosition);
            mBgmSwitch.setImageResource(R.mipmap.music_enable);

            mSelectedBgmPath = mBgms[mSelectedPosition].getRandomBgm();
            if (mPlayImage.getVisibility() == View.VISIBLE) {
                mPlayImage.setVisibility(View.INVISIBLE);
            }
            try {
                mBgmPlayer.restart();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView mImageView;

            public ViewHolder(View itemView) {
                super(itemView);

                mImageView = (ImageView) itemView.findViewById(R.id.image);
            }
        }
    }
}
