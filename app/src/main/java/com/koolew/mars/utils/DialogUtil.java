package com.koolew.mars.utils;

import android.app.ProgressDialog;
import android.content.Context;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 7/7/15.
 */
public class DialogUtil {

    public static ProgressDialog getConnectingServerDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.communcating_with_server));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);

        return dialog;
    }
}
