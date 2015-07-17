package com.koolew.mars.danmaku;

import android.app.Activity;

/**
 * Created by jinchangzhu on 7/16/15.
 */
public class DanmakuThread extends Thread {

    private Activity mActivity;
    private DanmakuShowManager mDanmakuManager;
    private PlayerWrapper mPlayerWrapper;

    private long lastPosition;
    private boolean runDanmaku;

    public DanmakuThread(Activity activity, DanmakuShowManager danmakuManager,
                         PlayerWrapper playerWrapper) {
        mActivity = activity;
        mDanmakuManager = danmakuManager;
        mPlayerWrapper = playerWrapper;
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
                mDanmakuManager.update(millis);
            }
        });
    }

    public interface PlayerWrapper {
        long getCurrentPosition();
        boolean isPlaying();
    }
}
