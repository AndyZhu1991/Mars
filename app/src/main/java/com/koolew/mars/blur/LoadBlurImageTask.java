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

        int viewWidth = 0;
        int viewHeight = 0;

        // Wait 1 second for measure
        for (int i = 0; i < 10; i++) {
            viewWidth = mView.getMeasuredWidth();
            viewHeight = mView.getMeasuredHeight();
            if (viewWidth == 0 || viewHeight == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                break;
            }
        }

        Bitmap blurBitmap = BitmapUtil.getClipedScaledBitmap(bmp,
                (int) (viewWidth / SCALE_BEFORE_BLUR), (int) (viewHeight / SCALE_BEFORE_BLUR));
        float radius = 20 / (1.0f * viewWidth / blurBitmap.getWidth());

        mBluredBitmap = ImageBlurTool.doBlur(blurBitmap, (int) radius, true);

        return null;
    }
}
