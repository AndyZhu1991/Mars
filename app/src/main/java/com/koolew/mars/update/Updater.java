package com.koolew.mars.update;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.R;
import com.koolew.android.downloadmanager.Downloader;
import com.koolew.android.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by jinchangzhu on 10/20/15.
 */
public class Updater implements Downloader.LoadListener, Response.Listener<JSONObject>,
        Response.ErrorListener {

    private static final String KEY_UPDATE_PREFERENCE = "update";
    private static final String KEY_LAST_CHECK_TIME_MILLIS = "check_time";

    //                                               millis  second minute  hour
    private static final long MIN_AUTO_CHECK_MILLIS = 1000  *  60  *  60  *  24;

    private static Updater instance;

    private Context mContext;

    private SharedPreferences sharedPreference;
    private VersionInfo lastVersionInfo;

    public static Updater newInstance(Context context) {
        if (instance == null) {
            instance = new Updater(context);
            return instance;
        }
        return null;
    }

    private Updater(Context context) {
        mContext = context;
        sharedPreference = mContext.getSharedPreferences(KEY_UPDATE_PREFERENCE, Context.MODE_APPEND);
    }

    public void checkUpdateAutomatic() {
        if (System.currentTimeMillis() - getLastAutoCheckMillis() > MIN_AUTO_CHECK_MILLIS) {
            setLastAutoCheckMillis(System.currentTimeMillis());
            doCheckUpdateRequest(true);
        }
        else {
            instance = null;
        }
    }

    private long getLastAutoCheckMillis() {
        return sharedPreference.getLong(KEY_LAST_CHECK_TIME_MILLIS, 0);
    }

    private void setLastAutoCheckMillis(long millis) {
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putLong(KEY_LAST_CHECK_TIME_MILLIS, millis);
        editor.commit();
    }

    public void checkUpdate() {
        Toast.makeText(mContext, R.string.checking_for_update, Toast.LENGTH_SHORT).show();
        doCheckUpdateRequest(false);
    }

    private boolean isAutoCheck = true;
    private void doCheckUpdateRequest(boolean isAutoCheck) {
        this.isAutoCheck = isAutoCheck;
        ApiWorker.getInstance().queueGetRequest(UrlHelper.CHECK_VERSION_URL, this, this);
    }

    private void onVersionInfoUpdate() {
        if (lastVersionInfo.versionCode > Utils.getCurrentVersionCode()
                && !Utils.isAppBackground()) {
            onNewVersionAvailable();
        }
        else {
            if (!isAutoCheck) {
                Toast.makeText(mContext, R.string.you_have_last_version, Toast.LENGTH_SHORT).show();
            }
            instance = null;
        }
    }

    private void onNewVersionAvailable() {
        new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.new_version, lastVersionInfo.versionName))
                .setMessage(lastVersionInfo.desc)
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startDownload();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        instance = null;
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        instance = null;
                    }
                })
                .show();
    }

    private void startDownload() {
        Toast.makeText(mContext, R.string.downloading_update_package, Toast.LENGTH_SHORT).show();
        try {
            Downloader.getInstance().download(this, lastVersionInfo.fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDownloadComplete(String url, String filePath) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)),
                "application/vnd.android.package-archive");
        mContext.startActivity(intent);
        instance = null;
    }

    @Override
    public void onDownloadProgress(long totalBytes, long downloadedBytes, int progress) {
    }

    @Override
    public void onDownloadFailed(int errorCode, String errorMessage) {
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            JSONObject result = response.getJSONObject("result");
            lastVersionInfo = new VersionInfo();
            lastVersionInfo.fileUrl = result.getString("url");
            lastVersionInfo.versionCode = result.getInt("code");
            lastVersionInfo.versionName = result.getString("version");
            lastVersionInfo.desc = result.getString("desc");
            onVersionInfoUpdate();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // TODO
    }

    private static class VersionInfo {
        private String fileUrl;
        private int versionCode;
        private String versionName;
        private String desc;
    }
}
