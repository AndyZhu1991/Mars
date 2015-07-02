package com.koolew.mars.preference;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 7/2/15.
 */
public class TreePreference extends BasePreference {

    protected Class mTargetActivity;

    public TreePreference(Context context, int titleResId, Class targetActivity) {
        super(context, titleResId);
        mTargetActivity = targetActivity;
    }

    @Override
    public void onClick(View view) {
        if (mTargetActivity != null) {
            Intent intent = new Intent(mContext, mTargetActivity);
            mContext.startActivity(intent);
        }
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.preference_item_tree;
    }
}
