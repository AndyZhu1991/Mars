package com.koolew.mars.infos;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 9/21/15.
 */
public class BaseCommentInfo {

    public static final String KEY_COMMENT_ID = "comment_id";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_KOO_COUNT = "koo_num";
    public static final String KEY_POSITION = "position";
    public static final String KEY_SHOW_TIME = "show_time";
    public static final String KEY_CREATE_TIME = "create_time";
    public static final String KEY_USER_INFO = "user";

    protected String commentId;
    protected String content;
    protected int kooCount;
    protected Position position;
    protected double showTime;
    protected long createTime;
    protected BaseUserInfo userInfo;

    public BaseCommentInfo(JSONObject jsonObject) {
        try {
            if (jsonObject.has(KEY_COMMENT_ID)) {
                commentId = jsonObject.getString(KEY_COMMENT_ID);
            }
            if (jsonObject.has(KEY_CONTENT)) {
                content = jsonObject.getString(KEY_CONTENT);
            }
            if (jsonObject.has(KEY_KOO_COUNT)) {
                kooCount = jsonObject.getInt(KEY_KOO_COUNT);
            }
            if (jsonObject.has(KEY_POSITION)) {
                position = new Position(jsonObject.getJSONObject(KEY_POSITION));
            }
            if (jsonObject.has(KEY_SHOW_TIME)) {
                showTime = jsonObject.getDouble(KEY_SHOW_TIME);
            }
            if (jsonObject.has(KEY_CREATE_TIME)) {
                createTime = jsonObject.getLong(KEY_CREATE_TIME);
            }
            if (jsonObject.has(KEY_USER_INFO)) {
                userInfo = new BaseUserInfo(jsonObject.getJSONObject(KEY_USER_INFO));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCommentId() {
        return commentId;
    }

    public String getContent() {
        return content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public int getKooCount() {
        return kooCount;
    }

    public Position getPosition() {
        return position;
    }

    public double getShowTime() {
        return showTime;
    }

    public BaseUserInfo getUserInfo() {
        return userInfo;
    }

    public static class Position {
        public static final String KEY_X = "x";
        public static final String KEY_Y = "y";

        protected double x;
        protected double y;

        public Position(JSONObject jsonObject) {
            try {
                if (jsonObject.has(KEY_X)) {
                    x = jsonObject.getDouble(KEY_X);
                }
                if (jsonObject.has(KEY_Y)) {
                    y = jsonObject.getDouble(KEY_Y);
                }
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
    }
}
