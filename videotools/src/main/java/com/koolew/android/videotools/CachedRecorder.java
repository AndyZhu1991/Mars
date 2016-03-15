package com.koolew.android.videotools;

import android.util.Log;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FrameRecorder;

/**
 * Created by jinchangzhu on 9/10/15.
 */
public abstract class CachedRecorder {


    protected String filePath;
    protected int width;
    protected int height;

    protected MyFFmpegFrameRecorder recorder;

    protected long firstFrameTimeStamp = -1;
    protected long lastFrameTimeStamp = 0;

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
        recorder.setFormat(Params.OUTPUT_FORMAT);
        recorder.setSampleRate(Params.AUDIO_SAMPLE_RATE);
        recorder.setFrameRate(Params.VIDEO_FRAME_RATE);
        recorder.setVideoCodec(Params.VIDEO_CODEC);
        recorder.setAudioCodec(Params.AUDIO_CODEC);
        recorder.setVideoBitrate(Params.VIDEO_BIT_RATE);
        recorder.setAudioBitrate(Params.AUDIO_BIT_RATE);

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

    public String getFilePath() {
        return filePath;
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

    /**
     *
     * @param image
     * @param timestamp
     * @return If recorder accepted the frame
     */
    public boolean putImage(opencv_core.IplImage image, long timestamp) {
        IplImageFrame imageFrame = imageCache.obtain();
        opencv_core.cvCopy(image, imageFrame.image);
        return putInner(imageFrame, timestamp);
    }

    /**
     *
     * @param imageData
     * @param timeStamp usec
     * @return If recorder accepted the frame
     */
    public boolean putImage(byte[] imageData, long timeStamp) {
        IplImageFrame imageFrame = imageCache.obtain();
        imageFrame.image.getByteBuffer().put(imageData);
        return putInner(imageFrame, timeStamp);
    }

    private boolean putInner(IplImageFrame imageFrame, long timeStamp) {
        timeStamp = adjustTimeStamp(timeStamp);

        if (firstFrameTimeStamp < 0) {
            firstFrameTimeStamp = timeStamp;
        }

        if (timeStamp == lastFrameTimeStamp) {
            return false;
        }
        else {
            lastFrameTimeStamp = timeStamp;
        }

        imageFrame.timeStamp = timeStamp - firstFrameTimeStamp;

        imageCache.put(imageFrame);

        return true;
    }

    private long adjustTimeStamp(long timeStamp) {
        long framePerUsec = 1000000 / Params.VIDEO_FRAME_RATE;
        return timeStamp / framePerUsec * framePerUsec;
    }

    public static final long FRAME_PER_USEC = 1000000 / Params.VIDEO_FRAME_RATE;
    private static long adjustTimestamp(long timestamp) {
        return timestamp / FRAME_PER_USEC * FRAME_PER_USEC;
    }

    class ImageRecordThread extends Thread {
        private long timestamp = -FRAME_PER_USEC;

        @Override
        public void run() {
            while (true) {
                IplImageFrame imageFrame = imageCache.take();
                if (imageFrame == null) {
                    break;
                }
                try {
                    long adjustedTimestamp = adjustTimestamp(imageFrame.getTimeStamp());
                    while (adjustedTimestamp > timestamp) {
                        timestamp += FRAME_PER_USEC;
                        recorder.setTimestamp(timestamp);
                        Log.d("stdzhu", "record a frame, timestamp: " + timestamp);
                        recorder.record(processImage(imageFrame.image));
                    }
                    imageCache.recycle(imageFrame);
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected opencv_core.IplImage processImage(opencv_core.IplImage originImage) {
        return originImage;
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
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public abstract static class ImageRecycleQueue extends BlockingRecycleQueue<IplImageFrame> {

        protected int width;
        protected int height;

        public ImageRecycleQueue(int maxItemCount, int width, int height) {
            super(maxItemCount);
            this.width = width;
            this.height = height;
        }

        @Override
        protected abstract IplImageFrame generateNewFrame();

        @Override
        public void recycle(IplImageFrame frame) {
            frame.image.getByteBuffer().clear();
            super.recycle(frame);
        }

        @Override
        protected void releaseOneItem(IplImageFrame item) {
            item.image.release();
        }
    }

    public static class YUV420RecycleQueue extends ImageRecycleQueue {

        public YUV420RecycleQueue(int maxItemCount, int width, int height) {
            super(maxItemCount, width, height);
        }

        @Override
        protected IplImageFrame generateNewFrame() {
            return new IplImageFrame(opencv_core.IplImage.create(width, height,
                    opencv_core.IPL_DEPTH_8U, 2), 0l);
        }
    }

    public static class RGBARecycleQueue extends ImageRecycleQueue {

        public RGBARecycleQueue(int maxItemCount, int width, int height) {
            super(maxItemCount, width, height);
        }

        @Override
        protected IplImageFrame generateNewFrame() {
            return new IplImageFrame(opencv_core.IplImage.create(width, height,
                    opencv_core.IPL_DEPTH_8U, 4), 0l);
        }
    }
}
