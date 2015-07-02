package com.koolew.mars.preference;

import android.content.Context;
import android.view.View;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 7/2/15.
 */
public class PreferenceGroupTitle extends BasePreference {

    public PreferenceGroupTitle(Context context, int titleResId) {
        super(context, titleResId);
    }

    @Override
    public void onClick(View view) {}

    @Override
    public int getLayoutResourceId() {
        return R.layout.preference_group_title;
    }
}
