package com.koolew.mars.blur;

import android.support.v7.graphics.Palette;
import android.widget.ImageView;

/**
 * Created by jinchangzhu on 8/22/15.
 */
public abstract class DisplayBlurImageAndPalette extends DisplayBlurImage {

    public DisplayBlurImageAndPalette(ImageView view, String uri) {
        super(view, uri);
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Palette.from(mBluredBitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                onPalette(palette);
            }
        });
    }

    protected abstract void onPalette(Palette palette);
}
