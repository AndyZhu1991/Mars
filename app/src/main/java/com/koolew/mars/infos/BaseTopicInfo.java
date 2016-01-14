package com.koolew.mars.infos;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.koolew.mars.MarsApplication;
import com.koolew.mars.MovieStudioActivity;
import com.koolew.mars.R;
import com.koolew.mars.VideoShootActivity;
import com.koolew.mars.utils.JsonUtil;

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
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_VIDEO_COUNT = "video_cnt";
    public static final String KEY_THUMB = "thumb_url";
    public static final String KEY_UPDATE_TIME = "update_time";
    public static final String KEY_ATTR = "attri";
    public static final String KEY_TAG_ID = "tag";

    public static final String CATEGORY_VIDEO = "video";
    public static final String CATEGORY_MOVIE = "movie";

    protected String topicId;
    protected String title;
    protected String desc;
    protected String category;
    protected int videoCount;
    protected String thumb;
    protected long updateTime;
    protected String tagId;

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
            category = JsonUtil.getStringIfHas(jsonObject, KEY_CATEGORY);
            if (jsonObject.has(KEY_VIDEO_COUNT)) {
                videoCount = jsonObject.getInt(KEY_VIDEO_COUNT);
            }
            if (jsonObject.has(KEY_THUMB)) {
                thumb = jsonObject.getString(KEY_THUMB);
            }
            if (jsonObject.has(KEY_UPDATE_TIME)) {
                updateTime = jsonObject.getLong(KEY_UPDATE_TIME);
            }
            tagId = JsonUtil.getStringIfHas(jsonObject, KEY_TAG_ID);
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

    public String getCategory() {
        return category;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public String getTagId() {
        return tagId;
    }

    public void gotoCapture(Context context) {
        if (BaseTopicInfo.CATEGORY_MOVIE.equals(category)) {
            startMovieStudioActivity(context);
        }
        else if (BaseTopicInfo.CATEGORY_VIDEO.equals(category)) {
            startVideoShootActivity(context);
        }
        else {
            if (MarsApplication.DEBUG) {
                throw new RuntimeException("Shen me gui: " + category);
            }
        }
    }

    private void startVideoShootActivity(Context context) {
        VideoShootActivity.startThisActivity(context, this);
    }

    private void startMovieStudioActivity(Context context) {
        if (this instanceof MovieTopicInfo) {
            MovieStudioActivity.startThisActivity(context, (MovieTopicInfo) this);
        }
        else {
            if (MarsApplication.DEBUG) {
                throw new RuntimeException("No MovieTopicInfo");
            }
            else {
                Toast.makeText(context, R.string.there_is_an_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static BaseTopicInfo dynamicTopicInfo(JSONObject jsonObject) {
        String category = JsonUtil.getStringIfHas(jsonObject, KEY_CATEGORY);

        if (!TextUtils.isEmpty(category)) {
            if (category.equals(CATEGORY_VIDEO)) {
                return new BaseTopicInfo(jsonObject);
            }
            else if (category.equals(CATEGORY_MOVIE)) {
                return new MovieTopicInfo(jsonObject);
            }
        }

        return null;
    }
}
