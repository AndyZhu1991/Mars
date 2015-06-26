package com.koolew.mars.webapi;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by jinchangzhu on 6/25/15.
 */
public class ApiWorker {

    private static ApiWorker sInstance;

    private Context mContext;
    private RequestQueue mRequestQueue;
    private Response.ErrorListener mErrorListener;

    public static void init(Context context) {
        sInstance = new ApiWorker(context);
    }

    private ApiWorker(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(mContext);
        mErrorListener = new StdErrorListener();
    }

    public static ApiWorker getInstance() {
        return sInstance;
    }


    public void requestInvolve(int page, Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {

        if (errorListener == null) {
            errorListener = mErrorListener;
        }

        String url = UrlHelper.getInvolveUrl(page);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return UrlHelper.getStandardPostHeaders();
            }
        };

        mRequestQueue.add(jsonObjectRequest);
    }

    public JsonObjectRequest requestTopicVideo(String topicId,
                                               Response.Listener<JSONObject> listener,
                                               Response.ErrorListener errorListener) {
        if (errorListener == null) {
            errorListener = mErrorListener;
        }

        String url = UrlHelper.getTopicVideoFriendUrl(topicId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return UrlHelper.getStandardPostHeaders();
            }
        };
        mRequestQueue.add(jsonObjectRequest);

        return jsonObjectRequest;
    }

    public JsonObjectRequest requestTopicVideo(String topicId, long beforeTime,
                                               Response.Listener<JSONObject> listener,
                                               Response.ErrorListener errorListener) {
        if (errorListener == null) {
            errorListener = mErrorListener;
        }

        String url = UrlHelper.getTopicVideoFriendUrl(topicId, beforeTime);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return UrlHelper.getStandardPostHeaders();
            }
        };
        mRequestQueue.add(jsonObjectRequest);

        return jsonObjectRequest;
    }

    class StdErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
        }
    }
}
