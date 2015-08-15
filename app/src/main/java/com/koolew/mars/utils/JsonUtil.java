package com.koolew.mars.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 8/15/15.
 */
public class JsonUtil {

    public static int getIntIfHas(JSONObject jsonObject, String key) {
        return getIntIfHas(jsonObject, key, 0);
    }

    public static int getIntIfHas(JSONObject jsonObject, String key, int defaultValue) {
        try {
            if (jsonObject.has(key)) {
                return jsonObject.getInt(key);
            }
        }
        catch (JSONException jse) {
            // Here should never arrive
            throw new RuntimeException("Here should never arrive");
        }
        return defaultValue;
    }
}
