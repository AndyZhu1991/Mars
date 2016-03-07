package com.koolew.mars.blur;

import android.app.Activity;
import android.support.v7.graphics.Palette;
import android.widget.ImageView;

import com.koolew.android.utils.ColorUtil;
import com.koolew.android.utils.Utils;

/**
 * Created by jinchangzhu on 8/22/15.
 */
public class DisplayBlurImageAndStatusBar extends DisplayBlurImageAndPalette {

    protected Activity mActivity;

    public DisplayBlurImageAndStatusBar(Activity activity, ImageView view, String uri) {
        super(view, uri);
        mActivity = activity;
    }

    @Override
    protected void onPalette(Palette palette) {
        Utils.setStatusBarColorBurn(mActivity,
                ColorUtil.burnColorForStatusBar(Utils.getStatusBarColorFromPalette(palette)));
    }
}
