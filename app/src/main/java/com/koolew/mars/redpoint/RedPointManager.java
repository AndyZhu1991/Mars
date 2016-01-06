package com.koolew.mars.redpoint;

import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jinchangzhu on 10/7/15.
 */
public class RedPointManager {

    public static final String PATH_DRAWER_TOGGLE = "/drawer";
    public static final String PATH_FRIENDS = PATH_DRAWER_TOGGLE + "/friends";
    public static final String PATH_MESSAGE = PATH_DRAWER_TOGGLE + "/message";
    public static final String PATH_KOO = PATH_MESSAGE + "/koo";
    public static final String PATH_TASK = PATH_MESSAGE + "/task";
    public static final String PATH_DANMAKU = PATH_MESSAGE + "/danmaku";
    public static final String PATH_NOTIFICATION = PATH_MESSAGE + "/notification";
    public static final String PATH_PROFIT = PATH_DRAWER_TOGGLE + "/profit";

    public static final Map<String, String> PATH_NAME = new HashMap<>();
    static {
        PATH_NAME.put(PATH_DRAWER_TOGGLE, "");
        PATH_NAME.put(PATH_FRIENDS, "suggestion");
        PATH_NAME.put(PATH_MESSAGE, "");
        PATH_NAME.put(PATH_KOO, "koo");
        PATH_NAME.put(PATH_TASK, "assignment");
        PATH_NAME.put(PATH_DANMAKU, "comment");
        PATH_NAME.put(PATH_NOTIFICATION, "activity");
        PATH_NAME.put(PATH_PROFIT, "profit");
    }

    private static RedPointInfo redPointInfo;
    private static Map<String, RedPointView> registed = new HashMap<>();

    public static void register(String path, RedPointView view) {
        registed.put(path, view);
        view.setCount(getCountByPath(path));
    }

    public static void unregister(String path) {
        registed.remove(path);
    }

    public static void clearRedPointByPath(String path) {
        for (String key: PATH_NAME.keySet()) {
            if (path.equals(key)) {
                setRedPointValueByPath(path, 0);
            }
        }

        refreshByPath(path, 0);
    }

    public static void refresh(RedPointInfo info) {
        if (!info.equals(redPointInfo)) {
            RedPointInfo originalInfo = redPointInfo;
            redPointInfo = info;

            for (String path: PATH_NAME.keySet()) {
                int originalValue = getRedPointValueByPath(originalInfo, path);
                int newValue = getRedPointValueByPath(redPointInfo, path);
                if (originalValue != newValue) {
                    refreshByPath(path, newValue);
                }
            }
        }
    }

    private static void refreshByPath(String path, int count) {
        Set<String> keys = registed.keySet();
        for (String key: keys) {
            if (path.equals(key)) {
                registed.get(key).setCount(count);
            }
            else if (path.startsWith(key)) { // Parent path
                registed.get(key).setCount(getCountByPath(key));
            }
        }
    }

    private static int getCountByPath(String path) {
        int count = 0;

        for (String eachPath: PATH_NAME.keySet()) {
            if (eachPath.startsWith(path)) {
                count += getRedPointValueByPath(eachPath);
            }
        }

        return count;
    }

    private static int getRedPointValueByPath(String path) {
        return getRedPointValueByPath(redPointInfo, path);
    }

    private static int getRedPointValueByPath(RedPointInfo redPointInfo, String path) {
        if (redPointInfo == null) {
            return 0;
        }

        Field field = getRedPointFieldByPath(path);
        if (field == null) {
            return 0;
        }

        try {
            return field.getInt(redPointInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return 0;
    }
    private static void setRedPointValueByPath(String path, int value) {
        setRedPointValueByPath(redPointInfo, path, value);
    }

    private static void setRedPointValueByPath(RedPointInfo redPointInfo, String path, int value) {
        if (redPointInfo == null) {
            return;
        }

        Field field = getRedPointFieldByPath(path);
        if (field == null) {
            return;
        }

        try {
            field.setInt(redPointInfo, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Field getRedPointFieldByPath(String path) {
        Class redPointInfoClass = RedPointInfo.class;
        try {
            String name = PATH_NAME.get(path);
            if (TextUtils.isEmpty(name)) {
                return null;
            }
            return redPointInfoClass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void refreshRedPoint() {
        ApiWorker.getInstance().queueGetRequest(UrlHelper.NOTIFICATION_BRIEF_URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("code") == 0) {
                                refresh(new RedPointInfo(response.getJSONObject("result")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO
                    }
                });
    }

    public static void refreshDelayed(final long time) {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                refreshRedPoint();
            }
        }.start();
    }
}
