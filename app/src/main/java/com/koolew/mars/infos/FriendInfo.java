package com.koolew.mars.infos;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 5/27/15.
 */
public class FriendInfo {

    private String uid;
    private String nickname;
    private String avatar;
    private String phone;
    private int type;

    public String getAvatar() {
        return avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhone() {
        return phone;
    }

    public int getType() {
        return type;
    }

    public String getUid() {
        return uid;
    }

    public static FriendInfo fromJson(JSONObject jsonObject) {
        FriendInfo info = new FriendInfo();
        try {
            info.uid = jsonObject.getString("uid");
            info.nickname = jsonObject.getString("nickname");
            info.avatar = jsonObject.getString("avatar");
            info.phone = jsonObject.getString("phone");
            info.type = jsonObject.getInt("type");
        } catch (JSONException e) {
            e.printStackTrace();
            info = null;
        }
        return info;
    }
}
