package com.koolew.mars;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.share.ShareManager;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

/**
 * Created by jinchangzhu on 7/28/15.
 */
public class ShareVideoWindow extends PopupWindow implements View.OnClickListener,
        PlatformActionListener {

    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_VIDEO_LIST = 1;

    private Context mContext;

    private View mContentView;

    private int mType;
    private String mId;
    private String mContent;
    private String mUid; // This only used for TYPE_VIDEO

    private Dialog mProgressDialog;
    private OnVideoOperatedListener mVideoOperatedListener;
    private ShareManager mShareManager;

    public ShareVideoWindow(Context context, int type, String id, String content) {
        this(context, type, id, content, null);
    }

    public ShareVideoWindow(Context context, int type, String id, String content, String uid) {
        super(context);

        mContext = context;
        mShareManager = new ShareManager(mContext, this);

        mProgressDialog = DialogUtil.getConnectingServerDialog(context);

        mType = type;
        mId = id;
        mContent = content;
        mUid = uid;

        mContentView = LayoutInflater.from(context).inflate(R.layout.share_video_layout, null);
        setContentView(mContentView);

        if (type == TYPE_VIDEO) {
            if (MyAccountInfo.getUid().equals(mUid)) {
                ((ImageView) mContentView.findViewById(R.id.operation_image))
                        .setVisibility(View.GONE);
                ((TextView) mContentView.findViewById(R.id.operation_text))
                        .setText(R.string.delete_this_video);
            }
        }
        else if (type == TYPE_VIDEO_LIST) {
            ((ImageView) mContentView.findViewById(R.id.icon))
                    .setImageResource(R.mipmap.ic_share_video_list);
            ((TextView) mContentView.findViewById(R.id.text)).setText(R.string.share_video_list);
            mContentView.findViewById(R.id.operation_layout).setVisibility(View.GONE);
        }

        mContentView.findViewById(R.id.wechat_moments).setOnClickListener(this);
        mContentView.findViewById(R.id.wechat_friends).setOnClickListener(this);
        mContentView.findViewById(R.id.qzone).setOnClickListener(this);
        mContentView.findViewById(R.id.weibo).setOnClickListener(this);
        mContentView.findViewById(R.id.email).setOnClickListener(this);
        mContentView.findViewById(R.id.sms).setOnClickListener(this);
        mContentView.findViewById(R.id.operation_layout).setOnClickListener(this);
        mContentView.findViewById(R.id.out_view).setOnClickListener(this);

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(0xB0000000));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wechat_moments:
                if (mType == TYPE_VIDEO) {
                    mShareManager.shareVideoTo(ShareManager.ShareChanel.WECHAT_MOMENTS, mId, mContent);
                }
                else if (mType == TYPE_VIDEO_LIST) {
                    mShareManager.shareTopicTo(ShareManager.ShareChanel.WECHAT_MOMENTS, mId, mContent);
                }
                break;
            case R.id.wechat_friends:
                if (mType == TYPE_VIDEO) {
                    mShareManager.shareVideoTo(ShareManager.ShareChanel.WECHAT_FRIENDS, mId, mContent);
                }
                else if (mType == TYPE_VIDEO_LIST) {
                    mShareManager.shareTopicTo(ShareManager.ShareChanel.WECHAT_FRIENDS, mId, mContent);
                }
                break;
            case R.id.qzone:
                if (mType == TYPE_VIDEO) {
                    mShareManager.shareVideoTo(ShareManager.ShareChanel.QZONE, mId, mContent);
                }
                else if (mType == TYPE_VIDEO_LIST) {
                    mShareManager.shareTopicTo(ShareManager.ShareChanel.QZONE, mId, mContent);
                }
                break;
            case R.id.weibo:
                if (mType == TYPE_VIDEO) {
                    mShareManager.shareVideoTo(ShareManager.ShareChanel.WEIBO, mId, mContent);
                }
                else if (mType == TYPE_VIDEO_LIST) {
                    mShareManager.shareTopicTo(ShareManager.ShareChanel.WEIBO, mId, mContent);
                }
                break;
            case R.id.email:
                if (mType == TYPE_VIDEO) {
                    mShareManager.shareVideoTo(ShareManager.ShareChanel.EMAIL, mId, mContent);
                }
                else if (mType == TYPE_VIDEO_LIST) {
                    mShareManager.shareTopicTo(ShareManager.ShareChanel.EMAIL, mId, mContent);
                }
                break;
            case R.id.sms:
                if (mType == TYPE_VIDEO) {
                    mShareManager.shareVideoTo(ShareManager.ShareChanel.SMS, mId, mContent);
                }
                else if (mType == TYPE_VIDEO_LIST) {
                    mShareManager.shareTopicTo(ShareManager.ShareChanel.SMS, mId, mContent);
                }
                break;
            case R.id.operation_layout:
                onOperate();
                break;
            case R.id.out_view:
                dismiss();
                break;
        }
    }

    private void onOperate() {
        if (MyAccountInfo.getUid().equals(mUid)) {
            ApiWorker.getInstance().deleteVideo(mId, mVideoDeletedListener, null);
        }
        else {
            ApiWorker.getInstance().againstVideo(mId, mVideoAgainstListener, null);
        }
        this.dismiss();
        mProgressDialog.show();
    }

    private Response.Listener<JSONObject> mVideoDeletedListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mProgressDialog.dismiss();
            if (mVideoOperatedListener != null) {
                mVideoOperatedListener.onVideoDeleted(mId);
            }
        }
    };

    private Response.Listener<JSONObject> mVideoAgainstListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mProgressDialog.dismiss();
            if (mVideoOperatedListener != null) {
                mVideoOperatedListener.onVideoAgainst(mId);
            }
        }
    };

    public void setOnVideoOperatedListener(OnVideoOperatedListener listener) {
        mVideoOperatedListener = listener;
    }

    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        this.dismiss();
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        Toast.makeText(mContext, R.string.share_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(Platform platform, int i) {
    }

    interface OnVideoOperatedListener {
        void onVideoDeleted(String videoId);
        void onVideoAgainst(String videoId);
    }
}
