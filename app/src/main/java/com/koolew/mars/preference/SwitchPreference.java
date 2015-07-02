package com.koolew.mars.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 7/2/15.
 */
public class SwitchPreference extends BasePreference
        implements CompoundButton.OnCheckedChangeListener {

    protected String mItemKey;

    protected Switch mSwitch;

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

        mSwitch = (Switch) view.findViewById(R.id.switcher);
        mSwitch.setChecked(mSharedPreference.getBoolean(mItemKey, mDefaultValue));
        mSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putBoolean(mItemKey, isChecked);
        editor.commit();
    }
}
