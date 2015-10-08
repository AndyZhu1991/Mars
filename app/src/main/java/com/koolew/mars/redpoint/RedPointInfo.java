package com.koolew.mars.redpoint;

import com.koolew.mars.utils.JsonUtil;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 10/7/15.
 */
public class RedPointInfo {
    private int feeds;
    private int assignment;
    private int suggestion;
    private int comment;
    private int me;
    private int activity;

    public RedPointInfo(JSONObject result) {
        feeds      = JsonUtil.getIntIfHas(result, "feeds");
        assignment = JsonUtil.getIntIfHas(result, "assignment");
        suggestion = JsonUtil.getIntIfHas(result, "suggestion");
        comment    = JsonUtil.getIntIfHas(result, "comment");
        me         = JsonUtil.getIntIfHas(result, "me");
        activity   = JsonUtil.getIntIfHas(result, "activity");
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

    public int getActivity() {
        return activity;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public void setAssignment(int assignment) {
        this.assignment = assignment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public void setFeeds(int feeds) {
        this.feeds = feeds;
    }

    public void setMe(int me) {
        this.me = me;
    }

    public void setSuggestion(int suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }
        if (this == o) {
            return true;
        }

        if (o instanceof RedPointInfo) {
            RedPointInfo other = (RedPointInfo) o;
            if (feeds != other.feeds) {
                return false;
            }
            if (assignment != other.getAssignment()) {
                return false;
            }
            if (suggestion != other.getSuggestion()) {
                return false;
            }
            if (comment != other.getComment()) {
                return false;
            }
            if (me != other.getMe()) {
                return false;
            }
            if (activity != other.getActivity()) {
                return false;
            }
            return true;
        }
        else {
            return false;
        }
    }
}
