package com.koolew.mars.blur;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.View;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by jinchangzhu on 7/7/15.
 */
public class BlurImageLoadingListener extends SimpleImageLoadingListener {

    private static final int SCALE_BEFORE_BLUE = 8;
    private static final int BLUR_RADIUS = 3;

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        int bmpWidth = loadedImage.getWidth();
        int bmpHeight = loadedImage.getHeight();

        Bitmap scaledBmp = Bitmap.createScaledBitmap(loadedImage,
                bmpWidth / SCALE_BEFORE_BLUE, bmpHeight / SCALE_BEFORE_BLUE, false);
        Bitmap bluredBmp = ImageBlurTool.doBlur(scaledBmp, BLUR_RADIUS, true);

        Canvas canvas = new Canvas(loadedImage);
        Matrix matrix = new Matrix();
        matrix.postScale(SCALE_BEFORE_BLUE, SCALE_BEFORE_BLUE);
        canvas.drawBitmap(bluredBmp, matrix, null);
    }
}
