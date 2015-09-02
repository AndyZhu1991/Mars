package com.koolew.mars.infos;

import com.koolew.mars.danmaku.DanmakuItemInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jinchangzhu on 8/12/15.
 */
public class BaseVideoInfo implements Serializable {

    public static final String KEY_VIDEO_ID = "video_id";
    public static final String KEY_VIDEO_URL = "video_url";
    public static final String KEY_THUMB_URL = "thumb_url";
    public static final String KEY_CREATE_TIME = "create_time";
    public static final String KEY_PRIVACY = "privacy";
    public static final String KEY_KOO_TOTAL = "koo_total";
    public static final String KEY_USER_INFO = "user_info";
    public static final String KEY_DANMAKU = "comment";

    private String mVideoId;
    private String mVideoUrl;
    private String mVideoThumb;
    private long mCreateTime;
    private int mPrivacy;
    private int mKooTotal;
    private BaseUserInfo mUserInfo;
    private ArrayList<DanmakuItemInfo> mDanmakus;

    protected BaseVideoInfo() {
    }

    public BaseVideoInfo(JSONObject jsonObject) {
        try {
            mVideoId = jsonObject.getString(KEY_VIDEO_ID);
        } catch (JSONException e) {
            throw new IllegalArgumentException("There is no video_id in a video item JSONObject");
        }
        try {
            mVideoUrl = jsonObject.getString(KEY_VIDEO_URL);
        } catch (JSONException e) {
            throw new IllegalArgumentException("There is no video_url in a video item JSONObject");
        }

        try {
            if (jsonObject.has(KEY_THUMB_URL)) {
                mVideoThumb = jsonObject.getString(KEY_THUMB_URL);
            }
            if (jsonObject.has(KEY_CREATE_TIME)) {
                mCreateTime = jsonObject.getLong(KEY_CREATE_TIME);
            }
            if (jsonObject.has(KEY_PRIVACY)) {
                mPrivacy = jsonObject.getInt(KEY_PRIVACY);
            }
            if (jsonObject.has(KEY_KOO_TOTAL)) {
                mKooTotal = jsonObject.getInt(KEY_KOO_TOTAL);
            }
            if (jsonObject.has(KEY_USER_INFO)) {
                mUserInfo = instanceUserInfo(jsonObject.getJSONObject(KEY_USER_INFO));
            }
            if (jsonObject.has(KEY_DANMAKU)) {
                mDanmakus = instanceDanmakus(jsonObject.getJSONArray(KEY_DANMAKU));
            }
            else {
                mDanmakus = new ArrayList<>();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected BaseUserInfo instanceUserInfo(JSONObject userJson) {
        return new BaseUserInfo(userJson);
    }

    protected ArrayList<DanmakuItemInfo> instanceDanmakus(JSONArray danmakuArray) {
        return DanmakuItemInfo.fromJSONArray(danmakuArray);
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public ArrayList<DanmakuItemInfo> getDanmakus() {
        return mDanmakus;
    }

    public int getPrivacy() {
        return mPrivacy;
    }

    public int getKooTotal() {
        return mKooTotal;
    }

    public void setKooTotal(int count) {
        mKooTotal = count;
    }

    public BaseUserInfo getUserInfo() {
        return mUserInfo;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public String getVideoThumb() {
        return mVideoThumb;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }
}
