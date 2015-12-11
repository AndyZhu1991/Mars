package com.koolew.mars.view;

import android.content.res.TypedArray;
import android.view.View;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 12/11/15.
 */
public class RatioMeasure {
    float mWidthRatio;
    float mHeightRatio;

    private int resultWidthMeasureSpec;
    private int resultHeightMeasureSpec;

    public RatioMeasure(TypedArray array) {
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

        if (View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY) {
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
        int size1 = View.MeasureSpec.getSize(measureSpec1);
        int size2 = (int) (size1 * ratio2 / ratio1);
        return View.MeasureSpec.makeMeasureSpec(size2, View.MeasureSpec.EXACTLY);
    }

    private boolean onlyOneExactly(int widthMeasureSpec, int heightMeasureSpec) {
        int exactlyCount = 0;
        exactlyCount += View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY ? 1 : 0;
        exactlyCount += View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY ? 1 : 0;

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
