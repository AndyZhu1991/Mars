package com.koolew.mars;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.MaxLengthWatcher;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;


public class ChangeNicknameActivity extends BaseActivity implements View.OnClickListener,
        TitleBarView.OnRightLayoutClickListener {

    private EditText mNewNickname;

    private ProgressDialog mConnectingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_nickname);

        initViews();
        initProgressDialog();
    }

    private void initViews() {
        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);

        ((TextView) findViewById(R.id.original_nickname)).setText(
                getString(R.string.original_name_is, MyAccountInfo.getNickname()));

        View editNickname = findViewById(R.id.edit_nickname);
        mNewNickname = (EditText) editNickname.findViewById(R.id.edit_text);
        mNewNickname.setHint(R.string.input_new_nickname_hint);
        mNewNickname.addTextChangedListener(
                new MaxLengthWatcher(AppProperty.getNicknameMaxLen(), mNewNickname));
        editNickname.findViewById(R.id.x).setOnClickListener(this);
    }

    private void initProgressDialog() {
        mConnectingDialog = DialogUtil.getConnectingServerDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.showSoftKeyInput(mNewNickname, 200);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.x:
                clearNewNickname();
                break;
        }
    }

    private void clearNewNickname() {
        mNewNickname.setText("");
    }

    @Override
    public void onRightLayoutClick() {
        // TODO: On 'save' click
        String newNickname = mNewNickname.getText().toString();
        if (newNickname.length() == 0) {
            Toast.makeText(this, R.string.no_nickname_message, Toast.LENGTH_SHORT).show();
            return;
        }

        ApiWorker.getInstance().updateNickname(newNickname, mResponseListener, mErrorListener);
        mConnectingDialog.show();
    }

    private Response.Listener<JSONObject> mResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            try {
                int code = jsonObject.getInt("code");
                if (code == 0) {
                    mConnectingDialog.dismiss();
                    MyAccountInfo.setNickname(mNewNickname.getText().toString());
                    setResult(RESULT_OK);
                    finish();
                }
                else {
                    mConnectingDialog.dismiss();
                    Toast.makeText(ChangeNicknameActivity.this,
                            R.string.failed, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            mConnectingDialog.dismiss();
            Toast.makeText(ChangeNicknameActivity.this,
                    R.string.connect_server_failed, Toast.LENGTH_SHORT).show();
        }
    };
}
