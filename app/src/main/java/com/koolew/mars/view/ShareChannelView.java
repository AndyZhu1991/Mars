package com.koolew.mars.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koolew.mars.R;
import com.koolew.mars.utils.Utils;

/**
 * Created by jinchangzhu on 7/25/15.
 */
public class ShareChannelView extends LinearLayout {

    // Same as enum values in attrs.xml
    public static final int TYPE_WEIBO          = 0;
    public static final int TYPE_WECHAT_MOMENTS = 1;
    public static final int TYPE_WECHAT_FRIENDS = 2;
    public static final int TYPE_QZONE          = 3;
    public static final int TYPE_EMAIL          = 4;
    public static final int TYPE_MMS            = 5;

    private static final int[] ICONS = {
            R.mipmap.ic_login_weibo,
            R.mipmap.ic_wechat_moments,
            R.mipmap.ic_login_wechat,
            R.mipmap.ic_qzone,
            R.mipmap.ic_email,
            R.mipmap.ic_mms
    };
    private static final int[] LABELS = {
            R.string.weibo,
            R.string.wechat_moments,
            R.string.wechat_friends,
            R.string.qzone,
            R.string.email,
            R.string.mms
    };

    private int mType;

    private ImageView mIconView;
    private TextView mLabel;

    public ShareChannelView(Context context) {
        this(context, null);
    }

    public ShareChannelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShareChannelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        setupViews();

        initIconAndLabel(attrs);
    }

    private void setupViews() {
        mIconView = new ImageView(getContext());
        int iconViewSize = (int) Utils.dpToPixels(getContext(), 48);
        addView(mIconView, new LayoutParams(iconViewSize, iconViewSize));

        mLabel = new TextView(getContext());
        mLabel.setTextSize(12); // sp
        mLabel.setTextColor(0xFF9EADB7);
        LayoutParams labelLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        labelLp.setMargins(0, (int) Utils.dpToPixels(getContext(), 8), 0, 0);
        addView(mLabel, labelLp);
    }

    private void initIconAndLabel(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ShareChannelView, 0, 0);
        mType = a.getInt(R.styleable.ShareChannelView_type, -1);
        if (mType >= 0) {
            mIconView.setImageResource(ICONS[mType]);
            mLabel.setText(LABELS[mType]);
        }
        a.recycle();
    }
}
