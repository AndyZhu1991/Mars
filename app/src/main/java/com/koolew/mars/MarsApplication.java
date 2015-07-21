package com.koolew.mars;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.koolew.mars.imageloader.VideoThumbDecoder;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.utils.BgmUtil;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * Created by jinchangzhu on 5/27/15.
 */
public class MarsApplication extends Application {

    private static final String TAG = "koolew-MarsApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        long start = System.currentTimeMillis();

        ApiWorker.init(getApplicationContext());
        initImageLoader(getApplicationContext());
        com.koolew.mars.wxapi.Api.initApi(getApplicationContext());
        MyAccountInfo.init(getApplicationContext());
        initBgm(this);

        Log.d(TAG, "Init in MarsApplication takes: " + (System.currentTimeMillis() - start));
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.imageDecoder(new VideoThumbDecoder(true));
        config.writeDebugLogs(); // Remove for release app
        config.defaultDisplayImageOptions(new DisplayImageOptions.Builder()
                                              .cacheInMemory(true)
                                              .cacheOnDisk(true)
                                              .considerExifParams(true)
                                              .bitmapConfig(Bitmap.Config.RGB_565)
                                              .build());

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

    private static void initBgm(final Context context) {
        new Thread() {
            @Override
            public void run() {
                BgmUtil.initBgms(context);
            }
        }.start();
    }
}
