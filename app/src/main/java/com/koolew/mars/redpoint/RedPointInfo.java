package com.koolew.mars.redpoint;

import com.koolew.mars.utils.DoNotConfuse;
import com.koolew.mars.utils.JsonUtil;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 10/7/15.
 */
public class RedPointInfo implements DoNotConfuse {
    int feeds;
    int assignment;
    int suggestion;
    int comment;
    int me;
    int activity;
    int koo;
    int profit;

    public RedPointInfo(JSONObject result) {
        feeds      = JsonUtil.getIntIfHas(result, "feeds");
        assignment = JsonUtil.getIntIfHas(result, "assignment");
        suggestion = JsonUtil.getIntIfHas(result, "suggestion");
        comment    = JsonUtil.getIntIfHas(result, "comment");
        me         = JsonUtil.getIntIfHas(result, "me");
        activity   = JsonUtil.getIntIfHas(result, "activity");
        koo        = JsonUtil.getIntIfHas(result, "koo");
        profit     = JsonUtil.getIntIfHas(result, "profit");
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
            if (assignment != other.assignment) {
                return false;
            }
            if (suggestion != other.suggestion) {
                return false;
            }
            if (comment != other.comment) {
                return false;
            }
            if (me != other.me) {
                return false;
            }
            if (activity != other.activity) {
                return false;
            }
            if (koo != other.koo) {
                return false;
            }
            if (profit != other.profit) {
                return false;
            }
            return true;
        }
        else {
            return false;
        }
    }
}
