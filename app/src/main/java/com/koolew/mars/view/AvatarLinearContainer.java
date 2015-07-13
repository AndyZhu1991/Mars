package com.koolew.mars.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.koolew.mars.R;
import com.koolew.mars.infos.BaseFriendInfo;
import com.koolew.mars.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by jinchangzhu on 7/9/15.
 */
public class AvatarLinearContainer extends LinearLayout implements View.OnClickListener {

    private int mAvatarMarginLr;
    private int mAvatarBorderWidth;
    private int mAvatarBorderColor;
    private int mAvatarBorderSpecialColor;
    private boolean mAvatarClickable;

    private int avatarSize;
    private int maxAvatarCount;
    private List<PersonInfo> mPersonInfos;

    public AvatarLinearContainer(Context context) {
        this(context, null);
    }

    public AvatarLinearContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarLinearContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AvatarLinearContainer, 0, 0);
        mAvatarMarginLr = a.getDimensionPixelSize(R.styleable.AvatarLinearContainer_avatar_margin_lr, 0);
        mAvatarBorderWidth = a.getDimensionPixelSize(R.styleable.AvatarLinearContainer_avatar_border_width,
                (int) Utils.dpToPixels(getContext(), 2));
        mAvatarBorderColor = a.getColor(R.styleable.AvatarLinearContainer_avatar_border_color,
                getResources().getColor(R.color.avatar_gray_border));
        mAvatarBorderSpecialColor = a.getColor(R.styleable.AvatarLinearContainer_avatar_border_special_color,
                getResources().getColor(R.color.koolew_light_green));
        mAvatarClickable = a.getBoolean(R.styleable.AvatarLinearContainer_avatar_clickable, false);

        mPersonInfos = new LinkedList<PersonInfo>();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (getOrientation() == HORIZONTAL) {
            avatarSize = getHeight();
            maxAvatarCount = getWidth() / (avatarSize + mAvatarMarginLr * 2);
        }
        else {
            avatarSize = getWidth();
            maxAvatarCount = getHeight() / (avatarSize + mAvatarMarginLr * 2);
        }

        while (getChildCount() < maxAvatarCount) {
            addView(generateAvatar());
        }

        for (int i = 0; i < mPersonInfos.size() && i < maxAvatarCount; i++) {
            CircleImageView avatar = (CircleImageView) getChildAt(i);
            if (avatar.getVisibility() == VISIBLE) {
                continue;
            }
            setupAvatar(avatar, mPersonInfos.get(i));
        }
    }

    public void addAvatar(PersonInfo info) {
        mPersonInfos.add(info);

        int childCount = getChildCount();
        if (mPersonInfos.size() <= childCount) {
            setupAvatar((CircleImageView) getChildAt(mPersonInfos.size() - 1), info);
        }
    }

    private void setupAvatar(CircleImageView avatar, PersonInfo info) {
        avatar.setTag(info.getUid());
        avatar.setBorderColor(info.isSpecial ? mAvatarBorderSpecialColor : mAvatarBorderColor);
        ImageLoader.getInstance().displayImage(info.getAvatar(), avatar);
        avatar.setVisibility(VISIBLE);
    }

    public void clearAvatar() {
        mPersonInfos.clear();

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setVisibility(GONE);
        }
    }

    private CircleImageView generateAvatar() {
        CircleImageView avatar = new CircleImageView(getContext());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(avatarSize, avatarSize);
        lp.setMargins(mAvatarMarginLr, 0, mAvatarMarginLr, 0);
        avatar.setLayoutParams(lp);
        avatar.setVisibility(GONE);

        avatar.setBorderWidth(mAvatarBorderWidth);

        if (mAvatarClickable) {
            avatar.setOnClickListener(this);
        }

        return avatar;
    }

    @Override
    public void onClick(View v) {

    }

    public static class PersonInfo extends BaseFriendInfo {

        private boolean isSpecial;

        public PersonInfo(JSONObject jsonObject) {
            this(jsonObject, false);
        }

        public PersonInfo(JSONObject jsonObject, boolean isSpecial) {
            super(jsonObject);
            this.isSpecial = isSpecial;
        }
    }
}
