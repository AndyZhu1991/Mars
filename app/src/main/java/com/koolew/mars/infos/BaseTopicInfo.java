package com.koolew.mars.infos;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by jinchangzhu on 7/15/15.
 */
public class BaseTopicInfo implements Serializable {

    public static final String KEY_TOPIC_ID = "topic_id";
    public static final String KEY_TITLE = "content";
    public static final String KEY_DESC = "desc";
    public static final String KEY_VIDEO_COUNT = "video_cnt";
    public static final String KEY_THUMB = "thumb_url";
    public static final String KEY_UPDATE_TIME = "update_time";

    protected String topicId;
    protected String title;
    protected String desc;
    protected int videoCount;
    protected String thumb;
    protected long updateTime;

    public BaseTopicInfo(JSONObject jsonObject) {
        try {
            if (jsonObject.has(KEY_TOPIC_ID)) {
                topicId = jsonObject.getString(KEY_TOPIC_ID);
            }
            if (jsonObject.has(KEY_TITLE)) {
                title = jsonObject.getString(KEY_TITLE);
            }
            if (jsonObject.has(KEY_DESC)) {
                desc = jsonObject.getString(KEY_DESC);
            }
            if (jsonObject.has(KEY_VIDEO_COUNT)) {
                videoCount = jsonObject.getInt(KEY_VIDEO_COUNT);
            }
            if (jsonObject.has(KEY_THUMB)) {
                thumb = jsonObject.getString(KEY_THUMB);
            }
            if (jsonObject.has(KEY_UPDATE_TIME)) {
                updateTime = jsonObject.getLong(KEY_UPDATE_TIME);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getThumb() {
        return thumb;
    }

    public String getTitle() {
        return title;
    }

    public String getTopicId() {
        return topicId;
    }

    public String getDesc() {
        return desc;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public int getVideoCount() {
        return videoCount;
    }
}
