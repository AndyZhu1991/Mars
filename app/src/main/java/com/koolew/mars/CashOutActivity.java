package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

public class CashOutActivity extends BaseActivity implements Response.Listener<JSONObject>,
        Response.ErrorListener {

    public static final String KEY_INCOME_CAN_CASH_OUT = "can cash out";
    public static final String KEY_ALIPAY_ACCOUNT = "alipay account";

    public static final int REQUEST_CODE_REBIND_ALIPAY = RESULT_FIRST_USER + 1;

    private static final int MIN_CASH_OUT_AMOUNT = 500;

    private double mIncomeCanCashOut;
    private String mAlipayAccount;

    private TextView mCanCashOutText;
    private TextView mAlipayText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_out);

        initExtras();
        initViews();
    }

    private void initExtras() {
        Intent intent = getIntent();
        mIncomeCanCashOut = intent.getDoubleExtra(KEY_INCOME_CAN_CASH_OUT, 0.0);
        mAlipayAccount = intent.getStringExtra(KEY_ALIPAY_ACCOUNT);
    }

    private void initViews() {
        mAlipayText = (TextView) findViewById(R.id.alipay_text);
        setAlipayText();
        mCanCashOutText = (TextView) findViewById(R.id.income_can_cash_out);
        mCanCashOutText.setText(TodayIncomeActivity.toIncomeString(mIncomeCanCashOut));
    }

    private void setAlipayText() {
        if (!TextUtils.isEmpty(mAlipayAccount)) {
            mAlipayText.setText(mAlipayAccount);
        }
        else {
            mAlipayText.setText(R.string.not_binded);
        }
    }

    public void onRebindAlipayAccount(View v) {
        startActivityForResult(new Intent(this, BindAlipayAccountActivity.class),
                REQUEST_CODE_REBIND_ALIPAY);
    }

    public void onCashOut(View v) {
        if (mIncomeCanCashOut < MIN_CASH_OUT_AMOUNT) {
            Toast.makeText(this, getString(R.string.too_less_money_to_cash_out, MIN_CASH_OUT_AMOUNT)
                    , Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mAlipayAccount)) {
            Toast.makeText(this, R.string.please_bind_alipay_first, Toast.LENGTH_SHORT).show();
            onRebindAlipayAccount(null);
            return;
        }
        ApiWorker.getInstance().cashOut(mAlipayAccount, (int) mIncomeCanCashOut, this, this);
    }

    public void onCashOutRecord(View v) {
        startActivity(new Intent(this, CashOutRecordActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_REBIND_ALIPAY) {
            if (resultCode == RESULT_OK) {
                mAlipayAccount = data.getStringExtra(KEY_ALIPAY_ACCOUNT);
                setAlipayText();
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        cashOutFailed(error.getLocalizedMessage());
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            if (response.getInt("code") == 0) {
                Toast.makeText(this, R.string.cash_out_requested, Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
            else {
                cashOutFailed(response.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void cashOutFailed(String message) {
        if (TextUtils.isEmpty(message)) {
            message = getString(R.string.cash_out_failed);
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
