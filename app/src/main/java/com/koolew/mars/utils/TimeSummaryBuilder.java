package com.koolew.mars.utils;

import android.content.Context;

import com.koolew.mars.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jinchangzhu on 3/7/16.
 */
public class TimeSummaryBuilder {

    private static long MILLIS_IN_SECOND = 1000;
    private static long MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
    private static long MILLIS_IN_HOUR   = MILLIS_IN_MINUTE * 60;
    private static long MILLIS_IN_DAY    = MILLIS_IN_HOUR   * 24;

    public static String buildTimeSummary(Context context, long millis) {
        long millisDiff = System.currentTimeMillis() - millis;
        if (millisDiff < 0) {
            millisDiff = 0;
        }

        if (millisDiff < MILLIS_IN_MINUTE) {
            return context.getString(R.string.before_n_seconds, millisDiff / MILLIS_IN_SECOND);
        }
        else if (millisDiff < MILLIS_IN_HOUR) {
            return context.getString(R.string.before_n_minutes, millisDiff / MILLIS_IN_MINUTE);
        }
        else if (millisDiff < MILLIS_IN_DAY) {
            return context.getString(R.string.before_n_hours, millisDiff / MILLIS_IN_HOUR);
        }

        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(millis));
    }
}
