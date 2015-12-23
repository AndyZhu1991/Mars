package com.koolew.mars;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koolew.mars.camerautils.CameraInstance;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.DeviceDetective;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.AbsLongVideoSwitch;
import com.koolew.mars.utils.PictureSelectUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.videotools.RealTimeRgbaRecorderWithAutoAudio;
import com.koolew.mars.videotools.VideoTranscoder;
import com.koolew.mars.view.RecordButton;
import com.koolew.mars.view.RecordingSessionView;

import org.bytedeco.javacpp.opencv_core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import at.aau.itec.android.mediaplayer.MediaPlayer;

public class VideoShootActivity extends BaseActivity implements OnClickListener,
        RecordingSessionView.Listener, CameraPreviewFragment.FrameListener {

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
    private ImageView mRecordComplete;

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
        vtlp.height = Utils.getScreenWidthPixel(this) / 4 * 3;
        mVideoThumb.setLayoutParams(vtlp);

        mPlayImage = (ImageView) findViewById(R.id.play);
        FrameLayout.LayoutParams pilp = (FrameLayout.LayoutParams) mPlayImage.getLayoutParams();
        pilp.topMargin = (int) ((Utils.getScreenWidthPixel(this) / 4 * 3 - Utils.dpToPixels(this, 39)) / 2);
        mPlayImage.setLayoutParams(pilp);

        mPlaybackSurface = (SurfaceView) findViewById(R.id.playback_surface);
        FrameLayout.LayoutParams pvlp = (FrameLayout.LayoutParams) mPlaybackSurface.getLayoutParams();
        pvlp.height = Utils.getScreenWidthPixel(this) / 4 * 3;
        mPlaybackSurface.setLayoutParams(pvlp);
        mPlaybackSurface.getHolder().addCallback(mPlaybackSurfaceCallback);

        recordingSessionView = (RecordingSessionView) findViewById(R.id.recording_session_view);
        recordingSessionView.setListener(this);

        mImportVideo = (ImageView) findViewById(R.id.import_video);
        mImportVideo.setOnClickListener(this);

        mCaptureText = (TextView) findViewById(R.id.capture_text);

        mRecordComplete = (ImageView) findViewById(R.id.record_complete);
        mRecordComplete.setOnClickListener(this);

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
        mRecordComplete.setImageResource(enable ? R.mipmap.video_complete_enable
                : R.mipmap.video_complete_disable);
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
}