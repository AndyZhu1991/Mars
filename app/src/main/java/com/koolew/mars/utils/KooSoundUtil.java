package com.koolew.mars.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 12/5/15.
 */
public class KooSoundUtil {

    private static SoundPool mSoundPool;
    private static int mKooSound;

    public static void init(Context context) {
        mSoundPool = new SoundPool(5, AudioManager.STREAM_RING, 0);
        mKooSound = mSoundPool.load(context, R.raw.koo, 1);
    }

    public static void playKooSound() {
        if (mSoundPool != null) {
            mSoundPool.play(mKooSound, 1, 1, 0, 0, 1);
        }
    }
}
