package com.koolew.mars.remoteconfig;

import android.content.SharedPreferences;

import com.koolew.mars.infos.Tag;
import com.koolew.mars.webapi.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 12/16/15.
 */
public class VideoTagsConfig extends BaseRemoteConfigItem<List<Tag>> {

    public VideoTagsConfig(RemoteConfigManager manager) {
        super(manager);
    }

    @Override
    String fetchUrl() {
        return UrlHelper.VIDEO_TAG_URL;
    }

    @Override
    List<Tag> fromResult(JSONObject result) {
        try {
            return fromJsonArray(result.getJSONArray("tags"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<Tag> fromJsonArray(JSONArray tagsJson) {
        List<Tag> tags = new ArrayList<>();
        try {
            int length = tagsJson.length();
            for (int i = 0; i < length; i++) {
                tags.add(new Tag(tagsJson.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tags;
    }

    @Override
    void saveToEditor(SharedPreferences.Editor editor, List<Tag> config) {
        JSONArray tagsJson = new JSONArray();
        for (int i = 0; i < config.size(); i++) {
            tagsJson.put(config.get(i));
        }

        editor.putString(defaultKey(), tagsJson.toString());
    }

    @Override
    List<Tag> readFromSp(SharedPreferences sp) {
        String savedString = sp.getString(defaultKey(), null);
        if (savedString != null) {
            try {
                return fromJsonArray(new JSONArray(savedString));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }
}
