package com.koolew.mars.infos;

/**
 * Created by jinchangzhu on 5/25/15.
 */
public class MyAccountInfo {

    private static String sPhoneNumber;
    private static String sToken;
    private static String sUid;
    private static String sAvatar;
    private static String sNickname;
    private static int sKooNum;
    private static int sCoinNum;

    public static String getAvatar() {
        return sAvatar;
    }

    public static void setAvatar(String avatar) {
        MyAccountInfo.sAvatar = avatar;
    }

    public static int getCoinNum() {
        return sCoinNum;
    }

    public static void setCoinNum(int coinNum) {
        MyAccountInfo.sCoinNum = coinNum;
    }

    public static int getKooNum() {
        return sKooNum;
    }

    public static void setKooNum(int kooNum) {
        MyAccountInfo.sKooNum = kooNum;
    }

    public static String getNickname() {
        return sNickname;
    }

    public static void setNickname(String nickname) {
        sNickname = nickname;
    }

    public static String getToken() {
        return sToken;
    }

    public static void setToken(String token) {
        MyAccountInfo.sToken = token;
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
        sPhoneNumber = phoneNumber;
    }
}
