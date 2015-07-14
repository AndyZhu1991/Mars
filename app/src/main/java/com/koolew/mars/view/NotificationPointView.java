package com.koolew.mars.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.koolew.mars.utils.Utils;

/**
 * Created by jinchangzhu on 7/14/15.
 */
public class NotificationPointView extends TextView {

    public static final int COUNT_UNKNOWN = 0;

    private Paint mPaint;
    private Rect mBgSquare;

    public NotificationPointView(Context context) {
        this(context, null);
    }

    public NotificationPointView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationPointView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint();
        mPaint.setColor(0xFFFF5656);
        mPaint.setAntiAlias(true);

        setGravity(Gravity.CENTER);
        setTextColor(getResources().getColor(android.R.color.white));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mBgSquare = Utils.getMaxCenterSquare(new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight()));

        setTextSize(Utils.pixelsToSp(getContext(), mBgSquare.width() * 2 / 3));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mBgSquare.left + mBgSquare.width() / 2,
                mBgSquare.top + mBgSquare.height() / 2, mBgSquare.width() / 2, mPaint);

        super.onDraw(canvas);
    }

    public void setCount(int count) {
        if (count == COUNT_UNKNOWN) {
            setText("");
        }
        else {
            setText(String.valueOf(count));
        }
    }
}
