package com.koolew.mars.camerautils;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import com.koolew.mars.AppProperty;

import java.io.IOException;
import java.util.List;

@SuppressLint("NewApi")
public class CameraWrapper {
	private static final String TAG = "koolew-CameraWrapper";
	private Camera mCamera;
	private Camera.Parameters mCameraParamters;
	private static CameraWrapper mCameraWrapper;
	private boolean mIsPreviewing = false;
	private float mPreviewRate = -1.0f;
	private int previewWidth;
	private int previewHeight;
	private CameraPreviewCallback mCameraPreviewCallback;
	private byte[] mImageCallbackBuffer;// = new byte[CameraWrapper.IMAGE_WIDTH
	                     				//* CameraWrapper.IMAGE_HEIGHT * 3 / 2];

	public interface CamOpenOverCallback {
		public void cameraHasOpened();
	}

	private CameraWrapper() {
	}

	public static synchronized CameraWrapper getInstance() {
		if (mCameraWrapper == null) {
			mCameraWrapper = new CameraWrapper();
		}
		return mCameraWrapper;
	}

	public void doOpenCamera(CamOpenOverCallback callback) {
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
		callback.cameraHasOpened();
	}

    public void doStartPreview(SurfaceHolder holder) {
        Log.i(TAG, "doStartPreview...");
        if (mIsPreviewing) {
            this.mCamera.stopPreview();
            return;
        }

        try {
            this.mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initCamera();
    }

	public void doStartPreview(SurfaceTexture surface) {
		Log.i(TAG, "doStartPreview()");
		if (mIsPreviewing) {
			this.mCamera.stopPreview();
			return;
		}

		try {
			this.mCamera.setPreviewTexture(surface);
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
			this.mIsPreviewing = false;
			this.mPreviewRate = -1f;
			this.mCamera.release();
			this.mCamera = null;
		}
	}
	
	private void initCamera() {
		if (this.mCamera != null) {
			this.mCameraParamters = this.mCamera.getParameters();
			initBestCameraPreviewSize(mCameraParamters);
			this.mCameraParamters.setPreviewFormat(ImageFormat.NV21);
			this.mCameraParamters.setFlashMode("off");
			this.mCameraParamters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
			this.mCameraParamters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
			this.mCameraParamters.setPreviewSize(previewWidth, previewHeight);
			this.mCamera.setDisplayOrientation(90);
            mImageCallbackBuffer = new byte[previewWidth * previewHeight * 3 / 2];
			mCameraPreviewCallback = new CameraPreviewCallback();
			mCamera.addCallbackBuffer(mImageCallbackBuffer);
			mCamera.setPreviewCallbackWithBuffer(mCameraPreviewCallback);
			List<String> focusModes = this.mCameraParamters.getSupportedFocusModes();
			if (focusModes.contains("continuous-video")) {
				this.mCameraParamters
						.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			}
			this.mCamera.setParameters(this.mCameraParamters);
			this.mCamera.startPreview();
			
			this.mIsPreviewing = true;
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

    class CameraPreviewCallback implements Camera.PreviewCallback {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Log.d(TAG, "onPreviewFrame");

            camera.addCallbackBuffer(data);
        }
    }

	public Camera getCamera() {
		return mCamera;
	}

}
