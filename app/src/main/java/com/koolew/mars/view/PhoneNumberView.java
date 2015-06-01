package com.koolew.mars.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by jinchangzhu on 6/1/15.
 */
public class PhoneNumberView extends TextView {

    public PhoneNumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNumber(String number) {
        if (number.startsWith("+86")) {
            setText(number);
        }
        else {
            setText("+86 " + number);
        }
    }
}
