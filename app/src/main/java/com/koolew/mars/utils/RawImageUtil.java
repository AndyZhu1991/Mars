package com.koolew.mars.utils;

import android.util.Log;

/**
 * Created by jinchangzhu on 6/10/15.
 */
public class RawImageUtil {

    private static final String TAG = "koolew-RawImageUtil";

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

    /**
     * NV21 is a 4:2:0 YCbCr, For 1 NV21 pixel: YYYYYYYY VUVU I420YUVSemiPlanar
     * is a 4:2:0 YUV, For a single I420 pixel: YYYYYYYY UVUV Apply NV21 to
     * I420YUVSemiPlanar(NV12) Refer to https://wiki.videolan.org/YUV/
     */
    public static void NV21toI420SemiPlanar(byte[] nv21bytes, byte[] i420bytes,
                                            int width, int height) {
        long start = System.currentTimeMillis();
        System.arraycopy(nv21bytes, 0, i420bytes, 0, width * height);
        for (int i = width * height; i < nv21bytes.length; i += 2) {
            i420bytes[i] = nv21bytes[i + 1];
            i420bytes[i + 1] = nv21bytes[i];
        }
        Log.d(TAG, "NV21toI420SemiPlanar: " + (System.currentTimeMillis() - start));
    }
}
