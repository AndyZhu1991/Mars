package com.koolew.mars.wxapi;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by jinchangzhu on 5/30/15.
 */
public class TokenKeeper {
    private static final String PREFERENCES_NAME = "wechat_sdk_android";

    private static final String KEY_OPENID        = "open_id";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_EXPIRES_IN    = "expires_in";
    private static final String KEY_UNIONID       = "union_id";

    public static void writeRefreshToken(Context context, String openId,
                                         String refreshToken, long expiresIn, String unionId) {
        if (null == context || null == refreshToken || null == openId
                || null == unionId || 0 == expiresIn) {
            return;
        }

        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_OPENID, openId);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putLong(KEY_EXPIRES_IN, expiresIn);
        editor.putString(KEY_UNIONID, unionId);
        editor.commit();
    }

    public static String readOpenId(Context context) {
        if (null == context) {
            return null;
        }
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND)
                .getString(KEY_OPENID, null);
    }

    public static String readRefreshToken(Context context) {
        if (null == context) {
            return null;
        }
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND)
                .getString(KEY_REFRESH_TOKEN, null);
    }

    public static long readExpiresIn(Context context) {
        if (null == context) {
            return 0;
        }
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND)
                .getLong(KEY_EXPIRES_IN, 0);
    }

    public static String readUnionId(Context context) {
        if (null == context) {
            return null;
        }
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND)
                .getString(KEY_UNIONID, null);
    }

    /**
     * 清空 SharedPreferences 中 Token信息。
     *
     * @param context 应用程序上下文环境
     */
    public static void clear(Context context) {
        if (null == context) {
            return;
        }

        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}
