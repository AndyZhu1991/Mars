package com.koolew.mars.videotools;

import android.content.Context;
import android.util.Log;

import com.koolew.mars.AppProperty;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by jinchangzhu on 9/10/15.
 */
public class Utils {

    public static void preloadRecorder(final Context context) {
        new Thread() {
            @Override
            public void run() {
                File temp = new File(com.koolew.mars.utils.Utils.getCacheDir(context)
                        + File.separator + "init.mp4");
                MyFFmpegFrameRecorder recorder = new MyFFmpegFrameRecorder(temp, 480, 360, 1);
                try {
                    recorder.start();
                    recorder.stop();
                    temp.delete();
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
                new IplImageFrame(opencv_core.IplImage.create(
                        480, 360, opencv_core.IPL_DEPTH_8U, 2), 0l);
            }
        }.start();
    }

    public static void cutVideo(String srcFile, String dstFile,
                                long startPosition, long endPosition) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(srcFile);
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        MyFFmpegFrameRecorder recorder = new MyFFmpegFrameRecorder(dstFile,
                grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
        recorder.setFormat("mp4");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(VIDEO_FRAME_RATE);//grabber.getFrameRate());
        recorder.setSampleRate(44100);//grabber.getSampleRate());
        recorder.setVideoBitrate(500000);
        recorder.setAudioBitrate(96000);
        recorder.setVideoOption("preset", "veryfast");

        try {
            recorder.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }

        try {
            if (startPosition > 0) {
                Log.d("stdzhu", "set timestamp: " + (startPosition * 1000));
                grabber.setTimestamp(startPosition * 1000);
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }

        long firstFrameTimestamp = -1;
        long lastAdjustedTimestamp = -1;
        while (true) {
            long timeStamp = grabber.getTimestamp();
            if (timeStamp >= endPosition * 1000) {
                break;
            }
            try {
                long timestamp = grabber.getTimestamp();
                Log.d("stdzhu", "before grab: " + grabber.getTimestamp());
                org.bytedeco.javacv.Frame frame = grabber.grabFrame();
                Log.d("stdzhu", "after grab " + (frame.image != null ? "image: " : "audio: ") + grabber.getTimestamp());
                if (frame == null) {
                    break;
                }
                if (frame.image != null) {
                    if (firstFrameTimestamp == -1) {
                        firstFrameTimestamp = timestamp;
                    }
                    long adjustedTimestamp = adjustTimestamp(timestamp - firstFrameTimestamp);
                    if (adjustedTimestamp > lastAdjustedTimestamp) {
                        recorder.setTimestamp(adjustedTimestamp);
                    }
                    else if (adjustedTimestamp == lastAdjustedTimestamp) {
                        adjustedTimestamp = adjustTimestamp(timestamp - firstFrameTimestamp, false);
                        recorder.setTimestamp(adjustedTimestamp);
                    }
                    else {
                        continue;
                    }
                    lastAdjustedTimestamp = adjustedTimestamp;
                }
                recorder.record(frame);
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }

        try {
            grabber.stop();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        try {
            recorder.stop();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    private static long adjustTimestamp(long timeStamp) {
        return adjustTimestamp(timeStamp, true);
    }

    private static long adjustTimestamp(long timestamp, boolean useBeforeTimestamp) {
        return adjustTimestamp(VIDEO_FRAME_RATE, timestamp, useBeforeTimestamp);
    }

    public static long adjustTimestamp(int frameRate, long timestamp, boolean useBeforeTimestamp) {
        long nanoSecondPerFrame = 1000000 / frameRate;
        return timestamp / nanoSecondPerFrame * nanoSecondPerFrame
                + (useBeforeTimestamp ? 0 : nanoSecondPerFrame);
    }

    public final static int VIDEO_CODEC = avcodec.AV_CODEC_ID_H264;
    public final static int VIDEO_FRAME_RATE = AppProperty.RECORD_VIDEO_FPS;
    public final static int VIDEO_QUALITY = 12;
    public final static int AUDIO_CODEC = avcodec.AV_CODEC_ID_AAC;
    public final static int AUDIO_CHANNEL = 1;
    public final static int AUDIO_BIT_RATE = 96000;
    // public final static int VIDEO_BIT_RATE = 1000000;
    public final static int VIDEO_BIT_RATE = 500000;
    public final static int AUDIO_SAMPLE_RATE = 44100;
    public final static String OUTPUT_FORMAT = "mp4";

    private static void initRecorder(MyFFmpegFrameRecorder recorder) {
        //mFFmpegFrameRecorder.setInterleaved(true);
        recorder.setFormat(OUTPUT_FORMAT);
        recorder.setSampleRate(AUDIO_SAMPLE_RATE);
        recorder.setFrameRate(VIDEO_FRAME_RATE);
        recorder.setVideoCodec(VIDEO_CODEC);
        //recorder.setVideoQuality(VIDEO_QUALITY);
        //recorder.setVideoBitrate(500000);
//        recorder.setAudioQuality(VIDEO_QUALITY);
//        recorder.setAudioCodec(AUDIO_CODEC);
//        recorder.setVideoBitrate(VIDEO_BIT_RATE);
//        recorder.setAudioBitrate(AUDIO_BIT_RATE);

        //recorder.setVideoQuality(VIDEO_QUALITY);
        //recorder.setAudioQuality(VIDEO_QUALITY);
        recorder.setAudioCodec(AUDIO_CODEC);
        recorder.setVideoBitrate(VIDEO_BIT_RATE);
        recorder.setAudioBitrate(AUDIO_BIT_RATE);

        // tradeoff between quality and encode speed
        // possible values are ultrafast,superfast, veryfast, faster, fast,
        // medium, slow, slower, veryslow
        // ultrafast offers us the least amount of compression (lower encoder
        // CPU) at the cost of a larger stream size
        // at the other end, veryslow provides the best compression (high
        // encoder CPU) while lowering the stream size
        // (see: https://trac.ffmpeg.org/wiki/Encode/H.264)
        recorder.setVideoOption("preset", "veryfast");
    }

    public static int getVideoDegree(String videoPath) {
        FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
        fmmr.setDataSource(videoPath);
        String rotation = fmmr.extractMetadata(
                FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        fmmr.release();
        int degree;
        try {
            degree = Integer.valueOf(rotation);
        }
        catch (NumberFormatException nfe) {
            degree = 0;
        }
        return degree;
    }

    /**
     * 将一个矩形缩放到可以容纳在框架中的最大尺寸
     * @param frameWidth 框架宽度
     * @param frameHeight 框架高度
     * @param srcWidth 原矩形宽度
     * @param srcHeight 原矩形高度
     * @return 缩放之后的矩形尺寸
     */
    public static opencv_core.CvSize getFrameScaleSize(int frameWidth, int frameHeight,
                                                       int srcWidth, int srcHeight) {
        int scaledWidth;
        int scaledHeight;

        if (frameWidth * srcHeight == frameHeight * srcWidth) {
            scaledWidth = frameWidth;
            scaledHeight = frameHeight;
        }
        else if (frameWidth * srcHeight < srcWidth * frameHeight) {
            scaledWidth = frameWidth;
            scaledHeight = scaledWidth * srcHeight / srcWidth;
        }
        else {
            scaledHeight = frameHeight;
            scaledWidth = scaledHeight * srcWidth / srcHeight;
        }

        return opencv_core.cvSize(scaledWidth, scaledHeight);
    }
}
