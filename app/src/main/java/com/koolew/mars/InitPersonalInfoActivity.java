package com.koolew.mars;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.services.UploadAvatarService;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.MaxLengthWatcher;
import com.koolew.mars.utils.PictureSelectUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class InitPersonalInfoActivity extends BaseActivity {

    private static final String TAG = "koolew-InitPersonalInfo";

    private static final String DEFAULT_AVATAR_URI =
            "http://avatar.koolew.com/default_avatar.jpg";

    private RequestQueue mRequestQueue;
    private ImageView mAvatar;
    private EditText mEtNickname;
    private ProgressDialog mLoginingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_personal_info);

        Utils.setStatusBarColorFromResource(this, R.mipmap.blur_background);

        mRequestQueue = Volley.newRequestQueue(this);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        ImageLoader.getInstance().displayImage(MyAccountInfo.getAvatar(), mAvatar);
        mEtNickname = (EditText) findViewById(R.id.et_nickname);
        mEtNickname.addTextChangedListener(
                new MaxLengthWatcher(AppProperty.getNicknameMaxLen(), mEtNickname) {
                    @Override
                    public void onTextOverInput() {
                        Toast.makeText(InitPersonalInfoActivity.this,
                                InitPersonalInfoActivity.this.getString
                                        (R.string.nickname_over_input_message,
                                                AppProperty.getNicknameMaxLen()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
        mLoginingDialog = new ProgressDialog(this);
        mLoginingDialog.setMessage(getString(R.string.communcating_with_server));
        mLoginingDialog.setCanceledOnTouchOutside(false);
        mLoginingDialog.setIndeterminate(true);
    }

    public void onAvatarClick(View v) {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/jpeg");
        if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.KITKAT){
            startActivityForResult(intent, PictureSelectUtil.SELECT_PIC_KITKAT);
        }else{
            startActivityForResult(intent, PictureSelectUtil.SELECT_PIC);
        }
    }

    private boolean isDefaultAvatar() {
        return TextUtils.isEmpty(MyAccountInfo.getAvatar()) ||
                MyAccountInfo.getAvatar().equals(DEFAULT_AVATAR_URI);
    }

    public void onNextClick(View v) {
        if (mEtNickname.getText().length() == 0) {
            Toast.makeText(this, R.string.no_nickname_message, Toast.LENGTH_SHORT).show();
            return;
        }

        mLoginingDialog.show();
        String url = UrlHelper.USER_INFO_URL;
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("nickname", mEtNickname.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, url, requestJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response -> " + response.toString());
                        try {
                            if (response.getInt("code") == 0) {
                                // Register/login success
                                mLoginingDialog.dismiss();
                                if (!isDefaultAvatar()) {
                                    UploadAvatarService.startActionUpload(InitPersonalInfoActivity.this);
                                }
                                startActivity(new Intent(InitPersonalInfoActivity.this,
                                        ImportPhoneFriendsActivity.class));
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
                        mLoginingDialog.dismiss();
                        Toast.makeText(InitPersonalInfoActivity.this,
                                R.string.connect_server_failed, Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return UrlHelper.getStandardPostHeaders();
            }
        };
        mRequestQueue.add(jsonRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String filePath = PictureSelectUtil.getPath(this, data.getData());
            String fileUri = "file://" + filePath;
            MyAccountInfo.setAvatar(fileUri);
            ImageLoader.getInstance().displayImage(fileUri, mAvatar);
        }
    }
}
