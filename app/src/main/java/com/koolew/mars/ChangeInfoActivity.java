package com.koolew.mars;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.blur.DisplayBlurImage;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.services.UploadAvatarService;
import com.koolew.mars.utils.PictureSelectUtil;
import com.koolew.mars.view.PhoneNumberView;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChangeInfoActivity extends Activity {

    public static final int REQUEST_CODE_CHANGE_NICKNAME = 1;
    public static final int REQUEST_CODE_CHANGE_NUMBER = 2;
    public static final int REQUEST_CODE_SELECT_PICTURE = 3;

    private CircleImageView mAvatar;
    private ImageView mBlurAvatar;

    private TextView mNickname;
    private PhoneNumberView mPhoneNumber;

    private View mWechatItem;
    private View mQQItem;
    private View mWeiboItem;

    private int mResultCode = RESULT_CANCELED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_info);

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UploadAvatarService.startActionUpload(this);
    }

    private void initViews() {
        mAvatar = (CircleImageView) findViewById(R.id.avatar);
        mBlurAvatar = (ImageView) findViewById(R.id.blur_avatar);

        mNickname = (TextView) findViewById(R.id.nickname);
        mPhoneNumber = (PhoneNumberView) findViewById(R.id.phone_number);

        mWechatItem = findViewById(R.id.wechat_item);
        mQQItem = findViewById(R.id.qq_item);
        mWeiboItem = findViewById(R.id.weibo_item);

        ImageLoader.getInstance().displayImage(MyAccountInfo.getAvatar(), mAvatar);
        new DisplayBlurImage(mBlurAvatar, MyAccountInfo.getAvatar()).execute();

        mNickname.setText(MyAccountInfo.getNickname());
        mPhoneNumber.setNumber(MyAccountInfo.getPhoneNumber());

        ((ImageView) mWechatItem.findViewById(R.id.icon)).setImageResource(R.mipmap.ic_login_wechat);
        ((ImageView) mQQItem.findViewById(R.id.icon)).setImageResource(R.mipmap.ic_login_qq);
        ((ImageView) mWeiboItem.findViewById(R.id.icon)).setImageResource(R.mipmap.ic_login_weibo);
    }

    public void onAvatarChange(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); //ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/jpeg");
        startActivityForResult(intent, REQUEST_CODE_SELECT_PICTURE);
    }

    public void onNicknameChange(View view) {
        startActivityForResult(new Intent(this, ChangeNicknameActivity.class),
                REQUEST_CODE_CHANGE_NICKNAME);
    }

    public void onPhoneNumberChange(View view) {
        startActivityForResult(new Intent(this, ChangePhoneNumberActivity.class),
                REQUEST_CODE_CHANGE_NUMBER);
    }

    @Override
    public void onBackPressed() {
        setResult(mResultCode);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CHANGE_NICKNAME:
                if (resultCode == RESULT_OK) {
                    mNickname.setText(MyAccountInfo.getNickname());
                }
                break;
            case REQUEST_CODE_CHANGE_NUMBER:
                if (resultCode == RESULT_OK) {
                    mPhoneNumber.setText(MyAccountInfo.getPhoneNumber());
                }
                break;
            case REQUEST_CODE_SELECT_PICTURE:
                if (resultCode == RESULT_OK) {
                    String filePath = PictureSelectUtil.getPath(this, data.getData());
                    String fileUri = "file://" + filePath;
                    MyAccountInfo.setAvatar(fileUri);
                    ImageLoader.getInstance().displayImage(MyAccountInfo.getAvatar(), mAvatar);
                    new DisplayBlurImage(mBlurAvatar, MyAccountInfo.getAvatar()).execute();
                }
                break;
        }

        // 只要有一次返回 RESULT_OK，就向MainActivity返回 RESULT_OK
        if (resultCode == RESULT_OK) {
            mResultCode = resultCode;
        }
    }
}
