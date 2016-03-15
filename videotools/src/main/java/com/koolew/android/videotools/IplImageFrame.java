package com.koolew.android.videotools;

import org.bytedeco.javacpp.opencv_core;

/**
 * Created by jinchangzhu on 9/10/15.
 */
public class IplImageFrame extends Frame {
    public opencv_core.IplImage image;

    public IplImageFrame(opencv_core.IplImage iplImage, long timeStampt) {
        this.image = iplImage;
        this.timeStamp = timeStampt;
    }
}
