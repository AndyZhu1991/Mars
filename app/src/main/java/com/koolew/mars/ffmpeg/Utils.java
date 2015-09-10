package com.koolew.mars.ffmpeg;

import android.content.Context;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;

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
}
