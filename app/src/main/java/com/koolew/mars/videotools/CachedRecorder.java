package com.koolew.mars.videotools;

import android.util.Log;

import com.koolew.mars.AppProperty;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FrameRecorder;

/**
 * Created by jinchangzhu on 9/10/15.
 */
public abstract class CachedRecorder {

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

    protected String filePath;
    protected int width;
    protected int height;

    protected MyFFmpegFrameRecorder recorder;

    protected ImageRecordThread imageRecordThread;
    protected AudioRecordThread audioRecordThread;

    protected BlockingRecycleQueue<IplImageFrame> imageCache;
    protected BlockingRecycleQueue<SamplesFrame> audioCache;

    public CachedRecorder(String filePath, int width, int height) {
        this.filePath = filePath;
        this.width = width;
        this.height = height;

        recorder = new MyFFmpegFrameRecorder(filePath, width, height, 1);
        initRecorder();

        imageRecordThread = new ImageRecordThread();
        audioRecordThread = new AudioRecordThread();

        initCacheQueues();
    }

    protected void initRecorder() {
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

    protected abstract void initCacheQueues();

    public void start() {
        try {
            recorder.start();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Can not start recorder!");
        }

        imageRecordThread.start();
    }

    public void stopSynced() {
        imageCache.stop();
        audioRecordThread.start();
        audioCache.stop();
        try {
            imageRecordThread.join();
            audioRecordThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            recorder.stop();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    class ImageRecordThread extends Thread {
        @Override
        public void run() {
            while (true) {
                IplImageFrame imageFrame = imageCache.take();
                if (imageFrame == null) {
                    break;
                }
                try {
                    recorder.setTimestamp(imageFrame.getTimeStamp());
                    recorder.record(imageFrame.image);
                    imageCache.recycle(imageFrame);
                    Log.d("stdzhu", "record a image: " + imageFrame.getTimeStamp());
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class AudioRecordThread extends Thread {
        @Override
        public void run() {
            while (true) {
                SamplesFrame samplesFrame = audioCache.take();
                if (samplesFrame == null) {
                    break;
                }
                try {
                    recorder.record(samplesFrame.samples);
                    audioCache.recycle(samplesFrame);
                    Log.d("stdzhu", "record an audio");
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
