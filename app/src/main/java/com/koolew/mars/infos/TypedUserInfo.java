package com.koolew.mars.infos;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 8/3/15.
 */
public class TypedUserInfo extends BaseUserInfo {

    public static final int TYPE_UNKNOWN         = -1;
    public static final int TYPE_SELF            = 0;
    public static final int TYPE_STRANGER        = 1;
    public static final int TYPE_FOLLOWED        = 2;
    public static final int TYPE_FAN             = 3;
    public static final int TYPE_FRIEND          = 4;
    public static final int TYPE_NO_REGISTER     = 5;

    protected int mType;

    public TypedUserInfo(JSONObject jsonObject, int type) {
        super(jsonObject);

        mType = type;
    }

    public TypedUserInfo(JSONObject jsonObject) {
        super(jsonObject);

        try {
            if (jsonObject.has("type")) {
                mType = jsonObject.getInt("type");
            }
        }
        catch (JSONException jse) {
        }
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }
}
