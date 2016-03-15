package com.koolew.android.preference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jinchangzhu on 3/7/16.
 */
public class PreferenceHelper {

    public static final String KEY_PREFERENCE = "koolew-preference";

    protected SharedPreferences mSharedPreference;

    public PreferenceHelper(Context context) {
        mSharedPreference = context.getSharedPreferences(KEY_PREFERENCE, Context.MODE_APPEND);
    }
}
