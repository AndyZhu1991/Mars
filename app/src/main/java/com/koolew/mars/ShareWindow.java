package com.koolew.mars;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.koolew.mars.share.ShareManager;

import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;

/**
 * Created by jinchangzhu on 10/19/15.
 */
public abstract class ShareWindow extends PopupWindow {

    protected Activity mActivity;
    protected View mContentView;
    protected ShareManager mShareManager;

    public ShareWindow(Activity activity) {
        super(activity);

        mActivity = activity;
        mShareManager = new ShareManager(mActivity, new ShareListener());

        mContentView = LayoutInflater.from(activity).inflate(R.layout.share_video_layout, null);
        setContentView(mContentView);

        mContentView.findViewById(R.id.wechat_moments).setOnClickListener(mShareClickListener);
        mContentView.findViewById(R.id.wechat_friends).setOnClickListener(mShareClickListener);
        mContentView.findViewById(R.id.qzone).setOnClickListener(mShareClickListener);
        mContentView.findViewById(R.id.weibo).setOnClickListener(mShareClickListener);
        mContentView.findViewById(R.id.email).setOnClickListener(mShareClickListener);
        mContentView.findViewById(R.id.sms).setOnClickListener(mShareClickListener);
        mContentView.findViewById(R.id.operation_layout).setOnClickListener(mOperateClickListener);
        mContentView.findViewById(R.id.out_view).setOnClickListener(mOperateClickListener);

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(0xB0000000));
    }

    private static Map<Integer, ShareManager.ShareChanel> chanelMap = new HashMap<>();
    static {
        chanelMap.put(R.id.wechat_moments, ShareManager.ShareChanel.WECHAT_MOMENTS);
        chanelMap.put(R.id.wechat_friends, ShareManager.ShareChanel.WECHAT_FRIENDS);
        chanelMap.put(R.id.qzone, ShareManager.ShareChanel.QZONE);
        chanelMap.put(R.id.weibo, ShareManager.ShareChanel.WEIBO);
        chanelMap.put(R.id.email, ShareManager.ShareChanel.EMAIL);
        chanelMap.put(R.id.sms, ShareManager.ShareChanel.SMS);
    }

    private View.OnClickListener mShareClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ShareManager.ShareChanel chanel = chanelMap.get(v.getId());
            onShareToChanel(chanel);
        }
    };

    private View.OnClickListener mOperateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.operation_layout:
                    onOperate();
                    break;
                case R.id.out_view:
                    dismiss();
                    break;
            }
        }
    };

    protected abstract void onOperate();

    protected abstract void onShareToChanel(ShareManager.ShareChanel shareChanel);

    class ShareListener extends ShareManager.ShareListener {

        public ShareListener() {
            super(ShareWindow.this.mActivity);
        }

        @Override
        protected void initMessages() {
            mSuccessMessage = mActivity.getString(R.string.share_success);
            mErrorMessage = mActivity.getString(R.string.share_failed);
            mCancelMessage = mActivity.getString(R.string.share_cancel);
        }

        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            super.onComplete(platform, i, hashMap);
            ShareWindow.this.dismiss();
        }
    }
}
