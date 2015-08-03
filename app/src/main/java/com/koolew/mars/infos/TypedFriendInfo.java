package com.koolew.mars.infos;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 8/3/15.
 */
public class TypedFriendInfo extends BaseFriendInfo {

    public static final int TYPE_UNKNOWN         = -1;
    public static final int TYPE_SELF            = 0;
    public static final int TYPE_STRANGER        = 1;
    public static final int TYPE_SENT_INVITATION = 2;
    public static final int TYPE_INVITED_ME      = 3;
    public static final int TYPE_FRIEND          = 4;
    public static final int TYPE_NO_REGISTER     = 5;

    protected int mType;

    public TypedFriendInfo(JSONObject jsonObject, int type) {
        super(jsonObject);

        mType = type;
    }

    public TypedFriendInfo(JSONObject jsonObject) {
        super(jsonObject);

        try {
            if (jsonObject.has("type")) {
                mType = jsonObject.getInt("type");
            }
        }
        catch (JSONException jse) {
        }
    }
}
