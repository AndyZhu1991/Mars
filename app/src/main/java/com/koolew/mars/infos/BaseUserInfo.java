package com.koolew.mars.infos;

import com.koolew.mars.utils.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 7/8/15.
 */
public class BaseUserInfo implements Serializable {

    public static final int TYPE_UNKNOWN         = -1;
    public static final int TYPE_SELF            = 0;
    public static final int TYPE_STRANGER        = 1;
    public static final int TYPE_FOLLOWED        = 2;
    public static final int TYPE_FAN             = 3;
    public static final int TYPE_FRIEND          = 4;
    public static final int TYPE_NO_REGISTER     = 5;

    public static final int VIP_TYPE_NO_VIP = 0;
    public static final int VIP_TYPE_GOLD_VIP = 1;
    public static final int VIP_TYPE_SILVER_VIP = 2;

    public static final String KEY_UID = "uid";
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_VIP = "vip";
    public static final String KEY_FOLLOWS_COUNT = "follows";
    public static final String KEY_FANS_COUNT = "fans";
    public static final String KEY_KOO_COUNT = "koo_num";
    public static final String KEY_TYPE = "type";

    private String uid;
    private String nickname;
    private String avatar;
    private int vip;
    private int followsCount;
    private int fansCount;
    private int kooCount;
    protected int type;

    public BaseUserInfo(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        try {
            if (jsonObject.has(KEY_UID)) {
                uid = jsonObject.getString(KEY_UID);
            }
            if (jsonObject.has(KEY_NICKNAME)) {
                nickname = jsonObject.getString(KEY_NICKNAME);
            }
            if (jsonObject.has(KEY_AVATAR)) {
                avatar = jsonObject.getString(KEY_AVATAR);
            }
            if (jsonObject.has(KEY_VIP)) {
                vip = jsonObject.getInt(KEY_VIP);
            }
            if (jsonObject.has(KEY_FOLLOWS_COUNT)) {
                followsCount = jsonObject.getInt(KEY_FOLLOWS_COUNT);
            }
            if (jsonObject.has(KEY_FANS_COUNT)) {
                fansCount = jsonObject.getInt(KEY_FANS_COUNT);
            }
            kooCount = JsonUtil.getIntIfHas(jsonObject, KEY_KOO_COUNT);
            type = JsonUtil.getIntIfHas(jsonObject, KEY_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isAllFeildNotNull() {
        return  uid != null &&
                nickname != null &&
                avatar != null;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUid() {
        return uid;
    }

    public int getVip() {
        return vip;
    }

    public int getFansCount() {
        return fansCount;
    }

    public int getFollowsCount() {
        return followsCount;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getKooCount() {
        return kooCount;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static List<BaseUserInfo> fromJSONArray(JSONArray jsonArray) {
        List<BaseUserInfo> list = new ArrayList<>();

        int count = jsonArray.length();
        for (int i = 0; i < count; i++) {
            try {
                list.add(new BaseUserInfo(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public boolean isFollowed() {
        return type == TYPE_FOLLOWED || type == TYPE_FRIEND;
    }

    public boolean isFan() {
        return type == TYPE_FAN || type == TYPE_FRIEND;
    }

    public void doFollow() {
        type = FRIEND_OP_ARRAY[DO_FOLLOW][type];
    }

    public void doUnfollow() {
        type = FRIEND_OP_ARRAY[DO_UNFOLLOW][type];
    }

    public static final int NO_OPERATION = 0;
    public static final int DO_FOLLOW    = 1;
    public static final int DO_UNFOLLOW  = 2;

    public static final int[][] FRIEND_OP_ARRAY = new int[][] {
            new int[] {0, 1, 2, 3, 4, 5},  // No operation
            new int[] {0, 2, 2, 4, 4, 5},  // Do follow
            new int[] {0, 1, 1, 3, 3, 5},  // Do unfollow
    };
}
