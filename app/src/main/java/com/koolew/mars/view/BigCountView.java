package com.koolew.mars.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koolew.mars.R;
import com.koolew.mars.utils.Utils;

/**
 * Created by jinchangzhu on 7/9/15.
 */
public class BigCountView extends RelativeLayout {

    private TextView mDescriptionText;
    private TextView mCountText;
    private GradientDrawable mBackground;

    public BigCountView(Context context) {
        this(context, null);
    }

    public BigCountView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigCountView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View root = LayoutInflater.from(getContext()).inflate(R.layout.big_count_layout, null);
        addView(root);

        mDescriptionText = (TextView) findViewById(R.id.description_text);
        mCountText = (TextView) findViewById(R.id.count);
        mBackground = (GradientDrawable) root.getBackground();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BigCountView, 0, 0);
        ((TextView) findViewById(R.id.description_text))
                .setText(a.getString(R.styleable.BigCountView_description_text));
        Drawable descImageDrawable = a.getDrawable(R.styleable.BigCountView_description_image);
        if (descImageDrawable != null) {
            descImageDrawable.setBounds(0, 0, descImageDrawable.getIntrinsicWidth(),
                    descImageDrawable.getIntrinsicHeight());
            mDescriptionText.setCompoundDrawables(descImageDrawable, null, null, null);
        }
        setSolidColor(a.getColor(R.styleable.BigCountView_solid_color,
                getResources().getColor(android.R.color.white)));
        setStrokeColor(a.getColor(R.styleable.BigCountView_stroke_color, 0xFFE2E6E9));
    }

    public void setCount(int count) {
        mCountText.setText(String.valueOf(count));
    }

    public void setSolidColor(int color) {
        mBackground.setColor(color);
    }

    public void setStrokeColor(int color) {
        mBackground.setStroke((int) Utils.dpToPixels(getContext(), 1), color);
    }
}
