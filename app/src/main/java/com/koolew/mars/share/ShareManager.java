package com.koolew.mars.share;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.koolew.mars.R;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MyAccountInfo;

import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.system.email.Email;
import cn.sharesdk.system.text.ShortMessage;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * Created by jinchangzhu on 8/13/15.
 */
public class ShareManager {

    private Context mContext;
    private PlatformActionListener mListener;

    public enum ShareChanel {
        WECHAT_FRIENDS,
        WECHAT_MOMENTS,
        QZONE,
        WEIBO,
        EMAIL,
        SMS,
    }

    private String[] sharePlatformName = new String[] {
            Wechat.NAME,
            WechatMoments.NAME,
            QZone.NAME,
            SinaWeibo.NAME,
            Email.NAME,
            ShortMessage.NAME,
    };

    private final ShareImage DEFAULT_SHARE_IMAGE = new ResourceShareImage(R.mipmap.ic_launcher);

    public ShareManager(Context context, ShareListener platformActionListener) {
        mContext = context;
        mListener = platformActionListener;
    }

    public void shareVideoTo(ShareChanel shareChanel, BaseVideoInfo videoInfo, String content) {
        share(sharePlatformName[shareChanel.ordinal()], "",
                buildVideoDescription(shareChanel, videoInfo, content),
                new UrlShareImage(videoInfo.getVideoThumb()), buildVideoUrl(videoInfo));
    }

    public void shareTopicTo(ShareChanel shareChanel, String topicId, String content) {
        share(sharePlatformName[shareChanel.ordinal()], content, topicDescription(),
                DEFAULT_SHARE_IMAGE, buildTopicUrl(topicId));
    }

    public void shareUrlTo(ShareChanel shareChanel, String url, String title, String imageUrl) {
        share(sharePlatformName[shareChanel.ordinal()], title, null,
                new UrlShareImage(imageUrl), url);
    }

    public void inviteBy(ShareChanel shareChanel, String topicId, String content) {
        String title = "";
        String description = buildInviteDesc(shareChanel, content, topicId);

        share(sharePlatformName[shareChanel.ordinal()], title, description,
                DEFAULT_SHARE_IMAGE, buildInviteUrl(topicId));
    }


    private void share(String platformName,
                       String title, String description, ShareImage shareImage, String url) {
        if (    platformName.equals(Wechat.NAME) ||
                platformName.equals(WechatMoments.NAME) ||
                platformName.equals(QZone.NAME) ||
                platformName.equals(SinaWeibo.NAME)) {
            shareWebPage(platformName, title, description, shareImage, url);
        }
        else if (platformName.equals(Email.NAME) ||
                 platformName.equals(ShortMessage.NAME)) {
            shareText(platformName, title, description, url);
        }
    }

    private void shareWebPage(String platformName,
                              String title, String description, ShareImage shareImage, String url) {
        Platform.ShareParams sp = new Platform.ShareParams();
        if (platformName.equals(WechatMoments.NAME)) {
            sp.setTitle(description);
            sp.setText(title);
        }
        else {
            sp.setTitle(title);
            sp.setText(description);
        }
        sp.setUrl(url);
        sp.setShareType(Platform.SHARE_WEBPAGE);
        shareImage.setImageParam(sp);
        Platform platform = ShareSDK.getPlatform(platformName);
        platform.setPlatformActionListener(mListener); // 设置分享事件回调
        // 执行图文分享
        platform.share(sp);
    }

    private void shareText(String platformName, String title, String description, String url) {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setText(buildShareText(description, url));
        sp.setShareType(Platform.SHARE_TEXT);
        Platform platform = ShareSDK.getPlatform(platformName);
        platform.setPlatformActionListener(mListener);
        platform.share(sp);
    }

    private String videoDescription() {
        return mContext.getString(R.string.share_video_description);
    }

    private String buildVideoDescription(ShareChanel chanel, BaseVideoInfo videoInfo, String content) {
        if (videoInfo.getUserInfo().getUid().equals(MyAccountInfo.getUid())) {
            if (chanel == ShareChanel.WECHAT_FRIENDS || chanel == ShareChanel.WECHAT_MOMENTS
                    || chanel == ShareChanel.QZONE) {
                return mContext.getString(R.string.share_my_video_to_wechat_desc,
                        videoInfo.getUserInfo().getNickname(), content);
            }
            else if (chanel == ShareChanel.WEIBO) {
                return mContext.getString(R.string.share_my_video_to_weibo_desc,
                        content, buildVideoUrl(videoInfo));
            }
            else {
                return content;
            }
        }
        else {
            if (chanel == ShareChanel.WECHAT_FRIENDS || chanel == ShareChanel.WECHAT_MOMENTS
                    || chanel == ShareChanel.QZONE) {
                return mContext.getString(R.string.share_others_video_to_wechat_desc,
                        content, videoInfo.getUserInfo().getNickname());
            }
            else if (chanel == ShareChanel.WEIBO) {
                return mContext.getString(R.string.share_others_video_to_weibo_desc,
                        content, buildVideoUrl(videoInfo));
            }
            else {
                return content;
            }
        }
    }

    private String topicDescription() {
        return mContext.getString(R.string.share_topic_description);
    }

    private String inviteDescWechat(String title, String nickname) {
        return mContext.getString(R.string.invite_desc_wechat, nickname, title);
    }

    private String buildInviteDesc(ShareChanel chanel, String content, String topicId) {
        if (chanel == ShareChanel.WECHAT_FRIENDS) {
            return mContext.getString(R.string.invite_wechat_friend_desc, content);
        }
        else if (chanel == ShareChanel.WECHAT_MOMENTS || chanel == ShareChanel.QZONE) {
            return mContext.getString(R.string.invite_moments_friend_desc, MyAccountInfo.getNickname());
        }
        else /*if (chanel == ShareChanel.WEIBO)*/ {
            return mContext.getString(R.string.invite_weibo_friend_desc,
                    content, buildInviteUrl(topicId));
        }
    }

    private String inviteDescWeiboNQzone(String title, String nickname) {
        return mContext.getString(R.string.invite_desc_weibo_n_qzone, nickname, title);
    }

    private String buildVideoUrl(BaseVideoInfo videoInfo) {
        return new StringBuilder("http://k.koolew.com/video?vid=")
                .append(videoInfo.getVideoId()).toString();
    }

    private String buildTopicUrl(String topicId) {
        return new StringBuilder("http://k.koolew.com/topic?tid=").append(topicId).toString();
    }

    private String buildInviteUrl(String topicId) {
        return Uri.parse("http://k.koolew.com/invitation")
                .buildUpon()
                .appendQueryParameter("tid", topicId)
                .appendQueryParameter("uid", MyAccountInfo.getUid())
                .build().toString();
    }

    private String buildShareText(String description, String url) {
        if (TextUtils.isEmpty(description)) {
            return url;
        }
        return new StringBuilder()
                .append(description).append("\n")
                .append(url)
                .toString();
    }


    private abstract class ShareImage {
        protected abstract void setImageParam(Platform.ShareParams sp);
    }

    private class ResourceShareImage extends ShareImage {
        private int mImageResId;

        private ResourceShareImage(int resId) {
            mImageResId = resId;
        }

        @Override
        protected void setImageParam(Platform.ShareParams sp) {
            sp.setImageData(BitmapFactory.decodeResource(
                    mContext.getResources(), mImageResId));
        }
    }

    private class UrlShareImage extends ShareImage {
        private String mUrl;

        private UrlShareImage(String url) {
            mUrl = url;
        }

        @Override
        protected void setImageParam(Platform.ShareParams sp) {
            sp.setImageUrl(mUrl);
        }
    }

    public static abstract class ShareListener
            implements cn.sharesdk.framework.PlatformActionListener {

        protected Activity mActivity;

        protected String mSuccessMessage;
        protected String mCancelMessage;
        protected String mErrorMessage;

        public ShareListener(Activity activity) {
            mActivity = activity;
            initMessages();
        }

        protected abstract void initMessages();

        @Override
        public void onCancel(Platform platform, int i) {
            Toast.makeText(mActivity, mCancelMessage, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            Toast.makeText(mActivity, mSuccessMessage, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Platform platform, int i, final Throwable throwable) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String failedMessage = mErrorMessage;
                    try {
                        JSONObject errorMessage = new JSONObject(throwable.getMessage());
                        JSONObject error = new JSONObject(errorMessage.getString("error"));
                        String errorString = error.getString("error");
                        failedMessage = failedMessage + ": " + errorString;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(mActivity, failedMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
