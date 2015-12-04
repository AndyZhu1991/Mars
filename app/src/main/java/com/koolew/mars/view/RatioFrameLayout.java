package com.koolew.mars.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 12/3/15.
 */
public class RatioFrameLayout extends FrameLayout {

    private Measure measure;

    public RatioFrameLayout(Context context) {
        this(context, null);
    }

    public RatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RatioLayout, 0, 0);
        measure = new Measure(a);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measure.measure(widthMeasureSpec, heightMeasureSpec);
        widthMeasureSpec = measure.getResultWidthMeasureSpec();
        heightMeasureSpec = measure.getResultHeightMeasureSpec();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    static class Measure {
        private static final int NO_STRATEGY = 0;
        private static final int EXACT_AUTO = 1;
        private static final int EXACT_BY_WIDTH = 2;
        private static final int EXACT_BY_HEIGHT = 3;

        private float mWidthRatio;
        private float mHeightRatio;
        private int mMeasureStrategy;

        private int resultWidthMeasureSpec;
        private int resultHeightMeasureSpec;

        public Measure(TypedArray array) {
            mWidthRatio = array.getFloat(R.styleable.RatioLayout_widthRatio, 0.0f);
            mHeightRatio = array.getFloat(R.styleable.RatioLayout_heightRatio, 0.0f);
            mMeasureStrategy = array.getInt(R.styleable.RatioLayout_measureStrategy, NO_STRATEGY);
            array.recycle();
        }

        public void measure(int widthMeasureSpec, int heightMeasureSpec) {
            if (mWidthRatio <= 0.0f || mHeightRatio <= 0.0f || mMeasureStrategy == NO_STRATEGY ||
                    !checkMeasureSpec(widthMeasureSpec, heightMeasureSpec)) {
                cannotMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }

            switch (mMeasureStrategy) {
                case EXACT_AUTO:
                    if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
                        measureByWidth(widthMeasureSpec, heightMeasureSpec);
                    }
                    else {
                        measureByHeight(widthMeasureSpec, heightMeasureSpec);
                    }
                    break;
                case EXACT_BY_WIDTH:
                    if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
                        measureByWidth(widthMeasureSpec, heightMeasureSpec);
                    }
                    else {
                        cannotMeasure(widthMeasureSpec, heightMeasureSpec);
                    }
                    break;
                case EXACT_BY_HEIGHT:
                    if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
                        measureByHeight(widthMeasureSpec, heightMeasureSpec);
                    }
                    else {
                        cannotMeasure(widthMeasureSpec, heightMeasureSpec);
                    }
                    break;
            }
        }

        private void measureByWidth(int widthMeasureSpec, int heightMeasureSpec) {
            resultWidthMeasureSpec = widthMeasureSpec;
            resultHeightMeasureSpec = getRatioedMeasureSpec(widthMeasureSpec, mWidthRatio, mHeightRatio);
        }

        private void measureByHeight(int widthMeasureSpec, int heightMeasureSpec) {
            resultHeightMeasureSpec = heightMeasureSpec;
            resultWidthMeasureSpec = getRatioedMeasureSpec(heightMeasureSpec, mHeightRatio, mWidthRatio);
        }

        private int getRatioedMeasureSpec(int measureSpec1, float ratio1, float ratio2) {
            int size1 = MeasureSpec.getSize(measureSpec1);
            int size2 = (int) (size1 * ratio2 / ratio1);
            return MeasureSpec.makeMeasureSpec(size2, MeasureSpec.EXACTLY);
        }

        private boolean checkMeasureSpec(int widthMeasureSpec, int heightMeasureSpec) {
            int exactlyCount = 0;
            exactlyCount += MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY ? 1 : 0;
            exactlyCount += MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY ? 1 : 0;

            return exactlyCount == 1;
        }

        private void cannotMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            resultWidthMeasureSpec = widthMeasureSpec;
            resultHeightMeasureSpec = heightMeasureSpec;
        }

        public int getResultHeightMeasureSpec() {
            return resultHeightMeasureSpec;
        }

        public int getResultWidthMeasureSpec() {
            return resultWidthMeasureSpec;
        }
    }
}
