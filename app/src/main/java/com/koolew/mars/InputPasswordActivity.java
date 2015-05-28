package com.koolew.mars;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.infos.MyAccountInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class InputPasswordActivity extends Activity implements View.OnClickListener{

    private static final String TAG = "koolew-InputPasswordA";

    private EditText mPasswordCapture;
    private TextView[] mPasswordDigits = new TextView[4];
    private TextView mHintPhoneNumber;
    private Button mResendPassword;
    private ProgressDialog mRequestingDialog;

    private RequestQueue mRequestQueue;
    private int resendPasswordCountDown;
    private Timer mCountDownTimer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_password);

        mRequestQueue = Volley.newRequestQueue(this);
        mPasswordCapture = (EditText) findViewById(R.id.password_capture);
        mPasswordCapture.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "Text changed: " + s);
                updatePasswordDigits(s.toString());
            }
        });
        mRequestingDialog = new ProgressDialog(this);
        mRequestingDialog.setMessage(getString(R.string.requesting_password));
        mRequestingDialog.setCanceledOnTouchOutside(false);
        mRequestingDialog.setIndeterminate(true);
        int[] passwordDigitIds = { R.id.password_digit1, R.id.password_digit2,
                                   R.id.password_digit3, R.id.password_digit4};
        for (int i = 0; i < 4; i++) {
            mPasswordDigits[i] = (TextView) findViewById(passwordDigitIds[i]).findViewById(R.id.digit_text);
            mPasswordDigits[i].setOnClickListener(this);
        }

        mHintPhoneNumber = (TextView) findViewById(R.id.hint_phone_number);
        mHintPhoneNumber.setText(MyAccountInfo.getPhoneNumber());
        mResendPassword = (Button) findViewById(R.id.btn_resend_password);

        requestPassword();
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

    private void requestPassword() {

        // Just 4 debug
        //if (true) return;

        String url = "http://test.koolew.com/v1/user/code?phone=" + MyAccountInfo.getPhoneNumber();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            if (response.getInt("code") == 0) {
                                requestPasswordSuccess();
                            }
                            else {
                                // Error?
                                mRequestingDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", error.getMessage(), error);
                        if (mRequestingDialog.isShowing()) {
                            mRequestingDialog.dismiss();
                        }
                        Toast.makeText(InputPasswordActivity.this,
                                R.string.connect_server_failed, Toast.LENGTH_SHORT).show();
                    }
        });
        mRequestQueue.add(jsonObjectRequest);
        mRequestingDialog.show();

//        KoolewApiClient.get("user/code", new RequestParams("phone", AccountInfo.getPhoneNumber()), new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                // If the response is JSONObject instead of expected JSONArray
//                try {
//                    Log.d(TAG, "code: " + response.getInt("code") + ", msg: " + response.getString("msg"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

    }

    private void requestPasswordSuccess() {
        resendPasswordCountDown = 60;
        mResendPassword.setEnabled(false);
        mResendPassword.setText(getResources().getString(R.string.resend_password) +
                getResources().getString(R.string.count_down_string, resendPasswordCountDown));
        resendPasswordCountDown--;
        mCountDownTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (resendPasswordCountDown != 0) {
                            mResendPassword.setText(getResources().getString(R.string.resend_password) +
                                    getResources().getString(R.string.count_down_string, resendPasswordCountDown));
                        } else {
                            mResendPassword.setText(getResources().getString(R.string.resend_password));
                            mResendPassword.setEnabled(true);
                            mCountDownTimer.cancel();
                        }
                        resendPasswordCountDown--;
                    }
                });
            }
        }, 1000, 1000);
        mRequestingDialog.dismiss();
        showSoftKeyInput(100);
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
//        RequestParams params = new RequestParams();
//        params.put("phone", AccountInfo.getPhoneNumber());
//        params.put("code", password);
//        KoolewApiClient.post("user/login", params, new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                // If the response is JSONObject instead of expected JSONArray
//                try {
//                    Log.d(TAG, "code: " + response.getInt("code") + ", msg: " + response.getString("msg"));
//                    Iterator<String> ss = response.keys();
//                    while(ss.hasNext()){
//                        Log.d(TAG, "key: " + ss.next());
//                    }
//                    JSONObject result = response.getJSONObject("result");
//                    Log.d(TAG, "token: " + result.getString("token") +
//                            ", uid: "  + result.getString("uid") + "register: " + result.getInt("register"));
//                    JSONObject info = result.getJSONObject("info");
//                    Log.d(TAG, "uid: " + info.getString("uid") + ", avatar: " + info.getString("avatar") +
//                            ", nickname: " + info.getString("nickname") + ", koo_num " + info.getInt("koo_num") +
//                            ", coin_num: " + info.getInt("coin_num") + ", phone: " + info.getString("phone"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        String url = "http://test.koolew.com/v1/user/login";
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("phone", MyAccountInfo.getPhoneNumber());
            requestJson.put("code", password);
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
                                JSONObject info = result.getJSONObject("info");
                                MyAccountInfo.setAvatar(info.getString("avatar"));
                                MyAccountInfo.setNickname(info.getString("nickname"));
                                MyAccountInfo.setKooNum(info.getInt("koo_num"));
                                MyAccountInfo.setCoinNum(info.getInt("coin_num"));

                                if (true){//response.getInt("code") == 0) {
                                    startActivity(new Intent(InputPasswordActivity.this,
                                            InitPersonalInfoActivity.class));
                                }
                                else {

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

    public void onResendPassword(View v) {
        Log.d(TAG, "onResendPassword");
        requestPassword();
    }
}
