package com.koolew.mars.preference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jinchangzhu on 7/2/15.
 */
public class PreferenceHelper extends com.koolew.android.preference.PreferenceHelper {

    public static final String KEY_INTEL_SAVE_DATA = "intelligent save data";
    public static final String KEY_NEW_FRIEND_APPLY = "new friend apply";
    public static final String KEY_NEW_VIDEO_BY_FRIEND = "new video by friend";
    public static final String KEY_DANMAKUED_BY_FRIEND = "danmakued by friend";
    public static final String KEY_I_GOT_KOO = "I got koo";
    public static final String KEY_INVITED = "invited";
    public static final String KEY_INVITATION_ACCEPTED = "invitation accepted";

    public static final boolean DEFAULT_INTEL_SAVE_DATA     = false;
    public static final boolean DEFAULT_NEW_FRIEND_APPLY    = true;
    public static final boolean DEFAULT_NEW_VIDEO_BY_FRIEND = false;
    public static final boolean DEFAULT_DANMAKUED_BY_FRIEND = true;
    public static final boolean DEFAULT_I_GOT_KOO           = false;
    public static final boolean DEFAULT_INVITED             = true;
    public static final boolean DEFAULT_INVITATION_ACCEPTED = true;

    public static final int NEW_FRIEND_APPLY_BIT    = 0x01;
    public static final int NEW_VIDEO_BY_FRIEND_BIT = 0x02;
    public static final int DANMAKUED_BY_FRIEND_BIT = 0x04;
    public static final int I_GOT_KOO_BIT           = 0x08;
    public static final int INVITED_BIT             = 0x10;
    public static final int INVITATION_ACCEPTED_BIT = 0x20;

    public PreferenceHelper(Context context) {
        super(context);
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

    public boolean getInvited() {
        return mSharedPreference.getBoolean(KEY_INVITED, DEFAULT_INVITED);
    }

    public boolean getInvitationAccepted() {
        return mSharedPreference.getBoolean(KEY_INVITATION_ACCEPTED, DEFAULT_INVITATION_ACCEPTED);
    }

    public void setPushBit(int pushBit) {
        SharedPreferences.Editor editor = mSharedPreference.edit();

        editor.putBoolean(KEY_NEW_FRIEND_APPLY,    (pushBit & NEW_FRIEND_APPLY_BIT)    != 0);
        editor.putBoolean(KEY_NEW_VIDEO_BY_FRIEND, (pushBit & NEW_VIDEO_BY_FRIEND_BIT) != 0);
        editor.putBoolean(KEY_DANMAKUED_BY_FRIEND, (pushBit & DANMAKUED_BY_FRIEND_BIT) != 0);
        editor.putBoolean(KEY_I_GOT_KOO,           (pushBit & I_GOT_KOO_BIT)           != 0);
        editor.putBoolean(KEY_INVITED,             (pushBit & INVITED_BIT)             != 0);
        editor.putBoolean(KEY_INVITATION_ACCEPTED, (pushBit & INVITATION_ACCEPTED_BIT) != 0);

        editor.commit();
    }

    public int getPushBit() {
        return  (getNewFriendApply()     ? NEW_FRIEND_APPLY_BIT    : 0) +
                (getNewVideoByFriend()   ? NEW_VIDEO_BY_FRIEND_BIT : 0) +
                (getDanmakuedByFriend()  ? DANMAKUED_BY_FRIEND_BIT : 0) +
                (getIGotKoo()            ? I_GOT_KOO_BIT           : 0) +
                (getInvited()            ? INVITED_BIT             : 0) +
                (getInvitationAccepted() ? INVITATION_ACCEPTED_BIT : 0) ;
    }
}
