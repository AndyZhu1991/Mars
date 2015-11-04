package com.koolew.mars.videotools;

import com.koolew.mars.AppProperty;

import org.bytedeco.javacpp.avcodec;

/**
 * Created by jinchangzhu on 10/28/15.
 */
public class Params {
    public final static String OUTPUT_FORMAT = "mp4";
    public final static int VIDEO_CODEC = avcodec.AV_CODEC_ID_H264;
    public final static int VIDEO_FRAME_RATE = AppProperty.RECORD_VIDEO_FPS;
    public final static int VIDEO_BIT_RATE = 700000;
    public final static int VIDEO_QUALITY = 12;
    public final static int AUDIO_CODEC = avcodec.AV_CODEC_ID_AAC;
    public final static int AUDIO_CHANNEL = 1;
    public final static int AUDIO_SAMPLE_RATE = 44100;
    public final static int AUDIO_BIT_RATE = 96000;
}
