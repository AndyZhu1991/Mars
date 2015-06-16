package com.koolew.mars;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.koolew.mars.camerautils.CameraSurfacePreview;
import com.koolew.mars.camerautils.CameraWrapper;
import com.koolew.mars.media.MediaAudioEncoder;
import com.koolew.mars.media.MediaEncoder;
import com.koolew.mars.media.MediaMuxerWrapper;
import com.koolew.mars.media.MediaVideoEncoder;
import com.koolew.mars.media.YUV420VideoEncoder;
import com.koolew.mars.utils.Mp4ParserUtil;
import com.koolew.mars.utils.RawImageUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.VideosProgressView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.LinkedList;
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

    private RecyclerView mRecyclerView;
    private VideoItemAdapter mAdapter;
    private VideosProgressView mVideosProgressView;

    private String currentRecordingDir;
    private List<VideoItemInfo> mRecordedVideos = new LinkedList<VideoItemInfo>();
    private String mCurrentRecodingFile;
    private boolean isRecording = false;
    private boolean isEncoding = false;

    private byte[] YUV420RotateBuffer;
    private byte[] YUV420CropBuffer;

    private MediaMuxerWrapper mMuxer;
    private MediaVideoEncoder mVideoEncoder;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_video_shoot);

        mEncoder = new YUV420VideoEncoder(
                AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);

        initMembers();
        initViews();
    }

    private void initMembers() {
        currentRecordingDir = Utils.getCacheDir(this);
    }

    private void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new VideoItemAdapter();
        mAdapter.setData(mRecordedVideos);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mVideosProgressView = (VideosProgressView) findViewById(R.id.videos_progress);
        mPreviewFrame = (FrameLayout) findViewById(R.id.preview_frame);

        // Create our Preview view and set it as the content of our activity.
        mPreview = (CameraSurfacePreview) findViewById(R.id.camera_preview);

        findViewById(R.id.image_record).setOnClickListener(this);
        findViewById(R.id.record_complete).setOnClickListener(this);
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
            params.setRecordingHint(true);
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
        YUV420CropBuffer = new byte[AppProperty.RECORD_VIDEO_WIDTH * AppProperty.RECORD_VIDEO_HEIGHT * 3 / 2];

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
            if (isEncoding){//mVideoEncoder != null && mVideoEncoder.isEncoding == true) {
                MediaVideoEncoder.YUV420SPFrame frame = mVideoEncoder.obtainFrame();
                frame.frameNanoTime = System.nanoTime();
                RawImageUtil.rotateYUV420Degree90(data, YUV420RotateBuffer, previewWidth, previewHeight);
                RawImageUtil.cropYUV420VerticalCenter(YUV420RotateBuffer, YUV420CropBuffer,
                        previewHeight, previewWidth, AppProperty.RECORD_VIDEO_HEIGHT);
                RawImageUtil.NV21toI420SemiPlanar(YUV420CropBuffer, frame.data,
                        AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);
                mVideoEncoder.putYUV420SPFrame(frame);
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
        if (isRecording) {
            stopRecord();
        }
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
            mCurrentRecodingFile = currentRecordingDir + System.currentTimeMillis() + ".mp4";
            mMuxer = new MediaMuxerWrapper(mCurrentRecodingFile);
            mVideoEncoder = new MediaVideoEncoder(mMuxer, mMediaEncoderListener,
                        AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);
            new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            mMuxer.prepare();
            mMuxer.startRecording();

            mVideosProgressView.start();

            isRecording = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
            // you should not wait here
        }
        mRecordedVideos.add(new VideoItemInfo(mCurrentRecodingFile));
        mAdapter.notifyItemInserted(mRecordedVideos.size() - 1);

        mVideosProgressView.finish();

        isRecording = false;
    }

    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_record:
                if (!isRecording) {
                    startRecord();
                }
                else {
                    stopRecord();
                }
                break;
            case R.id.record_complete:
                if (isRecording) {
                    return;
                }
                try {
                    List<String> videoPathList = new LinkedList<String>();
                    for (VideoItemInfo info: mRecordedVideos) {
                        videoPathList.add(info.path);
                    }
                    Mp4ParserUtil.mp4Cat(videoPathList, currentRecordingDir + "concated.mp4");
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    static class VideoItemInfo {
        String path;

        VideoItemInfo(String path) {
            this.path = path;
        }
    }

    static class VideoItemAdapter extends RecyclerView.Adapter<VideoItemAdapter.ViewHolder> {

        private List<VideoItemInfo> mData;

        void setData(List<VideoItemInfo> data) {
            mData = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.record_video_item, viewGroup, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            ImageLoader.getInstance().displayImage("file://" + mData.get(i).path,
                    viewHolder.thumbImage, null, null, null);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView thumbImage;
            ImageView dragHandle;

            public ViewHolder(View itemView) {
                super(itemView);
                thumbImage = (ImageView) itemView.findViewById(R.id.video_thumb);
                dragHandle = (ImageView) itemView.findViewById(R.id.drag_handle);
            }
        }
    }

}