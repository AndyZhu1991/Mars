package com.koolew.mars;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

public class TodayIncomeActivity extends BaseActivity
        implements TitleBarView.OnRightLayoutClickListener, Response.Listener<JSONObject>,
        Response.ErrorListener {

    private static final int REQUEST_CODE_CASH_OUT = 1;
    public static final String KEY_CASH_OUT_AMOUNT = "cash out amount";

    private double mRemainIncomeNum;
    private String mAlipayAccount;
    private int mCashOutLimit;

    private TextView mTodayIncome;
    private TextView mTotalIncome;
    private TextView mThisMonthIncome;
    private TextView mRemainIncome;

    private Dialog mConnectingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_income);

        initViews();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                requestIncomeData();
            }
        });
    }

    private void initViews() {
        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);

        mTodayIncome = (TextView) findViewById(R.id.today_income);
        mTotalIncome = (TextView) findViewById(R.id.total_income);
        mThisMonthIncome = (TextView) findViewById(R.id.this_month_income);
        mRemainIncome = (TextView) findViewById(R.id.remain_income);

        mConnectingDialog = DialogUtil.getConnectingServerDialog(this);
    }

    private void requestIncomeData() {
        mConnectingDialog.show();
        ApiWorker.getInstance().requestIncomeDesc(this, this);
    }

    public void onIncomeAnalysis(View v) {
        startActivity(new Intent(this, IncomeAnalysisActivity.class));
    }

    public void onCashOut(View v) {
        Intent intent = new Intent(this, CashOutActivity.class);
        intent.putExtra(CashOutActivity.KEY_INCOME_CAN_CASH_OUT, mRemainIncomeNum);
        intent.putExtra(CashOutActivity.KEY_ALIPAY_ACCOUNT, mAlipayAccount);
        intent.putExtra(CashOutActivity.KEY_CASH_OUT_LIMIT, mCashOutLimit);
        startActivityForResult(intent, REQUEST_CODE_CASH_OUT);
    }

    @Override
    public void onRightLayoutClick() {
        KoolewWebActivity.startThisActivity(this, AppProperty.INCOME_EXPLAIN_URL,
                getString(R.string.income_explain), getResources().getColor(R.color.koolew_red));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CASH_OUT) {
            if (resultCode == RESULT_OK) {
                mRemainIncomeNum -= data.getIntExtra(KEY_CASH_OUT_AMOUNT, 0
                );
                mRemainIncome.setText(toIncomeString(mRemainIncomeNum));
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mConnectingDialog.dismiss();
        onError();
    }

    @Override
    public void onResponse(JSONObject response) {
        mConnectingDialog.dismiss();
        try {
            if (response.getInt("code") == 0) {
                JSONObject result = response.getJSONObject("result");
                mTodayIncome.setText(toIncomeString(result.getDouble("today_profit")));
                mTotalIncome.setText(toIncomeString(result.getDouble("total_profit")));
                mThisMonthIncome.setText(toIncomeString(result.getDouble("month_profit")));
                mRemainIncomeNum = result.getDouble("rest_profit");
                mRemainIncome.setText(toIncomeString(mRemainIncomeNum));
                mAlipayAccount = result.getString("alipay");
                mCashOutLimit = result.getInt("limit");
            }
            else {
                onError();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onError() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.request_data_failed)
                .setMessage(R.string.retry_or_not)
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        requestIncomeData();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onBackPressed();
                    }
                })
                .show();
    }

    public static String toIncomeString(double income) {
        return String.format("%.3f", income);
    }
}
