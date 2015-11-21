package com.koolew.mars.utils;

import android.content.Context;

import com.koolew.mars.R;

import java.io.File;
import java.util.Random;

/**
 * Created by jinchangzhu on 7/20/15.
 */
public class BgmUtil {

    public static final BgmStyleItem[] ALL_BGMS = new BgmStyleItem[] {
            new BgmStyleItem(new String[] {"TheSummerSong", "xiaoxu05", "xiaoxu03"},
                    R.mipmap.music_1, R.mipmap.music_1_hover),
            new BgmStyleItem(new String[] {"DebussyReverie", "Humanity", "LonelyTides"},
                    R.mipmap.music_2, R.mipmap.music_2_hover),
            new BgmStyleItem(new String[] {"Easydoesit", "EarthWindandPower", "xiaoxu02"},
                    R.mipmap.music_3, R.mipmap.music_3_hover),
            new BgmStyleItem(new String[] {"WalkingonRainbows", "xiaoxu04", "IndieRockers"},
                    R.mipmap.music_4, R.mipmap.music_4_hover),
            new BgmStyleItem(new String[] {"Annie", "OnThisMoment", "xiaoxu01"},
                    R.mipmap.music_5, R.mipmap.music_5_hover)
    };

    private static String CACHE_DIR;

    public static void initBgms(Context context) {
        CACHE_DIR = context.getCacheDir().getAbsolutePath() + "/";
        File bgmDir = new File(CACHE_DIR + "bgm/");
        if (!bgmDir.exists()) {
            bgmDir.mkdir();
        }

        for (BgmStyleItem bgmStyleItem : ALL_BGMS) {
            for (int i = 0; i < bgmStyleItem.getBgmCount(); i++) {
                FileUtil.copyFromAssets(context, bgmStyleItem.getRelativePath(i),
                        bgmStyleItem.getAbsolutePath(i));
            }
        }
    }

    public static class BgmStyleItem {
        private String[] mBgmNames;
        private int mIconResId;
        private int mHoverIconResId;

        private int mLastRandomPosition;

        public BgmStyleItem(String[] bgmNames, int iconResId, int hoverIconResId) {
            mBgmNames = bgmNames;
            mIconResId = iconResId;
            mHoverIconResId = hoverIconResId;

            mLastRandomPosition = -1;
        }

        public int getBgmCount() {
            return mBgmNames.length;
        }

        public String getRelativePath(int position) {
            return "bgm/" + mBgmNames[position] + ".m4a";
        }

        public String getAbsolutePath(int position) {
            return CACHE_DIR + getRelativePath(position);
        }

        public String getRandomBgm() {
            Random random = new Random();
            while (true) {
                int randInt = random.nextInt(mBgmNames.length);
                if (randInt != mLastRandomPosition) {
                    mLastRandomPosition = randInt;
                    return getAbsolutePath(randInt);
                }
            }
        }

        public int getHoverIconResId() {
            return mHoverIconResId;
        }

        public int getIconResId() {
            return mIconResId;
        }
    }
}
