package com.koolew.mars.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.koolew.mars.R;
import com.sevenheaven.iosswitch.ShSwitchView;

/**
 * Created by jinchangzhu on 7/2/15.
 */
public class SwitchPreference extends BasePreference
        implements ShSwitchView.OnSwitchStateChangeListener {

    protected String mItemKey;

    protected ShSwitchView mSwitch;

    protected boolean mDefaultValue;

    public SwitchPreference(Context context, int titleResId, String key, boolean defaultValue) {
        super(context, titleResId);

        mItemKey = key;
        mDefaultValue = defaultValue;
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.preference_item_switch;
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);

        mSwitch = (ShSwitchView) view.findViewById(R.id.switcher);
        mSwitch.setOn(mSharedPreference.getBoolean(mItemKey, mDefaultValue), false);
        mSwitch.setOnSwitchStateChangeListener(this);
    }

    @Override
    public void onSwitchStateChange(boolean on) {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putBoolean(mItemKey, on);
        editor.commit();
    }
}
