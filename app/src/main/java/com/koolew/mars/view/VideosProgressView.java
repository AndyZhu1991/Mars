package com.koolew.mars.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.koolew.mars.AppProperty;
import com.koolew.mars.R;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.video.VideoRecordingSession;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jinchangzhu on 6/15/15.
 */
public class VideosProgressView extends View {

    private static final String TAG = "koolew-VideosProgressV";

    private static final long TOTAL_LENGTH = (long) (AppProperty.RECORD_VIDEO_MAX_LEN * 1000);

    private VideoRecordingSession mRecordingSession;

    private long mCurrentRecordingStartMillis = 0;

    private Paint mProgressPaint;
    private Paint mDividerPaint;
    private Paint mTextPaint;

    private int dividerWidth;

    private Timer mTimer;


    public VideosProgressView(Context context) {
        this(context, null);
    }

    public VideosProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setBackgroundColor(getResources().getColor(android.R.color.black));

        mProgressPaint = new Paint();
        mProgressPaint.setColor(getResources().getColor(R.color.progressing_color));
        mDividerPaint = new Paint();
        mDividerPaint.setColor(getResources().getColor(android.R.color.black));
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(getResources().getColor(android.R.color.white));
        mTextPaint.setTextSize(Utils.spToPixels(getContext(), 12));

        dividerWidth = 1; // px
    }

    public void setRecordingSession(VideoRecordingSession recordingSession) {
        mRecordingSession = recordingSession;
    }

    public void start() {
        mCurrentRecordingStartMillis = System.currentTimeMillis();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                postInvalidate();
            }
        }, 17, 17);
    }

    public void finish() {
        mTimer.cancel();
        if (mCurrentRecordingStartMillis != 0) {
            mCurrentRecordingStartMillis = 0;
        }
        else {
            throw new IllegalStateException("Progress already finished!");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRecordingSession == null) {
            return;
        }

        int count = mRecordingSession.getVideoCount();
        for (int i = 0; i < count; i++) {
            int left = millis2Pixels(mRecordingSession.getFrontVideoLength(i));
            int top = 0;
            int right = left + millis2Pixels(mRecordingSession.get(i).getVideoLength());
            int bottom = getHeight();

            if (right > getWidth()) {
                right = getWidth();
            }

            canvas.drawRect(left, top, right - dividerWidth, bottom, mProgressPaint);
            canvas.drawRect(right - dividerWidth, top, right, bottom, mDividerPaint);

            if (right >= Utils.getScreenWidthPixel(getContext())) {
                break;
            }
        }

        if (mCurrentRecordingStartMillis != 0) {
            int left = millis2Pixels(mRecordingSession.getFrontVideoLength(count));
            if (left < getWidth()) {
                int top = 0;
                int right = left + millis2Pixels(
                        System.currentTimeMillis() - mCurrentRecordingStartMillis);
                int bottom = getHeight();
                canvas.drawRect(left, top, right, bottom, mProgressPaint);
            }
        }

        long currentRecordingLength = mCurrentRecordingStartMillis == 0 ? 0
                : System.currentTimeMillis() - mCurrentRecordingStartMillis;
        String timeString = getResources().getString(R.string.time_text,
                (mRecordingSession.getTotalVideoLength() + currentRecordingLength) / 1000.0f);
        Rect textRect = new Rect();
        mTextPaint.getTextBounds(timeString, 0, timeString.length(), textRect);
        canvas.drawText(timeString, getWidth() - Utils.spToPixels(getContext(), 10) - textRect.width(),
                (getHeight() + textRect.height()) / 2, mTextPaint);
    }

    private int millis2Pixels(long millis) {
        return (int) (getWidth() * (1.0f * millis / TOTAL_LENGTH));
    }
}
