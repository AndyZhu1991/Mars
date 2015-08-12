package com.koolew.mars.infos;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by jinchangzhu on 5/25/15.
 */
public class MyAccountInfo {

    private static final String KEY_KOOLEW_PREFERENCES = "koolew preference";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_PHONE_NUMBER = "phone number";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_REGISTRATION_ID = "registration id";

    private static SharedPreferences sharedPreferences;
    private static String sPhoneNumber;
    private static String sToken;
    private static String sUid;
    private static String sAvatar;
    private static String sNickname;
    private static String sRegistrationId;
    private static long sKooNum;
    private static long sCoinNum;
    private static int sPushBit;
    private static LOGIN_TYPE sLoginType;
    private static String sSnsUid;
    private static String sSnsUnionId;

    public static enum LOGIN_TYPE {
        MOBILE, WEIBO, QQ, WECHAT
    }

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences(KEY_KOOLEW_PREFERENCES, Context.MODE_APPEND);
        sNickname = sharedPreferences.getString(KEY_NICKNAME, null);
        sPhoneNumber = sharedPreferences.getString(KEY_PHONE_NUMBER, null);
        sToken = sharedPreferences.getString(KEY_TOKEN, null);
        sRegistrationId = sharedPreferences.getString(KEY_REGISTRATION_ID, null);
    }

    public static void clear() {
        Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

        sToken = null;
    }

    public static String getAvatar() {
        return sAvatar;
    }

    public static void setAvatar(String avatar) {
        sAvatar = avatar;
    }

    public static void setRegistrationId(String registrationId) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_REGISTRATION_ID, registrationId);
        editor.commit();
        sRegistrationId = registrationId;
    }

    public static String getRegistrationId() {
        return sRegistrationId;
    }

    public static long getCoinNum() {
        return sCoinNum;
    }

    public static void setCoinNum(long coinNum) {
        sCoinNum = coinNum;
    }

    public static long getKooNum() {
        return sKooNum;
    }

    public static void setKooNum(long kooNum) {sKooNum = kooNum;
    }

    public static String getNickname() {
        return sNickname;
    }

    public static void setNickname(String nickname) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NICKNAME, nickname);
        editor.commit();
        sNickname = nickname;
    }

    public static String getToken() {
        return sToken;
    }

    public static void setToken(String token) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.commit();
        sToken = token;
    }

    public static String getUid() {
        return sUid;
    }

    public static void setUid(String uid) {
        MyAccountInfo.sUid = uid;
    }

    public static String getPhoneNumber() {
        return sPhoneNumber;
    }

    public static void setPhoneNumber(String phoneNumber) {
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);
        editor.commit();
        sPhoneNumber = phoneNumber;
    }

    public static LOGIN_TYPE getLoginType() {
        return sLoginType;
    }

    public static void setLoginType(LOGIN_TYPE loginType) {
        sLoginType = loginType;
    }

    public static String getSnsUid() {
        return sSnsUid;
    }

    public static void setSnsUid(String snsUid) {
        MyAccountInfo.sSnsUid = snsUid;
    }

    public static String getSnsUnionId() {
        return sSnsUnionId;
    }

    public static void setSnsUnionId(String snsUnionId) {
        MyAccountInfo.sSnsUnionId = snsUnionId;
    }
}
