package com.koolew.mars.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koolew.mars.R;
import com.koolew.mars.infos.BaseUserInfo;

/**
 * Created by jinchangzhu on 9/6/15.
 */
public class UserNameView extends LinearLayout {

    private TextView textView;
    private ImageView imageView;


    public UserNameView(Context context) {
        this(context, null);
    }

    public UserNameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserNameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UserNameView, 0, 0);

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        textView = new TextView(context);
        addView(textView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        int textSize = a.getDimensionPixelSize(R.styleable.UserNameView_textSize, 0);
        int textColor = a.getColor(R.styleable.UserNameView_textColor, 0);
        if (textSize != 0) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
        if (textColor != 0) {
            textView.setTextColor(textColor);
        }

        imageView = new ImageView(context);
        int realTextSize = (int) textView.getTextSize();
        ViewGroup.LayoutParams imageLayoutParam =
                new ViewGroup.LayoutParams(realTextSize, realTextSize);
        addView(imageView, imageLayoutParam);
        LinearLayout.LayoutParams lp = (LayoutParams) imageView.getLayoutParams();
        lp.setMargins(realTextSize / 3, 0, 0, 0);
    }

    public void setTextSizeSp(float sizeInSp) {
        textView.setTextSize(sizeInSp);
    }

    public void setTextSizePx(int sizeInPx) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeInPx);
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    /**
     *
     * @param nickname
     * @param vip See {@link BaseUserInfo} for the vip type
     */
    public void setUserInfo(String nickname, int vip) {
        textView.setText(nickname);

        switch (vip) {
            case BaseUserInfo.VIP_TYPE_NO_VIP:
                imageView.setVisibility(GONE);
                break;
            case BaseUserInfo.VIP_TYPE_GOLD_VIP:
                imageView.setImageResource(R.mipmap.ic_gold_crown);
                imageView.setVisibility(VISIBLE);
                break;
            case BaseUserInfo.VIP_TYPE_SILVER_VIP:
                imageView.setImageResource(R.mipmap.ic_silver_crown);
                imageView.setVisibility(VISIBLE);
                break;
        }
    }

    public void setUser(BaseUserInfo userInfo) {
        setUserInfo(userInfo.getNickname(), userInfo.getVip());
    }

    public String getNickname() {
        return textView.getText().toString();
    }
}
