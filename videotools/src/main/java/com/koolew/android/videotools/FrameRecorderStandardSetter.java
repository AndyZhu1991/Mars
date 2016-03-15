package com.koolew.android.videotools;

import org.bytedeco.javacv.FrameRecorder;

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
            recorder.setFormat(Params.OUTPUT_FORMAT);
        }
        if ((setBit & SET_VIDEO_CODEC) > 0) {
            recorder.setVideoCodec(Params.VIDEO_CODEC);
        }
        if ((setBit & SET_VIDEO_FRAME_RATE) > 0) {
            recorder.setFrameRate(Params.VIDEO_FRAME_RATE);
        }
        if ((setBit & SET_VIDEO_BIT_RATE) > 0) {
            recorder.setVideoBitrate(Params.VIDEO_BIT_RATE);
        }
        if ((setBit & SET_VIDEO_QUALITY) > 0) {
            recorder.setVideoQuality(Params.VIDEO_QUALITY);
        }
        if ((setBit & SET_AUDIO_CODEC) > 0) {
            recorder.setAudioCodec(Params.AUDIO_CODEC);
        }
        if ((setBit & SET_AUDIO_CHANNEL) > 0) {
            recorder.setAudioChannels(Params.AUDIO_CHANNEL);
        }
        if ((setBit & SET_AUDIO_SAMPLE_RATE) > 0) {
            recorder.setSampleRate(Params.AUDIO_SAMPLE_RATE);
        }
        if ((setBit & SET_AUDIO_BIT_RATE) > 0) {
            recorder.setAudioBitrate(Params.AUDIO_BIT_RATE);
        }
    }
}
