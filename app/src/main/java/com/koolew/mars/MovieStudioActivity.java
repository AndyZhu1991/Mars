package com.koolew.mars;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koolew.mars.infos.MovieTopicInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.Downloader;
import com.koolew.mars.utils.Mp4ParserUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.videotools.BlockingRecycleQueue;
import com.koolew.mars.videotools.CachedRecorder;
import com.koolew.mars.videotools.SamplesFrame;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MovieStudioActivity extends BaseActivity
        implements CameraPreviewFragment.FrameListener, View.OnClickListener {

    public static final String KEY_MOVIE_TOPIC_INFO = "movie topic info";
    public static final String KEY_MOVIE_URL = "movie url";
    public static final String KEY_FROM = "from";

    private static final int MOVIE_CONTENT_WIDTH = 480;
    private static final int MOVIE_CONTENT_HEIGHT = 270;

    private static final int REQUEST_CODE_UPLOAD_VIDEO = 1;

    private String movieUrl;
    private String originVideoPath;

    private View mFragmentContainer;
    private CameraPreviewFragment mCameraPreviewFragment;
    private View mCountDownLayout;
    private TextView mCountDownText;
    private RecyclerView mRecyclerView;

    private View mBlockTouchView;

    private ImageView mCaptureButton;
    private TextView mCaptureText;
    private static final int STATUS_CAPTURE = 0;
    private static final int STATUS_CANCLE = 1;
    private static final int STATUS_RECAPTURE = 2;
    private int captureButtonStatus = STATUS_CAPTURE;

    private View mDeleteView;
    private ImageView mDeleteImage;
    private View mNextView;
    private ImageView mNextImage;

    private ImageOnlyRecorder mRecorder;

    private MovieStudioItemAdapter mAdapter;

    private MovieTopicInfo mMovieTopicInfo;
    private String mWorkDir;
    private String mFrom;

    private opencv_core.IplImage koolewMask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_studio);

        initViews();

        initWorkDir();

        mMovieTopicInfo = (MovieTopicInfo) getIntent().getSerializableExtra(KEY_MOVIE_TOPIC_INFO);
        movieUrl = getIntent().getExtras().getString(KEY_MOVIE_URL, mMovieTopicInfo.getVideoUrl());
        mFrom = getIntent().getExtras().getString(KEY_FROM, "");

        int[] splitPoints = new int[mMovieTopicInfo.getFragments().length];
        for (int i = 0; i < splitPoints.length; i++) {
            splitPoints[i] = mMovieTopicInfo.getFragments()[i].getEnd();
        }
        new CutVideoTask().execute();
    }

    private void initViews() {
        mFragmentContainer = findViewById(R.id.fragment_container);
        int screenWidth = Utils.getScreenWidthPixel(this);
        mFragmentContainer.getLayoutParams().height = screenWidth * 9 / 16;
        mCameraPreviewFragment =
                (CameraPreviewFragment) getFragmentManager().findFragmentById(R.id.camera_preview);
        mCameraPreviewFragment.setWantedSize(MOVIE_CONTENT_WIDTH, MOVIE_CONTENT_HEIGHT);

        mCountDownLayout = findViewById(R.id.count_down_layout);
        mCountDownText = (TextView) findViewById(R.id.count_down_text);

        findViewById(R.id.x).setOnClickListener(this);
        findViewById(R.id.switch_camera).setOnClickListener(this);
        findViewById(R.id.block_touch_view).setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new MovieStudioItemAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerScrollListener());
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int recyclerWidth = mRecyclerView.getMeasuredWidth();
                        int recyclerItemWidth = (int) Utils.dpToPixels(MovieStudioActivity.this, 126);
                        int paddingLR = (recyclerWidth - recyclerItemWidth) / 2;
                        mRecyclerView.setPadding(paddingLR, 0, paddingLR, 0);
                    }
                });

        mBlockTouchView = findViewById(R.id.block_touch_view);

        mCaptureButton = (ImageView) findViewById(R.id.capture);
        mCaptureButton.setOnClickListener(this);
        mCaptureText = (TextView) findViewById(R.id.capture_text);

        mDeleteView = findViewById(R.id.delete_view);
        mDeleteView.setOnClickListener(this);
        mDeleteImage = (ImageView) findViewById(R.id.delete_image);
        mNextView = findViewById(R.id.next_step);
        mNextView.setOnClickListener(this);
        mNextImage = (ImageView) findViewById(R.id.next_image);
    }

    @Override
    public void onBackPressed() {
        if (captureButtonStatus == STATUS_CANCLE) {
            onCaptureClick();
        }
        else {
            super.onBackPressed();
        }
    }

    private void switchCaptureButtonStatus(int status) {
        captureButtonStatus = status;
        switch (status) {
            case STATUS_CAPTURE:
                mCaptureButton.setImageResource(R.mipmap.video_capture);
                mCaptureText.setText(R.string.capture_video);
                break;
            case STATUS_CANCLE:
                mCaptureButton.setImageResource(R.mipmap.video_capture_cancle);
                mCaptureText.setText(R.string.interrupt);
                break;
            case STATUS_RECAPTURE:
                mCaptureButton.setImageResource(R.mipmap.video_capture);
                mCaptureText.setText(R.string.recapture);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.x:
                onXClick();
                break;
            case R.id.switch_camera:
                onSwitchCamera();
                break;
            case R.id.block_touch_view:
                break;
            case R.id.capture:
                onCaptureClick();
                break;
            case R.id.delete_view:
                onDeleteClick();
                break;
            case R.id.next_step:
                onNextStep();
                break;
        }
    }

    public void onXClick() {
        onBackPressed();
    }

    public void onSwitchCamera() {
        mCameraPreviewFragment.switchCamera();
    }

    private Timer captureCountDownTimer;

    public void onCaptureClick() {
        if (captureButtonStatus == STATUS_CAPTURE || captureButtonStatus == STATUS_RECAPTURE) {
            mBlockTouchView.setVisibility(View.VISIBLE);
            mRecyclerView.scrollToPosition(mAdapter.selectedPosition);
            captureCountDownTimer = new Timer();
            captureCountDownTimer.schedule(new MovieStartCountDownTask(), 0, 1000);
            mCountDownLayout.setVisibility(View.VISIBLE);
            switchCaptureButtonStatus(STATUS_CANCLE);
        }
        else if (captureButtonStatus == STATUS_CANCLE) {
            mBlockTouchView.setVisibility(View.INVISIBLE);
            cancleCapture();
            if (TextUtils.isEmpty(mAdapter.getCurrentSelectedItem().capturedVideoPath)) {
                switchCaptureButtonStatus(STATUS_CAPTURE);
            }
            else {
                switchCaptureButtonStatus(STATUS_RECAPTURE);
            }
        }
    }

    public void onDeleteClick() {
        if (!TextUtils.isEmpty(mAdapter.getCurrentSelectedItem().capturedVideoPath)) {
            mAdapter.getCurrentSelectedItem().capturedVideoPath = null;
            mAdapter.notifyItemChanged(mAdapter.selectedPosition);
            if (!hasUserCapturedPiece()) {
                mNextImage.setImageResource(R.mipmap.video_complete_disable);
            }
        }
        else {
            Toast.makeText(this, R.string.no_movie_piece_hint, Toast.LENGTH_SHORT).show();
        }
    }

    public void onNextStep() {
        if (hasUserCapturedPiece()) {
            new NextStepTask().execute();
        }
        else {
            // TODO: Show a toast
        }
    }

    private boolean hasUserCapturedPiece() {
        for (MovieStudioItem item: mAdapter.items) {
            if (!TextUtils.isEmpty(item.capturedVideoPath)) {
                return true;
            }
        }
        return false;
    }

    private void doStartCapture() {
        mRecorder = new ImageOnlyRecorder(generateVideoPath(),
                AppProperty.RECORD_VIDEO_WIDTH, AppProperty.RECORD_VIDEO_HEIGHT);
        mRecorder.start();
        mAdapter.selectedHolder.startPlayOriginal();
        mCameraPreviewFragment.setFrameListener(this);
    }

    private void completeCapture() {
        mCameraPreviewFragment.clearFrameListener();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.selectedHolder.recordedPart.setX(0);
                new CompleteCaptureTask().execute();
            }
        });
    }

    private void cancleCapture() {
        if (captureCountDownTimer != null) {
            captureCountDownTimer.cancel();
            captureCountDownTimer = null;
            mCountDownLayout.setVisibility(View.INVISIBLE);
        }
        if (mRecorder != null) {
            new CancleCaptureTask().execute();
        }
    }

    class MovieStartCountDownTask extends TimerTask {
        private int second = 3;

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (second == 0) {
                        captureCountDownTimer.cancel();
                        captureCountDownTimer = null;
                        doStartCapture();
                        mCountDownLayout.setVisibility(View.INVISIBLE);
                    }
                    else {
                        mCountDownText.setText(String.valueOf(second));
                    }
                    second--;
                }
            });
        }
    }

    private String getVideoCutPath(int index) {
        return mWorkDir + "origin-" + index + ".mp4";
    }

    private String getAudioCutPath(int index) {
        return mWorkDir + "audio-" + index + ".mp4";
    }

    private String generateVideoPath() {
        return mWorkDir + System.currentTimeMillis() + ".mp4";
    }

    private String getConcatedVideoPath() {
        return mWorkDir + "concated.mp4";
    }

    private String getAudioedVideoPath() {
        return mWorkDir + "audioed.mp4";
    }

    private String getFinalVideoPath() {
        return mWorkDir + "final.mp4";
    }

    private String getThumbPath() {
        return mWorkDir + "thumb.jpg";
    }

    private void changeCheckedItem(int position) {
        if (mAdapter.selectedPosition != position) {
            int originSelectedPos = mAdapter.selectedPosition;
            mAdapter.selectedPosition = position;
            mAdapter.notifyItemChanged(originSelectedPos);
            mAdapter.notifyItemChanged(mAdapter.selectedPosition);
            if (mAdapter.getCurrentSelectedItem().capturedVideoPath == null) {
                switchCaptureButtonStatus(STATUS_CAPTURE);
                mDeleteImage.setImageResource(R.mipmap.image_remove_disable);
            }
            else {
                switchCaptureButtonStatus(STATUS_RECAPTURE);
                mDeleteImage.setImageResource(R.mipmap.image_remove_enable);
            }
        }
        mRecyclerView.scrollToPosition(position);
    }

    private void initWorkDir() {
        mWorkDir = Utils.getCacheDir(this) + System.currentTimeMillis() + "/";
        new Thread() {
            @Override
            public void run() {
                new File(mWorkDir).mkdirs();
            }
        }.start();
    }

    @Override
    public void onNewFrame(opencv_core.IplImage image, long timestamp) {
        if (mRecorder != null) {
            long currentCutLen = mAdapter.getCurrentSelectedItem().videoLen;
            if (mRecorder.getCurrentLen() + ImageOnlyRecorder.FRAME_PER_USEC / 1000 < currentCutLen) {
                if (mRecorder.getFirstFrameTimestamp() > 0 &&
                        timestamp - mRecorder.getFirstFrameTimestamp() - ImageOnlyRecorder.FRAME_PER_USEC / 1000 > currentCutLen * 1000) {
                    timestamp = mRecorder.getFirstFrameTimestamp() + currentCutLen * 1000 - ImageOnlyRecorder.FRAME_PER_USEC / 1000;
                }
                if (mRecorder.putImage(image, timestamp)) {
//                int totalWidth = mAdapter.selectedHolder.borderView.getWidth();
//                ViewGroup.LayoutParams lp = mAdapter.selectedHolder.recordedPart.getLayoutParams();
//                lp.width = (int) (totalWidth * (1.0 * mRecorder.getCurrentLen() / currentCutLength));
//                mAdapter.selectedHolder.recordedPart.setLayoutParams(lp);
                    updateCurrentRecordProgress((1.0f * mRecorder.getCurrentLen() / currentCutLen));
                }
            }
            else {
                updateCurrentRecordProgress(1.0f);
                completeCapture();
            }
        }
    }

    private void updateCurrentRecordProgress(final float progress) {
        mAdapter.selectedHolder.recordedPart.post(new Runnable() {
            @Override
            public void run() {
                int totalWidth = mAdapter.selectedHolder.borderView.getWidth();
                float showWidth = totalWidth * progress;
                mAdapter.selectedHolder.recordedPart.setX(showWidth - totalWidth);
            }
        });
    }

    class CompleteCaptureTask extends DialogUtil.AsyncTaskWithDialog<Void, Void, String> {
        public CompleteCaptureTask() {
            super(MovieStudioActivity.this);
        }

        @Override
        protected String doInBackground(Void... params) {
            mRecorder.stopSynced();
            String recordedFilePath = mRecorder.getFilePath();
            mRecorder = null;
            String audioedFilePath = generateVideoPath();
            try {
                Mp4ParserUtil.overrideAudio(recordedFilePath,
                        mAdapter.getCurrentSelectedItem().originalVideoPath, audioedFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return audioedFilePath;
        }

        @Override
        protected void onPostExecute(String filePath) {
            mAdapter.getCurrentSelectedItem().capturedVideoPath = filePath;
            mAdapter.getCurrentSelectedItem().status = STATUS_DONE;
            mAdapter.notifyItemChanged(mAdapter.selectedPosition);
            mDeleteImage.setImageResource(R.mipmap.image_remove_enable);
            mNextImage.setImageResource(R.mipmap.video_complete_enable);
            switchCaptureButtonStatus(STATUS_RECAPTURE);
            mBlockTouchView.setVisibility(View.INVISIBLE);
            super.onPostExecute(filePath);
        }
    }

    class CancleCaptureTask extends DialogUtil.AsyncTaskWithDialog<Void, Void, Void> {
        public CancleCaptureTask() {
            super(MovieStudioActivity.this);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mRecorder.stopSynced();
            String recordedFilePath = mRecorder.getFilePath();
            mRecorder = null;
            new File(recordedFilePath).delete();
            return null;
        }
    }

    class NextStepTask extends DialogUtil.AsyncTaskWithDialog<Void, Void, Boolean> {
        public NextStepTask() {
            super(MovieStudioActivity.this);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            List<String> videoList = new ArrayList<>();
            for (int i = 0; i < mAdapter.items.size(); i++) {
                videoList.add(mAdapter.items.get(i).getVideoPath());
            }
            try {
                if (TextUtils.isEmpty(generateThumb())) {
                    return false;
                }
                Mp4ParserUtil.mp4Cat(videoList, getConcatedVideoPath());
                Mp4ParserUtil.overrideAudio(getConcatedVideoPath(), originVideoPath,
                        getFinalVideoPath());
//                Mp4ParserUtil.setSubtitle(getAudioedVideoPath(), originVideoPath,
//                        getFinalVideoPath());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        public String generateThumb() {
            String firstUserCaptureVideo = null;
            for (MovieStudioItem item: mAdapter.items) {
                if (!TextUtils.isEmpty(item.capturedVideoPath)) {
                    firstUserCaptureVideo = item.capturedVideoPath;
                    break;
                }
            }
            if (TextUtils.isEmpty(firstUserCaptureVideo)) {
                return null;
            }
            String thumbPath = getThumbPath();
            Bitmap thumbBmp = ImageLoader.getInstance()
                    .loadImageSync("file://" + firstUserCaptureVideo);
            File f = new File(thumbPath);
            if (f.exists()) {
                f.delete();
            }
            try {
                FileOutputStream out = new FileOutputStream(f);
                thumbBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                return thumbPath;
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Intent intent = new Intent(MovieStudioActivity.this, VideoEditActivity.class);
                intent.putExtra(VideoEditActivity.KEY_CONCATED_VIDEO, getFinalVideoPath());
                intent.putExtra(VideoEditActivity.KEY_VIDEO_THUMB, getThumbPath());
                intent.putExtra(VideoEditActivity.KEY_TOPIC_ID, mMovieTopicInfo.getTopicId());
                intent.putExtra(VideoEditActivity.KEY_IS_MOVIE, true);
                intent.putExtra(VideoEditActivity.KEY_FROM, mFrom);
                startActivityForResult(intent, REQUEST_CODE_UPLOAD_VIDEO);
            }
            else {
                Toast.makeText(MovieStudioActivity.this, R.string.there_is_an_error,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_UPLOAD_VIDEO:
                onResultUploadVideo(resultCode);
                break;
        }
    }

    private void onResultUploadVideo(int resultCode) {
        if (resultCode == VideoEditActivity.RESULT_UPLOADED) {
            onBackPressed();
        }
    }

    class CutVideoTask extends AsyncTask<Void, String, Void> {
        private String splitedFiles[];
        private Dialog cuttingDialog;

        public CutVideoTask() {
            cuttingDialog = DialogUtil.getGeneralProgressDialog(
                    MovieStudioActivity.this, R.string.cutting_video);
        }

        @Override
        protected void onPreExecute() {
            cuttingDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            originVideoPath = DownloadSynced(movieUrl);
            String[] tempFiles = new String[mMovieTopicInfo.getFragments().length];
            for (int i = 0; i < tempFiles.length; i++) {
                tempFiles[i] = getTempVideoCutPath(i);
            }
            int[] endFramePoints = new int[mMovieTopicInfo.getFragments().length];
            for (int i = 0; i < endFramePoints.length; i++) {
                endFramePoints[i] = mMovieTopicInfo.getFragments()[i].getEnd();
            }
            com.koolew.mars.videotools.Utils.
                    splitVideoByFrame(originVideoPath, endFramePoints, tempFiles);

            List<String> splitedAudioFiles = new ArrayList<>();
            for (int i = 0; i < endFramePoints.length; i++) {
                splitedAudioFiles.add(getAudioCutPath(i));
            }
            try {
                long[] splitPointInMs = new long[endFramePoints.length];
                for (int i = 0; i < splitPointInMs.length; i++) {
                    splitPointInMs[i] = endFramePoints[i] * (1000 / AppProperty.RECORD_VIDEO_FPS);
                }
                Mp4ParserUtil.splitAudioTrack(originVideoPath, splitedAudioFiles, splitPointInMs);
            } catch (IOException e) {
                e.printStackTrace();
            }

            splitedFiles = new String[endFramePoints.length];
            for (int i = 0; i < splitedFiles.length; i++) {
                splitedFiles[i] = getVideoCutPath(i);
                try {
                    Mp4ParserUtil.overrideAudio(tempFiles[i], getAudioCutPath(i), splitedFiles[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        private String DownloadSynced(String url) {
            Downloader downloader = new Downloader();
            Downloader.DownloadFuture future = new Downloader.DownloadFuture();
            downloader.download(future, url);
            return future.download();
        }

        private String getTempVideoCutPath(int index) {
            return mWorkDir + "temp-" + index + ".mp4";
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (int i = 0; i < splitedFiles.length; i++) {
                MovieTopicInfo.MovieFragment movieFragment = mMovieTopicInfo.getFragments()[i];
                int videoLenInFrames = movieFragment.getFrameCount();
                long videoLenMillis = videoLenInFrames * (1000 / AppProperty.RECORD_VIDEO_FPS);
                mAdapter.items.add(new MovieStudioItem(splitedFiles[i],
                        movieFragment.getActorName(), videoLenMillis));
            }
            mAdapter.notifyItemRangeInserted(0, splitedFiles.length);
            cuttingDialog.dismiss();
        }
    }

    private static final int STATUS_ORIGIN_CUT = 0;
    private static final int STATUS_CAPTURING = 1;
    private static final int STATUS_DONE = 2;
    class MovieStudioItem {
        private String originalVideoPath;
        private String actor;
        private long videoLen;
        private String capturedVideoPath = null;
        private int status = STATUS_ORIGIN_CUT;

        public MovieStudioItem(String videoPath, String actor, long videoLen) {
            originalVideoPath = videoPath;
            this.actor = actor;
            this.videoLen = videoLen;
        }

        public String getVideoPath() {
            if (capturedVideoPath != null) {
                return capturedVideoPath;
            }
            else {
                return originalVideoPath;
            }
        }
    }

    class MovieStudioItemAdapter extends RecyclerView.Adapter<MovieStudioItemHolder> {

        private List<MovieStudioItem> items = new ArrayList<>();
        private int selectedPosition= 0;
        private MovieStudioItemHolder selectedHolder;

        public MovieStudioItem getCurrentSelectedItem() {
            return items.get(selectedPosition);
        }

        @Override
        public MovieStudioItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MovieStudioItemHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_studio_item, null));
        }

        @Override
        public void onBindViewHolder(MovieStudioItemHolder holder, int position) {
            MovieStudioItem item = items.get(position);

            holder.actorName.setText(item.actor);
            ImageLoader.getInstance().displayImage("file://" + item.getVideoPath(), holder.thumb);

            if (position == selectedPosition) {
                if (item.status == STATUS_ORIGIN_CUT) {
                    holder.recordedPart.setX(-Utils.dpToPixels(MovieStudioActivity.this, 120));
                }
                else {
                    holder.recordedPart.setX(0);
                }
                holder.borderView.setBackgroundResource(R.drawable.movie_studio_item_selected_bg);
                selectedHolder = holder;
            }
            else {
                holder.recordedPart.setX(-Utils.dpToPixels(MovieStudioActivity.this, 120));
                if (item.status == STATUS_ORIGIN_CUT) {
                    holder.borderView.setBackgroundColor(Color.TRANSPARENT);
                }
                else if (item.status == STATUS_DONE) {
                    holder.borderView.setBackgroundResource(R.drawable.movie_studio_item_done_bg);
                }
            }

            switch (item.status) {
                case STATUS_ORIGIN_CUT:
                    holder.descText.setText("");
                    break;
                case STATUS_CAPTURING:
                    holder.descText.setText(R.string.capturing);
                    break;
                case STATUS_DONE:
                    holder.descText.setText(R.string.already_done);
                    break;
            }

            if (position == selectedPosition) {
                holder.shaderView.setVisibility(View.INVISIBLE);
            }
            else {
                holder.shaderView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public void onViewRecycled(MovieStudioItemHolder holder) {
            super.onViewRecycled(holder);
            Log.d("stdzhu", "recycle " + holder.getAdapterPosition());
        }

        @Override
        public void onViewAttachedToWindow(MovieStudioItemHolder holder) {
            super.onViewAttachedToWindow(holder);
            Log.d("stdzhu", "attached " + holder.getAdapterPosition());
        }

        @Override
        public void onViewDetachedFromWindow(MovieStudioItemHolder holder) {
            super.onViewDetachedFromWindow(holder);
            Log.d("stdzhu", "detached " + holder.getAdapterPosition());
            holder.stopPlay();
        }
    }

    class RecyclerScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE:
                    PositionAndDistance pnd = findNearestRecyclerItem();
                    if (pnd.distance != 0 || pnd.position != mAdapter.selectedPosition) {
                        changeCheckedItem(pnd.position);
                    }
                    break;
            }
        }
    }

    private static class PositionAndDistance {
        private PositionAndDistance(int position, int distance) {
            this.position = position;
            this.distance = distance;
        }
        private int position;
        private int distance;
    }

    private PositionAndDistance findNearestRecyclerItem() {
        int childCount = mRecyclerView.getChildCount();
        int minDistance = Integer.MAX_VALUE;
        int nearestViewIndex = 0;
        for (int i = 0; i < childCount; i++) {
            View child = mRecyclerView.getChildAt(i);
            int centerDistance = Math.abs((child.getLeft() + child.getRight()) / 2
                    - mRecyclerView.getMeasuredWidth() / 2);
            if (centerDistance < minDistance) {
                minDistance = centerDistance;
                nearestViewIndex = i;
            }
        }
        View nearestView = mRecyclerView.getChildAt(nearestViewIndex);
        int position = mRecyclerView.getChildViewHolder(nearestView).getAdapterPosition();
        return new PositionAndDistance(position, minDistance);
    }

    class MovieStudioItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            MediaPlayer.OnCompletionListener {

        private TextView actorName;
        private TextureView playbackTexture;
        private MediaPlayer mediaPlayer;
        private ImageView thumb;
        private View borderView;
        private View recordedPart;
        private View playImage;
        private TextView descText;
        private View shaderView;

        public MovieStudioItemHolder(View itemView) {
            super(itemView);

            actorName = (TextView) itemView.findViewById(R.id.actor_name);
            playbackTexture = (TextureView) itemView.findViewById(R.id.playback_texture);
            thumb = (ImageView) itemView.findViewById(R.id.video_thumb);
            borderView = itemView.findViewById(R.id.border_view);
            recordedPart = itemView.findViewById(R.id.recorded_part);
            playImage = itemView.findViewById(R.id.play);
            playImage.setOnClickListener(this);
            descText = (TextView) itemView.findViewById(R.id.description_text);
            shaderView = itemView.findViewById(R.id.shader_view);
            shaderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play:
                    startPlay();
                    break;
                case R.id.shader_view:
                    changeCheckedItem(getAdapterPosition());
                    break;
            }
        }

        public void startPlay() {
            startPlay(mAdapter.items.get(getAdapterPosition()).getVideoPath());
        }

        public void startPlayOriginal() {
            startPlay(mAdapter.items.get(getAdapterPosition()).originalVideoPath);
        }

        private void startPlay(String path) {
            mediaPlayer = MediaPlayer.create(MovieStudioActivity.this, Uri.parse("file://" + path));
            mediaPlayer.setSurface(new Surface(playbackTexture.getSurfaceTexture()));
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.start();
            thumb.setVisibility(View.INVISIBLE);
            playImage.setVisibility(View.INVISIBLE);
            recordedPart.setVisibility(View.INVISIBLE);
        }

        public void stopPlay() {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                thumb.setVisibility(View.VISIBLE);
                playImage.setVisibility(View.VISIBLE);
                recordedPart.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            stopPlay();
        }
    }

    class ImageOnlyRecorder extends CachedRecorder {

        opencv_core.IplImage borderedImage;

        public ImageOnlyRecorder(String filePath, int width, int height) {
            super(filePath, width, height);
        }

        @Override
        protected void initCacheQueues() {
            imageCache = new RGBARecycleQueue(100, MOVIE_CONTENT_WIDTH, MOVIE_CONTENT_HEIGHT);
            audioCache = new BlockingRecycleQueue<SamplesFrame>(100) {
                @Override
                protected SamplesFrame generateNewFrame() {
                    return null;
                }
            };
        }

        private long getCurrentLen() {
            return (lastFrameTimeStamp - firstFrameTimeStamp) / 1000;
        }

        private long getFirstFrameTimestamp() {
            return firstFrameTimeStamp;
        }

        @Override
        protected opencv_core.IplImage processImage(opencv_core.IplImage originImage) {
            if (borderedImage == null) {
                borderedImage = opencv_core.IplImage.create(
                        width, height, opencv_core.IPL_DEPTH_8U, 4);
                Log.d("stdzhu", "origin width: " + originImage.width() + ", origin height: " + originImage.height());
            }

            opencv_core.CvPoint borderOffset = opencv_core.cvPoint(
                    (width - MOVIE_CONTENT_WIDTH) / 2, (height - MOVIE_CONTENT_HEIGHT) / 2);

            opencv_imgproc.cvCopyMakeBorder(originImage, borderedImage,
                    borderOffset, opencv_core.IPL_BORDER_CONSTANT);

            return borderedImage;
        }
    }
}
