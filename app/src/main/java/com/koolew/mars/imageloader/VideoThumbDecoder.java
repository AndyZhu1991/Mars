package com.koolew.mars.imageloader;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;

import java.io.IOException;

/**
 * Created by jinchangzhu on 6/16/15.
 */
public class VideoThumbDecoder extends BaseImageDecoder {

    private static final String TAG = "koolew-VideoThumbD";


    public VideoThumbDecoder(boolean loggingEnabled) {
        super(loggingEnabled);
    }

    @Override
    public Bitmap decode(ImageDecodingInfo decodingInfo) throws IOException {
        String cleanedUriString = cleanUriString(decodingInfo.getImageKey());
        if (isVideoPath(cleanedUriString)) {
            return makeVideoThumbnail(getVideoFilePath(cleanedUriString),
                    decodingInfo.getTargetSize().getWidth(), decodingInfo.getTargetSize().getHeight());
        }

        return super.decode(decodingInfo);
    }

    private Bitmap makeVideoThumbnail(String filePath, int width, int height) {
        if (filePath == null) {
            return null;
        }
        Bitmap thumbnail = null;
        int i;
        for (i = 0; i < 10; i++) {
            thumbnail = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
            if (thumbnail != null) {
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Wait " + filePath + " for " + (i * 50) + " millis");

        if (thumbnail == null) {
            return null;
        }

        Bitmap scaledThumb = scaleBitmap(thumbnail, width, height);
        thumbnail.recycle();
        return scaledThumb;
    }

    private boolean isVideoPath(String path) {
        return path.endsWith(".mp4");
    }

    private String getVideoFilePath(String originPath) {
        if (originPath.startsWith("file://")) {
            return originPath.substring(7 /*"file://".length()*/);
        }

        return originPath;
    }

    private Bitmap scaleBitmap(Bitmap origBitmap, int width, int height) {
        float scale = Math.min(
                ((float)width) / ((float)origBitmap.getWidth()),
                ((float)height) / ((float)origBitmap.getHeight())
        );
        return Bitmap.createScaledBitmap(origBitmap,
                (int)(((float)origBitmap.getWidth()) * scale),
                (int)(((float)origBitmap.getHeight()) * scale),
                false
        );
    }

    private String cleanUriString(String contentUriWithAppendedSize) {
        // replace the size at the end of the URI with an empty string.
        // the URI will be in the form "content://....._256x256
        return contentUriWithAppendedSize.replaceFirst("_\\d+x\\d+$", "");
    }
}
