package com.koolew.mars;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.infos.MyAccountInfo.LOGIN_TYPE;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;


public class FirstLoginActivity extends Activity implements PlatformActionListener {

    private static final String TAG = "koolew-FirstLoginA";

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_login);

        mProgressDialog = new ProgressDialog(this);
    }

    public void onLoginClick(View v) {

        switch (v.getId()) {
            case R.id.login_by_phone:
                Intent intent = new Intent(this, PhoneLoginActivity.class);
                intent.putExtra(PhoneLoginActivity.KEY_LOGIN_TYPE, LOGIN_TYPE.MOBILE.ordinal());
                startActivity(intent);
                break;
            case R.id.login_by_weibo:
                Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
                weibo.SSOSetting(false);  //设置false表示使用SSO授权方式
                weibo.setPlatformActionListener(this); // 设置分享事件回调
                weibo.authorize();
                break;
            case R.id.login_by_qq:
                Platform qq = ShareSDK.getPlatform(QQ.NAME);
                qq.SSOSetting(false);  //设置false表示使用SSO授权方式
                qq.setPlatformActionListener(this); // 设置分享事件回调
                qq.authorize();
                break;
            case R.id.login_by_wechat:
                Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
                wechat.SSOSetting(false);  //设置false表示使用SSO授权方式
                wechat.setPlatformActionListener(this); // 设置分享事件回调
                wechat.authorize();
                break;
        }
    }

    private void loginBySns(final LOGIN_TYPE type, final String openId, String refreshToken,
                            long expiresIn, final String unionId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.setMessage(getString(R.string.geting_sns_info));
                mProgressDialog.show();
            }
        });
        ApiWorker.getInstance().loginBySns(type, openId, refreshToken, expiresIn, unionId,
                new SnsLoginListener(type), null);
    }

    class SnsLoginListener implements Response.Listener<JSONObject> {

        private LOGIN_TYPE type;

        public SnsLoginListener(LOGIN_TYPE type) {
            this.type = type;
        }

        @Override
        public void onResponse(JSONObject response) {
            mProgressDialog.dismiss();
            try {
                if (response.getInt("code") == 0) { // Login success
                    // Set personal info
                    JSONObject result = response.getJSONObject("result");
                    MyAccountInfo.setToken(result.getString("token"));
                    MyAccountInfo.setUid(result.getString("uid"));
                    JSONObject info = result.getJSONObject("info");
                    MyAccountInfo.setAvatar(info.getString("avatar"));
                    MyAccountInfo.setNickname(info.getString("nickname"));
                    MyAccountInfo.setPhoneNumber(info.getString("phone"));
                    MyAccountInfo.setKooNum(info.getInt("koo_num"));
                    MyAccountInfo.setCoinNum(info.getInt("coin_num"));
                    ApiWorker.getInstance().postRegistrationId(MyAccountInfo.getRegistrationId(),
                            ApiWorker.getInstance().emptyResponseListener, null);

                    // Go to the MainActivity
                    Intent intent = new Intent(FirstLoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else if (response.getInt("code") == 101) { // Not register yet
                    Toast.makeText(FirstLoginActivity.this, R.string.please_bind_mobile,
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(FirstLoginActivity.this, PhoneLoginActivity.class);
                    intent.putExtra(PhoneLoginActivity.KEY_LOGIN_TYPE, type.ordinal());
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // From cn.sharesdk.framework.PlatformActionListener
    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        PlatformDb db = platform.getDb();
        MyAccountInfo.setAvatar(db.getUserIcon());

        if (platform.getName().equals(Wechat.NAME)) {
            loginBySns(LOGIN_TYPE.WECHAT, db.get("openid"), db.get("refresh_token"),
                    db.getExpiresIn(), db.get("unionid"));
        }
        else if (platform.getName().equals(SinaWeibo.NAME)) {
            loginBySns(LOGIN_TYPE.WEIBO, db.getUserId(), db.getToken(),
                    db.getExpiresTime(), db.getUserId());
        }
        else if (platform.getName().equals(QQ.NAME)) {
            loginBySns(LOGIN_TYPE.QQ, db.getUserId(), db.getToken(),
                    db.getExpiresIn(), db.getUserId());
        }
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
    }

    @Override
    public void onCancel(Platform platform, int i) {
    }
}
