package com.koolew.mars.notification;

import com.koolew.mars.utils.JsonUtil;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 8/15/15.
 */
public class NotificationEvent {
    private int feeds;
    private int assignment;
    private int suggestion;
    private int comment;
    private int me;
    
    public NotificationEvent(JSONObject result) {
        feeds      = JsonUtil.getIntIfHas(result, "feeds");
        assignment = JsonUtil.getIntIfHas(result, "assignment");
        suggestion = JsonUtil.getIntIfHas(result, "suggestion");
        comment    = JsonUtil.getIntIfHas(result, "comment");
        me         = JsonUtil.getIntIfHas(result, "me");
    }

    public int getAssignment() {
        return assignment;
    }

    public int getComment() {
        return comment;
    }

    public int getFeeds() {
        return feeds;
    }

    public int getMe() {
        return me;
    }

    public int getSuggestion() {
        return suggestion;
    }
}
