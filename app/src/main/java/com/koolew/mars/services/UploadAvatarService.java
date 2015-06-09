package com.koolew.mars.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.utils.BitmapUtil;
import com.koolew.mars.utils.WebApiUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UploadAvatarService extends IntentService {

    private static final String TAG = "koolew-UploadAvatarS";

    private static final int AVATAR_SIZE = 200;
    private static final int GET_QINIU_TOKEN_TIMEOUT = 3000;
    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPLOAD = "com.koolew.mars.services.action.UPLOAD";
    private static final String ACTION_BAZ = "com.koolew.mars.services.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.koolew.mars.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.koolew.mars.services.extra.PARAM2";

    private RequestQueue mRequestQueue;
    private String avatarFile;

    @Override
    public void onCreate() {
        super.onCreate();
        mRequestQueue = Volley.newRequestQueue(this);
        avatarFile = getExternalCacheDir() + "avatar.png";
    }

    /**
     * Starts this service to perform action Upload with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpload(Context context) {
        Intent intent = new Intent(context, UploadAvatarService.class);
        intent.setAction(ACTION_UPLOAD);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UploadAvatarService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public UploadAvatarService() {
        super("UploadAvatarService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                handleActionUpload();
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpload() {
        // TODO: Handle action Upload
        Bitmap avatarBitmap = BitmapUtil.getScaledSquareBmp(
                ImageLoader.getInstance().loadImageSync(MyAccountInfo.getAvatar()),
                AVATAR_SIZE);

        File f = new File(avatarFile);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            avatarBitmap.compress(Bitmap.CompressFormat.PNG, 66, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String qiniuToken = getQiniuToken();

        // 重用 uploadManager。一般地，只需要创建一个 uploadManager 对象
        UploadManager uploadManager = new UploadManager();
        Map<String, String> optionMap = new HashMap<String, String>();
        optionMap.put("x:uid", MyAccountInfo.getUid());
        optionMap.put("x:type", "avatar");
        uploadManager.put(f, null, qiniuToken,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject response) {
                        if (info.isOK()) {
                            // Upload success
                            Log.d(TAG, "Upload success: " + response);
                        }
                        else {
                            // Upload failed
                            Log.d(TAG, "Upload failed: " + response);
                        }
                    }
                }, new UploadOptions(optionMap, null, false, null, null));
    }

    private String getQiniuToken() {

        String url = WebApiUtil.REQUEST_QINIU_TOKEN_URL;
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(url, null, future, future) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", MyAccountInfo.getToken());
                        return headers;
                    }
                };
        mRequestQueue.add(jsonObjectRequest);

        String qiniuToken = null;
        try {
            JSONObject response = future.get(GET_QINIU_TOKEN_TIMEOUT, TIME_UNIT);
            if (response.getInt("code") == 0) {
                qiniuToken = response.getJSONObject("result").getString("avatar");
            }
        } catch (InterruptedException e) {
            // exception handling
        } catch (ExecutionException e) {
            // exception handling
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return qiniuToken;
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
