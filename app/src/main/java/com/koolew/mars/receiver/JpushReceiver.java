package com.koolew.mars.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.webapi.ApiWorker;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by jinchangzhu on 8/3/15.
 */
public class JpushReceiver extends BroadcastReceiver {
    private static final String TAG = "koolew-JpushReceiver";

    private NotificationManager nm;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == nm) {
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        Bundle bundle = intent.getExtras();
        Log.d(TAG, "onReceive - " + intent.getAction() + ", extras: " + bundle);

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String registrationId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            MyAccountInfo.setRegistrationId(registrationId);
            Log.d(TAG, "JPush用户注册成功: " + registrationId);
            if (!TextUtils.isEmpty(MyAccountInfo.getToken())) {
                ApiWorker.getInstance().postRegistrationId(
                        registrationId, ApiWorker.getInstance().emptyResponseListener, null);
            }
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "接受到推送下来的自定义消息");
            // Push Talk messages are push down by custom message format

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "接受到推送下来的通知");

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "用户点击打开了通知");

        } else {
            Log.d(TAG, "Unhandled intent - " + intent.getAction());
        }
    }
}
