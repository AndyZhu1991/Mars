package com.koolew.mars.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 6/23/15.
 */
public class TitleBarView extends FrameLayout implements View.OnClickListener {

    private LinearLayout mBackLayout;
    private TextView mTitleView;
    private LinearLayout mRightLayout;
    private ImageView mRightImage;
    private TextView mRightText;

    private OnLayoutClickListener mLayoutClickListener;

    public TitleBarView(Context context) {
        this(context, null);
    }

    public TitleBarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        View titlebarLayout = inflater.inflate(R.layout.title_bar_layout, null);
        mBackLayout = (LinearLayout) titlebarLayout.findViewById(R.id.back_layout);
        mTitleView = (TextView) titlebarLayout.findViewById(R.id.title_text);
        mRightLayout = (LinearLayout) titlebarLayout.findViewById(R.id.right_layout);
        mRightImage = (ImageView) mRightLayout.findViewById(R.id.right_image);
        mRightText = (TextView) mRightLayout.findViewById(R.id.right_text);
        addView(titlebarLayout);


        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleBarView, 0, 0);

        String title = a.getString(R.styleable.TitleBarView_title_text);
        mTitleView.setText(title);

        Drawable rightDrawable = a.getDrawable(R.styleable.TitleBarView_right_image_src);
        mRightImage.setImageDrawable(rightDrawable);

        String rightText = a.getString(R.styleable.TitleBarView_right_text);
        mRightText.setText(rightText);

        if (rightDrawable == null && (rightText == null || rightText.equals(""))) {
            mRightLayout.setVisibility(INVISIBLE);
        }


        mBackLayout.setOnClickListener(this);
        mRightLayout.setOnClickListener(this);
    }

    public void setOnLayoutClickListener(OnLayoutClickListener listener) {
        mLayoutClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                if (mLayoutClickListener != null) {
                    mLayoutClickListener.onBackLayoutClick();
                }
                else {
                    ((Activity) getContext()).onBackPressed();
                }
                break;
            case R.id.right_layout:
                if (mLayoutClickListener != null) {
                    mLayoutClickListener.onRightLayoutClick();
                }
                break;
        }
    }

    public interface OnLayoutClickListener {
        void onBackLayoutClick();
        void onRightLayoutClick();
    }
}
