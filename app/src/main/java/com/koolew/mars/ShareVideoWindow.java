package com.koolew.mars;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.share.ShareManager;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/28/15.
 */
public class ShareVideoWindow extends ShareWindow {

    private BaseVideoInfo mVideoInfo;
    private String mId;
    private String mContent;
    private String mUid;

    private Dialog mProgressDialog;
    private OnVideoOperatedListener mVideoOperatedListener;


    public ShareVideoWindow(Activity activity, BaseVideoInfo videoInfo, String content) {
        super(activity);

        mProgressDialog = DialogUtil.getConnectingServerDialog(activity);

        mVideoInfo = videoInfo;
        mId = videoInfo.getVideoId();
        mContent = content;
        mUid = videoInfo.getUserInfo().getUid();

        if (MyAccountInfo.getUid().equals(mUid)) {
            mContentView.findViewById(R.id.operation_image).setVisibility(View.GONE);
            ((TextView) mContentView.findViewById(R.id.operation_text))
                    .setText(R.string.delete_this_video);
        }
    }

    @Override
    protected void onOperate() {
        if (MyAccountInfo.getUid().equals(mUid)) {
            ApiWorker.getInstance().deleteVideo(mId, mVideoDeletedListener, null);
        }
        else {
            ApiWorker.getInstance().againstVideo(mId, mVideoAgainstListener, null);
        }
        this.dismiss();
        mProgressDialog.show();
    }

    @Override
    protected void onShareToChanel(ShareManager.ShareChanel shareChanel) {
        mShareManager.shareVideoTo(shareChanel, mVideoInfo, mContent);
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

    interface OnVideoOperatedListener {
        void onVideoDeleted(String videoId);
        void onVideoAgainst(String videoId);
    }
}
