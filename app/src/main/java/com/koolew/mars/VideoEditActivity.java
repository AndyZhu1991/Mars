package com.koolew.mars;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.koolew.mars.qiniu.UploadHelper;
import com.koolew.mars.utils.BgmUtil;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.Mp4ParserUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.TitleBarView;

import java.io.File;
import java.io.IOException;


public class VideoEditActivity extends Activity
        implements TitleBarView.OnRightLayoutClickListener, View.OnClickListener{

    public static final int REQUEST_VIDEO_PRIVACY = 1;

    public static final int RESULT_UPLOADED = RESULT_FIRST_USER + 1;
    public static final int RESULT_BACKGROUND_UPLOAD = RESULT_FIRST_USER + 2;

    public static final String KEY_CONCATED_VIDEO = "concated video";
    public static final String KEY_VIDEO_THUMB = "video thumb";
    public static final String KEY_TOPIC_ID = "topic id";

    private static final int NO_MUSIC_SELECTED = -1;

    private String mConcatedVideo;
    private String mVideoThumb;
    private String mTopicId;

    private String mSelectedBgmPath;

    private int mAuthority;

    private TitleBarView mTitleBar;
    private FrameLayout mVideoFrame;
    private SurfaceView mPlaySurface;
    private ImageView mPlayImage;
    private RelativeLayout mPrivacyLayout;
    private TextView mAuthorityText;
    private ImageView mBgmSwitch;

    private RecyclerView mRecyclerView;
    private MusicSelectAdapter mAdapter;

    private BgmPlayer mBgmPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);

        mConcatedVideo = getIntent().getStringExtra(KEY_CONCATED_VIDEO);
        mVideoThumb = getIntent().getStringExtra(KEY_VIDEO_THUMB);
        mTopicId = getIntent().getStringExtra(KEY_TOPIC_ID);

        initMembers();

        initViews();
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

        mPlayImage = (ImageView) findViewById(R.id.play_image);
        mPrivacyLayout = (RelativeLayout) findViewById(R.id.privacy_layout);
        mPrivacyLayout.setOnClickListener(this);
        mAuthorityText = (TextView) findViewById(R.id.authority_text);

        mBgmSwitch = (ImageView) findViewById(R.id.bgm_switch);
        mBgmSwitch.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new MusicSelectAdapter();
        mRecyclerView.setAdapter(mAdapter);
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
        String finalVideo;
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
                if (UploadHelper.uploadVideo(params[0], params[1], params[2], mAuthority)
                        == UploadHelper.RESULT_SUCCESS) {
                    return true;
                } else {
                    return false;
                }
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

    private void onVideoPlayClick() {
        if (mBgmPlayer.isPlaying()) {
            mPlayImage.setVisibility(View.VISIBLE);
            mBgmPlayer.pausePlay();
        }
        else {
            mBgmPlayer.resumePlay();
            mPlayImage.setVisibility(View.INVISIBLE);
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
            if (mp == mVideoPlayer) {
                mp.start();
                new Thread() { // This thread will show the 1st frame.
                    @Override
                    public void run() {
                        try {
                            for (int i = 0; i < 25; i++) {
                                if (mVideoPlayer.getCurrentPosition() > 0) {
                                    mVideoPlayer.pause();
                                    break;
                                }
                                sleep(40);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
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
