package com.koolew.mars.view;

import android.content.Context;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jinchangzhu on 6/23/15.
 */
public class DrawerToggleView extends View
        implements DrawerLayout.DrawerListener, View.OnClickListener {

    private DrawerArrowDrawable mArrowDrawable;
    private DrawerLayout mDrawerLayout;
    private int mDrawerGravity;

    public DrawerToggleView(Context context) {
        this(context, null);
    }

    public DrawerToggleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mArrowDrawable = new DrawerArrowDrawable(context) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        setBackgroundDrawable(mArrowDrawable);
        setClickable(true);
        setOnClickListener(this);
    }

    public void setDrawer(DrawerLayout drawer) {
        setDrawer(drawer, GravityCompat.START);
    }

    public void setDrawer(DrawerLayout drawer, int gravity) {
        mDrawerLayout = drawer;
        mDrawerGravity = gravity;
    }

    @Override
    public void onDrawerSlide(View view, float slideOffset) {
        if (mDrawerLayout != null) {
            mArrowDrawable.setVerticalMirror(!mDrawerLayout.isDrawerOpen(GravityCompat.START));
        }
        mArrowDrawable.setProgress(slideOffset);
    }

    @Override
    public void onDrawerOpened(View view) {
        mArrowDrawable.setProgress(1.0f);
    }

    @Override
    public void onDrawerClosed(View view) {
        mArrowDrawable.setProgress(0.0f);
    }

    @Override
    public void onDrawerStateChanged(int newState) {}

    @Override
    public void onClick(View v) {
        if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(mDrawerGravity)) {
                mDrawerLayout.closeDrawer(mDrawerGravity);
            }
            else {
                mDrawerLayout.openDrawer(mDrawerGravity);
            }
        }
    }
}
