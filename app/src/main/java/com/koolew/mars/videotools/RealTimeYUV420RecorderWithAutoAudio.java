package com.koolew.mars.videotools;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.koolew.mars.view.RecordingSessionView;

import org.bytedeco.javacpp.opencv_core;

import java.nio.Buffer;
import java.nio.ShortBuffer;

/**
 * Created by jinchangzhu on 9/10/15.
 */
public class RealTimeYUV420RecorderWithAutoAudio extends CachedRecorder
        implements RecordingSessionView.RecordingItem {

    private boolean isEncoding = false;
    private long firstFrameTimeStamp = -1;
    private long lastFrameTimeStamp = 0;

    private AudioDataFillThread audioDataFillThread;

    public RealTimeYUV420RecorderWithAutoAudio(String filePath, int width, int height) {
        super(filePath, width, height);

        audioDataFillThread = new AudioDataFillThread();
    }

    @Override
    protected void initCacheQueues() {
        imageCache = new YUV420RecycleQueue(200);
        audioCache = new AudioBufferRecycleQueue(2000);
    }

    /**
     *
     * @param YUV420Data
     * @param timeStamp Nano time !
     */
    public void put(byte[] YUV420Data, long timeStamp) {
        timeStamp = adjustTimeStamp(timeStamp);

        if (firstFrameTimeStamp < 0) {
            firstFrameTimeStamp = timeStamp;
        }

        if (timeStamp == lastFrameTimeStamp) {
            return;
        }
        else {
            lastFrameTimeStamp = timeStamp;
        }

        IplImageFrame imageFrame = imageCache.obtain();
        imageFrame.image.getByteBuffer().put(YUV420Data);
        imageFrame.timeStamp = timeStamp - firstFrameTimeStamp;

        imageCache.put(imageFrame);
        Log.d("stdzhu", "put yuv, time stamp: " + imageFrame.getTimeStamp());
    }

    private long adjustTimeStamp(long timeStamp) {
        long framePerNano = 1000000 / VIDEO_FRAME_RATE;
        return timeStamp / framePerNano * framePerNano;
    }

    @Override
    public void start() {
        isEncoding = true;
        audioDataFillThread.start();
        super.start();
    }

    @Override
    public void stopSynced() {
        isEncoding = false;
        super.stopSynced();
    }

    @Override
    public long getCurrentLength() {
        return (lastFrameTimeStamp - firstFrameTimeStamp) / 1000;
    }

    @Override
    public RecordingSessionView.VideoPieceItem completeSynced() {
        stopSynced();
        return new RecordingSessionView.VideoPieceItem(
                System.currentTimeMillis(), filePath, getCurrentLength());
    }

    class YUV420RecycleQueue extends BlockingRecycleQueue<IplImageFrame> {

        public YUV420RecycleQueue(int maxItemCount) {
            super(maxItemCount);
        }

        @Override
        protected IplImageFrame generateNewFrame() {
            return new IplImageFrame(opencv_core.IplImage.create(width, height,
                    opencv_core.IPL_DEPTH_8U, 2), 0l);
        }

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

    public static int audioBufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    class AudioBufferRecycleQueue extends BlockingRecycleQueue<SamplesFrame> {

        public AudioBufferRecycleQueue(int maxItemCount) {
            super(maxItemCount);
        }

        @Override
        protected SamplesFrame generateNewFrame() {
            return new SamplesFrame(AUDIO_SAMPLE_RATE, 1, ShortBuffer.allocate(audioBufferSize));
        }

        @Override
        public void recycle(SamplesFrame frame) {
            for (Buffer buffer: frame.samples) {
                buffer.clear();
            }
            super.recycle(frame);
        }
    }

    class AudioDataFillThread extends Thread {
        private AudioRecord audioRecord;

        public AudioDataFillThread() {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, audioBufferSize);
            audioRecord.startRecording();
        }

        @Override
        public void run() {
            while (isEncoding) {
                if (firstFrameTimeStamp > 0) {
                    SamplesFrame samplesFrame = audioCache.obtain();
                    ShortBuffer buffer = (ShortBuffer) samplesFrame.samples[0];
                    int readedShort = audioRecord.read(buffer.array(), 0, buffer.capacity());
                    buffer.limit(readedShort);
                    if (readedShort > 0) {
                        audioCache.put(samplesFrame);
                    }
                    else {
                        audioCache.recycle(samplesFrame);
                    }
                    Log.d("stdzhu", "put an audio");
                }
                else {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            audioRecord.stop();
            audioRecord.release();
        }
    }
}
