package com.koolew.mars.camerautils;

/**
 * Created by jinchangzhu on 5/21/15.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.koolew.mars.AppProperty;
import com.koolew.mars.R;

import java.io.IOException;
import java.util.List;

/** A basic Camera preview class */
public abstract class CameraPreview {

    private static final String TAG = CameraPreview.class.getSimpleName();

    private Activity mActivity;
    private int wantedWidth;
    private int wantedHeight;

    private Camera mCamera;
    private int mCurrentCameraPos = Camera.CameraInfo.CAMERA_FACING_BACK;

    private int previewWidth;
    private int previewHeight;

    public CameraPreview(Activity activity, int wantedWidth, int wantedHeight) {
        this.mActivity = activity;
        this.wantedWidth = wantedWidth;
        this.wantedHeight = wantedHeight;
    }

    private void initCamera() {
        if (this.mCamera != null) {
            Camera.Parameters params = this.mCamera.getParameters();
            params.setPreviewFormat(ImageFormat.NV21);
            setBestCameraPreviewFpsRange(params);
            params.setFlashMode("off");
            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            setBestCameraPreviewSize(params);
            this.mCamera.setDisplayOrientation(90);
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains("continuous-video")) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            this.mCamera.setParameters(params);
            this.mCamera.startPreview();
        }
    }

    public void switchCamera() {
        if (mCurrentCameraPos == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCurrentCameraPos = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        else {
            mCurrentCameraPos = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        releaseCamera();
        doOpenCamera(mCurrentCameraPos);
    }

    /**
     *
     * @param which Camera.CameraInfo.CAMERA_FACING_BACK for back camera
     *              Camera.CameraInfo.CAMERA_FACING_FRONT for front camera
     */
    public void doOpenCamera(int which) {
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
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(mActivity)
                        .setMessage(R.string.fail_to_open_camera)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        mActivity.onBackPressed();
                                    }
                                })
                        .show();
            }
        });
    }

    private void cameraHasOpened() {
        doStartPreview();
    }

    protected abstract void initLayoutParams();

    private void doStartPreview() {
        Log.i(TAG, "doStartPreview...");

        try {
            mCamera.setPreviewTexture(getSurfaceTexture());
        } catch (IOException e) {
            e.printStackTrace();
        }
        initCamera();

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initLayoutParams();
            }
        });
    }

    protected abstract SurfaceTexture getSurfaceTexture();

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private void setBestCameraPreviewSize(Camera.Parameters params) {
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
            if (Math.abs(size.height - wantedWidth) < minSizeDiff) {
                minSizeDiff = Math.abs(size.height - wantedWidth);
                bestSizeIndex = i;
                if (minSizeDiff == 0) {
                    break;
                }
            }
        }

        if (bestSizeIndex != -1) {
            Camera.Size bestSize = sizes.get(bestSizeIndex);
            previewWidth = bestSize.width;
            previewHeight = bestSize.height;
            params.setPreviewSize(bestSize.width, bestSize.height);
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

}
