package com.koolew.mars;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.koolew.mars.camerautils.CameraSurfacePreview;
import com.koolew.mars.camerautils.CameraWrapper;
import com.koolew.mars.media.MediaAudioEncoder;
import com.koolew.mars.media.MediaEncoder;
import com.koolew.mars.media.MediaMuxerWrapper;
import com.koolew.mars.media.MediaVideoEncoder;
import com.koolew.mars.media.YUV420VideoEncoder;
import com.koolew.mars.utils.YUV420Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VideoShootActivity extends Activity
        implements OnClickListener, CameraWrapper.CamOpenOverCallback{

    private final static String TAG = "koolew-VideoShootA";

    private FrameLayout mPreviewFrame;
    //private CameraSurfacePreview mPreview;
    private CameraSurfacePreview mPreview;
    private YUV420VideoEncoder mEncoder;
    private Camera mCamera;
    private int previewWidth;
    private int previewHeight;

    private boolean isRecording = false;

    private byte[] YUV420RotateBuffer;

    private MediaMuxerWrapper mMuxer;
    private MediaVideoEncoder mVideoEncoder;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_video_shoot);

        mEncoder = new YUV420VideoEncoder(
                AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);

        initViews();
    }

    private void initViews() {
        mPreviewFrame = (FrameLayout) findViewById(R.id.preview_frame);

        // Create our Preview view and set it as the content of our activity.
        mPreview = (CameraSurfacePreview) findViewById(R.id.camera_preview);

        findViewById(R.id.start_record).setOnClickListener(this);
        findViewById(R.id.stop_record).setOnClickListener(this);
    }

    private void initLayoutParams() {
        //initBestCameraPreviewSize(CameraWrapper.getInstance().getCamera().getParameters());

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mPreviewFrame.getLayoutParams();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        Log.d(TAG, "width: " + width + "height: " + lp.height);
        lp.height = width * previewWidth / previewHeight;
        int visiblePreviewHeight = width * AppProperty.RECORD_VIDEO_HEIGHT / AppProperty.RECORD_VIDEO_WIDTH;
        lp.topMargin = 0 - (lp.height - visiblePreviewHeight) / 2;
        mPreviewFrame.setLayoutParams(lp);

        LinearLayout bottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bottomLayout.getLayoutParams();
        params.topMargin = visiblePreviewHeight;
        bottomLayout.setLayoutParams(params);
    }

    public void doOpenCamera() {
        Log.i(TAG, "Camera open....");
        int numCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCamera = Camera.open(i);
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

    public void doStartPreview(SurfaceHolder holder) {
        Log.i(TAG, "doStartPreview...");

        try {
            this.mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initCamera();
    }

    public void doStopCamera() {
        Log.i(TAG, "doStopCamera, mCamera is null: " + (mCamera == null));
        if (this.mCamera != null) {
            //mCameraPreviewCallback.close();
            this.mCamera.setPreviewCallback(null);
            this.mCamera.stopPreview();
            this.mCamera.release();
            this.mCamera = null;
        }
    }

    private void initCamera() {
        if (this.mCamera != null) {
            Camera.Parameters params = this.mCamera.getParameters();
            params.setPreviewFormat(ImageFormat.NV21);
            params.setPreviewFrameRate(AppProperty.RECORD_VIDEO_FPS);
            params.setFlashMode("off");
            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            params.setPreviewSize(previewWidth, previewHeight);
            this.mCamera.setDisplayOrientation(90);
            mCamera.addCallbackBuffer(new byte[previewWidth * previewHeight * 3 / 2]);
            mCamera.setPreviewCallbackWithBuffer(new MyPreviewCallback());
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains("continuous-video")) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            this.mCamera.setParameters(params);
            this.mCamera.startPreview();
        }
    }

    private void initBestCameraPreviewSize(Camera.Parameters params) {
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        int count = sizes.size();
        int minSizeDiff = 0x7FFFFFFF;
        int bestSizeIndex = -1;
        for (int i = 0; i < count; i++) {
            Camera.Size size = sizes.get(i);
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

    @Override
    public void cameraHasOpened() {
        initBestCameraPreviewSize(mCamera.getParameters());
        YUV420RotateBuffer = new byte[previewWidth * previewHeight * 3 / 2];

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initLayoutParams();
            }
        });
//        SurfaceTexture surface = mPreview.getSurfaceTexture();
//        CameraWrapper.getInstance().doStartPreview(surface);
        SurfaceHolder holder = mPreview.getSurfaceHolder();
        doStartPreview(holder);
    }

    class MyPreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Log.d(TAG, "onPreviewFrame, isRecording: " + isRecording);
            if (isRecording){//mVideoEncoder != null && mVideoEncoder.isEncoding == true) {
                MediaVideoEncoder.NV21Frame frame = mVideoEncoder.obtainFrame();
                frame.frameNanoTime = System.nanoTime();
                YUV420Utils.rotateYUV420Degree90(data, YUV420RotateBuffer, previewWidth, previewHeight);
                YUV420Utils.cropYUV420VerticalCenter(YUV420RotateBuffer, frame.data,
                        previewHeight, previewWidth, AppProperty.RECORD_VIDEO_HEIGHT);
                mVideoEncoder.putNV21Frame(frame);
            }

            camera.addCallbackBuffer(data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        new Thread() {
            @Override
            public void run() {
                doOpenCamera();
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }


    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private void startRecord() {
        try {
            mMuxer = new MediaMuxerWrapper(".mp4");	// if you record audio only, ".m4a" is also OK.

            if (true) {
                // for video capturing
                mVideoEncoder = new MediaVideoEncoder(mMuxer, mMediaEncoderListener,
                        AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);
            }
            if (true) {
                // for audio capturing
                new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            }
            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (encoder == mVideoEncoder) {
                isRecording = true;
            }
        }
        @Override
        public void onStopped(final MediaEncoder encoder) {
            if (encoder == mVideoEncoder) {
                isRecording = false;
            }
        }
    };

    private void stopRecord() {
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
            // you should not wait here
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_record:
                startRecord();
                break;
            case R.id.stop_record:
                stopRecord();
                break;
        }
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        Log.d(TAG, mediaFile.getAbsolutePath());
        return mediaFile;
    }
}