package com.koolew.mars.videotools;

/**
 * Created by jinchangzhu on 9/16/15.
 */
public class TimestampAdjuster {

    public static final long DROP_THIS_FRAME = -1;

    private double frameRate;

    private long firstFrameTimestamp;
    private long lastFrameTimestamp;

    public TimestampAdjuster(double frameRate) {
        this.frameRate = frameRate;
        firstFrameTimestamp = -1;
        lastFrameTimestamp = -1;
    }

    public long adjustTimestamp(long timestamp) {
        return adjustTimestamp(timestamp, true);
    }

    public long adjustTimestamp(long timestamp, boolean usePrevTimestamp) {
        if (timestamp < 0) {
            throw new RuntimeException("timestamp can't < 0");
        }

        if (firstFrameTimestamp < 0) {
            firstFrameTimestamp = timestamp;
            lastFrameTimestamp = 0;
            return 0;
        }

        long usecPerFrame = (long) (1000000 / frameRate);
        long timestampOffset = timestamp - firstFrameTimestamp;
        long adjustedTimestamp = timestampOffset / usecPerFrame * usecPerFrame
                + (usePrevTimestamp ? 0 : usecPerFrame);
        if (lastFrameTimestamp < adjustedTimestamp) {
            lastFrameTimestamp = adjustedTimestamp;
            return adjustedTimestamp;
        }
        else if (lastFrameTimestamp == adjustedTimestamp) {
            if (usePrevTimestamp) {
                adjustedTimestamp += usecPerFrame;
                lastFrameTimestamp = adjustedTimestamp;
                return lastFrameTimestamp;
            }
            else {
                // 丢弃这一帧
                return DROP_THIS_FRAME;
            }
        }
        else {
            // 丢弃这一帧
            return DROP_THIS_FRAME;
        }
    }
}
