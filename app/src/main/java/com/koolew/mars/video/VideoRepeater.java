package com.koolew.mars.video;

import com.koolew.mars.utils.Mp4ParserUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by jinchangzhu on 8/18/15.
 */
public class VideoRepeater {

    private static final double TARGET_VIDEO_LEN = 6.0; // In second

    private static final String REPEATED_DIR_NAME = "repeated";


    public static boolean isNeedRepeat(String videoPath) {
        try {
            if (Mp4ParserUtil.getDuration(videoPath) < TARGET_VIDEO_LEN) {
                return true;
            }
            else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void repeatVideo(String videoPath) {

        try {
            double videoLen = Mp4ParserUtil.getDuration(videoPath);
            int videoRepeatCount = ((int) (TARGET_VIDEO_LEN / videoLen)) + 1;

            File videoFile = new File(videoPath);
            String parent = videoFile.getParent();
            String finalRepeatedVideoPath = getFinalRepeatedVideoPath(videoPath);

            if (!new File(finalRepeatedVideoPath).exists()) {
                File repeatedDir = new File(parent + File.separator + REPEATED_DIR_NAME);
                if (repeatedDir.isFile()) {
                    repeatedDir.delete();
                }
                if (!repeatedDir.exists()) {
                    repeatedDir.mkdir();
                }
                String tempRepeatedVideoPath = parent + "/repeat_temp.mp4";
                // 这里多cat一段视频是因为后面的clip一定会剪掉最后一段关键帧
                Mp4ParserUtil.mp4Repeat(videoPath, videoRepeatCount + 1, tempRepeatedVideoPath);
                // 这里clip是因为cat出来的视频最后会莫名其妙的多出一段东西
                Mp4ParserUtil.clip(tempRepeatedVideoPath, 0.0, videoLen * videoRepeatCount,
                        finalRepeatedVideoPath);
                new File(tempRepeatedVideoPath).delete();
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static String getFinalRepeatedVideoPath(String originVideoPath) {
        File originVideoFile = new File(originVideoPath);
        return new StringBuilder(originVideoFile.getParent())
                .append(File.separator)
                .append(REPEATED_DIR_NAME)
                .append(File.separator)
                .append(originVideoFile.getName())
                .toString();
    }

    public static int getVideoRealDuration(String videoPath) {
        if (isRepeatedVideo(videoPath)) {
            videoPath = getOriginVideoPath(videoPath);
        }
        try {
            return (int) (Mp4ParserUtil.getDuration(videoPath) * 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean isRepeatedVideo(String videoPath) {
        File videoFile = new File(videoPath);
        if (videoFile.getParent().endsWith(File.separator + REPEATED_DIR_NAME)) {
            return true;
        }
        else {
            return false;
        }
    }

    public static String getOriginVideoPath(String repeatedVideoPath) {
        File repeatedVideoFile = new File(repeatedVideoPath);
        File repeatedVideoDir = repeatedVideoFile.getParentFile();
        String originVideoDir = repeatedVideoDir.getParent();
        String originVideoPath = originVideoDir + File.separator + repeatedVideoFile.getName();
        return originVideoPath;
    }
}
