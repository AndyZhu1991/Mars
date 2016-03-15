package com.koolew.mars.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.koolew.android.utils.FileUtil;
import com.koolew.android.videotools.RealTimeRgbaRecorderWithAutoAudio;
import com.koolew.mars.AppProperty;
import com.koolew.mars.MarsApplication;
import com.koolew.mars.R;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.android.mp4parserutil.Mp4ParserUtil;
import com.koolew.android.utils.Utils;
import com.koolew.mars.utils.ViewUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import at.aau.itec.android.mediaplayer.FileSource;
import at.aau.itec.android.mediaplayer.MediaPlayer;

/**
 * Created by jinchangzhu on 9/10/15.
 */
public class RecordingSessionView extends LinearLayout {

    public static final String CONCATED_VIDEO_NAME = "concated.mp4";
    public static final String THUMB_NAME = "thumb.jpg";

    private VideosProgressView videosProgressView;
    private RecyclerView recyclerView;
    private View shader;

    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    //private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private VideoItemAdapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;

    private RecordingItem recordingItem;
    private List<VideoPieceItem> recordedItems;

    private Listener listener;
    private MediaPlayer mediaPlayer;

    private String workingDir;


    public RecordingSessionView(Context context) {
        this(context, null);
    }

    public RecordingSessionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordingSessionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);

        videosProgressView = new VideosProgressView(context);
        addView(videosProgressView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) Utils.dpToPixels(16)));

        FrameLayout frameLayout = new FrameLayout(context);
        recyclerView = new RecyclerView(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent e) {
                if (!isEnabled()) {
                    return false;
                }
                return super.onInterceptTouchEvent(e);
            }
        };
        recyclerView.setPadding(0, 0, 0, (int) Utils.dpToPixels(20));
        recyclerView.setClipToPadding(false);
        frameLayout.addView(recyclerView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        shader = new View(context);
        shader.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) { // Interrupt all input
            }
        });
        shader.setBackgroundColor(0xFF000000);
        shader.setAlpha(0.5f);
        frameLayout.addView(shader, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        shader.setVisibility(INVISIBLE);
        addView(frameLayout, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        initRecyclerView(context);

        initWorkingDir();

        initMembers();
    }

    private void initRecyclerView(Context context) {
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setInitiateOnLongPress(true);
        //mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        mAdapter = new VideoItemAdapter();
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter);
        //mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mWrappedAdapter);

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(recyclerView);
        //mRecyclerViewSwipeManager.attachRecyclerView(recyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(recyclerView);
    }

    private void initWorkingDir() {
        workingDir = Utils.getCacheDir() + File.separator + System.currentTimeMillis();
        new Thread() {
            @Override
            public void run() {
                new File(workingDir).mkdirs();
            }
        }.start();
    }

    private void initMembers() {
        recordedItems = new ArrayList<>();
    }

    private boolean playerPausedByActivity = false;
    public void onActivityResume() {
        if (playerPausedByActivity && mediaPlayer != null) {
            mediaPlayer.start();
            playerPausedByActivity = false;
        }
    }

    public void onActivityPause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playerPausedByActivity = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }

        deleteSession();
    }

    public void switchToPreviewMode() {
        // deselect all recorded items
        for (int i = 0; i < recordedItems.size(); i++) {
            if (recordedItems.get(i).isSelected) {
                recordedItems.get(i).isSelected = false;
                mAdapter.notifyItemChanged(i);
                videosProgressView.invalidate();
            }
        }
    }

    public void startOneRecording(RecordingItem recordingItem) {
        this.recordingItem = recordingItem;
        shader.setVisibility(VISIBLE);
        videoProgressUpdateTimer = new Timer();
        videoProgressUpdateTimer.schedule(new UpdateVideoProgressTask(), 17, 17);
    }

    public void postStopRecording() {
        new CompleteRecordingTask().execute();
    }

    public void addOneItem(String filePath, long videoLen) {
        recordedItems.add(new VideoPieceItem(System.currentTimeMillis(), filePath, videoLen));
        mAdapter.notifyItemInserted(recordedItems.size() - 1);
        videosProgressView.invalidate();
        updateNextStepBtnStatus();
    }

    public void invalidateProgressView() {
        videosProgressView.invalidate();
    }

    private Timer videoProgressUpdateTimer;
    class UpdateVideoProgressTask extends TimerTask {
        @Override
        public void run() {
            post(new Runnable() {
                @Override
                public void run() {
                    videosProgressView.invalidate();
                }
            });
        }
    }

    class CompleteRecordingTask extends AsyncTask<Void, Void, Void> {
        private Dialog waitDialog;

        @Override
        protected void onPreExecute() {
            waitDialog = DialogUtil.getGeneralProgressDialog(getContext(),
                    R.string.please_wait_a_moment);
            waitDialog.show();
            videoProgressUpdateTimer.cancel();
        }

        @Override
        protected Void doInBackground(Void... params) {
            recordedItems.add(recordingItem.completeSynced());
            recordingItem = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyItemInserted(recordedItems.size() - 1);
            updateNextStepBtnStatus();
            shader.setVisibility(INVISIBLE);
            try {
                waitDialog.dismiss();
            } catch (Exception e) { // Why?
                if (MarsApplication.DEBUG) {
                    throw e;
                }
            }
        }
    }

    private void updateNextStepBtnStatus() {
        if (recordedItems.size() == 0) {
            listener.onNextStepEnable(false, getContext().getString(R.string.there_is_no_video));
        }
        else if (getTotalVideoLen() > AppProperty.getRecordVideoMaxLen() * 1000) {
            listener.onNextStepEnable(false, getContext().getString(
                    R.string.video_too_long, (int) AppProperty.getRecordVideoMaxLen()));
        }
        else {
            listener.onNextStepEnable(true, null);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        mediaPlayer.setOnSeekCompleteListener(new SeekControlListener());
    }

    public long getFrontVideoLen(int endPos) {
        long totalLen = 0;
        for (int i = 0; i < endPos; i++) {
            totalLen += recordedItems.get(i).getClipedVideoLen();
        }
        return totalLen;
    }

    public long getTotalVideoLen() {
        return getFrontVideoLen(recordedItems.size());
    }

    public int getVideoCount() {
        return recordedItems.size();
    }

    public int getSelectedItemPosition() {
        for (int i = 0; i < recordedItems.size(); i++) {
            if (recordedItems.get(i).isSelected) {
                return i;
            }
        }
        return -1;
    }

    public void play() {
        int selectedItemPosition = getSelectedItemPosition();
        if (selectedItemPosition < 0) {
            return;
        }
        VideoPieceItem item = recordedItems.get(selectedItemPosition);
        try {
            mediaPlayer.setDataSource(new FileSource(new File(item.fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.seekTo((int) item.clipStart);
        mediaPlayer.setOnSeekCompleteListener(new AutoStartSeekListener((int) item.clipEnd));
    }

    class AutoStartSeekListener implements MediaPlayer.OnSeekCompleteListener {
        private int stopPosition;

        public AutoStartSeekListener(int stopPosition) {
            this.stopPosition = stopPosition;
        }

        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            mediaPlayer.setOnSeekCompleteListener(new SeekControlListener());
            mediaPlayer.start();
            mStopPlayListener = new StopPlayListener(mediaPlayer, stopPosition);
            mStopPlayListener.start();
        }
    }

    private StopPlayListener mStopPlayListener;

    class StopPlayListener extends Thread {
        private MediaPlayer mediaPlayer;
        private int stopPosition;
        private boolean isCanceled = false;

        public StopPlayListener(MediaPlayer mediaPlayer, int stopPosition) {
            this.mediaPlayer = mediaPlayer;
            this.stopPosition = stopPosition;
        }

        public void cancel() {
            isCanceled = true;
        }

        @Override
        public void run() {
            while (!isCanceled && mediaPlayer.isPlaying()) {
                if (mediaPlayer.getCurrentPosition() >= stopPosition) {
                    mediaPlayer.pause();
                    listener.onPlayComplete();
                    break;
                }
                else {
                    try {
                        sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (this == mStopPlayListener) {
                mStopPlayListener = null;
            }
        }
    }

    private boolean isSeeking = false;
    private int nextSeekPosition = -1;
    class SeekControlListener implements MediaPlayer.OnSeekCompleteListener {

        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            isSeeking = false;
            if (nextSeekPosition > 0) {
                mediaPlayer.seekTo(nextSeekPosition);
                nextSeekPosition = -1;
            }
        }
    }


    public String generateAVideoFilePath() {
        return new StringBuilder(workingDir)
                .append(File.separator)
                .append(System.currentTimeMillis())
                .append(".mp4")
                .toString();
    }

    public void concatVideo() {
        if (recordedItems.size() == 0) {
            throw new RuntimeException("Zero videos to concat!");
        }

        List<String> videos = new LinkedList<>();
        for (VideoPieceItem videoItem: recordedItems) {
            String cutted;
            if (videoItem.clipStart != 0 || videoItem.clipEnd != videoItem.videoLen || isMi3()) {
                cutted = videoItem.fileName + ".mp4";
                com.koolew.android.videotools.Utils.cutVideo(videoItem.fileName, cutted,
                        videoItem.clipStart, videoItem.clipEnd);
            }
            else {
                cutted = videoItem.fileName;
            }
            videos.add(cutted);
        }

        String concatedFilePath = getConcatedVideoName();
        try {
            if (videos.size() == 1) {
                FileUtil.copyFile(videos.get(0), concatedFilePath);
            }
            else {
                Mp4ParserUtil.mp4Cat(videos, concatedFilePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isMi3() {
        String model = Build.MODEL;
        if (model.startsWith("MI 3")) {
            return true;
        }
        else {
            return false;
        }
    }

    public String getConcatedVideoName() {
        return new StringBuilder(workingDir)
                .append(File.separator)
                .append(CONCATED_VIDEO_NAME)
                .toString();
    }

    public void generateThumb() {
        com.koolew.android.videotools.Utils.saveVideoFrame(
                getConcatedVideoName(), getThumbName());
    }

    public String getThumbName() {
        return new StringBuilder(workingDir)
                .append(File.separator)
                .append(THUMB_NAME)
                .toString();
    }

    private void deleteVideoItem(int position) {
        recordedItems.remove(position);
        mAdapter.notifyItemRemoved(position);
        videosProgressView.invalidate();
        if (recordedItems.size() == 0) {
            listener.onNextStepEnable(false, getContext().getString(R.string.there_is_no_video));
            listener.onSwitchToPreviewMode();
        }
        else {
            selectVideoItem(position == 0 ? 0 : position - 1);
        }
    }

    public void deleteSession() {
        new Thread() {
            @Override
            public void run() {
                FileUtil.deleteFileOrDir(workingDir);
            }
        }.start();
    }

    private void selectVideoItem(int position) {
        try {
            VideoPieceItem item = recordedItems.get(position);
            if (item.isSelected) {
                return;
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mStopPlayListener.cancel();
                listener.onPlayComplete();
            }
            mediaPlayer.setDataSource(new FileSource(new File(item.fileName)));
            mediaPlayer.seekTo((int) item.clipStart);
            for (int i = 0; i < recordedItems.size(); i++) {
                if (recordedItems.get(i).isSelected != (i == position)) {
                    recordedItems.get(i).isSelected = (i == position);
                    mAdapter.notifyItemChanged(i);
                }
            }
            videosProgressView.invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public interface RecordingItem {
        long getCurrentLength();
        VideoPieceItem completeSynced();
    }

    public static class VideoPieceItem {
        public long id;
        public String fileName;
        public long videoLen;
        public long clipStart;
        public long clipEnd;
        public boolean isSelected;

        public VideoPieceItem(long id, String fileName, long videoLen) {
            this.id = id;
            this.fileName = fileName;
            this.videoLen = videoLen;
            clipStart = 0;
            clipEnd = videoLen;
            isSelected = false;
        }

        public long getClipedVideoLen() {
            return clipEnd - clipStart;
        }
    }

    public interface Listener {
        void onSwitchToPreviewMode();
        void onSwitchToPlaybackMode();
        void onNextStepEnable(boolean enable, String hint);
        void onPlayComplete();
    }


    class VideoItemAdapter extends RecyclerView.Adapter<ViewHolder>
            implements DraggableItemAdapter<ViewHolder> {

        private Set<View> mVideoPieces;

        public VideoItemAdapter() {
            mVideoPieces = new HashSet<>();
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return recordedItems.get(position).id;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.record_video_item, viewGroup, false);

            ViewHolder viewHolder = new ViewHolder(itemView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            ImageLoader.getInstance().displayImage(
                    "file://" + recordedItems.get(position).fileName,
                    viewHolder.thumbImage, null, null, null);
            VideoPieceItem item = recordedItems.get(position);
            viewHolder.pieceView.setVideoStatus(item.videoLen, item.clipStart, item.clipEnd);
            viewHolder.pieceView.setSelected(item.isSelected);

            mVideoPieces.add(viewHolder.pieceView);
        }

        @Override
        public int getItemCount() {
            return recordedItems.size();
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

            recordedItems.add(toPosition, recordedItems.remove(fromPosition));

            notifyItemMoved(fromPosition, toPosition);
            videosProgressView.invalidate();
            for (View piece: mVideoPieces) {
                piece.invalidate();
            }
        }
    }

    class ViewHolder extends AbstractDraggableItemViewHolder
            implements VideoPieceView.OnVideoCutListener, View.OnClickListener {

        View container;
        ImageView thumbImage;
        VideoPieceView pieceView;
        ImageView dragHandle;

        public ViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            thumbImage = (ImageView) itemView.findViewById(R.id.video_thumb);
            thumbImage.setOnClickListener(this);
            pieceView = (VideoPieceView) itemView.findViewById(R.id.piece_view);
            pieceView.setOnClickListener(this);
            pieceView.setVideoCutListener(this);
            dragHandle = (ImageView) itemView.findViewById(R.id.drag_handle);
        }
//
//            @Override
//            public View getSwipeableContainerView() {
//                return container;
//            }

        @Override
        public void onStartCut() {
            recyclerView.setEnabled(false);
        }

        @Override
        public void onEndCut() {
            recyclerView.setEnabled(true);
        }

        @Override
        public void onVideoStartCut(long clipStart) {
            int position = getAdapterPosition();
            recordedItems.get(position).clipStart = clipStart;
            videosProgressView.invalidate();
            tryToSeek((int) clipStart);
            updateNextStepBtnStatus();
        }

        @Override
        public void onVideoEndCut(long clipEnd) {
            int position = getAdapterPosition();
            recordedItems.get(position).clipEnd = clipEnd;
            videosProgressView.invalidate();
            tryToSeek((int) clipEnd);
            updateNextStepBtnStatus();
        }

        private void tryToSeek(int position) {
            if (!isSeeking) {
                isSeeking = true;
                for (int i = 0; i < 10; i++) { // Try 10 times
                    try {
                        mediaPlayer.seekTo(position);
                        break;
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
            else {
                nextSeekPosition = position;
            }
        }

        @Override
        public void onVideoDeleteCut() {
            new AlertDialog.Builder(getContext())
                    .setMessage(getContext().getString(R.string.too_short_to_delete))
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteVideoItem(getAdapterPosition());
                        }
                    })
                    .setNegativeButton(R.string.retain, null)
                    .show();
        }

        @Override
        public void onClick(View v) {
            listener.onSwitchToPlaybackMode();
            selectVideoItem(getAdapterPosition());
        }
    }

    class VideosProgressView extends View {

        private Paint mProgressPaint;
        private Paint mSelectedPaint;
        private Paint mOverLengthPaint;
        private Paint mDividerPaint;
        private Paint mTextPaint;

        private int dividerWidth;


        public VideosProgressView(Context context) {
            this(context, null);
        }

        public VideosProgressView(Context context, AttributeSet attrs) {
            super(context, attrs);

            setBackgroundColor(getResources().getColor(android.R.color.black));

            mProgressPaint = new Paint();
            mProgressPaint.setColor(getResources().getColor(R.color.progressing_color));
            mSelectedPaint = new Paint();
            mSelectedPaint.setColor(getResources().getColor(R.color.progressing_color_selected));
            mOverLengthPaint = new Paint();
            mOverLengthPaint.setColor(getResources().getColor(R.color.video_over_length_color));
            mDividerPaint = new Paint();
            mDividerPaint.setColor(getResources().getColor(android.R.color.black));
            mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTextPaint.setColor(getResources().getColor(android.R.color.white));
            mTextPaint.setTextSize(Utils.spToPixels(12));

            dividerWidth = (int) Utils.dpToPixels(1);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            long currentRecordingLength = (recordingItem == null) ?
                    0 : recordingItem.getCurrentLength();
            float videoLenWithRecording =
                    (getFrontVideoLen(recordedItems.size()) + currentRecordingLength) / 1000.0f;

            Paint progressBarPaint = videoLenWithRecording <= AppProperty.getRecordVideoMaxLen()
                    ? mProgressPaint : mOverLengthPaint;

            int count = recordedItems.size();
            for (int i = 0; i < count; i++) {
                int left = millis2Pixels(getFrontVideoLen(i));
                int top = 0;
                int right = left + millis2Pixels(recordedItems.get(i).getClipedVideoLen());
                int bottom = getHeight();

                if (right > getWidth()) {
                    right = getWidth();
                }

                canvas.drawRect(left, top, right - dividerWidth, bottom,
                        recordedItems.get(i).isSelected ? mSelectedPaint : progressBarPaint);
                canvas.drawRect(right - dividerWidth, top, right, bottom, mDividerPaint);

                if (right >= Utils.getScreenWidthPixel()) {
                    break;
                }
            }

            int left = millis2Pixels(getFrontVideoLen(count));
            if (left < getWidth() && recordingItem != null) {
                int top = 0;
                int right = left + millis2Pixels(recordingItem.getCurrentLength());
                int bottom = getHeight();
                canvas.drawRect(left, top, right, bottom, progressBarPaint);
            }

            String timeString = getResources().getString(R.string.video_shoot_time_text,
                    (getFrontVideoLen(recordedItems.size()) + currentRecordingLength) / 1000.0f,
                    AppProperty.getRecordVideoMaxLen());
            Rect textRect = new Rect();
            mTextPaint.getTextBounds(timeString, 0, timeString.length(), textRect);
            canvas.drawText(timeString, getWidth() - Utils.spToPixels(10) - textRect.width(),
                    (getHeight() + textRect.height()) / 2, mTextPaint);
        }

        private int millis2Pixels(long millis) {
            long totalLength = (long) (AppProperty.getRecordVideoMaxLen() * 1000);
            return (int) (getWidth() * (1.0f * millis / totalLength));
        }
    }

    public static class RealTimeRecorderItem extends RealTimeRgbaRecorderWithAutoAudio implements RecordingItem {

        public RealTimeRecorderItem(String filePath, int width, int height) {
            super(filePath, width, height);
        }

        @Override
        public long getCurrentLength() {
            return (lastFrameTimeStamp - firstFrameTimeStamp) / 1000;
        }

        @Override
        public VideoPieceItem completeSynced() {
            stopSynced();
            return new RecordingSessionView.VideoPieceItem(
                    System.currentTimeMillis(), filePath, getCurrentLength());
        }
    }
}
