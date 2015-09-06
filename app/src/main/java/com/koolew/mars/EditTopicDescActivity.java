package com.koolew.mars;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiErrorCode;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

public class EditTopicDescActivity extends BaseActivity
        implements TitleBarView.OnRightLayoutClickListener,
        Response.Listener<JSONObject>, Response.ErrorListener {

    public static final String KEY_TOPIC_ID = "topic id";

    private String topicId;

    private EditText descEdit;

    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_topic_desc);

        topicId = getIntent().getStringExtra(KEY_TOPIC_ID);

        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);
        descEdit = (EditText) findViewById(R.id.edit_desc);

        progressDialog = DialogUtil.getConnectingServerDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.showSoftKeyInput(descEdit, 400);
    }

    @Override
    public void onRightLayoutClick() {
        if (TextUtils.isEmpty(descEdit.getText())) {
            Toast.makeText(this,R.string.no_topic_desc_hint , Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        ApiWorker.getInstance().postTopicDesc(topicId, descEdit.getText().toString(), this, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, R.string.connect_server_failed, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

    @Override
    public void onResponse(JSONObject response) {
        progressDialog.dismiss();
        try {
            int code = response.getInt("code");
            if (code == 0) {
                onBackPressed();
            }
            else if (code == ApiErrorCode.NO_RIGHT_EDIT_TOPIC_DESC) {
                Toast.makeText(this, R.string.not_owner_of_topic, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
