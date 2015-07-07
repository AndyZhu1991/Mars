package com.koolew.mars;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;


public class ChangePhoneNumberActivity extends Activity
        implements RequestPasswordFragment.OnFragmentInteractionListener,
        TitleBarView.OnRightLayoutClickListener {

    private EditText mNewNumber;
    private EditText mPasswordEdit;

    private EditText mFocusedEdit;

    private ProgressDialog mConnectingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_phone_number);

        initViews();
        initDialog();
    }

    private void initViews() {
        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);

        ((TextView) findViewById(R.id.original_number)).setText(
                getString(R.string.original_number_is, MyAccountInfo.getPhoneNumber()));

        View editNumber = findViewById(R.id.edit_number);
        mNewNumber = (EditText) editNumber.findViewById(R.id.edit_text);
        mNewNumber.setHint(R.string.input_new_phone_number_hint);
        mNewNumber.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        editNumber.findViewById(R.id.x).setOnClickListener(new OnNumberClearListener());

        View editPassword = findViewById(R.id.edit_password);
        mPasswordEdit = (EditText) editPassword.findViewById(R.id.edit_text);
        mPasswordEdit.setHint(R.string.input_password_hint);
        mPasswordEdit.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        editPassword.findViewById(R.id.x).setOnClickListener(new OnPasswordClearListener());
    }

    private void initDialog() {
        mConnectingDialog = DialogUtil.getConnectingServerDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mFocusedEdit == null) {
            Utils.showSoftKeyInput(mNewNumber, 200);
        }
        else {
            Utils.showSoftKeyInput(mFocusedEdit, 200);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mNewNumber.hasFocus()) {
            mFocusedEdit = mNewNumber;
        }
        else if (mPasswordEdit.hasFocus()) {
            mFocusedEdit = mPasswordEdit;
        }
        else {
            mFocusedEdit = null;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    class OnNumberClearListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mNewNumber.setText("");
        }
    }

    class OnPasswordClearListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mPasswordEdit.setText("");
        }
    }


    // RequestPasswordFragment.OnFragmentInteractionListener
    @Override
    public String getPhoneNumber() {
        return mNewNumber.getText().toString();
    }

    @Override
    public void onPhoneNumberInvalid() {
    }

    @Override
    public void onRightLayoutClick() {
        // TODO: On 'save' click
        ApiWorker.getInstance().updatePhoneNumber(mNewNumber.getText().toString(),
                mPasswordEdit.getText().toString(), mResponseListener, mErrorListener);
        mConnectingDialog.show();
    }

    private Response.Listener<JSONObject> mResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            try {
                int code = jsonObject.getInt("code");
                if (code == 0) {
                    mConnectingDialog.dismiss();
                    MyAccountInfo.setPhoneNumber(mNewNumber.getText().toString());
                    setResult(RESULT_OK);
                    finish();
                }
                else {
                    mConnectingDialog.dismiss();
                    Toast.makeText(ChangePhoneNumberActivity.this,
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
            Toast.makeText(ChangePhoneNumberActivity.this,
                    R.string.connect_server_failed, Toast.LENGTH_SHORT).show();
        }
    };

}
