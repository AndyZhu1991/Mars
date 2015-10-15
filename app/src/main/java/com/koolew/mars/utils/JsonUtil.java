package com.koolew.mars.utils;

import org.json.JSONArray;
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

    public static long getLongIfHas(JSONObject jsonObject, String key) {
        return getLongIfHas(jsonObject, key, 0);
    }

    public static long getLongIfHas(JSONObject jsonObject, String key, long defaultValue) {
        try {
            if (jsonObject.has(key)) {
                return jsonObject.getLong(key);
            }
        }
        catch (JSONException jse) {
            // Here should never arrive
            throw new RuntimeException("Here should never arrive");
        }
        return defaultValue;
    }


    public static double getDoubleIfHas(JSONObject jsonObject, String key) {
        return getDoubleIfHas(jsonObject, key, 0.0);
    }

    public static double getDoubleIfHas(JSONObject jsonObject, String key, double defaultValue) {
        try {
            if (jsonObject.has(key)) {
                return jsonObject.getDouble(key);
            }
        }
        catch (JSONException jse) {
            // Here should never arrive
            throw new RuntimeException("Here should never arrive");
        }
        return defaultValue;
    }


    public static JSONObject getJSONObjectIfHas(JSONObject jsonObject, String key) {
        return getJSONObjectIfHas(jsonObject, key, null);
    }

    public static JSONObject getJSONObjectIfHas(JSONObject jsonObject, String key,
                                                JSONObject defaultValue) {
        try {
            if (jsonObject.has(key)) {
                return jsonObject.getJSONObject(key);
            }
        }
        catch (JSONException jse) {
            // Here should never arrive
            throw new RuntimeException("Here should never arrive");
        }
        return defaultValue;
    }


    public static JSONArray getJSONArrayIfHas(JSONObject jsonObject, String key) {
        return getJSONArrayIfHas(jsonObject, key, null);
    }

    public static JSONArray getJSONArrayIfHas(JSONObject jsonObject, String key,
                                              JSONArray defaultValue) {
        try {
            if (jsonObject.has(key)) {
                return jsonObject.getJSONArray(key);
            }
        }
        catch (JSONException jse) {
            // Here should never arrive
            throw new RuntimeException("Here should never arrive");
        }
        return defaultValue;
    }
}
