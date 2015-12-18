package com.koolew.mars.infos;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 8/3/15.
 */
public class TypedUserInfo extends BaseUserInfo {

    public TypedUserInfo(JSONObject jsonObject, int type) {
        super(jsonObject);

        this.type = type;
    }

    public TypedUserInfo(JSONObject jsonObject) {
        super(jsonObject);
    }
}
