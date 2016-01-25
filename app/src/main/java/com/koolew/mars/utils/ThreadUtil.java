package com.koolew.mars.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jinchangzhu on 1/19/16.
 */
public class ThreadUtil {
    private static ExecutorService commonExecutor = Executors.newFixedThreadPool(3);
    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void executeOnCommonThread(Runnable runnable) {
        commonExecutor.execute(runnable);
    }

    public static void executeOnMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }
}
