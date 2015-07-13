package com.koolew.mars.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 7/9/15.
 */
public class BigCountView extends RelativeLayout {

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

        mCountText = (TextView) findViewById(R.id.count);
        mBackground = (GradientDrawable) root.getBackground();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BigCountView, 0, 0);
        ((TextView) findViewById(R.id.description_text))
                .setText(a.getString(R.styleable.BigCountView_description_text));
        Drawable descImageDrawable = a.getDrawable(R.styleable.BigCountView_description_image);
        if (descImageDrawable != null) {
            ((ImageView) findViewById(R.id.description_image)).setImageDrawable(descImageDrawable);
        }
        else {
            findViewById(R.id.description_image).setVisibility(GONE);
        }
        setSolidColor(a.getColor(R.styleable.BigCountView_solid_color,
                getResources().getColor(android.R.color.white)));
    }

    public void setCount(int count) {
        mCountText.setText(String.valueOf(count));
    }

    public void setSolidColor(int color) {
        mBackground.setColor(color);
    }
}
