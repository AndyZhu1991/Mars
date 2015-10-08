package com.koolew.mars.redpoint;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.koolew.mars.R;
import com.koolew.mars.utils.Utils;

/**
 * Created by jinchangzhu on 10/7/15.
 */
public class RedPointView extends TextView {

    private Paint mPaint;
    private Rect mBgSquare;

    private int mCount = 0;
    private boolean isNeedNum = false;
    private String mRedPointPath;
    private boolean mRedPointVisiable = false;

    public RedPointView(Context context) {
        this(context, null);
    }

    public RedPointView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RedPointView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint();
        mPaint.setColor(0xFFFF5656);
        mPaint.setAntiAlias(true);

        setGravity(Gravity.CENTER);
        setTextColor(getResources().getColor(android.R.color.white));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RedPointView, 0, 0);
        setNeedNum(a.getBoolean(R.styleable.RedPointView_needCountNum, false));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mBgSquare = Utils.getMaxCenterSquare(new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight()));

        setTextSize(Utils.pixelsToSp(getContext(), mBgSquare.width() * 2 / 3));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRedPointVisiable) {
            canvas.drawCircle(mBgSquare.left + mBgSquare.width() / 2,
                    mBgSquare.top + mBgSquare.height() / 2, mBgSquare.width() / 2, mPaint);
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterPath();
    }

    public void registerPath(String path) {
        mRedPointPath = path;
        RedPointManager.register(path, this);
    }

    public void unregisterPath() {
        if (mRedPointPath != null) {
            RedPointManager.unregister(mRedPointPath);
            mRedPointPath = null;
            mRedPointVisiable = false;
            invalidate();
        }
    }

    public void setCount(int count) {
        mCount = count;
        if (count > 0) {
            mRedPointVisiable = true;
            if (isNeedNum) {
                setText(String.valueOf(count));
            }
        }
        else {
            mRedPointVisiable = false;
        }
        invalidate();
    }

    public void setNeedNum(boolean isNeedNum) {
        this.isNeedNum = isNeedNum;
        if (isNeedNum) {
            setText(String.valueOf(mCount));
        }
        else {
            setText("");
        }
    }
}
