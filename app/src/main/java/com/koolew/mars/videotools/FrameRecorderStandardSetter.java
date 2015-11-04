package com.koolew.mars.videotools;

import org.bytedeco.javacv.FrameRecorder;

import static com.koolew.mars.videotools.Params.OUTPUT_FORMAT;
import static com.koolew.mars.videotools.Params.VIDEO_CODEC;
import static com.koolew.mars.videotools.Params.VIDEO_FRAME_RATE;
import static com.koolew.mars.videotools.Params.VIDEO_BIT_RATE;
import static com.koolew.mars.videotools.Params.VIDEO_QUALITY;
import static com.koolew.mars.videotools.Params.AUDIO_CODEC;
import static com.koolew.mars.videotools.Params.AUDIO_CHANNEL;
import static com.koolew.mars.videotools.Params.AUDIO_SAMPLE_RATE;
import static com.koolew.mars.videotools.Params.AUDIO_BIT_RATE;

/**
 * Created by jinchangzhu on 9/15/15.
 */
public class FrameRecorderStandardSetter {

    public static final int SET_OUTPUT_FORMAT     = 0x0001;
    public static final int SET_VIDEO_CODEC       = 0x0002;
    public static final int SET_VIDEO_FRAME_RATE  = 0x0004;
    public static final int SET_VIDEO_BIT_RATE    = 0x0008;
    public static final int SET_VIDEO_QUALITY     = 0x0010;
    public static final int SET_AUDIO_CODEC       = 0x0020;
    public static final int SET_AUDIO_CHANNEL     = 0x0040;
    public static final int SET_AUDIO_SAMPLE_RATE = 0x0080;
    public static final int SET_AUDIO_BIT_RATE    = 0x0100;


    public static void defaultRecorderSet(FrameRecorder recorder, int setBit) {
        if ((setBit & SET_OUTPUT_FORMAT) > 0) {
            recorder.setFormat(OUTPUT_FORMAT);
        }
        if ((setBit & SET_VIDEO_CODEC) > 0) {
            recorder.setVideoCodec(VIDEO_CODEC);
        }
        if ((setBit & SET_VIDEO_FRAME_RATE) > 0) {
            recorder.setFrameRate(VIDEO_FRAME_RATE);
        }
        if ((setBit & SET_VIDEO_BIT_RATE) > 0) {
            recorder.setVideoBitrate(VIDEO_BIT_RATE);
        }
        if ((setBit & SET_VIDEO_QUALITY) > 0) {
            recorder.setVideoQuality(VIDEO_QUALITY);
        }
        if ((setBit & SET_AUDIO_CODEC) > 0) {
            recorder.setAudioCodec(AUDIO_CODEC);
        }
        if ((setBit & SET_AUDIO_CHANNEL) > 0) {
            recorder.setAudioChannels(AUDIO_CHANNEL);
        }
        if ((setBit & SET_AUDIO_SAMPLE_RATE) > 0) {
            recorder.setSampleRate(AUDIO_SAMPLE_RATE);
        }
        if ((setBit & SET_AUDIO_BIT_RATE) > 0) {
            recorder.setAudioBitrate(AUDIO_BIT_RATE);
        }
    }
}
