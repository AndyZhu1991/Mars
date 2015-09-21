package com.koolew.mars.utils;

import android.support.v4.view.ViewPager;

/**
 * Created by jinchangzhu on 9/21/15.
 */
public abstract class PagerScrollSmoothColorListener implements ViewPager.OnPageChangeListener {

    private int[] colors;

    public PagerScrollSmoothColorListener(int... colors) {
        this.colors = colors;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position < colors.length - 1) {
                int backgroundColor = ColorUtil.getTransitionColor(
                        colors[position], colors[position + 1], positionOffset);
                onColorChanged(backgroundColor);
        }
        else if (position == colors.length - 1) {
                int backgroundColor = colors[position];
                onColorChanged(backgroundColor);
        }
    }

    public abstract void onColorChanged(int color);

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
