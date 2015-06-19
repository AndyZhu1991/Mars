package com.koolew.mars.danmaku;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

/**
 * Created by jinchangzhu on 6/18/15.
 */
public class DanmakuShowManager {

    private static final String TAG = "koolew-DanmakuShowM";

    private static final int DANMAKU_COUNT_LV1 = 1;
    private static final int DANMAKU_COUNT_LV2 = 5;
    private static final int DANMAKU_COUNT_LV3 = 25;

    // Danmaku show time, in ms
    private static final int GRADUAL_IN_TIME = 0;
    private static final int KEEP_TIME = 1500;
    private static final int GRADUAL_OUT_TIME = 500;
    private static final int DANMAKU_TOTAL_TIME = GRADUAL_IN_TIME + KEEP_TIME + GRADUAL_OUT_TIME;

    private FrameLayout mContainer;
    private Context mContext;
    private ArrayList<DanmakuItemInfo> mDanmakuList;
    private View[] mDanmakuViews;

    private int containerWidth;
    private int containerHeight;

    private LayoutInflater mInflater;

    private DanmakuViewPool mViewPool;

    public DanmakuShowManager(Context context, FrameLayout container,
            ArrayList<DanmakuItemInfo> danmakuList) {
        mContainer = container;
        mContext = context;
        mDanmakuList = danmakuList;

        containerWidth = mContainer.getWidth();
        containerHeight = mContainer.getHeight();

        mDanmakuViews = new View[mDanmakuList.size()];
        mInflater = LayoutInflater.from(mContext);
        mViewPool = new DanmakuViewPool();

        Collections.sort(mDanmakuList, new Comparator<DanmakuItemInfo>() {
            @Override
            public int compare(DanmakuItemInfo lhs, DanmakuItemInfo rhs) {
                // Sort by show time
                return (int) (lhs.showTime - rhs.showTime);
            }
        });
    }

    /**
     *
     * @param millis Video time
     */
    public void update(int millis) {
        int count = mDanmakuList.size();
        for (int i = 0; i < count; i++) {
            // Recover invisible danmaku view
            if (!isDanmakuShow(mDanmakuList.get(i), millis)) {
                if (mDanmakuViews[i] != null) {
                    mContainer.removeView(mDanmakuViews[i]);
                    mViewPool.recoverView(mDanmakuViews[i]);
                    mDanmakuViews[i] = null;
                }
            }
            else {
                if (mDanmakuViews[i] == null) {
                    mDanmakuViews[i] = mViewPool.getView();
                    setupView(mDanmakuViews[i], mDanmakuList.get(i));
                }

                mDanmakuViews[i].setAlpha(getDanmakuAlpha(mDanmakuList.get(i), millis));
            }
        }
    }

    public void clear() {
        for (int i = 0; i < mDanmakuViews.length; i++) {
            if (mDanmakuViews[i] != null) {
                mContainer.removeView(mDanmakuViews[i]);
                mViewPool.recoverView(mDanmakuViews[i]);
                mDanmakuViews[i] = null;
            }
        }
    }

    private boolean isDanmakuShow(DanmakuItemInfo info, int millis) {
        return info.showTime * 1000 < millis && info.showTime * 1000 + DANMAKU_TOTAL_TIME > millis;
    }

    private void setupView(View danmakuView, DanmakuItemInfo info) {
        ViewHolder holder = (ViewHolder) danmakuView.getTag();
        ImageLoader.getInstance().displayImage(info.avatar, holder.avatar);
        holder.message.setText(info.content);

        int danmakuColor = mContext.getResources().getColor(android.R.color.white);
        if (info.kooNum >= DANMAKU_COUNT_LV3) {
            danmakuColor = mContext.getResources().getColor(R.color.danmaku_master);
        }
        else if (info.kooNum >= DANMAKU_COUNT_LV2) {
            danmakuColor = mContext.getResources().getColor(R.color.danmaku_purple);
        }
        else if (info.kooNum >= DANMAKU_COUNT_LV1) {
            danmakuColor = mContext.getResources().getColor(R.color.danmaku_green);
        }
        holder.message.setTextColor(danmakuColor);

        FrameLayout.LayoutParams dlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlp.leftMargin = (int) (containerWidth * info.x);
        dlp.topMargin = (int) (containerHeight * info.y);
        Log.d(TAG, containerWidth + ", " + info.x + ", " + containerHeight + ", " + info.y);
        danmakuView.setLayoutParams(dlp);

        mContainer.addView(danmakuView);
    }

    private float getDanmakuAlpha(DanmakuItemInfo info, int millis) {
        // Pre-show
        if (millis < info.showTime * 1000) {
            return 0.0f;
        }
        // Gradual in
        else if (GRADUAL_IN_TIME != 0 && millis < info.showTime * 1000 + GRADUAL_IN_TIME) {
            return 1.0f * (millis - info.showTime * 1000) / GRADUAL_IN_TIME;
        }
        // Keep show
        else if (millis > info.showTime * 1000 + GRADUAL_IN_TIME &&
                 millis < info.showTime * 1000 + GRADUAL_IN_TIME + KEEP_TIME) {
            return 1.0f;
        }
        // Gradual out
        else if (GRADUAL_OUT_TIME != 0 && millis > info.showTime * 1000 + GRADUAL_IN_TIME + KEEP_TIME) {
            return 1.0f - 1.0f * (millis - (info.showTime * 1000 + GRADUAL_IN_TIME + KEEP_TIME)) / GRADUAL_OUT_TIME;
        }
        // Show finish
        else {
            return 0.0f;
        }
    }

    private void showDanmaku(DanmakuItemInfo danmakuInfo) {
        View danmaku = mInflater.inflate(R.layout.danmaku_item, null);
        ImageLoader.getInstance().displayImage(danmakuInfo.avatar,
                (ImageView) danmaku.findViewById(R.id.avatar));
        TextView messageView = (TextView) danmaku.findViewById(R.id.message);
        messageView.setText(danmakuInfo.content);

        ViewGroup.LayoutParams containerLp = mContainer.getLayoutParams();
        FrameLayout.LayoutParams danmakuLp = new FrameLayout.LayoutParams(danmaku.getLayoutParams());
        danmakuLp.leftMargin = (int) (containerLp.width * danmakuInfo.x);
        danmakuLp.topMargin = (int) (containerLp.height * danmakuInfo.y);

        mContainer.addView(danmaku, danmakuLp);
        setupAnimation(danmaku);
    }

    private void setupAnimation(final View danmaku) {
        ValueAnimator va = ValueAnimator.ofInt(0, GRADUAL_IN_TIME + KEEP_TIME + GRADUAL_OUT_TIME);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                if (value < GRADUAL_IN_TIME) {
                    danmaku.setAlpha(1.0f * value / GRADUAL_IN_TIME);
                }
                else if (value > GRADUAL_IN_TIME + KEEP_TIME) {
                    danmaku.setAlpha(1.0f - 1.0f * (value - (GRADUAL_IN_TIME + KEEP_TIME)) / GRADUAL_OUT_TIME);
                }
            }
        });
        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mContainer.removeView(danmaku);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    class ViewHolder {
        ImageView avatar;
        TextView message;
    }

    class DanmakuViewPool {
        private Stack<View> mStack;

        DanmakuViewPool() {
            mStack = new Stack<View>();
        }

        View getView() {
            if (mStack.empty()) {
                View danmakuView = mInflater.inflate(R.layout.danmaku_item, null);
                ViewHolder holder = new ViewHolder();
                holder.avatar = (ImageView) danmakuView.findViewById(R.id.avatar);
                holder.message = (TextView) danmakuView.findViewById(R.id.message);
                holder.message.setShadowLayer(2f, 0f, 3f, Color.GRAY);
                danmakuView.setTag(holder);
                return danmakuView;
            }

            return mStack.pop();
        }

        void recoverView(View view) {
            mStack.push(view);
        }
    }

}
