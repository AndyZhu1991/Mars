package com.koolew.mars;

import android.content.Intent;
import android.media.MediaPlayer;
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

import com.koolew.mars.adapters.TagAdapter;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.Tag;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.BgmUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.TitleBarView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;


public class VideoEditActivity extends BaseActivity implements View.OnClickListener,
        TitleBarView.OnRightLayoutClickListener, TagAdapter.OnSelectedTagChangedListener {

    public static final int REQUEST_CODE_UPLOAD_VIDEO = 1;

    public static final int RESULT_UPLOADED = RESULT_FIRST_USER + 1;
    public static final int RESULT_BACKGROUND_UPLOAD = RESULT_FIRST_USER + 2;

    public static final String KEY_CONCATED_VIDEO = "concated video";
    public static final String KEY_VIDEO_THUMB = "video thumb";
    public static final String KEY_TOPIC_ID = "topic id";
    public static final String KEY_TOPIC_TITLE = "topic title";
    public static final String KEY_IS_MOVIE = "is movie";
    public static final String KEY_FROM = "from";
    public static final String KEY_TAG_ID = "tag id";
    public static final String KEY_MOVIE_NAME = VideoShareActivity.KEY_MOVIE_NAME;
    public static final String KEY_CHARACTER_NAME = VideoShareActivity.KEY_CHARACTER_NAME;

    private static final int NO_MUSIC_SELECTED = -1;

    private String mConcatedVideo;
    private String mVideoThumb;
    private String mTopicId;
    private String mTopicTitle;
    private boolean isMovie;
    private String mFrom;
    private String mDefaultTagId;
    private String mMovieName;
    private String mCharacterName;
    private BaseVideoInfo mUploadedVideo;

    private String mSelectedBgmPath;

    private TitleBarView mTitleBar;
    private FrameLayout mVideoFrame;
    private SurfaceView mPlaySurface;
    private ImageView mThumb;
    private ImageView mPlayImage;
    private ImageView mBgmSwitch;

    private RecyclerView mRecyclerView;
    private MusicSelectAdapter mAdapter;

    private RecyclerView mTagRecycler;
    private Tag mTag;

    private BgmPlayer mBgmPlayer;

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
        mDefaultTagId = getIntent().getStringExtra(KEY_TAG_ID);
        mMovieName = getIntent().getStringExtra(KEY_MOVIE_NAME);
        mCharacterName = getIntent().getStringExtra(KEY_CHARACTER_NAME);

        initMembers();

        initViews();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .build();
        ImageLoader.getInstance().displayImage("file://" + mVideoThumb, mThumb, options);
    }

    private void initMembers() {
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

        if (isMovie) {
            findViewById(R.id.just_for_video).setVisibility(View.GONE);
        }

        if (!isMovie) {
            mBgmSwitch = (ImageView) findViewById(R.id.bgm_switch);
            mBgmSwitch.setOnClickListener(this);

            mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(layoutManager);
            mAdapter = new MusicSelectAdapter();
            mRecyclerView.setAdapter(mAdapter);

            mTagRecycler = (RecyclerView) findViewById(R.id.tag_recycler);
            mTagRecycler.setLayoutManager(new LinearLayoutManager(
                    this, LinearLayoutManager.HORIZONTAL, false));
            TagAdapter tagAdapter = new TagAdapter(this);
            tagAdapter.initTags(TagAdapter.TAGS_VIDEO, false);
            tagAdapter.setTextColorSelected(0xFF333333);
            tagAdapter.setTagChangedListener(this);
            tagAdapter.setSelectedTag(mDefaultTagId);
            mTag = tagAdapter.getSelectedTag();
            mTagRecycler.setAdapter(tagAdapter);
        }
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_frame:
                onVideoPlayClick();
                break;
            case R.id.bgm_switch:
                onBgmSwitchClick();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_UPLOAD_VIDEO:
                onVideoEditResult(resultCode);
                break;
        }
    }

    private void onVideoEditResult(int resultCode) {
        switch (resultCode) {
            case RESULT_CANCELED:
                break;
            case VideoShareActivity.RESULT_UPLOADED:
                setResult(RESULT_UPLOADED);
                finish();
                break;
            case VideoShareActivity.RESULT_BACKGROUND_UPLOAD:
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
            if (mThumb.getVisibility() == View.VISIBLE) {
                new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                Thread.sleep(40);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (mBgmPlayer.mVideoPlayer.getCurrentPosition() > 80) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mThumb.setVisibility(View.INVISIBLE);
                                    }
                                });
                                break;
                            }
                        }
                    }
                }.start();
            }
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

    @Override
    public void onRightLayoutClick() {
        Intent intent = new Intent(this, VideoShareActivity.class);
        intent.putExtra(VideoShareActivity.KEY_CONCATED_VIDEO, mConcatedVideo);
        intent.putExtra(VideoShareActivity.KEY_FROM, mFrom);
        intent.putExtra(VideoShareActivity.KEY_IS_MOVIE, isMovie);
        intent.putExtra(VideoShareActivity.KEY_TOPIC_ID, mTopicId);
        intent.putExtra(VideoShareActivity.KEY_TOPIC_TITLE, mTopicTitle);
        intent.putExtra(VideoShareActivity.KEY_VIDEO_THUMB, mVideoThumb);
        intent.putExtra(VideoShareActivity.KEY_MOVIE_NAME, mMovieName);
        intent.putExtra(VideoShareActivity.KEY_CHARACTER_NAME, mCharacterName);
        intent.putExtra(VideoShareActivity.KEY_SELECTED_BGM, mSelectedBgmPath);
        if (mTag != null) {
            intent.putExtra(VideoShareActivity.KEY_TAG_ID, mTag.getId());
        }
        startActivityForResult(intent, REQUEST_CODE_UPLOAD_VIDEO);
    }

    @Override
    public void onSelectedTagChanged(Tag tag) {
        mTag = tag;
    }


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
