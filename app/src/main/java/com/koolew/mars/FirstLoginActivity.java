package com.koolew.mars;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.infos.MyAccountInfo.LOGIN_TYPE;
import com.koolew.mars.weiboapi.AccessTokenKeeper;
import com.koolew.mars.weiboapi.Constants;
import com.koolew.mars.weiboapi.UsersAPI;
import com.koolew.mars.weiboapi.models.ErrorInfo;
import com.koolew.mars.weiboapi.models.User;
import com.koolew.mars.wxapi.Api;
import com.koolew.mars.wxapi.TokenKeeper;
import com.koolew.mars.wxapi.WXEntryActivity;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.tencent.mm.sdk.modelmsg.SendAuth;

import org.json.JSONException;
import org.json.JSONObject;


public class FirstLoginActivity extends Activity {

    private static final String TAG = "koolew-FirstLoginA";

    public static final String INTENT_FROM_KEY = "from";

    private RequestQueue mRequestQueue;
    private ProgressDialog mProgressDialog;


    // For weibo API
    private AuthInfo mAuthInfo;
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mWeiboAccessToken;
    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;

    // For wechat API
    private String mWechatRefreshToken;
    private long mWechatExpiresIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_login);

        mRequestQueue = Volley.newRequestQueue(this);
        mProgressDialog = new ProgressDialog(this);

        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(FirstLoginActivity.this, mAuthInfo);
    }

    public static boolean shouldWechatLogin = false;
    public static String wechatCode;
    @Override
    protected void onPause() {
        super.onPause();
        shouldWechatLogin = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldWechatLogin) {
            requestWechatToken(wechatCode);
        }
    }

    private void requestWechatToken(String code) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + com.koolew.mars.wxapi.Constants.APP_ID + "&secret=" +
                com.koolew.mars.wxapi.Constants.APP_SECRET + "&code=" + code +
                "&grant_type=authorization_code";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());
                        try {
                            String accessToken = response.getString("access_token");
                            mWechatExpiresIn = response.getLong("expires_in");
                            mWechatRefreshToken = response.getString("refresh_token");
                            String openId = response.getString("openid");
                            String scope = response.getString("scope");
                            requestWechatUserInfo(accessToken, openId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", error.getMessage(), error);
                    }
                });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void requestWechatUserInfo(String accessToken, final String openId) {
        String url = "https://api.weixin.qq.com/sns/userinfo?" +
                "access_token=" + accessToken + "&openid=" + openId;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());
                        try {
                            String unionId = response.getString("unionid");
                            String headImgUrl = response.getString("headimgurl");
                            MyAccountInfo.setAvatar(headImgUrl);
                            TokenKeeper.writeRefreshToken(FirstLoginActivity.this,
                                    openId, mWechatRefreshToken, mWechatExpiresIn, unionId);
                            loginBySns(LOGIN_TYPE.WECHAT, openId, mWechatRefreshToken,
                                    mWechatExpiresIn, unionId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", error.getMessage(), error);
                    }
                });
        mRequestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d(TAG, "onNewIntent" + intent.getExtras());

        Bundle bundle = intent.getExtras();
        if (bundle.getString(INTENT_FROM_KEY).equals(WXEntryActivity.FROM_WXENTRY)) {
            String code = bundle.getString(WXEntryActivity.CODE_KEY);
            Log.d(TAG, "Wechat code: " + code);
        }
    }

    public void onLoginClick(View v) {

        switch (v.getId()) {
            case R.id.login_by_phone:
                MyAccountInfo.setLoginType(LOGIN_TYPE.MOBILE);
                startActivity(new Intent(this, PhoneLoginActivity.class));
                break;
            case R.id.login_by_weibo:
                mSsoHandler.authorize(new AuthListener());
                break;
            case R.id.login_by_qq:
                break;
            case R.id.login_by_wechat:
                final SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = "get_user_info";
                Api.getApi().sendReq(req);
                break;
        }
    }

    private void loginBySns(final LOGIN_TYPE type, final String openId, String refreshToken,
                            long expiresIn, final String unionId) {
        mProgressDialog.setMessage(getString(R.string.geting_sns_info));
        mProgressDialog.show();
        String url = "http://test.koolew.com/v1/user/login/sns";
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("type", type.ordinal());
            requestJson.put("open_id", openId);
            requestJson.put("refresh_token", refreshToken);
            requestJson.put("expires_in", expiresIn);
            requestJson.put("union_id", unionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, url, requestJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response -> " + response.toString());
                        try {
                            MyAccountInfo.setLoginType(type);
                            MyAccountInfo.setSnsUid(openId);
                            MyAccountInfo.setSnsUnionId(unionId);
                            if (response.getInt("code") == 0) {
                                // Login success
                                mProgressDialog.dismiss();

                                // Set personal info
                                JSONObject result = response.getJSONObject("result");
                                MyAccountInfo.setToken(result.getString("token"));
                                MyAccountInfo.setUid(result.getString("uid"));
                                JSONObject info = result.getJSONObject("info");
                                MyAccountInfo.setAvatar(info.getString("avatar"));
                                MyAccountInfo.setNickname(info.getString("nickname"));
                                MyAccountInfo.setKooNum(info.getInt("koo_num"));
                                MyAccountInfo.setCoinNum(info.getInt("coin_num"));

                                // Go to the MainActivity
                                startActivity(new Intent(FirstLoginActivity.this, MainActivity.class));
                            }
                            else if (response.getInt("code") == 101) {
                                // Not register yet
                                if (type == LOGIN_TYPE.WEIBO) {
                                    long weiboUid = Long.parseLong(mWeiboAccessToken.getUid());
                                    new UsersAPI(FirstLoginActivity.this, Constants.APP_KEY, mWeiboAccessToken)
                                            .show(weiboUid, new WeiboRequestListener());
                                }
                                else if (type == LOGIN_TYPE.WECHAT) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(FirstLoginActivity.this, R.string.please_bind_mobile,
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(FirstLoginActivity.this, PhoneLoginActivity.class));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage(), error);
                    }
                });
        mRequestQueue.add(jsonRequest);
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
            mWeiboAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mWeiboAccessToken.isSessionValid()) {
                // 显示 Token

                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(FirstLoginActivity.this, mWeiboAccessToken);
                loginBySns(LOGIN_TYPE.WEIBO, mWeiboAccessToken.getUid(), mWeiboAccessToken.toString(),
                        mWeiboAccessToken.getExpiresTime(), mWeiboAccessToken.getUid());
                Toast.makeText(FirstLoginActivity.this,
                        "授权成功", Toast.LENGTH_SHORT).show();
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = null;
                if (!TextUtils.isEmpty(code)) {
                    message = "授权失败" + "\nObtained the code: " + code;
                }
                Toast.makeText(FirstLoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void onCancel() {
            Toast.makeText(FirstLoginActivity.this,
                    "授权取消", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(FirstLoginActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 微博 OpenAPI 回调接口。
     */
    class WeiboRequestListener implements RequestListener {
        @Override
        public void onComplete(String response) {
            mProgressDialog.dismiss();
            if (!TextUtils.isEmpty(response)) {
                Log.i(TAG, response);
                // 调用 User#parse 将JSON串解析成User对象
                User user = User.parse(response);
                if (user != null) {
                    MyAccountInfo.setAvatar(user.avatar_large);
                    Toast.makeText(FirstLoginActivity.this, R.string.please_bind_mobile,
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(FirstLoginActivity.this, PhoneLoginActivity.class));
                } else {
                    Toast.makeText(FirstLoginActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            mProgressDialog.dismiss();
            Log.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(FirstLoginActivity.this, info.toString(), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "reqCode: " + requestCode + ", resCode: " + resultCode);
        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResult
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
}
