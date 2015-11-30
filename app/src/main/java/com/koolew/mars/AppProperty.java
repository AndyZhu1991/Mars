package com.koolew.mars;

/**
 * Created by jinchangzhu on 5/27/15.
 */
public class AppProperty {

    public static final float DEFAULT_VIDEO_MAX_LEN = 9.0f;
    public static final float VIDEO_MAX_LEN_18s = 18.0f;
    public static final float VIDEO_MAX_LEN_60s = 60.0f;
    private static float RECORD_VIDEO_MAX_LEN = DEFAULT_VIDEO_MAX_LEN; // second

    public static final int RECORD_VIDEO_WIDTH = 480;
    public static final int RECORD_VIDEO_HEIGHT = 360;
    public static final int RECORD_VIDEO_FPS = 25;

    public static final String DEFAULT_AVATAR_URL = "http://avatar.koolew.com/default_avatar.jpg";
    public static final String APP_DOWNLOAD_URL = "d.koolew.com";
    public static final String CLOCK_IN_URL = "http://k.koolew.com/dynamic/register";
    public static final String INCOME_EXPLAIN_URL = "http://www.koolew.com/cash.html";

    public static final int DANMAKU_MAX_WORD = 15;

    public static final int TOPIC_TITLE_MAX_WORDS = 18;

    public static int getNicknameMaxLen() {
        return 10;
    }

    public static int getTopicMaxReturnParterCount() {
        return 10;
    }

    public static float getRecordVideoMaxLen() {
        return RECORD_VIDEO_MAX_LEN;
    }

    public static void setRecordVideoMaxLen(float len) {
        RECORD_VIDEO_MAX_LEN = len;
    }
}
