package com.koolew.mars.utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jinchangzhu on 6/4/15.
 */
public class Utils {

    public static float getScreenWidthDp(Context context) {
        return pixelsToDp(context, getScreenWidthPixel(context));
    }

    public static int getScreenWidthPixel(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    public static float pixelsToSp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }

    public static float spToPixels(Context context, float sp) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return sp * scaledDensity;
    }

    public static float pixelsToDp(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float dpToPixels(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static String getCacheDir(Context context) {
        return context.getExternalCacheDir().getAbsolutePath() + "/";
    }

    public static boolean deleteFileOrDir(String name) {
        return deleteFileOrDir(new File(name));
    }

    public static boolean deleteFileOrDir(File file) {
        if (file.isFile()) {
            return file.delete();
        }

        File subFiles[] = file.listFiles();
        for (File f: subFiles) {
            if (f.delete() == false) {
                return false;
            }
        }

        return file.delete();
    }

    public static void showSoftKeyInput(final EditText editText, int delay) {
        editText.requestFocus();
        new Timer().schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) editText.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }
        }, delay);
    }

    public static boolean isChinaPhoneNumber(String num) {

        if (num.length() != 11) {
            return false;
        }
        if (!num.startsWith("1")) {
            return false;
        }
        for (int i = 0; i < 11; i++) {
            if (!Character.isDigit(num.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static Rect getMaxCenterSquare(Rect rect) {
        int x = rect.left;
        int y = rect.top;
        int width = rect.width();
        int height = rect.height();

        int size;
        if (width > height) {
            size = height;
            x += (width - height) / 2;
        }
        else {
            size = width;
            y += (height - width) / 2;
        }

        return new Rect(x, y, x + size, y + size);
    }
}
