package com.koolew.mars.update;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.android.volley.Response;
import com.koolew.mars.R;
import com.koolew.mars.downloadmanager.DownloadRequest;
import com.koolew.mars.downloadmanager.DownloadStatusListener;
import com.koolew.mars.downloadmanager.ThinDownloadManager;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by jinchangzhu on 10/20/15.
 */
public class Updater implements DownloadStatusListener, Response.Listener<JSONObject> {

    private static final String KEY_UPDATE_PREFERENCE = "update";
    private static final String KEY_LAST_CHECK_TIME_MILLIS = "check_time";

    //                                               millis  second minute  hour
    private static final long MIN_AUTO_CHECK_MILLIS = 1000  *  60  *  60  *  24;

    private static Updater instance;

    private Context mContext;

    private SharedPreferences sharedPreference;
    private ThinDownloadManager downloadManager;
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
        downloadManager = new ThinDownloadManager(1);
    }

    public void checkUpdateAutomatic() {
        if (System.currentTimeMillis() - getLastAutoCheckMillis() > MIN_AUTO_CHECK_MILLIS) {
            setLastAutoCheckMillis(System.currentTimeMillis());
            doCheckUpdateRequest();
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
        doCheckUpdateRequest();
    }

    private void doCheckUpdateRequest() {
        ApiWorker.getInstance().checkVersion(this, null);
    }

    private void onVersionInfoUpdate() {
        if (lastVersionInfo.versionCode > getCurrentVersionCode()
                && !Utils.isAppBackground(mContext)) {
            onNewVersionAvailable();
        }
        else {
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

    private int getCurrentVersionCode() {
        PackageManager manager = mContext.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info.versionCode;
    }

    private void startDownload() {
        Toast.makeText(mContext, R.string.downloading_update_package, Toast.LENGTH_SHORT).show();
        DownloadRequest request = new DownloadRequest(Uri.parse(lastVersionInfo.fileUrl))
                .setDestinationURI(Uri.parse(getLocalPathFromUrl(lastVersionInfo.fileUrl)))
                .setDownloadListener(this);

        downloadManager.add(request);
    }

    private String getLocalPathFromUrl(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        return Utils.getCacheDir(mContext) + "/" + fileName;
    }


    @Override
    public void onDownloadComplete(int id) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(getLocalPathFromUrl(lastVersionInfo.fileUrl))),
                "application/vnd.android.package-archive");
        mContext.startActivity(intent);
        instance = null;
    }

    @Override
    public void onDownloadFailed(int id, int errorCode, String errorMessage) {
    }

    @Override
    public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {
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

    private static class VersionInfo {
        private String fileUrl;
        private int versionCode;
        private String versionName;
        private String desc;
    }
}
