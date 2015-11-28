package com.koolew.mars.videotools;

import android.content.Context;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_highgui;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static com.koolew.mars.videotools.Params.AUDIO_BIT_RATE;
import static com.koolew.mars.videotools.Params.AUDIO_CODEC;
import static com.koolew.mars.videotools.Params.AUDIO_SAMPLE_RATE;
import static com.koolew.mars.videotools.Params.OUTPUT_FORMAT;
import static com.koolew.mars.videotools.Params.VIDEO_BIT_RATE;
import static com.koolew.mars.videotools.Params.VIDEO_CODEC;
import static com.koolew.mars.videotools.Params.VIDEO_FRAME_RATE;

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
        recorder.setFormat(OUTPUT_FORMAT);
        recorder.setVideoCodec(VIDEO_CODEC);
        recorder.setAudioCodec(AUDIO_CODEC);
        recorder.setFrameRate(VIDEO_FRAME_RATE);//grabber.getFrameRate());
        recorder.setSampleRate(AUDIO_SAMPLE_RATE);//grabber.getSampleRate());
        recorder.setVideoBitrate(VIDEO_BIT_RATE);
        recorder.setAudioBitrate(AUDIO_BIT_RATE);
        recorder.setVideoOption("preset", "veryfast");

        try {
            recorder.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }

        try {
            if (startPosition > 0) {
                grabber.setTimestamp(startPosition * 1000);
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }

        int imageFrameCount = 0;
        while (true) {
            //Log.d("stdzhu", "real timestamp: " + getRealTimeStamp(grabber));
            long timestamp = getRealTimeStamp(grabber, imageFrameCount);
            if (timestamp > endPosition * 1000) {
                break;
            }
            try {
                org.bytedeco.javacv.Frame frame = grabber.grabFrame();
                if (frame == null) {
                    break;
                }
                if (frame.image != null) {
                    imageFrameCount++;
                    recorder.setTimestamp(timestamp);
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

    private static long getRealTimeStamp(FrameGrabber grabber, int frameCount) {
        double frameRate = grabber.getFrameRate();
        return (long) (frameCount * (1000000 / frameRate));
    }

    private static long adjustTimestamp(long timestamp, boolean useBeforeTimestamp) {
        return adjustTimestamp(VIDEO_FRAME_RATE, timestamp, useBeforeTimestamp);
    }

    public static long adjustTimestamp(int frameRate, long timestamp, boolean useBeforeTimestamp) {
        long nanoSecondPerFrame = 1000000 / frameRate;
        return timestamp / nanoSecondPerFrame * nanoSecondPerFrame
                + (useBeforeTimestamp ? 0 : nanoSecondPerFrame);
    }

    public static void splitVideoByFrame(String originPath, int[] splitFrame, String[] dstFiles) {
        if (splitFrame.length != dstFiles.length) {
            throw new RuntimeException("params error");
        }

        FrameGrabber grabber = new FFmpegFrameGrabber(originPath);
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        int imageFrameIndex = 0;
        Log.d("stdzhu", "frame rate: " + grabber.getFrameRate() + ", frameCount: " + grabber.getLengthInFrames());
        for (int i = 0; i < dstFiles.length; i++) {
            String dstFile = dstFiles[i];
            MyFFmpegFrameRecorder recorder = new MyFFmpegFrameRecorder(dstFile,
                    grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
            recorder.setFormat(OUTPUT_FORMAT);
            recorder.setVideoCodec(VIDEO_CODEC);
            recorder.setAudioCodec(AUDIO_CODEC);
            recorder.setFrameRate(VIDEO_FRAME_RATE);//grabber.getFrameRate());
            recorder.setSampleRate(AUDIO_SAMPLE_RATE);//grabber.getSampleRate());
            recorder.setVideoBitrate(VIDEO_BIT_RATE);
            recorder.setAudioBitrate(AUDIO_BIT_RATE);
            recorder.setVideoOption("preset", "veryfast");
            try {
                recorder.start();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }

            int endFrame = splitFrame[i];
            while (imageFrameIndex < endFrame) {
                try {
                    org.bytedeco.javacv.Frame frame = grabber.grabFrame();
                    if (frame == null) {
                        break;
                    }
                    Log.d("stdzhu", "timestamp: " + getRealTimeStamp(grabber, imageFrameIndex));
                    if (frame.image != null) {
                        Log.d("stdzhu", "image: " + imageFrameIndex);
                        imageFrameIndex++;
                        recorder.record(frame);
                    }
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                recorder.stop();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }

        try {
            grabber.stop();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
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

    public static void saveVideoFrame(String videoPath, String frameImagePath) {
        FrameGrabber frameGrabber = new FFmpegFrameGrabber(videoPath);
        try {
            frameGrabber.start();
            opencv_core.IplImage image = frameGrabber.grab();
            opencv_highgui.cvSaveImage(frameImagePath, image);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }
}
