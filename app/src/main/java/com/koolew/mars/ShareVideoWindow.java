package com.koolew.mars;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Created by jinchangzhu on 7/28/15.
 */
public class ShareVideoWindow extends PopupWindow implements View.OnClickListener {

    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_VIDEO_LIST = 1;

    private View mContentView;

    private int mType;
    private String mId;

    public ShareVideoWindow(Context context, int type, String id) {
        super(context);

        mType = type;
        mId = id;

        mContentView = LayoutInflater.from(context).inflate(R.layout.share_video_layout, null);
        setContentView(mContentView);

        if (type == TYPE_VIDEO_LIST) {
            ((ImageView) mContentView.findViewById(R.id.icon))
                    .setImageResource(R.mipmap.ic_share_video_list);
            ((TextView) mContentView.findViewById(R.id.text)).setText(R.string.share_video_list);
            mContentView.findViewById(R.id.report).setVisibility(View.GONE);
        }

        mContentView.findViewById(R.id.wechat_moments).setOnClickListener(this);
        mContentView.findViewById(R.id.wechat_friends).setOnClickListener(this);
        mContentView.findViewById(R.id.qzone).setOnClickListener(this);
        mContentView.findViewById(R.id.weibo).setOnClickListener(this);
        mContentView.findViewById(R.id.email).setOnClickListener(this);
        mContentView.findViewById(R.id.sms).setOnClickListener(this);
        mContentView.findViewById(R.id.report).setOnClickListener(this);
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
                break;
            case R.id.wechat_friends:
                break;
            case R.id.qzone:
                break;
            case R.id.weibo:
                break;
            case R.id.email:
                break;
            case R.id.sms:
                break;
            case R.id.report:
                break;
            case R.id.out_view:
                dismiss();
                break;
        }
    }
}
