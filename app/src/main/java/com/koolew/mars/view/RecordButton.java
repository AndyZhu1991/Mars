package com.koolew.mars.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.koolew.mars.utils.Utils;

/**
 * Created by jinchangzhu on 11/24/15.
 */
public class RecordButton extends View {

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_RECORDING = 1;

    private static final float START_RECT_SIZE_RATIO = 0.7f;
    private static final float END_RECT_SIZE_RATIO = 0.57f;
    private static final float START_ROUND_CORNER_RADIUS_RATIO = START_RECT_SIZE_RATIO / 2;
    private static final float END_ROUND_CORNER_RADIUS_RATIO = 8.0f / 60f;

    private int mStatus = STATUS_IDLE;
    private float mAnimationProgress = 0.0f; // 0.0表示在待拍摄状态，1.0表示拍摄中

    private Paint mRingPaint;
    private Paint mInsidePaint;

    public RecordButton(Context context) {
        this(context, null);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        mRingPaint = new Paint();
        mRingPaint.setAntiAlias(true);
        mRingPaint.setColor(getResources().getColor(android.R.color.white));
        mRingPaint.setStyle(Paint.Style.STROKE);

        mInsidePaint = new Paint();
        mInsidePaint.setAntiAlias(true);
        mInsidePaint.setColor(0xFFFF4643);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        float drawRangeRectSize = Math.min(width, height);

        float strokeWidth = Utils.dpToPixels(getContext(), 2) * (1 - mAnimationProgress * 0.5f);
        mRingPaint.setStrokeWidth(strokeWidth);
        float ringRadius = drawRangeRectSize / 2;
        canvas.drawCircle(centerX, centerY, ringRadius - strokeWidth / 2, mRingPaint);

        float rectSizeRatio = START_RECT_SIZE_RATIO * (1.0f - mAnimationProgress)
                + END_RECT_SIZE_RATIO * mAnimationProgress;
        float rectSize = rectSizeRatio * drawRangeRectSize;
        float roundCornerRadiusRatio = START_ROUND_CORNER_RADIUS_RATIO * (1.0f - mAnimationProgress)
                + END_ROUND_CORNER_RADIUS_RATIO * mAnimationProgress;
        float roundCornerRadius = roundCornerRadiusRatio * drawRangeRectSize;
        canvas.drawRoundRect(new RectF(centerX - rectSize / 2, centerY - rectSize / 2,
                        centerX + rectSize / 2, centerY + rectSize / 2),
                roundCornerRadius, roundCornerRadius, mInsidePaint);
    }

    public void setProgress(float progress) {
        mAnimationProgress = progress;
        invalidate();
    }

    public void doAnimation() {
        ObjectAnimator animator = ObjectAnimator
                .ofFloat(this, "progress", mAnimationProgress, 1.0f - mAnimationProgress)
                .setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mAnimationProgress == 0.0f) {
                    mStatus = STATUS_IDLE;
                }
                else if (mAnimationProgress == 1.0f) {
                    mStatus = STATUS_RECORDING;
                }
                else {
                    //throw new RuntimeException("不能用 == 比较float");
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }
}
