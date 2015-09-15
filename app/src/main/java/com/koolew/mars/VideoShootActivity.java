package com.koolew.mars;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.koolew.mars.camerautils.CameraSurfacePreview;
import com.koolew.mars.ffmpeg.RealTimeYUV420RecorderWithAutoAudio;
import com.koolew.mars.media.MediaEncoder;
import com.koolew.mars.media.MediaMuxerWrapper;
import com.koolew.mars.media.MediaVideoEncoder;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.MathUtil;
import com.koolew.mars.utils.RawImageUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.RecordingSessionView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import at.aau.itec.android.mediaplayer.MediaPlayer;

public class VideoShootActivity extends BaseActivity implements OnClickListener,
        RecordingSessionView.Listener {

    private final static String TAG = "koolew-VideoShootA";

    private static final int VIDEO_EDIT_REQUEST = 1;

    public static final String KEY_TOPIC_ID = "topic id";

    private static final int MODE_PREVIEW = 0;
    private static final int MODE_PLAYBACK = 1;

    private String mTopicId;

    private FrameLayout mPreviewFrame;
    //private CameraSurfacePreview mPreview;
    private CameraSurfacePreview mPreview;
    private Camera mCamera;
    private int mCurrentCamera;
    private int previewWidth;
    private int previewHeight;

    private ImageView mChangeCamera;
    private ImageView mRecordComplete;

    private RecordingSessionView recordingSessionView;

    private boolean isRecording = false;
    private boolean isEncoding = false;

    private byte[] YUV420RotateBuffer;
    private byte[] YUV420CropBuffer;

    private RealTimeYUV420RecorderWithAutoAudio mRecorder;
    private MediaMuxerWrapper mMuxer;
    private MediaVideoEncoder mVideoEncoder;

    // MODE_PREVIEW or MODE_PLAYBACK
    private int mCurrentSurfaceMode;
    private int mCurrentPlayedIndex;
    private SurfaceView mPlaybackSurface;
    private MediaPlayer mMediaPlayer;
    private ImageView mVideoThumb;
    private ImageView mPlayImage;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_video_shoot);

        mTopicId = getIntent().getStringExtra(KEY_TOPIC_ID);
        if (mTopicId == null || mTopicId.length() == 0) {
            throw new RuntimeException("Start VideoShootActivity must has a KEY_TOPIC_ID extra");
        }

        initMembers();
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mCurrentSurfaceMode == MODE_PREVIEW) {
            new Thread() {
                @Override
                public void run() {
                    doOpenCamera(mCurrentCamera);
                }
            }.start();
        }
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
            releaseCamera();
        }
        recordingSessionView.onActivityPause();
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
        mCurrentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
        mCurrentSurfaceMode = MODE_PREVIEW;
    }

    private void initViews() {
        mPreviewFrame = (FrameLayout) findViewById(R.id.preview_frame);
        mChangeCamera = (ImageView) findViewById(R.id.change_camera);

        // Create our Preview view and set it as the content of our activity.
        mPreview = (CameraSurfacePreview) findViewById(R.id.camera_preview);

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

        mRecordComplete = (ImageView) findViewById(R.id.record_complete);
        mRecordComplete.setOnClickListener(this);

        findViewById(R.id.image_record).setOnClickListener(this);
        findViewById(R.id.close_layout).setOnClickListener(this);
        mChangeCamera.setOnClickListener(this);
        mPlayImage.setOnClickListener(this);
    }


    private void initLayoutParams() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mPreview.getLayoutParams();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        lp.height = width * previewWidth / previewHeight;
        int visiblePreviewHeight = width * AppProperty.RECORD_VIDEO_HEIGHT / AppProperty.RECORD_VIDEO_WIDTH;
        if (mCurrentCamera == Camera.CameraInfo.CAMERA_FACING_BACK) {
            lp.topMargin = 0 - (lp.height - visiblePreviewHeight) / 2;
        }
        else {
            lp.topMargin = 0;
        }
        Log.d(TAG, "set preview layout params. topMargin: " + lp.topMargin + ", height: " + lp.height);
        mPreview.setLayoutParams(lp);

        LinearLayout bottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bottomLayout.getLayoutParams();
        params.topMargin = visiblePreviewHeight;
        bottomLayout.setLayoutParams(params);
    }


    private void initCamera() {
        if (this.mCamera != null) {
            Camera.Parameters params = this.mCamera.getParameters();
            params.setPreviewFormat(ImageFormat.NV21);
            setBestCameraPreviewFpsRange(params);
            params.setFlashMode("off");
            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            params.setPreviewSize(previewWidth, previewHeight);
            this.mCamera.setDisplayOrientation(90);
            params.setRecordingHint(true);
            mCamera.addCallbackBuffer(new byte[previewWidth * previewHeight * 3 / 2]);
            mCamera.setPreviewCallbackWithBuffer(new MyPreviewCallback());
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains("continuous-video") && shouldAutoFocus()) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            this.mCamera.setParameters(params);
            this.mCamera.startPreview();
        }
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
        if (mCurrentCamera == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCurrentCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        else {
            mCurrentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        releaseCamera();
        doOpenCamera(mCurrentCamera);
    }

    /**
     *
     * @param which Camera.CameraInfo.CAMERA_FACING_BACK for back camera
     *              Camera.CameraInfo.CAMERA_FACING_FRONT for front camera
     */
    private void doOpenCamera(int which) {
        Log.i(TAG, "Camera open....");
        int numCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == which) {
                try {
                    mCamera = Camera.open(i);
                }
                catch (RuntimeException re) {
                    showCameraFailDialog();
                    return;
                }
                break;
            }
        }
        if (mCamera == null) {
            Log.d(TAG, "No front-facing camera found; opening default");
            mCamera = Camera.open();    // opens first back-facing camera
        }
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
        Log.i(TAG, "Camera open over....");
        cameraHasOpened();
    }

    private void showCameraFailDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(VideoShootActivity.this)
                        .setMessage(R.string.fail_to_open_camera)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                VideoShootActivity.this.onBackPressed();
                            }
                        })
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
    }

    private void cameraHasOpened() {
        initBestCameraPreviewSize(mCamera.getParameters());
        YUV420RotateBuffer = new byte[previewWidth * previewHeight * 3 / 2];
        YUV420CropBuffer = new byte[AppProperty.RECORD_VIDEO_WIDTH * AppProperty.RECORD_VIDEO_HEIGHT * 3 / 2];

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initLayoutParams();
            }
        });
        SurfaceHolder holder = mPreview.getSurfaceHolder();
        doStartPreview(holder);
    }

    private void doStartPreview(SurfaceHolder holder) {
        Log.i(TAG, "doStartPreview...");

        try {
            this.mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initCamera();
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private void initBestCameraPreviewSize(Camera.Parameters params) {
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        int count = sizes.size();
        int minSizeDiff = 0x7FFFFFFF;
        int bestSizeIndex = -1;
        for (int i = 0; i < count; i++) {
            Camera.Size size = sizes.get(i);
            if (size.width == 480 && size.height == 480) {
                // 米3在480*480的预览下会显示不正常
                continue;
            }
            Log.d(TAG, "width:" + size.width + ", height:" + size.height);
            if (Math.abs(size.height - AppProperty.RECORD_VIDEO_WIDTH) < minSizeDiff) {
                minSizeDiff = Math.abs(size.height - AppProperty.RECORD_VIDEO_WIDTH);
                bestSizeIndex = i;
                if (minSizeDiff == 0) {
                    break;
                }
            }
        }

        if (bestSizeIndex != -1) {
            Camera.Size bestSize = sizes.get(bestSizeIndex);
            previewHeight = bestSize.height;
            previewWidth = bestSize.width;
        }
    }

    private void setBestCameraPreviewFpsRange(Camera.Parameters params) {
        List<int[]> previewFpsRanges = params.getSupportedPreviewFpsRange();

        int count = previewFpsRanges.size();
        int minFpsDiff = Integer.MAX_VALUE;
        int bestRangeIndex = -1;
        int appVideoFps = AppProperty.RECORD_VIDEO_FPS * 1000; // getSupportedPreviewFpsRange return 1000-time values
        for (int i = 0; i < count; i++) {
            int[] fpsRange = previewFpsRanges.get(i);
            int fpsDiff = Math.abs(fpsRange[0] - appVideoFps) + Math.abs(fpsRange[1] - appVideoFps);
            if (fpsDiff < minFpsDiff) {
                minFpsDiff = fpsDiff;
                bestRangeIndex = i;
                if (minFpsDiff == 0) {
                    break;
                }
            }
        }

        if (bestRangeIndex == -1) {
            throw new RuntimeException("No camera preview fps range!");
        }
        int[] bestFpsRange = previewFpsRanges.get(bestRangeIndex);
        params.setPreviewFpsRange(bestFpsRange[0], bestFpsRange[1]);
    }

    class MyPreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (isRecording){//mVideoEncoder != null && mVideoEncoder.isEncoding == true) {
                if (mCurrentCamera == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    RawImageUtil.rotateYUV420Degree90(data, YUV420RotateBuffer,
                            previewWidth, previewHeight);
                    RawImageUtil.cropYUV420VerticalCenter(YUV420RotateBuffer, YUV420CropBuffer,
                            previewHeight, previewWidth, AppProperty.RECORD_VIDEO_HEIGHT);
                }
                else {
                    RawImageUtil.rotateYUV420Degree270(data, YUV420RotateBuffer,
                            previewWidth, previewHeight);
                    RawImageUtil.cropYUV420Vertical(YUV420RotateBuffer, YUV420CropBuffer,
                            previewHeight, previewWidth, 0, AppProperty.RECORD_VIDEO_HEIGHT);
                }
                long start = System.currentTimeMillis();
                mRecorder.put(YUV420CropBuffer, System.currentTimeMillis() * 1000);
                Log.d("stdzhu", "camera preview: " + (System.currentTimeMillis() - start));
            }

            camera.addCallbackBuffer(data);
        }
    }
//
//    class MyPreviewCallback implements Camera.PreviewCallback {
//        @Override
//        public void onPreviewFrame(byte[] data, Camera camera) {
//            if (isEncoding){//mVideoEncoder != null && mVideoEncoder.isEncoding == true) {
//                MediaVideoEncoder.YUV420SPFrame frame = mVideoEncoder.obtainFrame();
//                frame.frameNanoTime = System.nanoTime();
//                if (mCurrentCamera == Camera.CameraInfo.CAMERA_FACING_BACK) {
//                    RawImageUtil.rotateYUV420Degree90(data, YUV420RotateBuffer,
//                            previewWidth, previewHeight);
//                    RawImageUtil.cropYUV420VerticalCenter(YUV420RotateBuffer, YUV420CropBuffer,
//                            previewHeight, previewWidth, AppProperty.RECORD_VIDEO_HEIGHT);
//                }
//                else {
//                    RawImageUtil.rotateYUV420Degree270(data, YUV420RotateBuffer,
//                            previewWidth, previewHeight);
//                    RawImageUtil.cropYUV420Vertical(YUV420RotateBuffer, YUV420CropBuffer,
//                            previewHeight, previewWidth, 0, AppProperty.RECORD_VIDEO_HEIGHT);
//                }
//                RawImageUtil.NV21toI420SemiPlanar(YUV420CropBuffer, frame.data,
//                        AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);
//                mVideoEncoder.putYUV420SPFrame(frame);
//            }
//
//            camera.addCallbackBuffer(data);
//        }
//    }


    private void switchToPlaybackMode() {
        if (mCurrentSurfaceMode == MODE_PLAYBACK) {
            return;
        }
        mCurrentSurfaceMode = MODE_PLAYBACK;

        releaseCamera();

        mPreview.setVisibility(View.INVISIBLE);
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
        mPreview.setVisibility(View.VISIBLE);
        mChangeCamera.setVisibility(View.VISIBLE);
        mVideoThumb.setVisibility(View.INVISIBLE);
        mPlayImage.setVisibility(View.INVISIBLE);

        doOpenCamera(mCurrentCamera);
    }

//    private void setSelectedVideo(int position) {
//        ImageLoader.getInstance().displayImage(
//                "file://" + mRecordingSession.get(position).getVideoPath(),
//                mVideoThumb, null, null, null);
//    }
//
//    private void play() {
//        mCurrentPlayedIndex = 0;
//        try {
//            mMediaPlayer.setDataSource(mRecordingSession.get(0).getVideoPath());
//            mMediaPlayer.prepareAsync();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

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
        long start = System.currentTimeMillis();
        mRecorder = new RealTimeYUV420RecorderWithAutoAudio(
                recordingSessionView.generateAVideoFilePath(),
                AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);
        Log.d("stdzhu", "new recorder: " + (System.currentTimeMillis() - start));
        recordingSessionView.startOneRecording(mRecorder);
        mRecordMonitorTask = new RecordMonitorTask();
        new Timer().schedule(mRecordMonitorTask,
                (long) (AppProperty.RECORD_VIDEO_MAX_LEN * 1000));
        start = System.currentTimeMillis();
        mRecorder.start();

        isRecording = true;
        Log.d("stdzhu", "startRecord: " + (System.currentTimeMillis() - start));
    }
//
//    private void startRecord() {
//        try {
//            mCurrentRecodingVideo = mRecordingSession.new VideoPieceItem();
//            mVideosProgressView.start();
//            mRecordMonitorTask = new RecordMonitorTask();
//            new Timer().schedule(mRecordMonitorTask,
//                    (long) (AppProperty.RECORD_VIDEO_MAX_LEN * 1000
//                            - mRecordingSession.getTotalVideoLength()));
//            mMuxer = new MediaMuxerWrapper(getCurrentRecordingFile());
//            mVideoEncoder = new MediaVideoEncoder(mMuxer, mMediaEncoderListener,
//                    AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);
//            new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
//            mMuxer.prepare();
//            mMuxer.startRecording();
//
//            isRecording = true;
//        } catch (IOException e) {
//            throw new RuntimeException("IOException: " + e);
//        }
//    }

    private void stopRecord() {
        mRecordMonitorTask.cancel();

        doStopRecord();
    }

    private void doStopRecord() {
        if (isRecording) {
            isRecording = false;

            recordingSessionView.postStopRecording();
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
//
//    private void doStopRecord() {
//        if (isRecording) {
//            if (mMuxer != null) {
//                mMuxer.stopRecording();
//                mMuxer = null;
//                // you should not wait here
//            }
//            mCurrentRecodingVideo.finishRecord();
//            mRecordingSession.add(mCurrentRecodingVideo);
//            mVideosProgressView.finish();
//            mCurrentRecodingVideo = null;
//            mAdapter.notifyItemInserted(mRecordingSession.getVideoCount() - 1);
//
//            isRecording = false;
//
//            if (mRecordingSession.getVideoCount() == 1) {
//                mRecordComplete.setImageResource(R.mipmap.video_complete_enable);
//            }
//        }
//    }

    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener =
            new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (encoder == mVideoEncoder) {
                isEncoding = true;
            }
        }
        @Override
        public void onStopped(final MediaEncoder encoder) {
            if (encoder == mVideoEncoder) {
                isEncoding = false;
            }
        }
    };



    // View click listeners
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_record:
                onRecordClick();
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

    private void onRecordClick() {
        if (mCurrentSurfaceMode == MODE_PLAYBACK) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }

            switchToPreviewMode();
        }
        else {
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
                startActivityForResult(intent, VIDEO_EDIT_REQUEST);
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case VIDEO_EDIT_REQUEST:
                onVideoEditResult(resultCode);
                break;
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

    // 已知红米1和红米NOTE1在升级MIUI V6之后，打开自动对焦会在预览界面卡住
    private boolean shouldAutoFocus() {
        String model = Build.MODEL;
        String miuiVersion = getMiuiVersionName();
        if ((model.startsWith("HM 1") || model.startsWith("HM NOTE 1")) &&
                (miuiVersion.equals("V6") || miuiVersion.equals("V7"))) {
            return false;
        }
        else {
            return true;
        }
    }

    public static String getMiuiVersionName() {
        String line;
        BufferedReader reader = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name" );
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = reader.readLine();
            return line;
        } catch (IOException e) {
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "UNKNOWN";
    }
}