package com.koolew.mars.redpoint;

import com.android.volley.Response;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by jinchangzhu on 10/7/15.
 */
public class RedPointManager {

    public static final String PATH_DRAWER_TOGGLE = "/drawer";
    public static final String PATH_FRIENDS = PATH_DRAWER_TOGGLE + "/friends";
    public static final String PATH_MESSAGE = "/message";
    public static final String PATH_KOO = PATH_MESSAGE + "/koo";
    public static final String PATH_TASK = PATH_MESSAGE + "/task";
    public static final String PATH_DANMAKU = PATH_MESSAGE + "/danmaku";
    public static final String PATH_NOTIFICATION = PATH_MESSAGE + "/notification";


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
        refreshByPath(path, 0);

        if (path.equals(PATH_FRIENDS)) {
            redPointInfo.setSuggestion(0);
        }
        else if (path.equals(PATH_TASK)) {
            redPointInfo.setAssignment(0);
        }
        else if (path.equals(PATH_DANMAKU)) {
            redPointInfo.setComment(0);
        }
        else if (path.equals(PATH_NOTIFICATION)) {
            redPointInfo.setActivity(0);
        }
    }

    public static void refresh(RedPointInfo info) {
        if (!info.equals(redPointInfo)) {
            if (getSuggestionCount() != info.getSuggestion()) {
                refreshByPath(PATH_FRIENDS, info.getSuggestion());
            }
            if (getAssignmentCount() != info.getAssignment()) {
                refreshByPath(PATH_TASK, info.getAssignment());
            }
            if (getDanmakuCount() != info.getComment()) {
                refreshByPath(PATH_DANMAKU, info.getComment());
            }
            if (getNotificationCount() != info.getActivity()) {
                refreshByPath(PATH_NOTIFICATION, info.getActivity());
            }

            redPointInfo = info;
        }
    }

    private static void refreshByPath(String path, int count) {
        Set<String> keys = registed.keySet();
        for (String key: keys) {
            if (path.startsWith(key)) {
                registed.get(key).setCount(count);
            }
        }
    }

    private static int getCountByPath(String path) {
        int count = 0;

        if (PATH_FRIENDS.startsWith(path)) {
            count += getSuggestionCount();
        }
        if (PATH_TASK.startsWith(path)) {
            count += getAssignmentCount();
        }
        if (PATH_DANMAKU.startsWith(path)) {
            count += getDanmakuCount();
        }
        if (PATH_NOTIFICATION.startsWith(path)) {
            count += getNotificationCount();
        }

        return count;
    }

    private static int getFeedsCount() {
        if (redPointInfo == null) {
            return 0;
        }
        else {
            return redPointInfo.getFeeds();
        }
    }

    private static int getAssignmentCount() {
        if (redPointInfo == null) {
            return 0;
        }
        else {
            return redPointInfo.getAssignment();
        }
    }

    private static int getSuggestionCount() {
        if (redPointInfo == null) {
            return 0;
        }
        else {
            return redPointInfo.getSuggestion();
        }
    }

    private static int getDanmakuCount() {
        if (redPointInfo == null) {
            return 0;
        }
        else {
            return redPointInfo.getComment();
        }
    }

    private static int getMeCount() {
        if (redPointInfo == null) {
            return 0;
        }
        else {
            return redPointInfo.getMe();
        }
    }

    private static int getNotificationCount() {
        if (redPointInfo == null) {
            return 0;
        }
        else {
            return redPointInfo.getActivity();
        }
    }

    public static void refreshRedPoint() {
        ApiWorker.getInstance().requestNotificationBrief(
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
                null);
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
