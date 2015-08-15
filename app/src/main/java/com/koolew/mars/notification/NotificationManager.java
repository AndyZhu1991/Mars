package com.koolew.mars.notification;

import com.android.volley.Response;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;

/**
 * Created by jinchangzhu on 8/15/15.
 */
public class NotificationManager {

    public static void refreshNotification() {
        ApiWorker.getInstance().requestNotificationBrief(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("code") == 0) {
                        NotificationEvent event =
                                new NotificationEvent(response.getJSONObject("result"));
                        NotificationKeeper.set(event);
                        EventBus.getDefault().post(event);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
        null);
    }

    public static void refreshDelayed(final long time) {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                refreshNotification();
            }
        }.start();
    }
}
