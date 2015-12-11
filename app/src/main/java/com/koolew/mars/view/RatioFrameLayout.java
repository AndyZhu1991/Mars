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

    public void setRatio(float widthRatio, float heightRatio) {
        measure.mWidthRatio = widthRatio;
        measure.mHeightRatio = heightRatio;
    }


    static class Measure {
        private float mWidthRatio;
        private float mHeightRatio;

        private int resultWidthMeasureSpec;
        private int resultHeightMeasureSpec;

        public Measure(TypedArray array) {
            mWidthRatio = array.getFloat(R.styleable.RatioLayout_widthRatio, 0.0f);
            mHeightRatio = array.getFloat(R.styleable.RatioLayout_heightRatio, 0.0f);
            array.recycle();
        }

        public void measure(int widthMeasureSpec, int heightMeasureSpec) {
            if (mWidthRatio <= 0.0f || mHeightRatio <= 0.0f) {
                cannotMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }

            if (!onlyOneExactly(widthMeasureSpec, heightMeasureSpec)) {
                cannotMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }

            if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
                measureByWidth(widthMeasureSpec, heightMeasureSpec);
            } else {
                measureByHeight(widthMeasureSpec, heightMeasureSpec);
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

        private boolean onlyOneExactly(int widthMeasureSpec, int heightMeasureSpec) {
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
