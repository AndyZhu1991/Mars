package com.koolew.mars.infos;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by jinchangzhu on 9/2/15.
 */
public class KooCountUserInfo extends BaseUserInfo implements Serializable {

    private int kooCount;

    public KooCountUserInfo(JSONObject jsonObject) {
        super(jsonObject);
        if (jsonObject.has("koo_num")) {
            try {
                kooCount = jsonObject.getInt("koo_num");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public int getKooCount() {
        return kooCount;
    }
}
