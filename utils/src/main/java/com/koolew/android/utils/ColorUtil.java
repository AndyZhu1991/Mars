package com.koolew.android.utils;

import android.graphics.Color;

/**
 * Created by jinchangzhu on 8/22/15.
 */
public class ColorUtil {

    public static int burnColorForStatusBar(int color) {
        return burnColor(color, 0.12f, true);
    }

    public static int burnColorForStatusBarWithAlpha(int color) {
        return burnColor(color, 0.12f, false);
    }

    /**
     * 加深颜色
     * @param color 原始颜色
     * @param degree 加深程度，0.0f~1.0f，0.0f返回原色，1.0f返回黑色
     * @return
     */
    public static int burnColor(int color, float degree, boolean ignoreAlpha) {
        int alpha = color >> 24;
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        red = (int) Math.floor(red * (1 - degree));
        green = (int) Math.floor(green * (1 - degree));
        blue = (int) Math.floor(blue * (1 - degree));
        if (ignoreAlpha) {
            return Color.rgb(red, green, blue);
        }
        else {
            return Color.argb(alpha, red, green, blue);
        }
    }

    public static int getTransitionColor(int startColor, int endColor, float offset) {
        return Color.argb(
                (int) (Color.alpha(startColor) * (1.0f - offset) + Color.alpha(endColor) * offset),
                (int) (Color.red(startColor) * (1.0f - offset) + Color.red(endColor) * offset),
                (int) (Color.green(startColor) * (1.0f - offset) + Color.green(endColor) * offset),
                (int) (Color.blue(startColor) * (1.0f - offset) + Color.blue(endColor) * offset));
    }
}
