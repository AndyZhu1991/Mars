package com.koolew.mars.blur;

import android.widget.ImageView;

/**
 * Created by jinchangzhu on 6/24/15.
 */
public class DisplayBlurImage extends LoadBlurImageTask {

    public DisplayBlurImage(ImageView view, String uri) {
        super(view, uri);
    }

    @Override
    protected void onPostExecute(Object o) {
        ((ImageView) mView).setImageBitmap(mBluredBitmap);
    }
}
