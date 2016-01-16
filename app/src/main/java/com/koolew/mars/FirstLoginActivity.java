package com.koolew.mars;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.infos.MyAccountInfo.LOGIN_TYPE;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.jpush.android.api.JPushInterface;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;


public class FirstLoginActivity extends BaseActivity implements PlatformActionListener {

    private static final String TAG = "koolew-FirstLoginA";

    private VideoView mVideoView;

    private ProgressDialog mProgressDialog;

    private AuthInfo mAuthInfo;
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;
    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_login);

        setupVideoView();

        initLinearPadding();

        mProgressDialog = new ProgressDialog(this);

        mAuthInfo = new AuthInfo(this, "3515609297", "http://www.koolew.com/auth/weibo", SCOPE);
        mSsoHandler = new SsoHandler(this, mAuthInfo);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            intoImmersiveMode();
        }
    }

    private void intoImmersiveMode() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void initLinearPadding() {
        View mainLinear = findViewById(R.id.bottom_layout);
        int paddingBottom = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ||
                Utils.hasNavigationBar()) {
            paddingBottom = Utils.getNavigationBarHeightPixel(this);
        }
        mainLinear.setPadding(0, 0, 0, paddingBottom);
    }

    private void setupVideoView() {
        mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName()
                + "/" + R.raw.openning));
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mVideoView.start();
            }
        });
        mVideoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.start();
    }

    public void onLoginClick(View v) {

        switch (v.getId()) {
            case R.id.login_by_phone:
                Intent intent = new Intent(this, PhoneLoginActivity.class);
                intent.putExtra(PhoneLoginActivity.KEY_LOGIN_TYPE, LOGIN_TYPE.MOBILE.ordinal());
                startActivity(intent);
                break;
            case R.id.login_by_weibo:
                mSsoHandler.authorize(new AuthListener());
//                Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
//                weibo.SSOSetting(false);  //设置false表示使用SSO授权方式
//                weibo.setPlatformActionListener(this); // 设置分享事件回调
//                weibo.authorize();
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

    private void loginBySns(final LOGIN_TYPE type, final String openId, String accessToken,
                            String refreshToken, long expiresIn, final String unionId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.setMessage(getString(R.string.geting_sns_info));
                mProgressDialog.show();
            }
        });
        ApiWorker.getInstance().loginBySns(type, openId, accessToken, refreshToken, expiresIn,
                unionId, new SnsLoginListener(type), new SnsLoginErrorListener());
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
                    MyAccountInfo.setPhoneNumber(info.getString("phone"));
                    MyAccountInfo.setKooNum(info.getInt("koo_num"));
                    MyAccountInfo.setCoinNum(info.getInt("coin_num"));
                    String registrationId = JPushInterface.getRegistrationID(FirstLoginActivity.this);
                    MyAccountInfo.setRegistrationId(registrationId);
                    ApiWorker.getInstance().postRegistrationId(MyAccountInfo.getRegistrationId(),
                            postRegistrationIdListener, postRegistrationIdErrorListener);

                    if (result.getInt("fresh") == 1) {
                        startActivity(new Intent(FirstLoginActivity.this, InitPersonalInfoActivity.class));
                    }
                    else {
                        // Go to the MainActivity
                        Intent intent = new Intent(FirstLoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
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

    class SnsLoginErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO
        }
    }

    private Response.Listener<JSONObject> postRegistrationIdListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            // TODO
        }
    };

    private Response.ErrorListener postRegistrationIdErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO
        }
    };

    // From cn.sharesdk.framework.PlatformActionListener
    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        PlatformDb db = platform.getDb();
        MyAccountInfo.setAvatar(db.getUserIcon());
        MyAccountInfo.setNickname(db.getUserName());

        if (platform.getName().equals(Wechat.NAME)) {
            loginBySns(LOGIN_TYPE.WECHAT, db.get("openid"), db.getToken(), db.get("refresh_token"),
                    db.getExpiresIn(), db.get("unionid"));
        }
        else if (platform.getName().equals(SinaWeibo.NAME)) {
            loginBySns(LOGIN_TYPE.WEIBO, db.getUserId(), db.getToken(), null,
                    db.getExpiresTime(), db.getUserId());
        }
        else if (platform.getName().equals(QQ.NAME)) {
            loginBySns(LOGIN_TYPE.QQ, db.getUserId(), db.getToken(), null,
                    db.getExpiresIn(), db.getUserId());
        }
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
    }

    @Override
    public void onCancel(Platform platform, int i) {
    }


    public static final String SCOPE = "direct_messages_read,direct_messages_write,"
            + "friendships_groups_write,statuses_to_me_read," + "invitation_write";

    /**
     * 当 SSO 授权 Activity 退出时，该函数被调用。
     *
     * @see {@link Activity#onActivityResult}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }

    }

    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     *    该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
                PlatformDb db = weibo.getDb();
                db.putToken(mAccessToken.getToken());
                db.putUserId(mAccessToken.getUid());
                db.putExpiresIn(mAccessToken.getExpiresTime());
                db.put("refresh_token", mAccessToken.getRefreshToken());

                loginBySns(LOGIN_TYPE.WEIBO, mAccessToken.getUid(), mAccessToken.getToken(),
                        mAccessToken.getRefreshToken(),
                        mAccessToken.getExpiresTime(), mAccessToken.getUid());
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = getString(R.string.authorize_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(FirstLoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(FirstLoginActivity.this,
                    R.string.authorize_failed, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(FirstLoginActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
