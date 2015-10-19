package com.koolew.mars;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.koolew.mars.share.ShareManager;

/**
 * Created by jinchangzhu on 10/19/15.
 */
public class ShareUrlWindow extends ShareWindow {

    private String mUrl;
    private String mTitle;
    private String mImageUrl;

    public ShareUrlWindow(Activity activity, String url, String title, String imageUrl) {
        super(activity);

        mUrl = url;
        mTitle = title;
        mImageUrl = imageUrl;
        mContentView.findViewById(R.id.operation_layout).setVisibility(View.GONE);
        ((TextView) mContentView.findViewById(R.id.text)).setText(R.string.share_url);
    }

    @Override
    protected void onOperate() {
    }

    @Override
    protected void onShareToChanel(ShareManager.ShareChanel shareChanel) {
        mShareManager.shareUrlTo(shareChanel, mUrl, mTitle, mImageUrl);
    }
}
