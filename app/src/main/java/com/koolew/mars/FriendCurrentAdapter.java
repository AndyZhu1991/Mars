package com.koolew.mars;

import android.app.ProgressDialog;
import android.content.Context;

import com.koolew.mars.utils.DialogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/1/15.
 */
public class FriendCurrentAdapter extends FriendSimpleAdapter {

    private ProgressDialog mProgressDialog;

    public FriendCurrentAdapter(Context context) {
        super(context);
        mProgressDialog = DialogUtil.getConnectingServerDialog(context);
    }

    @Override
    public void add(JSONObject jsonObject) {
        FriendInfo info = new FriendInfo();
        try {
            info.type = TYPE_FRIEND;
            info.uid = jsonObject.getString("uid");
            info.nickname = jsonObject.getString("nickname");
            info.avatar = jsonObject.getString("avatar");
            retrievalContactName(info);
            generateSummary(info);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mData.add(info);
    }

    @Override
    public void add(JSONArray jsonArray) {
        int count = jsonArray.length();
        for (int i = 0; i < count; i++) {
            try {
                add((JSONObject) jsonArray.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
