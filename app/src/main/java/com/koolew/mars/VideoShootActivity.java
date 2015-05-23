package com.koolew.mars;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.koolew.mars.camerautils.CameraPreview;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VideoShootActivity extends Activity
        implements OnClickListener{

    private final static String TAG = "koolew-VideoShootA";

    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;

    private boolean isRecording = false;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_shoot);

        Log.d(TAG, "FFmpeg version: " + getAvcodecVersion());

        //mMediaRecorder = new MediaRecorder();
        FrameLayout previewFrame = (FrameLayout) findViewById(R.id.preview_frame);
        ViewGroup.LayoutParams lp = previewFrame.getLayoutParams();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        Log.d(TAG, "width: " + width + "height: " + lp.height);
        lp.height = width / 4 * 3;
        previewFrame.setLayoutParams(lp);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        Camera.Parameters param = mCamera.getParameters();
        //param.setRotation(90);
        param.setPreviewSize(640, 480);
        if (param.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(param);
        List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
        for (Camera.Size s: sizes) {
            Log.d(TAG, "width:" + s.width + ", height:" + s.height);
        }
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        previewFrame.addView(mPreview);

        findViewById(R.id.start_record).setOnClickListener(this);
        findViewById(R.id.stop_record).setOnClickListener(this);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder(){

        //omCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setOrientationHint(90);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        //mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Set output file format
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        // 这两项需要放在setOutputFormat之后
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        mMediaRecorder.setVideoSize(640, 480);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoEncodingBitRate(2000 * 1024);

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private void startRecord() {
        // initialize video camera
        if (prepareVideoRecorder()) {
            // Camera is available and unlocked, MediaRecorder is prepared,
            // now you can start recording
            mMediaRecorder.start();

            // inform the user that recording has started
            isRecording = true;
        } else {
            // prepare didn't work, release the camera
            releaseMediaRecorder();
            // inform user
        }
    }

    private void stopRecord() {
        // stop recording and release camera
        mMediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        mCamera.lock();         // take camera access back from MediaRecorder

        // inform the user that recording has stopped
        isRecording = false;
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

    static {
        System.loadLibrary("ffmpegbridge");
        System.loadLibrary("avcodec-56");
        System.loadLibrary("avformat-56");
        System.loadLibrary("avutil-54");
        System.loadLibrary("avfilter-5");
        System.loadLibrary("swresample-1");
        System.loadLibrary("swscale-3");
    }

    public native int getAvcodecVersion();

}