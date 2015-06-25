package com.koolew.mars.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 6/3/15.
 */
public class KoolewViewPagerIndicator extends LinearLayout
        implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private static final String TAG = "koolew-ViewPagerIdc";

    private ViewPager mViewPager;

    private int textSize;
    private int textColorIndicated;
    private int textColorUnindicate;
    private int selectedUnderlineHeight;
    private int[] backgroundColors;

    private Paint selectedUnderlinePaint;

    private int underlineLeft;
    private int underlineRight;

    private OnBackgroundColorChangedListener mOnBackgroundColorChangedListener;


    public KoolewViewPagerIndicator(Context context) {
        this(context, null);
    }

    public KoolewViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        initAttrs();
        selectedUnderlinePaint = new Paint();
        selectedUnderlinePaint.setColor(
                getContext().getResources().getColor(R.color.indicator_underline));

        setWillNotDraw(false);
    }

    public void setOnBackgroundColorChangedListener(OnBackgroundColorChangedListener listener) {
        mOnBackgroundColorChangedListener = listener;
    }

    public void setViewPager(ViewPager viewPager) {
        setViewPager(viewPager, null);
    }

    public void setViewPager(ViewPager viewPager, int[] backgroundColors) {
        mViewPager = viewPager;
        mViewPager.setOnPageChangeListener(this);
        if (backgroundColors != null && viewPager.getAdapter().getCount() == backgroundColors.length) {
            this.backgroundColors = backgroundColors;
        }

        addTabs(viewPager.getAdapter());

        initColor();
    }

    private void addTabs(PagerAdapter adapter) {
        removeAllViews();

        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            addTab(adapter.getPageTitle(i));
        }
    }

    private void addTab(CharSequence title) {
        TextView textView = new TextView(getContext());
        textView.setText(title);
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(this);
        textView.setTextSize(textSize);
        textView.setTextColor(textColorUnindicate);

        addView(textView, new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1));
    }

    private void initAttrs() {
        Resources res = getContext().getResources();
        textSize = (int) (res.getDimensionPixelSize(R.dimen.indicator_title_text_size) /
                          getContext().getResources().getDisplayMetrics().scaledDensity);
        Log.d(TAG, "textSize: " + textSize);
        textColorIndicated = res.getColor(R.color.title_text_color_indicated);
        textColorUnindicate = res.getColor(R.color.title_text_color_unindicate);
        selectedUnderlineHeight = res.getDimensionPixelSize(R.dimen.underline_height);
    }

    private void initColor() {
        int currentItem = mViewPager.getCurrentItem();
        ((TextView) getChildAt(currentItem)).setTextColor(textColorIndicated);

        if (backgroundColors != null) {
            setBackgroundColor(backgroundColors[currentItem]);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = getHeight();
        canvas.drawRect(underlineLeft, height - selectedUnderlineHeight,
                underlineRight, height,
                selectedUnderlinePaint);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        int childCount = getChildCount();
        if (childCount > 0 && position < childCount - 1) {
            View selectedTitle = getChildAt(position);
            View nextTitle = getChildAt(position + 1);

            underlineLeft = (int) (selectedTitle.getLeft() * (1.0f - positionOffset)
                                     + nextTitle.getLeft() * positionOffset);
            underlineRight = (int) (selectedTitle.getRight() * (1.0f - positionOffset)
                    + nextTitle.getRight() * positionOffset);

            if (backgroundColors != null && backgroundColors.length != 0) {
                int backgroundColor = getTransitionColor(
                        backgroundColors[position], backgroundColors[position + 1], positionOffset);
                setBackgroundColor(backgroundColor);
                if (mOnBackgroundColorChangedListener != null) {
                    mOnBackgroundColorChangedListener.onBackgroundColorChanged(backgroundColor);
                }
            }

            invalidate();
        }
    }

    @Override
    public void onPageSelected(int position) {
        ((TextView) getChildAt(position)).setTextColor(textColorIndicated);
        if (position - 1 >= 0) {
            ((TextView) getChildAt(position - 1)).setTextColor(textColorUnindicate);
        }
        if (position + 1 <= getChildCount() - 1) {
            ((TextView) getChildAt(position + 1)).setTextColor(textColorUnindicate);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if (getChildAt(i) == v) {
                mViewPager.setCurrentItem(i, true);
                return;
            }
        }
    }

    public interface OnBackgroundColorChangedListener {
        public void onBackgroundColorChanged(int color);
    }

    private int getTransitionColor(int startColor, int endColor, float offset) {
        return Color.argb(
                (int) (Color.alpha(startColor) * (1.0f - offset) + Color.alpha(endColor) * offset),
                (int) (Color.red(startColor) * (1.0f - offset) + Color.red(endColor)   * offset),
                (int) (Color.green(startColor) * (1.0f - offset) + Color.green(endColor) * offset),
                (int) (Color.blue(startColor)  * (1.0f - offset) + Color.blue(endColor)  * offset));
    }
}
