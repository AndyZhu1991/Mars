package com.koolew.mars.statistics;

import com.koolew.mars.MarsApplication;

/**
 * Created by jinchangzhu on 1/19/16.
 */
public class StatisticsUtil {
    //                                        Set this to false to disable statistics
    //                                             ⬇    Always enable statistics in release version️
    public static final boolean NEED_STATISTICS = true | !MarsApplication.DEBUG;
}
