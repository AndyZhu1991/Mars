package com.koolew.mars.preference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jinchangzhu on 7/2/15.
 */
public class PreferenceHelper {

    public static final String KEY_PREFERENCE = "koolew-preference";

    public static final String KEY_INTEL_SAVE_DATA = "intelligent save data";
    public static final String KEY_NEW_FRIEND_APPLY = "new friend apply";
    public static final String KEY_NEW_VIDEO_BY_FRIEND = "new video by friend";
    public static final String KEY_DANMAKUED_BY_FRIEND = "danmakued by friend";
    public static final String KEY_I_GOT_KOO = "I got koo";

    public static final boolean DEFAULT_INTEL_SAVE_DATA = true;
    public static final boolean DEFAULT_NEW_FRIEND_APPLY = true;
    public static final boolean DEFAULT_NEW_VIDEO_BY_FRIEND = true;
    public static final boolean DEFAULT_DANMAKUED_BY_FRIEND = true;
    public static final boolean DEFAULT_I_GOT_KOO = false;

    private SharedPreferences mSharedPreference;

    public PreferenceHelper(Context context) {
        mSharedPreference = context.getSharedPreferences(KEY_PREFERENCE, Context.MODE_APPEND);
    }

    public boolean getIntelligentSaveData() {
        return mSharedPreference.getBoolean(KEY_INTEL_SAVE_DATA, DEFAULT_INTEL_SAVE_DATA);
    }

    public boolean getNewFriendApply() {
        return mSharedPreference.getBoolean(KEY_NEW_FRIEND_APPLY, DEFAULT_NEW_FRIEND_APPLY);
    }

    public boolean getNewVideoByFriend() {
        return mSharedPreference.getBoolean(KEY_NEW_VIDEO_BY_FRIEND, DEFAULT_NEW_VIDEO_BY_FRIEND);
    }

    public boolean getDanmakuedByFriend() {
        return mSharedPreference.getBoolean(KEY_DANMAKUED_BY_FRIEND, DEFAULT_DANMAKUED_BY_FRIEND);
    }

    public boolean getIGotKoo() {
        return mSharedPreference.getBoolean(KEY_I_GOT_KOO, DEFAULT_I_GOT_KOO);
    }
}
