package com.koolew.mars.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jinchangzhu on 11/20/15.
 */
public class ProgressView extends View {
    private static final int PAINT_COLOR = 0xB380DFA6;

    private float progress = 0.0f;
    private Paint paint;

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(PAINT_COLOR);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float left = 0.0f;
        float top = 0.0f;
        float right = canvas.getWidth() * progress;
        float bottom = canvas.getHeight();
        canvas.drawRect(left, top, right, bottom, paint);
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public void invalidateProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public void postProgress(float progress) {
        this.progress = progress;
        postInvalidate();
    }
}
