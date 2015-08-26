package com.koolew.mars.utils;

import android.util.Log;

/**
 * Created by jinchangzhu on 6/10/15.
 */
public class RawImageUtil {

    private static final String TAG = "koolew-RawImageUtil";

    public static byte[] cropYUV420VerticalCenter(byte[] data, byte[] outData,
                                                  int imageW, int imageH,
                                                  int newImageH) {
        return cropYUV420Vertical(data, outData, imageW, imageH,
                                  (imageH - newImageH) / 2, (imageH + newImageH) / 2);
    }

    /**
     * Crop YUV420 image vertical [startY, endY)
     *
     * @param data    Original YUV420 data
     * @param outData Buffer will filled by croped data
     * @param imageW  Image width
     * @param imageH  Image height
     * @param startY  Start Y
     * @param endY    End Y
     * @return        Same as outData(not null) or new buffer
     */
    public static byte[] cropYUV420Vertical(byte[] data, byte[] outData,
                                            int imageW, int imageH,
                                            int startY, int endY) {
        long start = System.currentTimeMillis();

        int newImageHeight = endY - startY;
        if (outData == null) {
            outData = new byte[imageW * newImageHeight * 3 / 2];
        }

        int copyedCount = newImageHeight * imageW;
        System.arraycopy(data, startY * imageW, outData, 0, newImageHeight * imageW);
        System.arraycopy(data, (imageH + startY / 2) * imageW,
                         outData, copyedCount,
                         newImageHeight / 2 * imageW);

        Log.d(TAG, "crop a YUV420 image: " + (System.currentTimeMillis() - start));
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

    public static byte[] rotateYUV420Degree180(byte[] data, byte[] outData,
                                               int imageWidth, int imageHeight) {
        if (outData == null) {
            outData = new byte[imageWidth * imageHeight * 3 / 2];
        }
        int i = 0;
        int count = 0;

        for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
            outData[count] = data[i];
            count++;
        }

        for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
                * imageHeight; i -= 2) {
            outData[count++] = data[i - 1];
            outData[count++] = data[i];
        }
        return outData;
    }


    public static byte[] rotateYUV420Degree270(byte[] data, byte[] outData,
                                               int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int nWidth = 0, nHeight = 0;
        int wh = 0;
        int uvHeight = 0;
        if (imageWidth != nWidth || imageHeight != nHeight) {
            nWidth = imageWidth;
            nHeight = imageHeight;
            wh = imageWidth * imageHeight;
            uvHeight = imageHeight >> 1;//uvHeight = height / 2
        }

        //旋转Y
        int k = 0;
        for (int i = 0; i < imageWidth; i++) {
            int nPos = 0;
            for (int j = 0; j < imageHeight; j++) {
                yuv[k] = data[nPos + i];
                k++;
                nPos += imageWidth;
            }
        }

        for (int i = 0; i < imageWidth; i += 2) {
            int nPos = wh;
            for (int j = 0; j < uvHeight; j++) {
                yuv[k] = data[nPos + i];
                yuv[k + 1] = data[nPos + i + 1];
                k += 2;
                nPos += imageWidth;
            }
        }

        return rotateYUV420Degree180(yuv, outData, imageWidth, imageHeight);
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

    public static void YUV420SPtoYUV420P(byte[] YUV420SPbytes, int width, int height) {
        int Ysize = width * height;
        int UVsize = width * height / 2;

        byte[] UVbytes = new byte[UVsize];
        System.arraycopy(YUV420SPbytes, Ysize, UVbytes, 0, UVsize);

        for (int i = 0; i < UVsize; i += 2) {
            YUV420SPbytes[Ysize + i / 2] = UVbytes[i];
            YUV420SPbytes[Ysize + UVsize / 2 + i / 2] = UVbytes[i + 1];
        }
    }
}
