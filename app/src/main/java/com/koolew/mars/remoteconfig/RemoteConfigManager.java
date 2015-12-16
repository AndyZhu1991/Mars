package com.koolew.mars.remoteconfig;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jinchangzhu on 12/16/15.
 */
public class RemoteConfigManager {
    private static final String KEY_REMOTE_CONFIG = "remote_config";

    private static RemoteConfigManager ourInstance;

    private Context mContext;

    // All remote configs
    private VideoTagsConfig videoTagsConfig;
    private MovieTagsConfig movieTagsConfig;


    public static void init(Context context) {
        ourInstance = new RemoteConfigManager(context);
        ourInstance.initInBackground();
    }

    public static RemoteConfigManager getInstance() {
        return ourInstance;
    }

    private RemoteConfigManager(Context context) {
        mContext = context;
        videoTagsConfig = new VideoTagsConfig(this);
        movieTagsConfig = new MovieTagsConfig(this);
    }

    private void initInBackground() {
        new Thread() {
            @Override
            public void run() {
                videoTagsConfig.init(true);
                movieTagsConfig.init(true);
            }
        }.start();
    }

    SharedPreferences getSp() {
        return mContext.getSharedPreferences(KEY_REMOTE_CONFIG, Context.MODE_APPEND);
    }

    public VideoTagsConfig getVideoTagsConfig() {
        return videoTagsConfig;
    }

    public MovieTagsConfig getMovieTagsConfig() {
        return movieTagsConfig;
    }
}
