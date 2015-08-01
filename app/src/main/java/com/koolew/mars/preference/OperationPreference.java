package com.koolew.mars.preference;

import android.content.Context;
import android.view.View;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 8/1/15.
 */
public class OperationPreference extends BasePreference {

    private View.OnClickListener mClickListener;

    public OperationPreference(Context context, int titleResId) {
        super(context, titleResId);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mClickListener = listener;
    }

    @Override
    public void onClick(View view) {
        if (mClickListener != null) {
            mClickListener.onClick(view);
        }
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.preference_item_tree;
    }
}
