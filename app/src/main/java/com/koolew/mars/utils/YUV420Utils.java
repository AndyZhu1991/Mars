package com.koolew.mars.utils;

import android.util.Log;

/**
 * Created by jinchangzhu on 6/10/15.
 */
public class YUV420Utils {

    private static final String TAG = "koolew-YUV420Utils";

    public static byte[] cropYUV420VerticalCenter
            (byte[] data, byte[] outData, int imageW, int imageH, int newImageH) {
        long start = System.currentTimeMillis();

        if (outData == null) {
            outData = new byte[imageW*newImageH*3/2];
        }

        int cropH = (imageH - newImageH)/2;

        int copyedCount = newImageH * imageW;
        System.arraycopy(data, cropH * imageW, outData, 0, newImageH * imageW);
        System.arraycopy(data, (imageH + cropH / 2) * imageW,
                         outData, copyedCount,
                         newImageH / 2 * imageW);

        Log.d(TAG, "crop a YUV420 imager: " + (System.currentTimeMillis() - start));
        return outData;
    }

    public static byte[] rotateYUV420Degree90(byte[] data, byte[] outData,
                                              int imageWidth, int imageHeight) {
        long start = System.currentTimeMillis();
        if (outData == null) {
            outData = new byte[imageWidth * imageHeight * 3 / 2];
        }
        // Rotate the Y luma
        int i = 0;
        for(int x = 0; x < imageWidth; x++) {
            for(int y = imageHeight - 1; y >= 0; y--) {
                outData[i] = data[y * imageWidth + x];
                i++;
            }

        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for(int x = imageWidth - 1; x > 0; x = x - 2) {
            for(int y = 0;y < imageHeight/2;y++) {
                outData[i] = data[(imageWidth*imageHeight) + (y*imageWidth) + x];
                i--;
                outData[i] = data[(imageWidth*imageHeight) + (y*imageWidth) + (x-1)];
                i--;
            }
        }
        Log.d(TAG, "Rotate a YUV420 90 degree: " + (System.currentTimeMillis() - start));
        return outData;
    }
}
