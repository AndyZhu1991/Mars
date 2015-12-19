package com.koolew.mars.utils;

import android.os.Build;

/**
 * Created by jinchangzhu on 12/19/15.
 */
public class DeviceDetective {

    public static boolean isMi3() {
        return Build.MODEL.startsWith("MI 3");
    }
}
