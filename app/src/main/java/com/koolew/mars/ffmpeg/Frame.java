package com.koolew.mars.ffmpeg;

/**
 * Created by jinchangzhu on 9/10/15.
 */
public abstract class Frame {
    protected long timeStamp;

    public long getTimeStamp() {
        return timeStamp;
    }
}
