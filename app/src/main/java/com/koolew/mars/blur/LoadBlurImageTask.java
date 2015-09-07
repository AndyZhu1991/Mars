package com.koolew.mars.blur;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by jinchangzhu on 6/24/15.
 */
public class LoadBlurImageTask extends AsyncTask {

    private static final int SCALE_BEFORE_BLUR = 8;

    protected View mView;
    protected String mUri;

    protected Bitmap mBluredBitmap;

    public LoadBlurImageTask(View view, String uri) {
        mView = view;
        mUri = uri;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        Bitmap bmp = null;
        for (int i = 0; i < 3 && bmp == null; i++) { // Try 3 times
            bmp = ImageLoader.getInstance().loadImageSync(mUri);
        }

        if (bmp != null) {
            mBluredBitmap = ImageBlurTool.doBlur(bmp, SCALE_BEFORE_BLUR * 3, SCALE_BEFORE_BLUR);
        }

        return null;
    }
}
