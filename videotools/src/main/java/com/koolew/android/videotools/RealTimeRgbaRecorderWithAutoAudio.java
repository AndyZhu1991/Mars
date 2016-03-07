package com.koolew.android.videotools;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.Buffer;
import java.nio.ShortBuffer;

import static com.koolew.android.videotools.Params.AUDIO_SAMPLE_RATE;

/**
 * Created by jinchangzhu on 9/10/15.
 */
public class RealTimeRgbaRecorderWithAutoAudio extends CachedRecorder {

    private boolean isEncoding = false;

    private AudioDataFillThread audioDataFillThread;

    public RealTimeRgbaRecorderWithAutoAudio(String filePath, int width, int height) {
        super(filePath, width, height);

        audioDataFillThread = new AudioDataFillThread();
    }

    @Override
    protected void initCacheQueues() {
        imageCache = new RGBARecycleQueue(100, width, height);
        audioCache = new AudioBufferRecycleQueue(2000);
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
                    if (readedShort > 0) {
                        buffer.limit(readedShort);
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
