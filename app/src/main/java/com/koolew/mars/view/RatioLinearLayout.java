package com.koolew.mars.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 12/11/15.
 */
public class RatioLinearLayout extends LinearLayout {
    private RatioMeasure measure;

    public RatioLinearLayout(Context context) {
        this(context, null);
    }

    public RatioLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RatioLayout, 0, 0);
        measure = new RatioMeasure(a);
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
}
