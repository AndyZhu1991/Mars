package com.koolew.mars.blur;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by jinchangzhu on 6/24/15.
 */
public class LoadBlurImageTask extends AsyncTask {

    private static final int SCALE_BEFORE_BLUR = 5;

    protected View mView;
    protected String mUri;

    protected Bitmap mBluredBitmap;
    protected int mScaleBeforeBlurRatio = SCALE_BEFORE_BLUR;

    public LoadBlurImageTask(View view, String uri) {
        mView = view;
        mUri = uri;
    }

    public void setScaleBeforeBlur(int ratio) {
        mScaleBeforeBlurRatio = ratio;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        Bitmap bmp = null;
        for (int i = 0; i < 3 && bmp == null; i++) { // Try 3 times
            bmp = ImageLoader.getInstance().loadImageSync(mUri);
        }

        if (bmp != null) {
            mBluredBitmap = ImageBlurTool.doBlur(bmp, mScaleBeforeBlurRatio * 3, mScaleBeforeBlurRatio);
        }

        return null;
    }
}
