package com.koolew.mars;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.koolew.android.utils.FileUtil;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.qiniu.UploadHelper;
import com.koolew.mars.share.ShareManager;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.Mp4ParserUtil;
import com.koolew.mars.view.TitleBarView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;

public class VideoShareActivity extends AppCompatActivity implements View.OnClickListener,
        TitleBarView.OnRightLayoutClickListener {

    public static final int REQUEST_VIDEO_PRIVACY = 1;

    public static final int RESULT_UPLOADED = RESULT_FIRST_USER + 1;
    public static final int RESULT_BACKGROUND_UPLOAD = RESULT_FIRST_USER + 2;

    public static final String KEY_CONCATED_VIDEO = "concated video";
    public static final String KEY_VIDEO_THUMB = "video thumb";
    public static final String KEY_TOPIC_ID = "topic id";
    public static final String KEY_TOPIC_TITLE = "topic title";
    public static final String KEY_IS_MOVIE = "is movie";
    public static final String KEY_SELECTED_BGM = "bgm";
    public static final String KEY_FROM = "from";
    public static final String KEY_TAG_ID = "tag";
    public static final String KEY_MOVIE_NAME = "movie name";
    public static final String KEY_CHARACTER_NAME = "character name";

    private int mAuthority;
    private RelativeLayout mPrivacyLayout;
    private TextView mAuthorityText;
    private EditText mDescEdit;

    private View mShareItemWechatMoments;
    private View mShareItemWechatFriends;
    private View mShareItemQQ;
    private View mShareItemWeibo;

    private String mConcatedVideo;
    private String mVideoThumb;
    private String mTopicId;
    private String mTopicTitle;
    private boolean isMovie;
    private String mFrom;
    private String mSelectedBgmPath;
    private String mTagId;
    private String mMovieName;
    private String mCharacterName;
    private BaseVideoInfo mUploadedVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_share);

        mConcatedVideo = getIntent().getStringExtra(KEY_CONCATED_VIDEO);
        mVideoThumb = getIntent().getStringExtra(KEY_VIDEO_THUMB);
        mTopicId = getIntent().getStringExtra(KEY_TOPIC_ID);
        mTopicTitle = getIntent().getStringExtra(KEY_TOPIC_TITLE);
        isMovie = getIntent().getBooleanExtra(KEY_IS_MOVIE, false);
        mSelectedBgmPath = getIntent().getStringExtra(KEY_SELECTED_BGM);
        mFrom = getIntent().getStringExtra(KEY_FROM);
        mTagId = getIntent().getStringExtra(KEY_TAG_ID);
        mMovieName = getIntent().getStringExtra(KEY_MOVIE_NAME);
        mCharacterName = getIntent().getStringExtra(KEY_CHARACTER_NAME);

        initMembers();
        initViews();
    }

    private void initViews() {
        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);

        mDescEdit = (EditText) findViewById(R.id.desc_edit);

        mPrivacyLayout = (RelativeLayout) findViewById(R.id.privacy_layout);
        mPrivacyLayout.setOnClickListener(this);
        mAuthorityText = (TextView) findViewById(R.id.authority_text);

        ImageLoader.getInstance().displayImage("file://" + mVideoThumb,
                (ImageView) findViewById(R.id.thumb));

        mShareItemWechatMoments = findViewById(R.id.wechat_moments);
        mShareItemWechatFriends = findViewById(R.id.wechat_friends);
        mShareItemQQ = findViewById(R.id.qq);
        mShareItemWeibo = findViewById(R.id.weibo);
    }

    private void initMembers() {
        mAuthority = VideoPrivacyActivity.AUTHORITY_PUBLIC;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.privacy_layout:
                onPrivacyLayoutClick();
                break;
        }
    }

    private void onPrivacyLayoutClick() {
        Intent intent = new Intent(this, VideoPrivacyActivity.class);
        startActivityForResult(intent, REQUEST_VIDEO_PRIVACY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_VIDEO_PRIVACY:
                setAuthority(resultCode);
                break;
        }
    }

    private void setAuthority(int authority) {
        mAuthority = authority;
        if (authority == VideoPrivacyActivity.AUTHORITY_PUBLIC) {
            mAuthorityText.setText(R.string.public_visible);
        }
        else if (authority == VideoPrivacyActivity.AUTHORITY_FRIEND_ONLY) {
            mAuthorityText.setText(R.string.only_friend_visible);
        }
    }

    public void onShareItemClick(View v) {
        ShareManager.ShareChanel shareChanel = null;
        switch (v.getId()) {
            case R.id.wechat_moments:
                shareChanel = ShareManager.ShareChanel.WECHAT_MOMENTS;
                break;
            case R.id.wechat_friends:
                shareChanel = ShareManager.ShareChanel.WECHAT_FRIENDS;
                break;
            case R.id.qq:
                shareChanel = ShareManager.ShareChanel.QZONE;
                break;
            case R.id.weibo:
                shareChanel = ShareManager.ShareChanel.WEIBO;
                break;
        }

        if (ShareManager.isAuthValid(shareChanel)) {
            onShareItemClickReal(v);
        }
        else {
            authorizeByChanel(shareChanel, v);
        }
    }

    private void authorizeByChanel(ShareManager.ShareChanel shareChanel, View originView) {
        Toast.makeText(this, R.string.request_authorize, Toast.LENGTH_SHORT).show();
        ShareManager.authorize(shareChanel, new AuthorizeListener(originView));
    }

    class AuthorizeListener extends ShareManager.ShareListener {
        private View originView;

        public AuthorizeListener(View originView) {
            super(VideoShareActivity.this);

            this.originView = originView;
        }

        @Override
        protected void initMessages() {
        }

        @Override
        public void onCancel(Platform platform, int i) {
            onAuthorizeFailed();
        }

        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            onAuthorizeSuccess();
        }

        @Override
        public void onError(Platform platform, int i, Throwable throwable) {
            onAuthorizeFailed();
        }

        private void onAuthorizeFailed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoShareActivity.this, R.string.authorize_failed,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void onAuthorizeSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoShareActivity.this, R.string.authorize_success,
                            Toast.LENGTH_SHORT).show();
                    onShareItemClickReal(originView);
                }
            });
        }
    }

    private void onShareItemClickReal(View v) {
        boolean originalState = v.isSelected();
        switch (v.getId()) {
            case R.id.wechat_moments:
            case R.id.wechat_friends:
            case R.id.qq:
                mShareItemWechatMoments.setSelected(false);
                mShareItemWechatFriends.setSelected(false);
                mShareItemQQ.setSelected(false);
                break;
            case R.id.weibo:
                break;
        }
        v.setSelected(!originalState);
    }

    @Override
    public void onRightLayoutClick() {
        final String finalVideo;
        if (mSelectedBgmPath == null) {
            finalVideo = mConcatedVideo;
        } else {
            finalVideo = new File(mConcatedVideo).getParent() + "/final.mp4";
            try {
                Mp4ParserUtil.setVideoBgm(mConcatedVideo, mSelectedBgmPath, finalVideo);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        new AsyncTask<String, Void, Boolean>() {
            private ProgressDialog mUploadingDialog;

            @Override
            protected void onPreExecute() {
                mUploadingDialog = DialogUtil.getGeneralProgressDialog
                        (VideoShareActivity.this, R.string.uploading);
                mUploadingDialog.show();
            }

            @Override
            protected Boolean doInBackground(String... params) {
                if (isMovie) {
                    mUploadedVideo = UploadHelper.uploadMovie(params[0], params[1],
                            params[2], mAuthority, mFrom);
                }
                else {
                    mUploadedVideo = UploadHelper.uploadVideo(params[0], params[1],
                            params[2], params[3], mAuthority);
                }
                if (mUploadedVideo != null) {
                    BaseUserInfo userInfo = new BaseUserInfo(new JSONObject());
                    userInfo.setNickname(MyAccountInfo.getNickname());
                    userInfo.setUid(MyAccountInfo.getUid());
                    mUploadedVideo.setUserInfo(userInfo);
                    try {
                        saveAndRegisterVideo();
                    }
                    catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VideoShareActivity.this, R.string.save_to_local_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    shareVideoIfNeed();
                    return true;
                } else {
                    return false;
                }
            }

            private void saveAndRegisterVideo() {
                Uri videoTable = Uri.parse("content://media/external/video/media");

                long timeMillis = System.currentTimeMillis();
                String newFileName = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss")
                        .format(new Date(timeMillis)) + ".mp4";
                String newFilePath = Environment.getExternalStorageDirectory()
                        + "/koolew/" + newFileName;

                FileUtil.copyFile(finalVideo, newFilePath);

                ContentValues values = new ContentValues(7);
                //values.put(MediaStore.Video.Media.TITLE, title);
                values.put(MediaStore.Video.Media.DISPLAY_NAME, newFileName);
                values.put(MediaStore.Video.Media.DATE_TAKEN, timeMillis);
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Video.Media.DATA, newFilePath);

                values.put(MediaStore.Video.Media.SIZE, new File(newFilePath).length());

                getContentResolver().insert(videoTable, values);
            }

            @Override
            protected void onPostExecute(Boolean isSuccess) {
                mUploadingDialog.dismiss();
                if (isSuccess) {
                    setResult(RESULT_UPLOADED);
                    finish();
                } else {
                    Toast.makeText(VideoShareActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(mTopicId, finalVideo, mVideoThumb, mTagId);
    }

    private void shareVideoIfNeed() {
        ShareManager.ShareChanel shareChanel = null;
        if (mShareItemWechatMoments.isSelected()) {
            shareChanel = ShareManager.ShareChanel.WECHAT_MOMENTS;
        }
        else if (mShareItemWechatFriends.isSelected()) {
            shareChanel = ShareManager.ShareChanel.WECHAT_FRIENDS;
        }
        else if (mShareItemQQ.isSelected()) {
            shareChanel = ShareManager.ShareChanel.QZONE;
        }
        else if (mShareItemWeibo.isSelected()) {
            shareChanel = ShareManager.ShareChanel.WEIBO;
        }

        if (shareChanel != null) {
            ShareManager.ShareListener shareListener;
            if (ShareManager.ShareChanel.WEIBO.equals(shareChanel)) {
                shareListener = new WeiboShareListener();
            } else {
                shareListener = new ShareListener();
            }
            if (isMovie) {
                new ShareManager(this, shareListener).shareMyMovieTo(shareChanel, mUploadedVideo,
                        mMovieName, mCharacterName, mDescEdit.getText().toString());
            }
            else {
                new ShareManager(this, shareListener).shareVideoTo(
                        shareChanel, mUploadedVideo, mTopicTitle, mDescEdit.getText().toString());
            }
        }
    }

    class ShareListener extends ShareManager.ShareListener {

        public ShareListener() {
            super(VideoShareActivity.this);
        }

        @Override
        protected void initMessages() {
            mSuccessMessage = mActivity.getString(R.string.share_success);
            mErrorMessage = mActivity.getString(R.string.share_failed);
            mCancelMessage = mActivity.getString(R.string.share_cancel);
        }

        @Override
        public void onCancel(Platform platform, int i) {
            super.onCancel(platform, i);
            shareToWeibo();
        }

        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            super.onComplete(platform, i, hashMap);
            shareToWeibo();
        }

        @Override
        public void onError(Platform platform, int i, Throwable throwable) {
            super.onError(platform, i, throwable);
            shareToWeibo();
        }

        private void shareToWeibo() {
            if (mShareItemWeibo.isSelected()) {
                if (isMovie) {
                    new ShareManager(VideoShareActivity.this, new WeiboShareListener()).
                            shareMyMovieTo(ShareManager.ShareChanel.WEIBO, mUploadedVideo, mMovieName,
                                    mCharacterName, mDescEdit.getText().toString());
                }
                else {
                    new ShareManager(VideoShareActivity.this, new WeiboShareListener()).
                            shareVideoTo(ShareManager.ShareChanel.WEIBO, mUploadedVideo, mTopicTitle,
                                    mDescEdit.getText().toString());
                }
            }
        }
    }

    class WeiboShareListener extends ShareManager.ShareListener {

        public WeiboShareListener() {
            super(VideoShareActivity.this);
        }

        @Override
        protected void initMessages() {
            mSuccessMessage = mActivity.getString(R.string.share_success);
            mErrorMessage = mActivity.getString(R.string.share_failed);
            mCancelMessage = mActivity.getString(R.string.share_cancel);
        }
    }
}
