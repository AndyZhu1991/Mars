package com.koolew.mars.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.koolew.android.utils.Utils;

/**
 * Created by jinchangzhu on 7/18/15.
 */
public class VideoPieceView extends View {

    private static final float SLIDER_WIDTH_DP = 25;

    public static final long MIN_CUT_LEN = 200;

    private static final int UNSELECT_COLOR_INDEX = 0;
    private static final int SELECT_COLOR_INDEX = 1;
    private static final int[] SLIDER_BG_COLOR = new int[] { 0xFFA7A5A5, 0xFFFFFFFF };
    private static final int[] ARROW_COLOR = new int[] { 0xFF757373, 0xFFF4D288 };
    private static final int[] SELECTED_CUT_COLOR = new int[] { 0xFF757373, 0xFFF4D288 };
    private static final int UNSELECTED_CUT_COLOR = 0xFF757373;

    private long videoLen;
    private long clipStart;
    private long clipEnd;

    private boolean isSelected;

    private int sliderWidthPix;
    private int videoCutWidth;

    private float leftSliderRight;
    private float rightSliderLeft;

    private OnVideoCutListener videoCutListener;


    public VideoPieceView(Context context) {
        this(context, null);
    }

    public VideoPieceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPieceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        videoLen = 1;
        clipStart = 0;
        clipEnd = 1;
        isSelected = false;

        sliderWidthPix = (int) Utils.dpToPixels(SLIDER_WIDTH_DP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int videoCutStart = sliderWidthPix;
        videoCutWidth = width - sliderWidthPix * 2;

        int colorIndex = isSelected ? SELECT_COLOR_INDEX : UNSELECT_COLOR_INDEX;
        Paint sliderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sliderPaint.setColor(SLIDER_BG_COLOR[colorIndex]);
        Paint selectedCutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedCutPaint.setColor(SELECTED_CUT_COLOR[colorIndex]);

        float videoCutAreaLeft = videoCutStart + videoCutWidth * startPos();
        float videoCutAreaRight = videoCutStart + videoCutWidth * endPos();
        Log.d("stdzhu", "videoCutAreaLeft: " + videoCutAreaLeft + ", startPos: " + startPos() +
                ", videoCutAreaRight: " + videoCutAreaRight + ", endPos: " + endPos());

        // Draw left slider
        leftSliderRight = videoCutAreaLeft;
        canvas.drawRect(leftSliderLeft(), 0, leftSliderRight, height, sliderPaint);

        // Draw video cut area
        canvas.drawRect(videoCutAreaLeft, 0, videoCutAreaRight, height, selectedCutPaint);

        // Draw right slider
        rightSliderLeft = videoCutAreaRight;
        canvas.drawRect(rightSliderLeft, 0, rightSliderRight(), height, sliderPaint);
    }

    private float startPos() {
        return 1.0f * clipStart / videoLen;
    }

    private float endPos() {
        return 1.0f * clipEnd / videoLen;
    }

    private float leftSliderLeft() {
        return leftSliderRight - sliderWidthPix;
    }

    private float rightSliderRight() {
        return rightSliderLeft + sliderWidthPix;
    }

    private float minClipPix() {
        return videoCutWidth * (1.0f * MIN_CUT_LEN / videoLen);
    }


    private static final int NO_CUTTING = 0;
    private static final int CUTTING_LEFT = 1;
    private static final int CUTTING_RIGHT = 2;

    private int cuttingStatus = NO_CUTTING;
    private float startX;
    private float startSliderRefrenceX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isSelected) {
            return super.onTouchEvent(event);
        }
        Log.d("stdzhu", "action: " + event.getAction() + ", x: " + event.getX());
        if (cuttingStatus == NO_CUTTING) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                if (x > leftSliderLeft() && x < leftSliderRight) {
                    startCut(CUTTING_LEFT);
                    startX = x;
                    startSliderRefrenceX = leftSliderRight;
                    return true;
                }
                else if (x > rightSliderLeft && x < rightSliderRight()) {
                    startCut(CUTTING_RIGHT);
                    startX = x;
                    startSliderRefrenceX = rightSliderLeft;
                    return true;
                }
            }
        }
        else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    handleTouchMove(event.getX());
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
//                    if (cuttingStatus == CUTTING_LEFT) {
//                        videoCutListener.onVideoStartCut(clipStart);
//                    }
//                    else {
//                        videoCutListener.onVideoEndCut(clipEnd);
//                    }
                    endCut();
                    return true;
            }
        }

        Log.d("stdzhu", "super.onTouchEvent");
        return super.onTouchEvent(event);
    }

    private static final float OVER_DRAG_DELETE_RATIO = 0.5f;
    private void handleTouchMove(float x) {
        if (cuttingStatus == CUTTING_LEFT) {
            float xDiff = x - startX;
            // 判断是否超出左边界
            if (startSliderRefrenceX + xDiff < sliderWidthPix) {
                leftSliderRight = sliderWidthPix;
            }
            // 判断是否超出右边的slider
            else if (startSliderRefrenceX + xDiff > rightSliderLeft - minClipPix()) {
                leftSliderRight = rightSliderLeft - minClipPix();
                if (startSliderRefrenceX + xDiff >
                        rightSliderLeft - minClipPix() * OVER_DRAG_DELETE_RATIO) {
//                    clipStart = (long) ((leftSliderRight - sliderWidthPix)
//                            / videoCutWidth * videoLen);
//                    videoCutListener.onVideoStartCut(clipStart);
                    // 继续向右拖动的时候才会提示删除
                    videoCutListener.onVideoDeleteCut();
                    endCut();
                }
            }
            else {
                leftSliderRight = startSliderRefrenceX + xDiff;
            }
            clipStart = (long) ((leftSliderRight - sliderWidthPix)
                    / videoCutWidth * videoLen);
            invalidate();
            videoCutListener.onVideoStartCut(clipStart);
        }
        else if (cuttingStatus == CUTTING_RIGHT) {
            float xDiff = x - startX;
            // 判断是否超出右边界
            if (startSliderRefrenceX + xDiff > videoCutWidth + sliderWidthPix) {
                rightSliderLeft = videoCutWidth + sliderWidthPix;
            }
            // 判断是否超出左边的slider
            else if (startSliderRefrenceX + xDiff < leftSliderRight + minClipPix()) {
                rightSliderLeft = leftSliderRight + minClipPix();
                if (startSliderRefrenceX + xDiff <
                        leftSliderRight + minClipPix() * OVER_DRAG_DELETE_RATIO) {
//                    clipEnd = (long) ((rightSliderLeft - sliderWidthPix)
//                            / videoCutWidth * videoLen);
//                    videoCutListener.onVideoEndCut(clipEnd);
                    // 继续向左拖动的时候才会提示删除
                    videoCutListener.onVideoDeleteCut();
                    endCut();
                }
            }
            else {
                rightSliderLeft = startSliderRefrenceX + xDiff;
            }
            clipEnd = (long) ((rightSliderLeft - sliderWidthPix)
                    / videoCutWidth * videoLen);
            invalidate();
            videoCutListener.onVideoEndCut(clipEnd);
        }

        Log.d("stdzhu", "leftSliderLeft: " + leftSliderLeft() + ", leftSliderRight: " + leftSliderRight +
                ", rightSliderLeft: " + rightSliderLeft + ", rightSliderRight: " + rightSliderRight() +
                ", videoCutWidth: " + videoCutWidth);
        Log.d("stdzhu", "clipStart: " + clipStart + ", clipEnd: " + clipEnd + ", videoLen: " + videoLen);
    }

    private void startCut(int cuttingType) {
        this.cuttingStatus = cuttingType;
        videoCutListener.onStartCut();
    }

    private void endCut() {
        this.cuttingStatus = NO_CUTTING;
        videoCutListener.onEndCut();
    }

    public void setVideoStatus(long videoLen, long clipStart, long clipEnd) {
        this.videoLen = videoLen;
        this.clipStart = clipStart;
        this.clipEnd = clipEnd;

        invalidate();
    }

    public void setSelected(boolean selected) {
        isSelected = selected;

        invalidate();
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setVideoCutListener(OnVideoCutListener videoCutListener) {
        this.videoCutListener = videoCutListener;
    }

    public interface OnVideoCutListener {
        /**
         * Called when start slide the slider
         */
        void onStartCut();

        /**
         * Called when drop the slider
         */
        void onEndCut();

        /**
         * Called when the video's start is located
         * @param clipStart
         */
        void onVideoStartCut(long clipStart);

        /**
         * Called when the video's end is located
         * @param clipEnd
         */
        void onVideoEndCut(long clipEnd);

        /**
         * Called when the video will be deleted
         */
        void onVideoDeleteCut();
    }
}
