package com.koolew.mars.imageloader;

import android.graphics.Bitmap;

import com.koolew.mars.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

/**
 * Created by jinchangzhu on 7/25/15.
 */
public class ImageLoaderHelper {

    public static final DisplayImageOptions topicThumbLoadOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.mipmap.topic_default_thumb)
            //.showImageForEmptyUri(R.mipmap.topic_default_thumb)
            //.showImageOnFail(R.mipmap.topic_default_thumb)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static final DisplayImageOptions avatarLoadOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.mipmap.default_avatar)
            //.showImageForEmptyUri(R.mipmap.topic_default_thumb)
            //.showImageOnFail(R.mipmap.topic_default_thumb)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();
}
