package com.koolew.mars.infos;

import com.koolew.mars.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 12/16/15.
 */
public class Tag {

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";

    protected String id;
    protected String name;

    public Tag(JSONObject jsonObject) {
        id = JsonUtil.getStringIfHas(jsonObject, KEY_ID);
        name = JsonUtil.getStringIfHas(jsonObject, KEY_NAME);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_ID, id);
            jsonObject.put(KEY_NAME, name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
