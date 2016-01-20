package com.koolew.mars.imageloader;

import android.content.Context;
import android.graphics.Bitmap;

import com.koolew.mars.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

/**
 * Created by jinchangzhu on 7/25/15.
 */
public class ImageLoaderHelper {

    public static DisplayImageOptions topicThumbLoadOptions;

    public static DisplayImageOptions avatarLoadOptions;

    public static void init(Context context) {
        topicThumbLoadOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(context.getDrawable(R.mipmap.topic_default_thumb))
                        //.showImageForEmptyUri(R.mipmap.topic_default_thumb)
                        //.showImageOnFail(R.mipmap.topic_default_thumb)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        avatarLoadOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(context.getDrawable(R.mipmap.default_avatar))
                        //.showImageForEmptyUri(R.mipmap.topic_default_thumb)
                        //.showImageOnFail(R.mipmap.topic_default_thumb)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }
}
