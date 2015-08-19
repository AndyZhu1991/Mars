package com.koolew.mars.utils;

/**
 * Created by jinchangzhu on 8/19/15.
 */
public class MathUtil {

    public static boolean equalsApproximate(double d1, double d2, double deviation) {
        return Math.abs(d1 - d2) < Math.abs(deviation);
    }
}
