package com.koolew.mars.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jinchangzhu on 11/23/15.
 */
public class FirstHintUtil {

    private static final String KEY_FIRST_HINT = "first_hint";

    private static final String KEY_FIRST_MOVIE = "first_movie";

    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences(KEY_FIRST_HINT, Context.MODE_APPEND);
    }

    public static boolean isFirstMovie() {
        return isFirst(KEY_FIRST_MOVIE);
    }

    private static boolean isFirst(String key) {
        boolean isFirst = sharedPreferences.getBoolean(key, true);
        if (isFirst) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(key, false);
            editor.commit();
        }
        return isFirst;
    }
}
