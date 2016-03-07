package com.koolew.android.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jinchangzhu on 6/4/15.
 */
public class Utils {

    private static Context context;

    public static void init(Context context) {
        Utils.context = context;
    }

    public static float getScreenWidthDp() {
        return pixelsToDp(getScreenWidthPixel());
    }

    public static int getScreenWidthPixel() {
        return getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeightPixel() {
        return getDisplayMetrics().heightPixels;
    }

    private static DisplayMetrics getDisplayMetrics() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        return outMetrics;
    }

    public static float pixelsToSp(float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }

    public static float spToPixels(float sp) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return sp * scaledDensity;
    }

    public static float pixelsToDp(final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float dpToPixels(final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static String getCacheDir() {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        return cacheDir.getAbsolutePath() + "/";
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

    public static void hideSoftKey(TextView textView) {
        InputMethodManager in = (InputMethodManager) textView.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(textView.getApplicationWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
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

    public static float getTextWidth(String text, float textSize) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        return paint.measureText(text);
    }

    public static int getStatusBarHeightPixel() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getActionBarHeightPixel() {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(
                    tv.data, context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    public static int getNavigationBarHeightPixel() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static boolean hasNavigationBar() {
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

        if (hasBackKey && hasHomeKey) {
            // no navigation bar, unless it is enabled in the settings
            return false;
        } else {
            // 99% sure there's a navigation bar
            return true;
        }
    }

    public static boolean isAppBackground() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public static int getPositionType(int position, int[] types, List... lists) {
        for (int i = 0; i < lists.length; i++) {
            position -= lists[i].size();
            if (position < 0) {
                return types[i];
            }
        }

        return lists.length;
    }

    public static Object getItemFromLists(int position, List... lists) {
        ListNPosition listNPosition = getListNPosition(position, lists);
        return listNPosition.list.get(listNPosition.position);
    }

    public static void removeItem(int position, List... lists) {
        ListNPosition listNPosition = getListNPosition(position, lists);
        listNPosition.list.remove(listNPosition.position);
    }

    private static ListNPosition getListNPosition(int position, List... lists) {
        for (int i = 0; i < lists.length; i++) {
            if (position - lists[i].size() < 0) {
                return new ListNPosition(lists[i], position);
            }
            else {
                position -= lists[i].size();
            }
        }

        return null;
    }

    private static class ListNPosition {
        private List list;
        private int position;

        public ListNPosition(List list, int position) {
            this.list = list;
            this.position = position;
        }
    }

    public interface TypeCounter {
        int getCount();
    }

    public static int getRecyclerType(TypeCounter[] counters, int[] types, int position) {
        int count = 0;
        for (int i = 0; i < counters.length; i++) {
            count += counters[i].getCount();
            if (position < count) {
                return types[i];
            }
        }

        throw new RuntimeException("Error position!");
    }

    public static void setStatusBarColorBurn(Activity activity, int color) {
        setStatusBarColor(activity, ColorUtil.burnColorForStatusBar(color));
    }

    public static void setStatusBarColorBurnWithAlpha(Activity activity, int color) {
        setStatusBarColor(activity, ColorUtil.burnColorForStatusBarWithAlpha(color));
    }

    public static void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(color);
        }
    }

    public static int getStatusBarColorFromPalette(Palette palette) {
        return palette.getMutedColor(0xFF000000);
    }

    public static void setStatusBarColorFromResource(final Activity activity, int resourceId) {
        Palette.from(BitmapFactory.decodeResource(activity.getResources(), resourceId))
                .generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        setStatusBarColorBurn(activity, getStatusBarColorFromPalette(palette));
                    }
                });
    }

    public static Buffer bufferCopy(Buffer original) {
        if (original instanceof ByteBuffer) {
            return byteBufferCopy((ByteBuffer) original);
        }
        else if (original instanceof ShortBuffer) {
            return shortBufferCopy((ShortBuffer) original);
        }
        else if (original instanceof IntBuffer) {
            return intBufferCopy((IntBuffer) original);
        }
        else if (original instanceof FloatBuffer) {
            return floatBufferCopy((FloatBuffer) original);
        }
        else if (original instanceof DoubleBuffer) {
            return doubleBufferCopy((DoubleBuffer) original);
        }
        else {
            throw new RuntimeException("Unsupported buffer copy!");
        }
    }

    public static ByteBuffer byteBufferCopy(ByteBuffer original) {
        ByteBuffer clone = ByteBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }

    public static ShortBuffer shortBufferCopy(ShortBuffer original) {
        ShortBuffer clone = ShortBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }

    public static IntBuffer intBufferCopy(IntBuffer original) {
        IntBuffer clone = IntBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }

    public static FloatBuffer floatBufferCopy(FloatBuffer original) {
        FloatBuffer clone = FloatBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }

    public static DoubleBuffer doubleBufferCopy(DoubleBuffer original) {
        DoubleBuffer clone = DoubleBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }

    public static int getCurrentVersionCode() {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info.versionCode;
    }

    public static void setWindowAlpha(Activity activity, float alpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = alpha; //0.0-1.0
        activity.getWindow().setAttributes(lp);
    }

    public static void setTextViewDrawableLeft(TextView textView, int resId) {
        Drawable drawable = context.getResources().getDrawable(resId);
        // 这一步必须要做,否则不会显示.
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(drawable, null, null, null);
    }

    public static void setTextViewDrawableTop(TextView textView, int resId) {
        Drawable drawable = context.getResources().getDrawable(resId);
        // 这一步必须要做,否则不会显示.
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(null, drawable, null, null);
    }

    public static int getStatusBarHeight() {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
