package com.koolew.mars;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koolew.android.camerapreview.CameraInstance;
import com.koolew.android.camerapreview.CameraPreviewFragment;
import com.koolew.android.camerapreview.opengl.filter.FrameRenderer;
import com.koolew.android.camerapreview.opengl.filter.FrameRendererToneCurve;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.AbsLongVideoSwitch;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.PictureSelectUtil;
import com.koolew.android.utils.Utils;
import com.koolew.mars.videotools.RealTimeRgbaRecorderWithAutoAudio;
import com.koolew.mars.videotools.VideoTranscoder;
import com.koolew.mars.view.RecordButton;
import com.koolew.mars.view.RecordingSessionView;

import org.bytedeco.javacpp.opencv_core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import at.aau.itec.android.mediaplayer.MediaPlayer;

public class VideoShootActivity extends BaseActivity implements OnClickListener,
        RecordingSessionView.Listener, CameraPreviewFragment.FrameListener,
        ViewTreeObserver.OnGlobalLayoutListener {

    private final static String TAG = "koolew-VideoShootA";

    private static final int REQUEST_CODE_SELECT_VIDEO = 1;

    private static final int REQUEST_CODE_EDIT_VIDEO = 2;

    public static final String KEY_TOPIC_ID = "topic id";
    public static final String KEY_TOPIC_TITLE = "topic title";
    public static final String KEY_TAG_ID = "tag id";

    private static final int MODE_PREVIEW = 0;
    private static final int MODE_PLAYBACK = 1;

    private String mTopicId;
    private String mTopicTitle;
    private String mDefaultTag;

    private FrameLayout mPreviewFrame;
    private CameraPreviewFragment mCameraPreviewFragment;

    private ImageView mChangeCamera;
    private ImageView mImportVideo;
    private TextView mRecordComplete;

    private View mFilterSwitchBtn;
    private View mFilterSwitchBar;
    private View mFilterSwitchArrow;
    private View mFilterLayout;
    private RecyclerView mFilterRecycler;
    private FilterAdapter mFilterAdapter;

    private RecordButton mRecordButton;
    private TextView mCaptureText;

    private RecordingSessionView recordingSessionView;

    private boolean isRecording = false;

    private RealTimeRgbaRecorderWithAutoAudio mRecorder;

    // MODE_PREVIEW or MODE_PLAYBACK
    private int mCurrentSurfaceMode;
    private SurfaceView mPlaybackSurface;
    private MediaPlayer mMediaPlayer;
    private ImageView mVideoThumb;
    private ImageView mPlayImage;


    public VideoShootActivity() {
        CameraInstance.getInstance().setBackCameraAsDefault();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_video_shoot);

        mTopicId = getIntent().getStringExtra(KEY_TOPIC_ID);
        mTopicTitle = getIntent().getStringExtra(KEY_TOPIC_TITLE);
        mDefaultTag = getIntent().getStringExtra(KEY_TAG_ID);
        if (TextUtils.isEmpty(mTopicId) || TextUtils.isEmpty(mTopicTitle)) {
            throw new RuntimeException("Start VideoShootActivity must has KEY_TOPIC_ID and KEY_TOPIC_TITLE extras");
        }

        initMembers();
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        recordingSessionView.onActivityResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCurrentSurfaceMode == MODE_PREVIEW) {
            if (isRecording) {
                stopRecord();
            }
        }
        recordingSessionView.onActivityPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (AppProperty.getRecordVideoMaxLen() == AppProperty.VIDEO_MAX_LEN_18s) {
            AppProperty.setRecordVideoMaxLen(AppProperty.DEFAULT_VIDEO_MAX_LEN);
        }
    }

    @Override
    public void onBackPressed() {
        if (recordingSessionView.getVideoCount() == 0) {
            cancelRecord();
        }
        else {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_give_up_videos)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelRecord();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    private void cancelRecord() {
        setResult(RESULT_CANCELED);
        VideoShootActivity.super.onBackPressed();
    }

    private void initMembers() {
        mCurrentSurfaceMode = MODE_PREVIEW;
    }

    private void initViews() {
        mPreviewFrame = (FrameLayout) findViewById(R.id.preview_frame);
        mChangeCamera = (ImageView) findViewById(R.id.change_camera);

        findViewById(R.id.long_video_switch).setOnTouchListener(new LongVideoSwitch());

        mVideoThumb = (ImageView) findViewById(R.id.video_thumb);
        FrameLayout.LayoutParams vtlp = (FrameLayout.LayoutParams) mVideoThumb.getLayoutParams();
        vtlp.height = Utils.getScreenWidthPixel() / 4 * 3;
        mVideoThumb.setLayoutParams(vtlp);

        mPlayImage = (ImageView) findViewById(R.id.play);
        FrameLayout.LayoutParams pilp = (FrameLayout.LayoutParams) mPlayImage.getLayoutParams();
        pilp.topMargin = (int) ((Utils.getScreenWidthPixel() / 4 * 3 - Utils.dpToPixels(39)) / 2);
        mPlayImage.setLayoutParams(pilp);

        mPlaybackSurface = (SurfaceView) findViewById(R.id.playback_surface);
        FrameLayout.LayoutParams pvlp = (FrameLayout.LayoutParams) mPlaybackSurface.getLayoutParams();
        pvlp.height = Utils.getScreenWidthPixel() / 4 * 3;
        mPlaybackSurface.setLayoutParams(pvlp);
        mPlaybackSurface.getHolder().addCallback(mPlaybackSurfaceCallback);

        recordingSessionView = (RecordingSessionView) findViewById(R.id.recording_session_view);
        recordingSessionView.setListener(this);

        mImportVideo = (ImageView) findViewById(R.id.import_video);
        mImportVideo.setOnClickListener(this);

        mCaptureText = (TextView) findViewById(R.id.capture_text);

        mRecordComplete = (TextView) findViewById(R.id.record_complete);
        mRecordComplete.setOnClickListener(this);

        mFilterSwitchBtn = findViewById(R.id.filter_switch_btn);
        mFilterSwitchBtn.setOnClickListener(this);
        mFilterSwitchBar = findViewById(R.id.filter_switch_bar);
        mFilterSwitchBar.setOnClickListener(this);
        mFilterSwitchArrow = findViewById(R.id.filter_switch_arrow);
        mFilterLayout = findViewById(R.id.filter_layout);
        mFilterLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mFilterRecycler = (RecyclerView) findViewById(R.id.filter_recycler);
        mFilterRecycler.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        mFilterAdapter = new FilterAdapter();
        mFilterRecycler.setAdapter(mFilterAdapter);

        mRecordButton = (RecordButton) findViewById(R.id.image_record);
        mRecordButton.setOnClickListener(this);
        findViewById(R.id.close_layout).setOnClickListener(this);
        mChangeCamera.setOnClickListener(this);
        mPlayImage.setOnClickListener(this);

        mCameraPreviewFragment =
                (CameraPreviewFragment) getFragmentManager().findFragmentById(R.id.camera_preview);
        mCameraPreviewFragment.setWantedSize(AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);
    }

    @Override
    public void onSwitchToPreviewMode() {
        switchToPreviewMode();
    }

    @Override
    public void onSwitchToPlaybackMode() {
        switchToPlaybackMode();
    }

    @Override
    public void onNextStepEnable(boolean enable, String hint) {
        enableCompleteBtn(enable, hint);
    }

    @Override
    public void onPlayComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlayImage.setVisibility(View.VISIBLE);
            }
        });
    }

    private void switchCamera() {
        mCameraPreviewFragment.switchCamera();
    }

    @Override
    public void onNewFrame(opencv_core.IplImage frameImage, long timestamp) {
        if (isRecording) {
            mRecorder.putImage(frameImage, timestamp);
        }
    }

    private void switchToPlaybackMode() {
        if (mCurrentSurfaceMode == MODE_PLAYBACK) {
            return;
        }
        mCurrentSurfaceMode = MODE_PLAYBACK;

        mPlaybackSurface.setVisibility(View.VISIBLE);
        mChangeCamera.setVisibility(View.INVISIBLE);
        mVideoThumb.setVisibility(View.VISIBLE);
        mPlayImage.setVisibility(View.VISIBLE);
    }

    private void switchToPreviewMode() {
        if (mCurrentSurfaceMode == MODE_PREVIEW) {
            return;
        }
        mCurrentSurfaceMode = MODE_PREVIEW;

        recordingSessionView.switchToPreviewMode();

        mPlaybackSurface.setVisibility(View.INVISIBLE);
        mChangeCamera.setVisibility(View.VISIBLE);
        mVideoThumb.setVisibility(View.INVISIBLE);
        mPlayImage.setVisibility(View.INVISIBLE);
    }

    private SurfaceHolder.Callback mPlaybackSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDisplay(mPlaybackSurface.getHolder());
            recordingSessionView.setMediaPlayer(mMediaPlayer);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };


    private TimerTask mRecordMonitorTask;

    class RecordMonitorTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doStopRecord();
                }
            });
        }
    }

    private void startRecord() {
        mRecorder = new RealTimeRgbaRecorderWithAutoAudio(
                recordingSessionView.generateAVideoFilePath(),
                AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);
        recordingSessionView.startOneRecording(mRecorder);
        enableImportBtn(false);
        mRecordMonitorTask = new RecordMonitorTask();
        new Timer().schedule(mRecordMonitorTask,
                (long) (AppProperty.getRecordVideoMaxLen() * 1000 * 2));
        mRecorder.start();
        mCaptureText.setText(R.string.capturing);

        isRecording = true;
        mCameraPreviewFragment.setFrameListener(this);
    }

    private void stopRecord() {
        mRecordMonitorTask.cancel();

        doStopRecord();
    }

    private void doStopRecord() {
        if (isRecording) {
            mCameraPreviewFragment.clearFrameListener();
            isRecording = false;

            mCaptureText.setText(R.string.capture_video);
            recordingSessionView.postStopRecording();
            enableImportBtn(true);
        }
    }

    private boolean completeBtnEnable = false;
    private String completeBtnDisableHint = null;
    private void enableCompleteBtn(boolean enable, String hint) {
        completeBtnEnable = enable;
        completeBtnDisableHint = hint;
        Utils.setTextViewDrawableTop(mRecordComplete, enable ? R.mipmap.video_complete_enable
                : R.mipmap.video_complete_disable);
        mRecordComplete.setVisibility(View.VISIBLE);
        mFilterSwitchBtn.setVisibility(View.INVISIBLE);
        closeFilterLayout();
    }

    private void enableImportBtn(boolean enable) {
        mImportVideo.setEnabled(enable);
        if (enable) {
            mImportVideo.setImageResource(R.mipmap.video_import);
        }
        else {
            mImportVideo.setImageResource(R.mipmap.import_video_disable);
        }
    }


    // View click listeners
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_record:
                onRecordClick();
                break;
            case R.id.import_video:
                onImportVideo();
                break;
            case R.id.record_complete:
                onRecordCompleteClick();
                break;
            case R.id.close_layout:
                onCloseClick();
                break;
            case R.id.change_camera:
                onCameraChangeClick();
                break;
            case R.id.play:
                recordingSessionView.play();
                mPlayImage.setVisibility(View.INVISIBLE);
                break;
            case R.id.filter_switch_btn:
            case R.id.filter_switch_bar:
                switchFilterLayout();
                break;
        }
    }

    private void onImportVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); //ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
    }

    private void onRecordClick() {
        if (mCurrentSurfaceMode == MODE_PLAYBACK) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }

            switchToPreviewMode();
        }
        else {
            mRecordButton.doAnimation();
            if (!isRecording) {
                startRecord();
            } else {
                stopRecord();
            }
        }
    }

    private static final int VIDEO_PROCESS_SUCCESS = 0;
    private static final int VIDEO_PROCESS_CONCAT_VIDEO_FAILED = 1;
    private static final int VIDEO_PROCESS_GENERATE_THUMB_FAILED = 2;

    private void onRecordCompleteClick() {
        if (isRecording) {
            return;
        }

        if (!completeBtnEnable) {
            if (!TextUtils.isEmpty(completeBtnDisableHint)) {
                Toast.makeText(this, completeBtnDisableHint, Toast.LENGTH_LONG).show();
            }
            return;
        }

        new AsyncTask<Void, Void, Integer>() {
            private ProgressDialog mProgressDialog;

            @Override
            protected void onPreExecute() {
                mProgressDialog = DialogUtil.getGeneralProgressDialog(
                        VideoShootActivity.this, R.string.processing_video);
                mProgressDialog.show();
            }

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    recordingSessionView.concatVideo();
                } catch (Exception e) {
                    return VIDEO_PROCESS_CONCAT_VIDEO_FAILED;
                }
                try {
                    recordingSessionView.generateThumb();
                } catch (Exception e) {
                    return VIDEO_PROCESS_GENERATE_THUMB_FAILED;
                }
                return VIDEO_PROCESS_SUCCESS;
            }

            @Override
            protected void onPostExecute(Integer result) {
                mProgressDialog.dismiss();

                switch (result) {
                    case VIDEO_PROCESS_SUCCESS:
                        break;
                    case VIDEO_PROCESS_CONCAT_VIDEO_FAILED:
                        Toast.makeText(VideoShootActivity.this, R.string.concat_video_failed,
                                Toast.LENGTH_SHORT).show();
                        return;
                    case VIDEO_PROCESS_GENERATE_THUMB_FAILED:
                        Toast.makeText(VideoShootActivity.this, R.string.generate_thumb_failed,
                                Toast.LENGTH_SHORT).show();
                        return;
                }

                Intent intent = new Intent(VideoShootActivity.this, VideoEditActivity.class);
                intent.putExtra(VideoEditActivity.KEY_CONCATED_VIDEO,
                        recordingSessionView.getConcatedVideoName());
                intent.putExtra(VideoEditActivity.KEY_VIDEO_THUMB,
                        recordingSessionView.getThumbName());
                intent.putExtra(VideoEditActivity.KEY_TOPIC_ID, mTopicId);
                intent.putExtra(VideoEditActivity.KEY_TOPIC_TITLE, mTopicTitle);
                intent.putExtra(VideoEditActivity.KEY_TAG_ID, mDefaultTag);
                startActivityForResult(intent, REQUEST_CODE_EDIT_VIDEO);
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SELECT_VIDEO:
                if (resultCode == RESULT_OK) {
                    String filePath = PictureSelectUtil.getPath(this, data.getData());
                    new VideoTranscodeTask(filePath).execute();
                }
                break;
            case REQUEST_CODE_EDIT_VIDEO:
                onVideoEditResult(resultCode);
                break;
        }
    }

    private float filterLayoutOriginTop;

    @Override
    public void onGlobalLayout() {
        filterLayoutOriginTop = mFilterLayout.getTop();
    }

    private boolean isFilterLayoutOpened = true;

    private void switchFilterLayout() {
        if (isFilterLayoutOpened) {
            closeFilterLayout();
        }
        else {
            openFilterLayout();
        }
    }

    private void closeFilterLayout() {
        if (isFilterLayoutOpened) {
            getCloseFilterLayoutAnimation().start();
            isFilterLayoutOpened = false;
        }
    }

    private void openFilterLayout() {
        if (!isFilterLayoutOpened) {
            AnimatorSet openAnimator = getCloseFilterLayoutAnimation();
            openAnimator.setInterpolator(new Interpolator() {
                @Override
                public float getInterpolation(float input) {
                    return Math.abs(input - 1f);
                }
            });
            openAnimator.start();
            isFilterLayoutOpened = true;
        }
    }

    private static final long FILTER_LAYOUT_ANIMATOR_DURATION = 300;
    private AnimatorSet getCloseFilterLayoutAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator filterLayoutAnimator = ObjectAnimator
                .ofFloat(mFilterLayout, "y", filterLayoutOriginTop,
                        filterLayoutOriginTop + Utils.dpToPixels(80))
                .setDuration(FILTER_LAYOUT_ANIMATOR_DURATION);

        ObjectAnimator arrowAnimator = ObjectAnimator
                .ofFloat(mFilterSwitchArrow, "rotation", 270, 450)
                .setDuration(FILTER_LAYOUT_ANIMATOR_DURATION);

        animatorSet.playTogether(filterLayoutAnimator, arrowAnimator);

        return animatorSet;
    }

    class VideoTranscodeTask extends AsyncTask<Void, Void, Void> {
        private String filePath;
        private String transcodedFile;
        private Dialog progressDialog;
        public long videoLenMillis;
        public VideoTranscoder videoTranscoder;

        public VideoTranscodeTask(String filePath) {
            this.filePath = filePath;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = DialogUtil.getGeneralProgressDialog(
                    VideoShootActivity.this, R.string.transcoding);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            transcodedFile = recordingSessionView.generateAVideoFilePath();
            videoTranscoder = new VideoTranscoder(filePath, transcodedFile,
                    AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);
            videoTranscoder.start();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            videoLenMillis = videoTranscoder.getVideoLenMillis();
            progressDialog.dismiss();
            recordingSessionView.addOneItem(transcodedFile, videoLenMillis);
        }
    }

    private void onVideoEditResult(int resultCode) {
        switch (resultCode) {
            case RESULT_CANCELED:
                break;
            case VideoEditActivity.RESULT_UPLOADED:
                // TODO: clear cache videos
                setResult(RESULT_OK);
                finish();
                break;
            case VideoEditActivity.RESULT_BACKGROUND_UPLOAD:
                break;
        }
    }

    private void onCloseClick() {
        onBackPressed();
    }

    private void onCameraChangeClick() {
        switchCamera();
    }

    public static void startThisActivity(Context context, BaseTopicInfo topicInfo) {
        startThisActivity(context, topicInfo.getTopicId(), topicInfo.getTitle(), topicInfo.getTagId());
    }

    public static void startThisActivity(Context context, String topicId, String topicTitle,
                                         String tagId) {
        Intent intent = new Intent(context, VideoShootActivity.class);
        intent.putExtra(VideoShootActivity.KEY_TOPIC_ID, topicId);
        intent.putExtra(VideoShootActivity.KEY_TOPIC_TITLE, topicTitle);
        intent.putExtra(VideoShootActivity.KEY_TAG_ID, tagId);
        context.startActivity(intent);
    }

    class LongVideoSwitch extends AbsLongVideoSwitch {
        @Override
        protected void onPasswordHandle(String password) {
            if (password.equals(LONG_VIDEO_18S)) {
                AppProperty.setRecordVideoMaxLen(AppProperty.VIDEO_MAX_LEN_18s);
                notifyUser((int) AppProperty.VIDEO_MAX_LEN_18s);
                recordingSessionView.invalidateProgressView();
            }
            else if (MyAccountInfo.getVip() == 1 ||
                    MyAccountInfo.getUid().equals("55657de205f7080cd3000021")) {
                if (password.equals(LONG_VIDEO_60S)) {
                    AppProperty.setRecordVideoMaxLen(AppProperty.VIDEO_MAX_LEN_60s);
                    notifyUser((int) AppProperty.VIDEO_MAX_LEN_60s);
                    recordingSessionView.invalidateProgressView();
                }
            }
        }

        private void notifyUser(int newVideoLen) {
            new AlertDialog.Builder(VideoShootActivity.this)
                    .setMessage(getString(R.string.long_video_enabled, newVideoLen))
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
    }


    private class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder> {

        private int selectedPosition = 0;

        @Override
        public FilterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FilterHolder(LayoutInflater.from(VideoShootActivity.this)
                    .inflate(R.layout.filter_item, parent, false));
        }

        @Override
        public void onBindViewHolder(FilterHolder holder, int position) {
            holder.filter.setText(FrameRendererToneCurve.CURVE_FILTERS[position].briefNameResId);
            holder.filter.setBackgroundResource(FrameRendererToneCurve.CURVE_FILTERS[position].thumbResId);
            if (position == selectedPosition) {
                holder.filter.setAlpha(1.0f);
                holder.foreground.setVisibility(View.VISIBLE);
            }
            else {
                holder.filter.setAlpha(0.5f);
                holder.foreground.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return FrameRendererToneCurve.CURVE_FILTERS.length;
        }

        private void refreshSelectPosition(int newPosition) {
            if (selectedPosition == newPosition) {
                return;
            }

            int oldPosition = selectedPosition;
            selectedPosition = newPosition;
            notifyItemChangedWithPositionCheck(oldPosition);
            notifyItemChangedWithPositionCheck(newPosition);
        }

        private void notifyItemChangedWithPositionCheck(int position) {
            if (position >= 0 && position < getItemCount()) {
                notifyItemChanged(position);
            }
        }

        class FilterHolder extends RecyclerView.ViewHolder implements OnClickListener {
            private TextView filter;
            private View foreground;

            public FilterHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                filter = (TextView) itemView.findViewById(R.id.filter);
                foreground = itemView.findViewById(R.id.foreground);
            }

            @Override
            public void onClick(View v) {
                if (selectedPosition != getAdapterPosition()) {
                    refreshSelectPosition(getAdapterPosition());
                    mCameraPreviewFragment.setFrameRenderer(new CurveFilterCreator(
                            FrameRendererToneCurve.CURVE_FILTERS[getAdapterPosition()]));
                }
            }
        }

        class CurveFilterCreator implements CameraPreviewFragment.RendererCreator {
            private FrameRendererToneCurve.CurveFilter curveFilter;

            public CurveFilterCreator(FrameRendererToneCurve.CurveFilter curveFilter) {
                this.curveFilter = curveFilter;
            }

            @Override
            public FrameRenderer createRenderer() {
                FrameRendererToneCurve renderer = new FrameRendererToneCurve();
                try {
                    renderer.setFromCurveFileInputStream(new BufferedInputStream(
                            getResources().getAssets().open(curveFilter.getAssertFileName())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (renderer.init(true)) {
                    return renderer;
                }
                else {
                    return null;
                }
            }
        }
    }
}