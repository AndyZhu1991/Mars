package com.koolew.mars.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.MainActivity;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.redpoint.RedPointManager;
import com.koolew.mars.utils.UriProcessor;
import com.koolew.android.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by jinchangzhu on 8/3/15.
 */
public class JpushReceiver extends BroadcastReceiver {
    private static final String TAG = "koolew-JpushReceiver";

    private NotificationManager nm;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.isEmpty(MyAccountInfo.getToken())) {
            //ApiWorker.getInstance().logout();
            return;
        }
        if (null == nm) {
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        Bundle bundle = intent.getExtras();
        Log.d(TAG, "onReceive - " + intent.getAction() + ", extras: " + bundle);
        Log.d(TAG, "registrationId: " + bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID));

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String registrationId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            MyAccountInfo.setRegistrationId(registrationId);
            Log.d(TAG, "JPush用户注册成功: " + registrationId);
            if (!TextUtils.isEmpty(MyAccountInfo.getToken())) {
                ApiWorker.getInstance().postRegistrationId(
                        registrationId, postRegistrationIdListener, postRegistrationIdErrorListener);
            }
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "接受到推送下来的自定义消息");
            // Push Talk messages are push down by custom message format

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "接受到推送下来的通知");
            RedPointManager.refreshRedPoint();
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "用户点击打开了通知");
            try {
                String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
                String url = new JSONObject(extra).getString("url");
                if (Utils.isAppBackground()) {
                    startMainActivityPushed(context, url);
                }
                else {
                    new UriProcessor(context).process(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RedPointManager.refreshDelayed(1000);
        } else {
            Log.d(TAG, "Unhandled intent - " + intent.getAction());
        }
    }

    private Response.Listener<JSONObject> postRegistrationIdListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            // TODO
        }
    };

    private Response.ErrorListener postRegistrationIdErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO
        }
    };

    private void startMainActivityPushed(Context context, String uri) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MainActivity.KEY_PUSH_URI, uri);
        context.startActivity(intent);
    }
}
