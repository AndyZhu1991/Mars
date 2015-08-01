package com.koolew.mars;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.infos.MyAccountInfo.LOGIN_TYPE;
import com.koolew.mars.webapi.UrlHelper;
import com.koolew.mars.weiboapi.AccessTokenKeeper;
import com.koolew.mars.wxapi.TokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class InputPasswordActivity extends Activity implements View.OnClickListener,
        RequestPasswordFragment.OnFragmentInteractionListener{

    private static final String TAG = "koolew-InputPasswordA";

    private EditText mPasswordCapture;
    private TextView[] mPasswordDigits = new TextView[4];
    private TextView mHintPhoneNumber;

    private RequestPasswordFragment mFragment;

    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_password);

        mRequestQueue = Volley.newRequestQueue(this);
        mPasswordCapture = (EditText) findViewById(R.id.password_capture);
        mPasswordCapture.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "Text changed: " + s);
                updatePasswordDigits(s.toString());
            }
        });
        int[] passwordDigitIds = { R.id.password_digit1, R.id.password_digit2,
                                   R.id.password_digit3, R.id.password_digit4};
        for (int i = 0; i < 4; i++) {
            mPasswordDigits[i] = (TextView)
                    findViewById(passwordDigitIds[i]).findViewById(R.id.digit_text);
            mPasswordDigits[i].setOnClickListener(this);
        }

        mHintPhoneNumber = (TextView) findViewById(R.id.hint_phone_number);
        mHintPhoneNumber.setText(MyAccountInfo.getPhoneNumber());

        mFragment = (RequestPasswordFragment) getFragmentManager().
                findFragmentById(R.id.request_password_fragment);
        mFragment.sendMessage();
    }

    @Override
    protected void onResume() {
        showSoftKeyInput(300);
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        showSoftKeyInput(0);
    }

    private void showSoftKeyInput(int delay) {
        mPasswordCapture.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mPasswordCapture, 0);
            }
        }, delay);
    }

    private void updatePasswordDigits(String s) {
        int length = s.length();
        if (length > 4) {
            Log.e(TAG, "updatePasswordDigits got: " + s);
            return;
        }
        for (int i = 0; i < 4; i++) {
            mPasswordDigits[i].setText(i < length ? "" + s.charAt(i) : "");
        }
        if (length == 4) {
            onPasswordInputComplete(s);
        }
    }

    private void onPasswordInputComplete(final String password) {

        String url = null;
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("phone", MyAccountInfo.getPhoneNumber());
            requestJson.put("code", password);
            if (MyAccountInfo.getLoginType() == LOGIN_TYPE.MOBILE) {
                Log.d(TAG, "Login by mobile");
                url = UrlHelper.LOGIN_URL;
            }
            else {
                Log.d(TAG, "Signup by: " + MyAccountInfo.getLoginType().ordinal());
                url = UrlHelper.SNS_SIGNUP_URL;
                addSnsRegisterParams(MyAccountInfo.getLoginType(), requestJson);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Method.POST, url, requestJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response -> " + response.toString());
                        try {
                            if (response.getInt("code") == 0) {
                                JSONObject result = response.getJSONObject("result");
                                MyAccountInfo.setToken(result.getString("token"));
                                MyAccountInfo.setUid(result.getString("uid"));
                                if (result.has("info")) {
                                    JSONObject info = result.getJSONObject("info");
                                    if (MyAccountInfo.getLoginType() == LOGIN_TYPE.MOBILE) {
                                        MyAccountInfo.setAvatar(info.getString("avatar"));
                                    }
                                    MyAccountInfo.setNickname(info.getString("nickname"));
                                    MyAccountInfo.setKooNum(info.getInt("koo_num"));
                                    MyAccountInfo.setCoinNum(info.getInt("coin_num"));
                                }
                                else {
                                    // Why no "info" ?
                                    if (MyAccountInfo.getLoginType() == LOGIN_TYPE.MOBILE) {
                                        MyAccountInfo.setAvatar(result.getString("avatar"));
                                    }
                                    MyAccountInfo.setNickname("");
                                    MyAccountInfo.setKooNum(0);
                                    MyAccountInfo.setCoinNum(0);
                                }

                                if (result.getInt("register") == 0) {
                                    Intent intent = new Intent(InputPasswordActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                                else {
                                    startActivity(new Intent(InputPasswordActivity.this,
                                            InitPersonalInfoActivity.class));
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
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        mRequestQueue.add(jsonRequest);
    }

    private void addSnsRegisterParams(LOGIN_TYPE type, JSONObject requestJson) {
        try {
            requestJson.put("type", MyAccountInfo.getLoginType().ordinal());
            if (type == LOGIN_TYPE.WEIBO) {
                Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(InputPasswordActivity.this);
                requestJson.put("open_id", token.getUid());
                requestJson.put("refresh_token", token);
                requestJson.put("expires_in", token.getExpiresTime());
                requestJson.put("union_id", token.getUid());
            } else if (type == LOGIN_TYPE.WECHAT) {
                requestJson.put("open_id", TokenKeeper.readOpenId(InputPasswordActivity.this));
                requestJson.put("refresh_token", TokenKeeper.readRefreshToken(InputPasswordActivity.this));
                requestJson.put("expires_in", TokenKeeper.readExpiresIn(InputPasswordActivity.this));
                requestJson.put("union_id", TokenKeeper.readUnionId(InputPasswordActivity.this));
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    @Override
    public String getPhoneNumber() {
        return MyAccountInfo.getPhoneNumber();
    }

    @Override
    public void onPhoneNumberInvalid() {
    }
}
