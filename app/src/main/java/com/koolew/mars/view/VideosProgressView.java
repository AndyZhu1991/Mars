package com.koolew.mars.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.koolew.mars.R;
import com.koolew.mars.utils.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jinchangzhu on 6/15/15.
 */
public class VideosProgressView extends View {

    private static final String TAG = "koolew-VideosProgressV";

    private static final float TOTAL_LENGTH = 9.0f; // second

    private List<ProgressItem> mVideosTime;

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

        mVideosTime = new LinkedList<ProgressItem>();
        mProgressPaint = new Paint();
        mProgressPaint.setColor(getResources().getColor(R.color.progressing_color));
        mDividerPaint = new Paint();
        mDividerPaint.setColor(getResources().getColor(android.R.color.black));
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(getResources().getColor(android.R.color.white));
        mTextPaint.setTextSize(Utils.spToPixels(getContext(), 12));

        dividerWidth = 1; // px
    }

    public void start() {
        mVideosTime.add(new ProgressItem(System.currentTimeMillis()));
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
        ProgressItem lastProgress = mVideosTime.get(mVideosTime.size() - 1);
        if (lastProgress.endMillis == 0) {
            lastProgress.endMillis = System.currentTimeMillis();
        }
        else {
            throw new IllegalStateException("Progress already finished!");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = mVideosTime.size();
        Log.d(TAG, "onDraw, count = " + count);
        for (int i = 0; i < count; i++) {
            ProgressItem currentItem = mVideosTime.get(i);
            long startTime = currentItem.startMillis;
            long endTime = currentItem.endMillis == 0 ?
                    System.currentTimeMillis() : currentItem.endMillis;
            int left = getFrontPixelWidth(i);
            int top = 0;
            int right = left + getPixelByTime((endTime - startTime) / 1000.0f);
            int bottom = getHeight();

            if (right > Utils.getScreenWidthPixel(getContext())) {
                right = Utils.getScreenWidthPixel(getContext());
            }

            if (currentItem.endMillis != 0) {
                canvas.drawRect(left, top, right - dividerWidth, bottom, mProgressPaint);
                canvas.drawRect(right - dividerWidth, top, right, bottom, mDividerPaint);
            }
            else {
                canvas.drawRect(left, top, right, bottom, mProgressPaint);
            }

            if (right >= Utils.getScreenWidthPixel(getContext())) {
                break;
            }
        }

        String timeString = getResources().getString(R.string.time_text, getTotalTime());
        Rect textRect = new Rect();
        mTextPaint.getTextBounds(timeString, 0, timeString.length(), textRect);
        canvas.drawText(timeString, Utils.getScreenWidthPixel(getContext())
                - Utils.spToPixels(getContext(), 10) - textRect.width(),
                (getHeight() + textRect.height()) / 2, mTextPaint);
    }

    private int getFrontPixelWidth(int index) {
        return getPixelByTime(getFrontTime(index));
    }

    private float getTotalTime() {
        return getFrontTime(mVideosTime.size());
    }

    private float getFrontTime(int index) {
        float frontVideoTime = 0.0f;
        for (int i = 0; i < index; i++) {
            ProgressItem time = mVideosTime.get(i);
            frontVideoTime += ((0 == time.endMillis ? System.currentTimeMillis() : time.endMillis)
                                   - time.startMillis) / 1000.0f;
        }
        return frontVideoTime;
    }

    private int getPixelByTime(float time/*second*/) {
        return (int) (Utils.getScreenWidthPixel(getContext()) * (time / TOTAL_LENGTH));
    }

    class ProgressItem {
        long startMillis;
        long endMillis;

        ProgressItem() {
            startMillis = 0;
            endMillis = 0;
        }

        ProgressItem(long startMillis) {
            this.startMillis = startMillis;
            endMillis = 0;
        }

        ProgressItem(long startMillis, long endMillis) {
            this.startMillis = startMillis;
            this.endMillis = endMillis;
        }
    }
}
