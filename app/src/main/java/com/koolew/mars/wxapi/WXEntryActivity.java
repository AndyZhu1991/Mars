package com.koolew.mars.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.FirstLoginActivity;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;


public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	private static final String TAG = "koolew-WXEntryA";

	public static final String FROM_WXENTRY = "wechat";
	public static final String CODE_KEY = "code";

	private RequestQueue mRequestQueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		mRequestQueue = Volley.newRequestQueue(this);

		Log.d(TAG, "onCreate");
        Api.getApi().handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		setIntent(intent);
        Api.getApi().handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		Log.d(TAG, "onReq");
		finish();
	}

	@Override
	public void onResp(BaseResp resp) {
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			String code = ((SendAuth.Resp) resp).code;
			FirstLoginActivity.shouldWechatLogin = true;
			FirstLoginActivity.wechatCode = code;
			Log.d(TAG, "onResp OK: " + code);
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			Log.d(TAG, "onResp CANCEL");
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			Log.d(TAG, "onResp DENIED");
			break;
		default:
			Log.d(TAG, "onResp default");
			break;
		}
		finish();
	}
}