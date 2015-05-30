package com.koolew.mars.wxapi;

import android.content.Context;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by jinchangzhu on 5/29/15.
 */
public class Api {

    private static IWXAPI api;

    public static IWXAPI getApi() {
        return api;
    }

    public static void initApi(Context context) {
        api = WXAPIFactory.createWXAPI(context, Constants.APP_ID, true);
        api.registerApp(Constants.APP_ID);
    }
}
