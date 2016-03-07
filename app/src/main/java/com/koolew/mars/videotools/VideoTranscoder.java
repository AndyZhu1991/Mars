package com.koolew.mars.videotools;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.nio.Buffer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by jinchangzhu on 9/15/15.
 */
public class VideoTranscoder {

    private FrameGrabber mFrameGrabber;
    private FrameRecorder mFrameRecorder;

    private String srcFile;
    private String dstFile;
    private int dstWidth;
    private int dstHeight;

    private OnlyCacheImageQueue imageQueue;
    private int grabedImageDepth;
    private int grabedImageChannels;

    private long videoLenMillis;

    private FrameRecordeThread frameRecordeThread;


    public VideoTranscoder(String srcFile, String dstFile, int dstWidth, int dstHeight) {
        this.srcFile = srcFile;
        this.dstFile = dstFile;
        this.dstWidth = dstWidth;
        this.dstHeight = dstHeight;

        imageQueue = new OnlyCacheImageQueue();
    }

    public void start() {
        int degree = Utils.getVideoDegree(srcFile);
        if (degree != 90 && degree != 180 && degree != 270) {
            degree = 0;
        }

        mFrameGrabber = new FFmpegFrameGrabber(srcFile);
        try {
            mFrameGrabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Start FrameGrabber failed!");
        }
        int srcWidth = mFrameGrabber.getImageWidth();
        int srcHeight = mFrameGrabber.getImageHeight();

        frameRecordeThread = new FrameRecordeThread();
        frameRecordeThread.start();


        boolean isNeedRotate = (degree == 90 || degree == 270);

        opencv_core.CvSize scaledSize;
        if (isNeedRotate) {
            scaledSize = Utils.getFrameScaleSize(dstHeight, dstWidth, srcWidth, srcHeight);
        }
        else {
            scaledSize = Utils.getFrameScaleSize(dstWidth, dstHeight, srcWidth, srcHeight);
        }
        boolean isNeedScale = !(scaledSize.width() == srcWidth && scaledSize.height() == srcHeight);

        int rotatedWidth;
        int rotatedHeight;
        if (isNeedRotate) {
            rotatedWidth = scaledSize.height();
            rotatedHeight = scaledSize.width();
        }
        else {
            rotatedWidth = scaledSize.width();
            rotatedHeight = scaledSize.height();
        }

        boolean isNeedFlipHorizontal = degree == 90;
        boolean isNeedFlipVertical = (degree == 270 || degree == 180);

        boolean isNeedBorder = !(dstWidth == rotatedWidth && dstHeight == rotatedHeight);
        opencv_core.CvPoint bordeOffset = null;
        if (isNeedBorder) {
            if (rotatedWidth < dstWidth) {
                bordeOffset = opencv_core.cvPoint((dstWidth - rotatedWidth) / 2, 0);
            }
            else {
                bordeOffset = opencv_core.cvPoint(0, (dstHeight - rotatedHeight) / 2);
            }
        }

        boolean imageCreated = false;
        IplImage scaledImage = null;
        IplImage rotatedImage = null;
        IplImage borderedImage = null;

        long curTimestamp;
        org.bytedeco.javacv.Frame grabedFrame;
        while (true) {
            try {
                curTimestamp = mFrameGrabber.getTimestamp();
                grabedFrame = mFrameGrabber.grabFrame();

                if (grabedFrame == null) {
                    imageQueue.stop();
                    break;
                }

                if (!imageCreated && grabedFrame.image != null) {
                    imageCreated = true;
                    grabedImageDepth = grabedFrame.image.depth();
                    grabedImageChannels = grabedFrame.image.nChannels();
                    if (isNeedScale) {
                        scaledImage = IplImage.create(scaledSize,
                                grabedImageDepth, grabedImageChannels);
                    }
                    if (isNeedRotate) {
                        rotatedImage = IplImage.create(rotatedWidth, rotatedHeight,
                                grabedImageDepth, grabedImageChannels);
                    }
                    if (isNeedBorder) {
                        borderedImage = IplImage.create(dstWidth, dstHeight,
                                grabedImageDepth, grabedImageChannels);
                    }
                }

                if (grabedFrame.image != null) {
                    IplImage currentImage = grabedFrame.image;
                    if (isNeedScale) {
                        opencv_imgproc.cvResize(currentImage, scaledImage);//, opencv_imgproc.CV_INTER_AREA);
                        currentImage = scaledImage;
                    }
                    if (isNeedRotate) {
                        opencv_core.cvTranspose(currentImage, rotatedImage);
                        currentImage = rotatedImage;
                    }
                    if (isNeedFlipHorizontal) {
                        opencv_core.cvFlip(currentImage, null, 1);
                    }
                    if (isNeedFlipVertical) {
                        opencv_core.cvFlip(currentImage, null, 0);
                    }
                    if (isNeedBorder) {
                        opencv_imgproc.cvCopyMakeBorder(currentImage, borderedImage,
                                bordeOffset, opencv_core.IPL_BORDER_CONSTANT);
                        currentImage = borderedImage;
                    }

                    IplImageFrame imageFrame = (IplImageFrame) imageQueue.obtain();
                    imageFrame.timeStamp = curTimestamp;
                    opencv_core.cvCopy(currentImage, imageFrame.image);
                    imageQueue.put(imageFrame);
                }
                else if (grabedFrame.samples != null) {
                    Buffer[] samples = new Buffer[grabedFrame.samples.length];
                    for (int i = 0; i < samples.length; i++) {
                        samples[i] = com.koolew.android.utils.Utils.bufferCopy(grabedFrame.samples[i]);
                    }
                    imageQueue.put(new SamplesFrame(grabedFrame.sampleRate,
                            grabedFrame.audioChannels, samples));
                }
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }

        if (scaledImage != null) {
            scaledImage.release();
        }
        if (rotatedImage != null) {
            rotatedImage.release();
        }
        if (borderedImage != null) {
            borderedImage.release();
        }

        try {
            frameRecordeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public long getVideoLenMillis() {
        return videoLenMillis;
    }

    class FrameRecordeThread extends Thread {
        @Override
        public void run() {
            mFrameRecorder = new MyFFmpegFrameRecorder(
                    dstFile, dstWidth, dstHeight, mFrameGrabber.getAudioChannels());
            FrameRecorderStandardSetter.defaultRecorderSet(mFrameRecorder,
                    FrameRecorderStandardSetter.SET_OUTPUT_FORMAT |
                            FrameRecorderStandardSetter.SET_VIDEO_CODEC |
                            FrameRecorderStandardSetter.SET_VIDEO_FRAME_RATE |
                            FrameRecorderStandardSetter.SET_VIDEO_BIT_RATE |
                            FrameRecorderStandardSetter.SET_AUDIO_CODEC |
                            FrameRecorderStandardSetter.SET_AUDIO_BIT_RATE);
            mFrameRecorder.setSampleRate(mFrameGrabber.getSampleRate());
            mFrameRecorder.setVideoOption("preset", "veryfast");
            try {
                mFrameRecorder.start();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }

            TimestampAdjuster adjuster = new TimestampAdjuster(mFrameRecorder.getFrameRate());
            long lastTimestamp = 0;
            while (true) {
                Frame frame = imageQueue.take();
                if (frame == null) {
                    break;
                }
                if (frame instanceof IplImageFrame) {
                    try {
                        long timestamp = adjuster.adjustTimestamp(frame.getTimeStamp());
                        if (timestamp != TimestampAdjuster.DROP_THIS_FRAME) {
                            mFrameRecorder.setTimestamp(timestamp);
                            mFrameRecorder.record(((IplImageFrame) frame).image);
                            lastTimestamp = timestamp;
                        }
                        imageQueue.recycle(frame);
                    } catch (FrameRecorder.Exception e) {
                        e.printStackTrace();
                    }
                }
                else /*if (frame instanceof SamplesFrame)*/ {
                    try {
                        mFrameRecorder.record(((SamplesFrame) frame).samples);
                    } catch (FrameRecorder.Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                mFrameRecorder.stop();
                videoLenMillis = lastTimestamp / 1000;
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }

            imageQueue.releaseAllCachedItem();
        }
    }

    class OnlyCacheImageQueue extends BlockingRecycleQueue<Frame> {

        private Queue<Frame> tempQueue;
        private Frame curFrame;
        private Frame nextFrame;

        public OnlyCacheImageQueue() {
            super(16);
            tempQueue = new LinkedList<>();
        }

        @Override
        public void put(Frame frame) {
            if (frame instanceof IplImageFrame) {
                if (curFrame == null) {
                    curFrame = frame;
                }
                else {
                    nextFrame = frame;
                    if (curFrame.timeStamp < nextFrame.timeStamp) {
                        tempQueue.offer(curFrame);
                        putAnImageFrameToWorkQueue();
                    }
                    curFrame = nextFrame;
                    nextFrame = null;
                }
            }
            else {
                tempQueue.offer(frame);
            }
        }

        private void putAnImageFrameToWorkQueue() {
            while (true) {
                Frame frame = tempQueue.poll();
                if (frame == null) {
                    break;
                }
                super.put(frame);
                if (frame instanceof IplImageFrame) {
                    break;
                }
            }
        }

        private void putAllFrameToWorkQueue() {
            while (true) {
                Frame frame = tempQueue.poll();
                if (frame == null) {
                    break;
                }
                super.put(frame);
            }
        }

        @Override
        public void stop() {
            putAllFrameToWorkQueue();
            super.stop();
        }

        @Override
        public void recycle(Frame frame) {
            if (frame instanceof IplImageFrame) {
                super.recycle(frame);
            }
        }

        @Override
        protected Frame generateNewFrame() {
            return new IplImageFrame(IplImage.create(dstWidth, dstHeight,
                    grabedImageDepth, grabedImageChannels), 0);
        }

        @Override
        protected void releaseOneItem(Frame item) {
            ((IplImageFrame) item).image.release();
        }
    }
}
