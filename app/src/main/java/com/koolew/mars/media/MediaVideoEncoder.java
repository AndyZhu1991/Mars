package com.koolew.mars.media;
/*
 * AudioVideoRecordingSample
 * Sample project to cature audio and video from internal mic/camera and save as MPEG4 file.
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: MediaVideoEncoder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
*/

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import com.koolew.mars.utils.RawImageUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;

public class MediaVideoEncoder extends MediaEncoder {
	private static final boolean DEBUG = true;	// TODO set false on release
	private static final String TAG = "MediaVideoEncoder";

	private static final int FRAME_QUEUE_SIZE = 32;
	private static final String MIME_TYPE = "video/avc";
	// parameters for recording
    private static final int FRAME_RATE = 25;
    private static final float BPP = 0.2f;

    private final int mWidth;
    private final int mHeight;

	private int mColorFormat;

	private VideoThread mVideoThread;
	private YUV420SPFramePool mFramePool = new YUV420SPFramePool();
	private ArrayBlockingQueue<YUV420SPFrame> mFrameQueue = new ArrayBlockingQueue<YUV420SPFrame>(FRAME_QUEUE_SIZE);


	public MediaVideoEncoder(final MediaMuxerWrapper muxer, final MediaEncoderListener listener, final int width, final int height) {
		super(muxer, listener);
		if (DEBUG) Log.i(TAG, "MediaVideoEncoder: ");
		mWidth = width;
		mHeight = height;
	}

	public YUV420SPFrame obtainFrame() {
		return mFramePool.obtainFrame();
	}

	public static class YUV420SPFrame {
		public byte[] data;
		public long frameNanoTime;

		private YUV420SPFrame(byte[] data, long frameNanoTime) {
			this.data = data;
			this.frameNanoTime = frameNanoTime;
		}
	}

	public class YUV420SPFramePool {
		private Stack<YUV420SPFrame> mStack;

		public YUV420SPFramePool() {
			mStack = new Stack<YUV420SPFrame>();
		}

		public YUV420SPFrame obtainFrame() {
			synchronized (this) {
				if (mStack.isEmpty()) {
					return new YUV420SPFrame(new byte[mWidth * mHeight * 3 / 2], 0l);
				} else {
					return mStack.pop();
				}
			}
		}

		public void returnFrame(YUV420SPFrame frame) {
			synchronized (this) {
				mStack.push(frame);
			}
		}
	}

	public void putYUV420SPFrame(YUV420SPFrame frame) {
		try {
			frame.frameNanoTime = getPTSUs();
			if (mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
				RawImageUtil.YUV420SPtoYUV420P(frame.data, mWidth, mHeight);
			}
			mFrameQueue.put(frame);
		}
		catch (InterruptedException ie) {
			Log.d(TAG, "Stack put interrupyed");
		}
	}

	@Override
	void startRecording() {
		super.startRecording();
		if (mVideoThread == null) {
			mVideoThread = new VideoThread();
			mVideoThread.start();
		}
	}

	/**
	 * Thread to capture audio data from internal mic as uncompressed 16bit PCM data
	 * and write them to the MediaCodec encoder
	 */
	private class VideoThread extends Thread {
		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			try {
                if (mIsCapturing) {
                    if (DEBUG) Log.v(TAG, "AudioThread:start audio recording");
                    final ByteBuffer buf = ByteBuffer.allocateDirect(mWidth * mHeight * 3 / 2);
                    for (; mIsCapturing && !mRequestStop && !mIsEOS; ) {
                        // read audio data from internal mic
                        YUV420SPFrame frame = mFrameQueue.take();
                        //readBytes = audioRecord.read(buf, SAMPLES_PER_FRAME);
                        if (frame != null && frame.data != null && frame.data.length != 0) {
                            buf.clear();
                            buf.put(frame.data, 0, mWidth * mHeight * 3 / 2);
                            // set audio data to encoder
                            buf.position(mWidth * mHeight * 3 / 2);
                            buf.flip();
                            encode(buf, mWidth * mHeight * 3 / 2, frame.frameNanoTime);
                            frameAvailableSoon();
                        }
                        mFramePool.returnFrame(frame);
                    }
                    frameAvailableSoon();
                    Log.d(TAG, "Frame pool use: " + mFramePool.mStack.size() + ", max: " + FRAME_QUEUE_SIZE);
                }

            } catch (final Exception e) {
                Log.e(TAG, "AudioThread#run", e);
            }
            if (DEBUG) Log.v(TAG, "AudioThread:finished");
		}
	}


	@Override
	protected void prepare() throws IOException {
		if (DEBUG) Log.i(TAG, "prepare: ");
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;

        final MediaCodecInfo videoCodecInfo = selectVideoCodec(MIME_TYPE);
        if (videoCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
		if (DEBUG) Log.i(TAG, "selected codec: " + videoCodecInfo.getName());

		mColorFormat = selectColorFormat(videoCodecInfo, MIME_TYPE);

        final MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);	// API >= 18
        format.setInteger(MediaFormat.KEY_BIT_RATE, calcBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
		if (DEBUG) Log.i(TAG, "format: " + format);

        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // get Surface for encoder input
        // this method only can call between #configure and #start
        mMediaCodec.start();
        if (DEBUG) Log.i(TAG, "prepare finishing");
        if (mListener != null) {
        	try {
        		mListener.onPrepared(this);
        	} catch (final Exception e) {
        		Log.e(TAG, "prepare:", e);
        	}
        }
	}

	@Override
    protected void release() {
		if (DEBUG) Log.i(TAG, "release:");
		super.release();
	}

	private int calcBitRate() {
		final int bitrate = (int)(BPP * FRAME_RATE * mWidth * mHeight);
		Log.i(TAG, String.format("bitrate=%5.2f[Mbps]", bitrate / 1024f / 1024f));
		return bitrate;
	}

    /**
     * select the first codec that match a specific MIME type
     * @param mimeType
     * @return null if no codec matched
     */
    protected static final MediaCodecInfo selectVideoCodec(final String mimeType) {
    	if (DEBUG) Log.v(TAG, "selectVideoCodec:");

    	// get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
        	final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {	// skipp decoder
                continue;
            }
            // select first codec that match a specific MIME type and color format
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                	if (DEBUG) Log.i(TAG, "codec:" + codecInfo.getName() + ",MIME=" + types[j]);
            		final int format = selectColorFormat(codecInfo, mimeType);
                	if (format > 0) {
                		return codecInfo;
                	}
                }
            }
        }
        return null;
    }

    /**
     * select color format available on specific codec and we can use.
     * @return 0 if no colorFormat is matched
     */
    protected static final int selectColorFormat(final MediaCodecInfo codecInfo, final String mimeType) {
		if (DEBUG) Log.i(TAG, "selectColorFormat: ");
    	int result = 0;
    	final MediaCodecInfo.CodecCapabilities caps;
    	try {
    		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    		caps = codecInfo.getCapabilitiesForType(mimeType);
    	} finally {
    		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    	}
        int colorFormat;
        for (int i = 0; i < caps.colorFormats.length; i++) {
        	colorFormat = caps.colorFormats[i];
            if (isRecognizedViewoFormat(colorFormat)) {
            	if (result == 0)
            		result = colorFormat;
                break;
            }
        }
        if (result == 0)
        	Log.e(TAG, "couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return result;
    }

	/**
	 * color formats that we can use in this class
	 */
    protected static int[] recognizedFormats;
	static {
		recognizedFormats = new int[] {
				MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
				MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
//        	    MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar,
//        	    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
		};
	}

    private static final boolean isRecognizedViewoFormat(final int colorFormat) {
		if (DEBUG) Log.i(TAG, "isRecognizedViewoFormat:colorFormat=" + colorFormat);
    	final int n = recognizedFormats != null ? recognizedFormats.length : 0;
    	for (int i = 0; i < n; i++) {
    		if (recognizedFormats[i] == colorFormat) {
    			return true;
    		}
    	}
    	return false;
    }

    @Override
    protected void signalEndOfInputStream() {
		if (DEBUG) Log.d(TAG, "sending EOS to encoder");
		//mMediaCodec.signalEndOfInputStream();	// API >= 18
		//mIsEOS = true;
		super.signalEndOfInputStream();
	}

}
