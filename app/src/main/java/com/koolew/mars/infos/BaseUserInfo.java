package com.koolew.mars.infos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 7/8/15.
 */
public class BaseUserInfo implements Serializable {

    public static final int VIP_TYPE_NO_VIP = 0;
    public static final int VIP_TYPE_GOLD_VIP = 1;
    public static final int VIP_TYPE_SILVER_VIP = 2;

    public static final String KEY_UID = "uid";
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_VIP = "vip";

    private String uid;
    private String nickname;
    private String avatar;
    private int vip;

    public BaseUserInfo(JSONObject jsonObject) {
        try {
            if (jsonObject.has(KEY_UID)) {
                uid = jsonObject.getString(KEY_UID);
            }
            if (jsonObject.has(KEY_NICKNAME)) {
                nickname = jsonObject.getString(KEY_NICKNAME);
            }
            if (jsonObject.has(KEY_AVATAR)) {
                avatar = jsonObject.getString(KEY_AVATAR);
            }
            if (jsonObject.has(KEY_VIP)) {
                vip = jsonObject.getInt(KEY_VIP);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isAllFeildNotNull() {
        return  uid != null &&
                nickname != null &&
                avatar != null;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUid() {
        return uid;
    }

    public int getVip() {
        return vip;
    }

    public static List<BaseUserInfo> fromJSONArray(JSONArray jsonArray) {
        List<BaseUserInfo> list = new ArrayList<>();

        int count = jsonArray.length();
        for (int i = 0; i < count; i++) {
            try {
                list.add(new BaseUserInfo(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return list;
    }
}
