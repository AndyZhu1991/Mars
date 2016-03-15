package com.koolew.android.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

/**
 * Created by jinchangzhu on 7/2/15.
 */
public abstract class BasePreference implements View.OnClickListener {

    protected Context mContext;

    protected int mTitleResId;

    protected SharedPreferences mSharedPreference;

    public BasePreference(Context context, int titleResId) {
        mContext = context;
        mTitleResId = titleResId;
        mSharedPreference = context.getSharedPreferences(
                PreferenceHelper.KEY_PREFERENCE, Context.MODE_APPEND);
    }

    public abstract int getLayoutResourceId();

    public void onBindView(View view) {
        ((TextView) view.findViewById(R.id.title)).setText(mTitleResId);
        view.setOnClickListener(this);
    }
}
