package com.koolew.mars.infos;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/8/15.
 */
public class BaseFriendInfo {
    private String uid;
    private String phoneNumber;
    private String nickname;
    private String avatar;

    public BaseFriendInfo(JSONObject jsonObject) {
        try {
            if (jsonObject.has("uid")) {
                uid = jsonObject.getString("uid");
            }
            if (jsonObject.has("phone")) {
                phoneNumber = jsonObject.getString("phone");
            }
            if (jsonObject.has("nickname")) {
                nickname = jsonObject.getString("nickname");
            }
            if (jsonObject.has("avatar")) {
                avatar = jsonObject.getString("avatar");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isAllFeildNotNull() {
        return  uid != null &&
                phoneNumber != null &&
                nickname != null &&
                avatar != null;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUid() {
        return uid;
    }
}
