package com.koolew.mars.share;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.koolew.mars.R;
import com.koolew.mars.infos.MyAccountInfo;

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

    public ShareManager(Context context, PlatformActionListener platformActionListener) {
        mContext = context;
        mListener = platformActionListener;
    }

    public void shareVideoTo(ShareChanel shareChanel, String videoId, String content) {
        share(sharePlatformName[shareChanel.ordinal()], content, videoDescription(),
                DEFAULT_SHARE_IMAGE, buildVideoUrl(videoId));
    }

    public void shareTopicTo(ShareChanel shareChanel, String topicId, String content) {
        share(sharePlatformName[shareChanel.ordinal()], content, topicDescription(),
                DEFAULT_SHARE_IMAGE, buildTopicUrl(topicId));
    }

    public void inviteBy(ShareChanel shareChanel, String topicId, String content) {
        String title = null;
        String description = null;

        if (shareChanel.equals(ShareChanel.WECHAT_FRIENDS) ||
                shareChanel.equals(ShareChanel.WECHAT_MOMENTS)) {
            title = inviteDescWechat(content, MyAccountInfo.getNickname());
            description = mContext.getString(R.string.app_name);
        }
        else if (shareChanel.equals(ShareChanel.QZONE)) {
            title = inviteDescWechat(content, MyAccountInfo.getNickname());
        }
        else if (shareChanel.equals(ShareChanel.WEIBO)) {
            description = inviteDescWechat(content, MyAccountInfo.getNickname());
        }

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
        sp.setTitle(title);
        sp.setText(description);
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
        sp.setText(buildShareText(title, description, url));
        sp.setShareType(Platform.SHARE_TEXT);
        Platform platform = ShareSDK.getPlatform(platformName);
        platform.setPlatformActionListener(mListener);
        platform.share(sp);
    }

    private String videoDescription() {
        return mContext.getString(R.string.share_video_description);
    }

    private String topicDescription() {
        return mContext.getString(R.string.share_topic_description);
    }

    private String inviteDescWechat(String title, String nickname) {
        return mContext.getString(R.string.invite_desc_wechat, nickname, title);
    }

    private String inviteDescWeiboNQzone(String title, String nickname) {
        return mContext.getString(R.string.invite_desc_weibo_n_qzone, nickname, title);
    }

    private String buildVideoUrl(String videoId) {
        return new StringBuilder("http://k.koolew.com/video?vid=").append(videoId).toString();
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

    private String buildShareText(String title, String description, String url) {
        return new StringBuilder()
                .append(title).append("\n")
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
}
