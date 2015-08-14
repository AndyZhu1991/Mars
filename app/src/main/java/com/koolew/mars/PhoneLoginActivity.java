package com.koolew.mars;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;


public class PhoneLoginActivity extends Activity {

    private static final String TAG = "koolew-PhoneLoginA";

    public static final String KEY_LOGIN_TYPE = InputPasswordActivity.KEY_LOGIN_TYPE;

    private EditText mPhoneNumberEdit;

    private int mLoginType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mLoginType = getIntent().getExtras().
                getInt(KEY_LOGIN_TYPE, MyAccountInfo.LOGIN_TYPE.MOBILE.ordinal());

        mPhoneNumberEdit = (EditText) findViewById(R.id.et_phone_number);
    }

    @Override
    protected void onResume() {
        mPhoneNumberEdit.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mPhoneNumberEdit, 0);
            }
        }, 300);
        super.onResume();
    }

    public void onClick(View v) {

        if (!Utils.isChinaPhoneNumber(mPhoneNumberEdit.getText().toString())) {
            Toast.makeText(this, R.string.please_input_correct_phone_num, Toast.LENGTH_SHORT).show();
            return;
        }

        MyAccountInfo.setPhoneNumber(mPhoneNumberEdit.getText().toString());
        Intent intent = new Intent(this, InputPasswordActivity.class);
        intent.putExtra(InputPasswordActivity.KEY_LOGIN_TYPE, mLoginType);
        startActivity(intent);
    }
}
