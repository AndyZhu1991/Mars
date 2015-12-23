package com.koolew.mars.utils;

import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by jinchangzhu on 12/19/15.
 */
public class DeviceDetective {

    public static boolean isMi3() {
        return Build.MODEL.startsWith("MI 3");
    }

    public static String getMiuiVersionName() {
        String line;
        BufferedReader reader = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name" );
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = reader.readLine();
            return line;
        } catch (IOException e) {
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "UNKNOWN";
    }
}
