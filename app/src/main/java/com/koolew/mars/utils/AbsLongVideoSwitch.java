package com.koolew.mars.utils;

import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;

/**
 * Created by jinchangzhu on 11/18/15.
 */
public abstract class AbsLongVideoSwitch implements View.OnTouchListener {
    public static final String LONG_VIDEO_18S = "2824";
    public static final String LONG_VIDEO_60S = "8268";

    private static final String[] ALL_PASSWORDS = { LONG_VIDEO_18S, LONG_VIDEO_60S };

    private static final int H_SPLIT = 3;
    private static final int V_SPLIT = 3;
    private static final int PASSWORD_LEN = 4;

    private LinkedList<Integer> touchOps = new LinkedList<>();

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (touchOps.size() == PASSWORD_LEN) {
                touchOps.remove(0);
            }
            touchOps.add(findNum(v.getWidth(), v.getHeight(),
                    (int) event.getX(), (int) event.getY()));
            if (touchOps.size() == PASSWORD_LEN) {
                checkOperation();
            }
        }
        return false;
    }

    private static int findNum(int width, int height, int x, int y) {
        int hIndex = x / (width / H_SPLIT);
        int vIndex = y / (height / V_SPLIT);
        int index = vIndex * H_SPLIT + hIndex;
        return index + 1; // Index starts width 0
    }

    protected void checkOperation() {
        StringBuilder sb = new StringBuilder();
        for (int i: touchOps) {
            sb.append(i);
        }
        String currentPassword = sb.toString();
        for (String password: ALL_PASSWORDS) {
            if (currentPassword.equals(password)) {
                onPasswordHandle(password);
                break;
            }
        }
    }

    protected abstract void onPasswordHandle(String password);
}
