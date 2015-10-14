package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

public class BindAlipayAccountActivity extends BaseActivity
        implements TitleBarView.OnRightLayoutClickListener, Response.Listener<JSONObject>,
        Response.ErrorListener {

    private EditText mAlipayIdEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_alipay_account);

        initViews();

        Utils.showSoftKeyInput(mAlipayIdEdit, 200);
    }

    private void initViews() {
        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);

        mAlipayIdEdit = (EditText) findViewById(R.id.alipay_edit);
    }

    public void onAlipayClear(View v) {
        mAlipayIdEdit.setText("");
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onRightLayoutClick() {
        if (TextUtils.isEmpty(mAlipayIdEdit.getText())) {
            Toast.makeText(this, R.string.please_input_correct_alipay, Toast.LENGTH_SHORT).show();
            return;
        }

        ApiWorker.getInstance().bindAlipay(mAlipayIdEdit.getText().toString(), this, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        onBindError();
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            if (response.getInt("code") == 0) {
                Intent intent = new Intent();
                intent.putExtra(CashOutActivity.KEY_ALIPAY_ACCOUNT,
                        mAlipayIdEdit.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
            else {
                onBindError();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onBindError() {
        Toast.makeText(this, R.string.bind_error, Toast.LENGTH_SHORT).show();
    }
}
