package com.koolew.android.ratiolayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by jinchangzhu on 12/3/15.
 */
public class RatioFrameLayout extends FrameLayout {

    private RatioMeasure measure;

    public RatioFrameLayout(Context context) {
        this(context, null);
    }

    public RatioFrameLayout(Context context, AttributeSet attrs) {
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
