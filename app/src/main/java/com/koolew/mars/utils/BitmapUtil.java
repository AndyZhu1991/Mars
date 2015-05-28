package com.koolew.mars.utils;

import android.graphics.Bitmap;

/**
 * Created by jinchangzhu on 5/27/15.
 */
public class BitmapUtil {

    public static Bitmap getScaledSquareBmp(Bitmap originBitmap, int size) {
        int srcWidth = originBitmap.getWidth();
        int srcHeight = originBitmap.getHeight();
        int dstSize;
        int x = 0;
        int y = 0;
        if (srcWidth > srcHeight) {
            dstSize = srcHeight;
            x = (srcWidth - srcHeight) / 2;
        }
        else {
            dstSize = srcWidth;
            y = (srcHeight - srcWidth) / 2;
        }
        Bitmap squareBitmap = Bitmap.createBitmap(originBitmap, x, y, dstSize, dstSize);
        Bitmap scaledBitmap;
        if (dstSize > size) {
            scaledBitmap = Bitmap.createScaledBitmap(squareBitmap,
                    size, size, false);
        }
        else {
            scaledBitmap = squareBitmap;
        }

        return scaledBitmap;
    }
}
