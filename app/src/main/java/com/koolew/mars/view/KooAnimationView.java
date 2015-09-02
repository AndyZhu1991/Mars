package com.koolew.mars.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.koolew.mars.utils.Utils;

import java.util.Random;

/**
 * Created by jinchangzhu on 8/20/15.
 */
public class KooAnimationView extends View {

    private static final int DEFAULT_PARTICLE_COUNT = 24;
    private static final int DEFAULT_PARTICLE_COLOR = 0xFFFA9193;
    private static final float DEFAULT_PARTICLE_RADIUS_DP = 1.5f;
    private static final float DEFAULT_START_RATIO = 0.0f;

    private int mParticleCount;
    private int mParticleColor;
    private float mParticleRadius;
    private float mStartRatio; // 0.0 ~ 1.0

    private float mAnimationProgress; // 0.0 ~ 1.0

    private Particle[] mParticles;

    private Paint mPaint;

    private boolean isAnimating;

    public KooAnimationView(Context context) {
        this(context, null);
    }

    public KooAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KooAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mParticleCount = DEFAULT_PARTICLE_COUNT;
        mParticleColor = DEFAULT_PARTICLE_COLOR;
        mParticleRadius = Utils.dpToPixels(context, DEFAULT_PARTICLE_RADIUS_DP);
        mStartRatio = DEFAULT_START_RATIO;
        generateParticles();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isAnimating) {
            return;
        }

        Rect canvasRect = canvas.getClipBounds();
        int centerX = canvasRect.centerX();
        int centerY = canvasRect.centerY();
        calcParticlePositions(canvasRect);
        for (Particle particle: mParticles) {
            mPaint.setColor(particle.color);
            canvas.drawCircle(centerX + particle.x, centerY + particle.y, particle.radius, mPaint);
        }
    }

    public void setProgress(float progress) {
        mAnimationProgress = progress;
        invalidate();
    }

    public void startAnimation() {
        ObjectAnimator animator = ObjectAnimator
                .ofFloat(this, "progress", 0.0f, 1.0f)
                .setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                generateParticleRevises();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }

    private void generateParticles() {
        mParticles = new Particle[mParticleCount];
        for (int i = 0; i < mParticles.length; i++) {
            mParticles[i] = new Particle();
            mParticles[i].setColor(mParticleColor);
            mParticles[i].setRadius(mParticleRadius);
        }
        initParticleRadians();
    }

    private void initParticleRadians() {
        float fullCircleRadian = (float) (Math.PI * 2);
        for (int i = 0; i < mParticleCount; i++) {
            mParticles[i].radian = fullCircleRadian / mParticleCount * i;
        }
    }

    private void calcParticlePositions(Rect drawRect) {
        int drawRadius = Math.min(drawRect.width(), drawRect.height()) / 2;
        int startRadius = (int) (drawRadius * mStartRatio);
        int particleDistance = drawRadius - startRadius;

        for (Particle particle: mParticles) {
            float particleProgress = getParticleProgress();
            float particleFromCenter = startRadius + particleProgress * particleDistance;
            int x = (int) (Math.cos(particle.radian) * particleFromCenter);
            int y = (int) (Math.sin(particle.radian) * particleFromCenter);
            particle.setPosition(x, y);
        }
    }

    private void generateParticleRevises() {
        for (Particle particle: mParticles) {
            particle.generateRevises();
        }
    }

    /**
     *
     * @return 0.0 ~ 1.0 , the particle's progress by mAnimationProgress
     */
    private float getParticleProgress() {
        return (float) Math.sqrt(mAnimationProgress / 9) * 3;
    }

    private static final int UNSTABLE_PERCENTAGE_POSITION = 60;
    private static final int UNSTABLE_PERCENTAGE_RADIUS = 40;

    class Particle {
        private int x;
        private int y;
        private float radius;
        private int color;
        private float radian;

        private float positionRevise;
        private float radiusRevise;

        public Particle() {
            generateRevises();
        }

        public void generateRevises() {
            generatePositionRevise();
            generateRadiusRevise();
        }

        private void generatePositionRevise() {
            Random random = new Random();
            int positionRevisePercentage = 100 - random.nextInt(UNSTABLE_PERCENTAGE_POSITION);
            positionRevise = 1.0f * positionRevisePercentage / 100;
        }

        private void generateRadiusRevise() {
            Random random = new Random();
            int radiusRevisePercentage = 100 +
                    (random.nextInt(UNSTABLE_PERCENTAGE_RADIUS * 2) - UNSTABLE_PERCENTAGE_RADIUS);
            radiusRevise = 1.0f * radiusRevisePercentage / 100;
        }

        public void setPosition(int x, int y) {
            this.x = (int) (x * positionRevise);
            this.y = (int) (y * positionRevise);
        }

        public void setRadius(float radius) {
            this.radius = radius * radiusRevise;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }
}
