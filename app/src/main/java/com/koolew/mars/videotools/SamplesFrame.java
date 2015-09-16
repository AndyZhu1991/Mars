package com.koolew.mars.videotools;

import java.nio.Buffer;

/**
 * Created by jinchangzhu on 9/10/15.
 */
public class SamplesFrame extends Frame {

    public int sampleRate;
    public int audioChannels;
    public Buffer[] samples;

    public SamplesFrame(int sampleRate, int audioChannels, Buffer... samples) {
        this.sampleRate = sampleRate;
        this.audioChannels = audioChannels;
        this.samples = samples;
    }
}
