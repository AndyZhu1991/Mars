package com.koolew.mars.camerautils;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.koolew.mars.AppProperty;

import java.io.IOException;
import java.util.List;

public class CameraInstance {
    public static final String LOG_TAG = CameraInstance.class.getSimpleName();

    private int wantedWidth;
    private int wantedHeight;

    private Camera mCameraDevice;

    private boolean mIsPreviewing = false;

    private int mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int mDefaultCameraID = -1;

    private static CameraInstance mThisInstance;
    private int mPreviewWidth;
    private int mPreviewHeight;

    private CameraInstance() {}

    public static synchronized CameraInstance getInstance(int wantedWidth, int wantedHeight) {
        if(mThisInstance == null) {
            mThisInstance = new CameraInstance();
        }
        mThisInstance.wantedWidth = wantedWidth;
        mThisInstance.wantedHeight = wantedHeight;
        return mThisInstance;
    }

    public boolean isPreviewing() { return mIsPreviewing; }

    public int previewWidth() { return mPreviewWidth; }
    public int previewHeight() { return mPreviewHeight; }
    public int getCameraID() {
        return mCameraID;
    }

    public void switchCamera() {
        if (mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        else {
            mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        stopCamera();
        openCamera();
    }

    public boolean openCamera() {
        int numCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == mCameraID) {
                try {
                    mCameraDevice = Camera.open(i);
                }
                catch (RuntimeException re) {
                    return false;
                }
                break;
            }
        }

        if(mCameraDevice != null) {
            initCamera();
        }
        return true;
    }

    public void stopCamera() {
        if(mCameraDevice != null) {
            mCameraDevice.stopPreview();
            mIsPreviewing = false;
            mCameraDevice.release();
            mCameraDevice = null;
        }
    }

    public void startPreview(SurfaceTexture texture) {
        if(mCameraDevice != null) {
            try {
                mCameraDevice.startPreview();
                mCameraDevice.setPreviewTexture(texture);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mIsPreviewing = true;
        }
    }

    public void stopPreview() {
        Log.i(LOG_TAG, "Camera stopPreview...");
        if(mIsPreviewing && mCameraDevice != null) {
            mIsPreviewing = false;
            mCameraDevice.stopPreview();
        }
    }

    private void initCamera() {
        if (this.mCameraDevice != null) {
            Camera.Parameters params = this.mCameraDevice.getParameters();
            setBestCameraPreviewFpsRange(params);
            params.setFlashMode("off");
            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            params.setRecordingHint(true);
            setBestCameraPreviewSize(params);
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains("continuous-video")) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            mCameraDevice.setParameters(params);
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
            mPreviewWidth = bestSize.width;
            mPreviewHeight = bestSize.height;
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
