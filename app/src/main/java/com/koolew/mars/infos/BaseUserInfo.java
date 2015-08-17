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
    private String uid;
    private String nickname;
    private String avatar;

    public BaseUserInfo(JSONObject jsonObject) {
        try {
            if (jsonObject.has("uid")) {
                uid = jsonObject.getString("uid");
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
