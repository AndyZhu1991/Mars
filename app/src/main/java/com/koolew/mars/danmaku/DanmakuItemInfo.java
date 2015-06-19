package com.koolew.mars.danmaku;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jinchangzhu on 6/18/15.
 */
public class DanmakuItemInfo {
    String uid;
    String nickname;
    String avatar;

    String content;
    int kooNum;

    float x;
    float y;

    float showTime;


    public static DanmakuItemInfo fromJSONObject(JSONObject jsonObject) {
        DanmakuItemInfo info = new DanmakuItemInfo();

        try {
            JSONObject userInfo = jsonObject.getJSONObject("user_info");
            info.uid = userInfo.getString("uid");
            info.nickname = userInfo.getString("nickname");
            info.avatar = userInfo.getString("avatar");
            info.content = jsonObject.getString("content");
            info.kooNum = jsonObject.getInt("koo_num");
            JSONObject position = jsonObject.getJSONObject("position");
            info.x = (float) position.getDouble("x");
            info.y = (float) position.getDouble("y");
            info.showTime = (float) jsonObject.getDouble("show_time");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return info;
    }

    public static ArrayList<DanmakuItemInfo> fromJSONArray(JSONArray jsonArray) {
        ArrayList<DanmakuItemInfo> list = new ArrayList<DanmakuItemInfo>();

        int count = jsonArray.length();
        for (int i = 0; i < count; i++) {
            try {
                list.add(fromJSONObject((JSONObject) jsonArray.get(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return list;
    }
}