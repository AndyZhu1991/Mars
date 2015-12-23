package com.koolew.mars;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.MaxLengthWatcher;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateTopicActivity extends BaseActivity implements Response.Listener<JSONObject>,
        Response.ErrorListener, TitleBarView.OnRightLayoutClickListener {

    public static final String KEY_TOPIC_TITLE = "topic title";

    private EditText mTitleEdit;
    private EditText mDescEdit;

    private Dialog mConnectingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_topic);

        initViews();

        Intent intent = getIntent();
        mTitleEdit.setText(intent.getStringExtra(KEY_TOPIC_TITLE));
        mTitleEdit.setSelection(mTitleEdit.getText().length());

        mConnectingDialog = DialogUtil.getConnectingServerDialog(this);
    }

    private void initViews() {
        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);

        mTitleEdit = (EditText) findViewById(R.id.title_edit);
        mTitleEdit.addTextChangedListener(
                new TextWatcher(AppProperty.TOPIC_TITLE_MAX_WORDS, mTitleEdit));
        mDescEdit = (EditText) findViewById(R.id.desc_edit);
    }

    @Override
    public void onRightLayoutClick() {
        if (mTitleEdit.getText().length() == 0) {
            Toast.makeText(this, R.string.no_title_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        mConnectingDialog.show();
        ApiWorker.getInstance().addTopic(mTitleEdit.getText().toString(),
                mDescEdit.getText().toString(), this, this);
    }

    @Override
    public void onResponse(JSONObject response) {
        mConnectingDialog.dismiss();
        try {
            if (response.getInt("code") == 0) {
                String tid = response.getJSONObject("result").getString("uid");
                TopicMediaActivity.startThisActivity(CreateTopicActivity.this, tid,
                        TopicMediaActivity.TYPE_FEEDS);

                setResult(RESULT_OK);
                finish();
            }
            else {
                Toast.makeText(CreateTopicActivity.this,
                        R.string.connect_server_failed, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mConnectingDialog.dismiss();
        Toast.makeText(this, R.string.create_topic_failed, Toast.LENGTH_SHORT).show();
    }

    class TextWatcher extends MaxLengthWatcher {
        public TextWatcher(int maxLen, EditText editText) {
            super(maxLen, editText);
        }

        @Override
        public void onTextOverInput() {
            Toast.makeText(CreateTopicActivity.this,
                    getString(R.string.topic_title_over_length_hint,
                            AppProperty.TOPIC_TITLE_MAX_WORDS), Toast.LENGTH_SHORT).show();
        }
    };
}
