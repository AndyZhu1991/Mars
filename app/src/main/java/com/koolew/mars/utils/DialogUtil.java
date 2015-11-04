package com.koolew.mars.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 7/7/15.
 */
public class DialogUtil {

    public static ProgressDialog getConnectingServerDialog(Context context) {
        ProgressDialog dialog = getGeneralProgressDialog(context);
        dialog.setMessage(context.getString(R.string.communcating_with_server));

        return dialog;
    }

    public static ProgressDialog getGeneralProgressDialog(Context context, String message) {
        ProgressDialog dialog = getGeneralProgressDialog(context);
        dialog.setMessage(message);

        return dialog;
    }

    public static ProgressDialog getGeneralProgressDialog(Context context, int messageRes) {
        ProgressDialog dialog = getGeneralProgressDialog(context);
        dialog.setMessage(context.getString(messageRes));

        return dialog;
    }

    private static ProgressDialog getGeneralProgressDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);

        return dialog;
    }

    public static abstract class AsyncTaskWithDialog<Params, Progress, Result>
            extends AsyncTask<Params, Progress, Result> {
        protected ProgressDialog mProgressingDialog;

        public AsyncTaskWithDialog(Context context) {
            mProgressingDialog = getGeneralProgressDialog(context, R.string.please_wait_a_moment);
        }

        @Override
        protected void onPreExecute() {
            mProgressingDialog.show();
        }

        @Override
        protected void onPostExecute(Result result) {
            mProgressingDialog.dismiss();
        }
    }
}
