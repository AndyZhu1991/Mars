package com.koolew.mars.infos;

import com.koolew.mars.utils.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by jinchangzhu on 11/13/15.
 */
public class MovieTopicInfo extends BaseTopicInfo implements Serializable {

    public static final String KEY_MOVIE_ATTR = "movie";
    public static final String KEY_VIDEO_URL = "video_url";
    public static final String KEY_THUMBNAIL = "thumbnail";
    public static final String KEY_FRAGMENTS = "fragments";

    protected String videoUrl;
    protected String thumbnail;
    protected MovieFragment[] fragments;

    public MovieTopicInfo(JSONObject jsonObject) {
        super(jsonObject);

        JSONObject attr = JsonUtil.getJSONObjectIfHas(jsonObject, KEY_ATTR);
        if (attr == null) {
            return;
        }
        JSONObject movieAttr = JsonUtil.getJSONObjectIfHas(attr, KEY_MOVIE_ATTR);
        if (movieAttr == null) {
            return;
        }
        videoUrl = JsonUtil.getStringIfHas(movieAttr, KEY_VIDEO_URL);
        thumbnail = JsonUtil.getStringIfHas(movieAttr, KEY_THUMBNAIL);
        JSONArray jsonFragments = JsonUtil.getJSONArrayIfHas(movieAttr, KEY_FRAGMENTS);
        int length = jsonFragments.length();
        fragments = new MovieFragment[length];
        for (int i = 0; i < length; i++) {
            try {
                fragments[i] = new MovieFragment(jsonFragments.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public MovieFragment[] getFragments() {
        return fragments;
    }

    public static class MovieFragment implements Serializable {
        public static final String KEY_ACTOR_NAME = "actor";
        public static final String KEY_START = "start";
        public static final String KEY_END = "end";

        protected String actorName;
        protected int start;
        protected int end;

        public MovieFragment(JSONObject jsonObject) {
            actorName = JsonUtil.getStringIfHas(jsonObject, KEY_ACTOR_NAME);
            start = JsonUtil.getIntIfHas(jsonObject, KEY_START);
            end = JsonUtil.getIntIfHas(jsonObject, KEY_END);
        }

        public String getActorName() {
            return actorName;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getFrameCount() {
            return end - start + 1;
        }
    }
}
