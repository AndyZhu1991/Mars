package com.koolew.mars.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jinchangzhu on 1/19/16.
 */
public class ThreadUtil {
    private static ExecutorService commonExecutor = Executors.newFixedThreadPool(3);

    public static void executeOnCommonThread(Runnable runnable) {
        commonExecutor.execute(runnable);
    }
}
