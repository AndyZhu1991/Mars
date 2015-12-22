package com.koolew.mars;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.koolew.mars.imageloader.VideoThumbDecoder;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.remoteconfig.RemoteConfigManager;
import com.koolew.mars.utils.BgmUtil;
import com.koolew.mars.utils.Downloader;
import com.koolew.mars.utils.FirstHintUtil;
import com.koolew.mars.utils.KooSoundUtil;
import com.koolew.mars.utils.PatchUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import cn.jiajixin.nuwa.Nuwa;
import cn.jpush.android.api.JPushInterface;
import cn.sharesdk.framework.ShareSDK;

/**
 * Created by jinchangzhu on 5/27/15.
 */
public class MarsApplication extends Application {

    public static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = "koolew-MarsApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        long start = System.currentTimeMillis();

        PatchUtil.tryToLoadPatch(this);
        Utils.init(this);
        ApiWorker.init(getApplicationContext());
        initImageLoader(getApplicationContext());
        MyAccountInfo.init(getApplicationContext());
        initBgm(this);
        initJpush(getApplicationContext());
        ShareSDK.initSDK(getApplicationContext());
        initBugly();
        initUmeng();
        com.koolew.mars.videotools.Utils.preloadRecorder(this);
        FirstHintUtil.init(this);
        Downloader.init();
        KooSoundUtil.init(this);
        RemoteConfigManager.init(this);
        PatchUtil.checkAndUpdatePatchAsync(this);

        Log.d(TAG, "Is debug: " + DEBUG);
        Log.d(TAG, "Init in MarsApplication takes: " + (System.currentTimeMillis() - start));
    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
        Nuwa.init(this);
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
        if (DEBUG) {
            config.writeDebugLogs();
        }
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

    private void initJpush(Context context) {
        JPushInterface.setDebugMode(DEBUG);
        JPushInterface.init(context);
    }

    private void initBugly() {
        if (!DEBUG) {
            CrashReport.initCrashReport(getApplicationContext(), "900006713", false);
        }
    }

    private void initUmeng() {
        MobclickAgent.openActivityDurationTrack(false); // 禁止友盟默认的页面统计方式
        AnalyticsConfig.enableEncrypt(true); // 设置是否对日志信息进行加密
        MobclickAgent.setDebugMode(DEBUG);
    }
}
