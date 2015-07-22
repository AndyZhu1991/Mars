package com.koolew.mars;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.koolew.mars.camerautils.CameraSurfacePreview;
import com.koolew.mars.media.MediaAudioEncoder;
import com.koolew.mars.media.MediaEncoder;
import com.koolew.mars.media.MediaMuxerWrapper;
import com.koolew.mars.media.MediaVideoEncoder;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.RawImageUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.utils.ViewUtil;
import com.koolew.mars.video.VideoRecordingSession;
import com.koolew.mars.view.VideoPieceView;
import com.koolew.mars.view.VideosProgressView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VideoShootActivity extends Activity
        implements OnClickListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

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

    private RecyclerView mRecyclerView;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private VideoItemAdapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private VideosProgressView mVideosProgressView;
    private ImageView mChangeCamera;

    private VideoRecordingSession mRecordingSession;
    private VideoRecordingSession.VideoPieceItem mCurrentRecodingVideo;
    private boolean isRecording = false;
    private boolean isEncoding = false;

    private byte[] YUV420RotateBuffer;
    private byte[] YUV420CropBuffer;

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

        if (mCurrentSurfaceMode == MODE_PREVIEW) {
        }
        else { // mCurrentSurfaceMode == MODE_PLAYBACK
        }
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
        else { // mCurrentSurfaceMode == MODE_PLAYBACK
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
    }

    private void initMembers() {
        mCurrentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
        mRecordingSession = new VideoRecordingSession(this);
        mCurrentSurfaceMode = MODE_PREVIEW;
    }

    private void initViews() {

        initRecyclerView();

        mVideosProgressView = (VideosProgressView) findViewById(R.id.videos_progress);
        mVideosProgressView.setRecordingSession(mRecordingSession);
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

        findViewById(R.id.image_record).setOnClickListener(this);
        findViewById(R.id.record_complete).setOnClickListener(this);
        findViewById(R.id.close_layout).setOnClickListener(this);
        mChangeCamera.setOnClickListener(this);
        mPlayImage.setOnClickListener(this);
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setInitiateOnLongPress(true);
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        mAdapter = new VideoItemAdapter();
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter);
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mWrappedAdapter);

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
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

    class MyPreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Log.d(TAG, "onPreviewFrame, isRecording: " + isRecording);
            if (isEncoding){//mVideoEncoder != null && mVideoEncoder.isEncoding == true) {
                MediaVideoEncoder.YUV420SPFrame frame = mVideoEncoder.obtainFrame();
                frame.frameNanoTime = System.nanoTime();
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
                RawImageUtil.NV21toI420SemiPlanar(YUV420CropBuffer, frame.data,
                        AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);
                mVideoEncoder.putYUV420SPFrame(frame);
            }

            camera.addCallbackBuffer(data);
        }
    }


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

        mPlaybackSurface.setVisibility(View.INVISIBLE);
        mPreview.setVisibility(View.VISIBLE);
        mChangeCamera.setVisibility(View.VISIBLE);
        mVideoThumb.setVisibility(View.INVISIBLE);
        mPlayImage.setVisibility(View.INVISIBLE);

        doOpenCamera(mCurrentCamera);
    }

    private void setSelectedVideo(int position) {
        ImageLoader.getInstance().displayImage(
                "file://" + mRecordingSession.get(position).getVideoPath(),
                mVideoThumb, null, null, null);
    }

    private void playVideoList() {
        mCurrentPlayedIndex = 0;
        try {
            mMediaPlayer.setDataSource(mRecordingSession.get(0).getVideoPath());
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mCurrentPlayedIndex++;
        mMediaPlayer.reset();

        if (mCurrentPlayedIndex < mRecordingSession.getVideoCount()) {
            try {
                mMediaPlayer.setDataSource(mRecordingSession.get(mCurrentPlayedIndex).getVideoPath());
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            mPlayImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mCurrentPlayedIndex == 0) {
            mVideoThumb.setVisibility(View.INVISIBLE);
            mPlayImage.setVisibility(View.INVISIBLE);
        }
        mMediaPlayer.start();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        mMediaPlayer.start();
    }

    private SurfaceHolder.Callback mPlaybackSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(VideoShootActivity.this);
            mMediaPlayer.setOnCompletionListener(VideoShootActivity.this);
            mMediaPlayer.setOnSeekCompleteListener(VideoShootActivity.this);
            mMediaPlayer.setDisplay(mPlaybackSurface.getHolder());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };



    private String getCurrentRecordingFile() {
        return mCurrentRecodingVideo.getVideoPath();
    }

    private void startRecord() {
        try {
            mCurrentRecodingVideo = mRecordingSession.new VideoPieceItem();
            mVideosProgressView.start();
            mMuxer = new MediaMuxerWrapper(getCurrentRecordingFile());
            mVideoEncoder = new MediaVideoEncoder(mMuxer, mMediaEncoderListener,
                        AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);
            new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            mMuxer.prepare();
            mMuxer.startRecording();

            isRecording = true;
        } catch (IOException e) {
            throw new RuntimeException("IOException: " + e);
        }
    }

    private void stopRecord() {
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
            // you should not wait here
        }
        mCurrentRecodingVideo.finishRecord();
        mRecordingSession.add(mCurrentRecodingVideo);
        mCurrentRecodingVideo = null;
        mAdapter.notifyItemInserted(mRecordingSession.getVideoCount() - 1);

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
                playVideoList();
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

    private void onRecordCompleteClick() {
        if (isRecording) {
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;

            @Override
            protected void onPreExecute() {
                mProgressDialog = DialogUtil.getGeneralProgressDialog(
                        VideoShootActivity.this, R.string.processing_video);
                mProgressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                mRecordingSession.concatVideo();
                mRecordingSession.generateThumb();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgressDialog.dismiss();
                Intent intent = new Intent(VideoShootActivity.this, VideoEditActivity.class);
                intent.putExtra(VideoEditActivity.KEY_CONCATED_VIDEO,
                        mRecordingSession.getConcatedVideoName());
                intent.putExtra(VideoEditActivity.KEY_VIDEO_THUMB,
                        mRecordingSession.getVideoThumbName());
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
                finish();
                break;
            case VideoEditActivity.RESULT_BACKGROUND_UPLOAD:
                break;
        }
    }

    private void onCloseClick() {
        finish();
    }

    private void onCameraChangeClick() {
        switchCamera();
    }

    private OnClickListener videoThumbClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();

            if (mCurrentSurfaceMode == MODE_PLAYBACK) {
                setSelectedVideo(position);
                return;
            }

            if (isRecording) {
                stopRecord();
            }

            switchToPlaybackMode();
            setSelectedVideo(position);
        }
    };

    class VideoItemAdapter extends RecyclerView.Adapter<VideoItemAdapter.ViewHolder>
            implements DraggableItemAdapter<VideoItemAdapter.ViewHolder>,
            SwipeableItemAdapter<VideoItemAdapter.ViewHolder> {

        private Set<View> mVideoPieces;

        public VideoItemAdapter() {
            mVideoPieces = new HashSet<>();
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return mRecordingSession.get(position).getId();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.record_video_item, viewGroup, false);

            ViewHolder viewHolder = new ViewHolder(itemView);
            viewHolder.thumbImage.setOnClickListener(videoThumbClickListener);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            ImageLoader.getInstance().displayImage(
                    "file://" + mRecordingSession.get(position).getVideoPath(),
                    viewHolder.thumbImage, null, null, null);
            viewHolder.thumbImage.setTag(new Integer(position));
            viewHolder.pieceView.setVideoPieceItem(mRecordingSession.get(position));

            mVideoPieces.add(viewHolder.pieceView);
        }

        @Override
        public int getItemCount() {
            return mRecordingSession.getVideoCount();
        }

        @Override
        public boolean onCheckCanStartDrag(ViewHolder holder, int position, int x, int y) {
            // x, y --- relative from the itemView's top-left
            final View containerView = holder.container;
            final View dragHandleView = holder.dragHandle;

            int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
            int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

            return ViewUtil.hitTest(dragHandleView, x - offsetX, y - offsetY);
        }

        @Override
        public ItemDraggableRange onGetItemDraggableRange(ViewHolder holder, int position) {
            return null;
        }

        @Override
        public void onMoveItem(int fromPosition, int toPosition) {
            if (fromPosition == toPosition) {
                return;
            }

            mRecordingSession.moveItem(fromPosition, toPosition);

            notifyItemMoved(fromPosition, toPosition);
            mVideosProgressView.invalidate();
            for (View piece: mVideoPieces) {
                piece.invalidate();
            }
        }

        @Override
        public int onGetSwipeReactionType(ViewHolder viewHolder, int position, int x, int y) {
            return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_RIGHT;
        }

        @Override
        public void onSetSwipeBackground(ViewHolder viewHolder, int position, int type) {

        }

        @Override
        public int onSwipeItem(ViewHolder viewHolder, int position, int result) {
            switch (result) {
                // swipe right
                case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;
                // other --- do nothing
                case RecyclerViewSwipeManager.RESULT_CANCELED:
                default:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
            }
        }

        @Override
        public void onPerformAfterSwipeReaction(ViewHolder viewHolder, int position, int result, int reaction) {
            if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM) {
                mRecordingSession.remove(position);
                notifyItemRemoved(position);
                mVideosProgressView.invalidate();
                for (View piece: mVideoPieces) {
                    piece.invalidate();
                }
            }
        }

        class ViewHolder extends AbstractDraggableSwipeableItemViewHolder {

            View container;
            ImageView thumbImage;
            VideoPieceView pieceView;
            ImageView dragHandle;

            public ViewHolder(View itemView) {
                super(itemView);
                container = itemView.findViewById(R.id.container);
                thumbImage = (ImageView) itemView.findViewById(R.id.video_thumb);
                pieceView = (VideoPieceView) itemView.findViewById(R.id.piece_view);
                dragHandle = (ImageView) itemView.findViewById(R.id.drag_handle);
            }

            @Override
            public View getSwipeableContainerView() {
                return container;
            }
        }
    }

}