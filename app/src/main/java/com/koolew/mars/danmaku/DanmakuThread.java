package com.koolew.mars.danmaku;

import android.app.Activity;

/**
 * Created by jinchangzhu on 7/16/15.
 */
public class DanmakuThread extends Thread {

    private static final int IGNORE_REAL_VIDEO_LEN = -1;

    private Activity mActivity;
    private DanmakuShowManager mDanmakuManager;
    private PlayerWrapper mPlayerWrapper;
    private int mRealVideoLen;

    private long lastPosition;
    private boolean runDanmaku;

    public DanmakuThread(Activity activity, DanmakuShowManager danmakuManager,
                         PlayerWrapper playerWrapper) {
        this(activity, danmakuManager, playerWrapper, IGNORE_REAL_VIDEO_LEN);
    }

    public DanmakuThread(Activity activity, DanmakuShowManager danmakuManager,
                         PlayerWrapper playerWrapper, int realVideoLen) {
        mActivity = activity;
        mDanmakuManager = danmakuManager;
        mPlayerWrapper = playerWrapper;
        mRealVideoLen = realVideoLen;
    }

    @Override
    public void run() {
        lastPosition = 0;
        runDanmaku = true;
        while (runDanmaku) {
            if (mPlayerWrapper.isPlaying()) {
                long currentPosition = mPlayerWrapper.getCurrentPosition();
                if (currentPosition < lastPosition) {
                    danmakuManagerClear();
                }
                danmakuManagerUpdate((int) currentPosition);
                lastPosition = currentPosition;
            }

            try {
                Thread.sleep(40); // 40ms is 1 frame
            } catch (InterruptedException e) {
            }
        }
        danmakuManagerClear();
    }

    public void stopDanmaku() {
        runDanmaku = false;
    }

    private void danmakuManagerClear() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDanmakuManager.clear();
            }
        });
    }

    private void danmakuManagerUpdate(final int millis) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRealVideoLen == IGNORE_REAL_VIDEO_LEN) {
                    mDanmakuManager.update(millis);
                }
                else {
                    mDanmakuManager.update(millis % mRealVideoLen);
                }
            }
        });
    }

    public interface PlayerWrapper {
        long getCurrentPosition();
        boolean isPlaying();
    }
}
