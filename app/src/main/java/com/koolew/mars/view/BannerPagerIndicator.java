package com.koolew.mars.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.koolew.mars.R;
import com.koolew.mars.utils.Utils;

/**
 * Created by jinchangzhu on 7/15/15.
 */
public class BannerPagerIndicator extends FrameLayout
        implements ViewPager.OnPageChangeListener {

    private static final float DEFAULT_INDICATOR_SIZE = 10;

    private int mIndicatorSize;

    private LinearLayout mUncheckLayout;
    private ImageView[] mUncheckImages;
    private ImageView mCheckedImage;

    public BannerPagerIndicator(Context context) {
        this(context, null);
    }

    public BannerPagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BannerPagerIndicator, 0, 0);
        mIndicatorSize = a.getDimensionPixelSize(R.styleable.BannerPagerIndicator_indicator_size,
                (int) Utils.dpToPixels(getContext(), DEFAULT_INDICATOR_SIZE));

        mUncheckLayout = new LinearLayout(getContext());
        mUncheckLayout.setOrientation(LinearLayout.HORIZONTAL);
        mUncheckLayout.setGravity(Gravity.CENTER);
        addView(mUncheckLayout, new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mCheckedImage = new ImageView(getContext());
        mCheckedImage.setImageResource(R.mipmap.banner_indicator_checked);
        LayoutParams lp = new LayoutParams(mIndicatorSize, mIndicatorSize);
        lp.gravity = Gravity.CENTER_VERTICAL;
        addView(mCheckedImage, lp);
    }

    public void setViewPager(ViewPager viewPager) {
        viewPager.addOnPageChangeListener(this);

        mUncheckLayout.removeAllViews();

        int count = viewPager.getAdapter().getCount();
        mUncheckImages = new ImageView[count];
        for (int i = 0; i < count; i++) {
            mUncheckImages[i] = new ImageView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mIndicatorSize, mIndicatorSize);
            if (i != count - 1) {
                lp.setMargins(0, 0, mIndicatorSize, 0);
            }
            mUncheckImages[i].setLayoutParams(lp);
            mUncheckLayout.addView(mUncheckImages[i]);
            mUncheckImages[i].setImageResource(R.mipmap.banner_indicator_uncheck);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mUncheckImages == null || mUncheckImages.length == 0) {
            return;
        }

        int curLeft = mUncheckImages[position].getLeft();
        int nextLeft = (position + 1 < mUncheckImages.length) ? mUncheckImages[position + 1].getLeft() : 0;
        mCheckedImage.setX(curLeft * (1.0f - positionOffset) + nextLeft * positionOffset);
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
