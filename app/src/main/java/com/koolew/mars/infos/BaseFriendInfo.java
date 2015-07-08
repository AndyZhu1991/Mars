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
            uid = jsonObject.getString("uid");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            phoneNumber = jsonObject.getString("phone");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            nickname = jsonObject.getString("nickname");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            avatar = jsonObject.getString("avatar");
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
