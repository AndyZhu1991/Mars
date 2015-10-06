package com.koolew.mars;

/**
 * Created by jinchangzhu on 5/27/15.
 */
public class AppProperty {

    public static final float RECORD_VIDEO_MAX_LEN = 9.0f; // second
    public static final int RECORD_VIDEO_WIDTH = 480;
    public static final int RECORD_VIDEO_HEIGHT = 360;
    public static final int RECORD_VIDEO_FPS = 25;

    public static final String DEFAULT_AVATAR_URL = "http://avatar.koolew.com/default_avatar.jpg";
    public static final String APP_DOWNLOAD_URL = "d.koolew.com";
    public static final String CLOCK_IN_URL = "http://k.koolew.com/dynamic/register";

    public static final int DANMAKU_MAX_WORD = 15;

    public static int getNicknameMaxLen() {
        return 10;
    }

    public static int getTopicMaxReturnParterCount() {
        return 10;
    }
}
