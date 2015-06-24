package com.koolew.mars.blur;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

import com.koolew.mars.utils.BitmapUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by jinchangzhu on 6/24/15.
 */
public class LoadBlurImageTask extends AsyncTask {

    private static final float SCALE_BEFORE_BLUR = 8;

    protected View mView;
    protected String mUri;

    protected Bitmap mBluredBitmap;

    public LoadBlurImageTask(View view, String uri) {
        mView = view;
        mUri = uri;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        Bitmap bmp = ImageLoader.getInstance().loadImageSync(mUri);

        int viewWidth = mView.getMeasuredWidth();
        int viewHeight = mView.getMeasuredHeight();

        Bitmap blurBitmap = BitmapUtil.getClipedScaledBitmap(bmp,
                (int) (viewWidth / SCALE_BEFORE_BLUR), (int) (viewHeight / SCALE_BEFORE_BLUR));
        float radius = 20 / (1.0f * viewWidth / blurBitmap.getWidth());

        mBluredBitmap = ImageBlurTool.doBlur(blurBitmap, (int) radius, true);

        return null;
    }
}
