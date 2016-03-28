package com.koolew.mars.webapi;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.MarsApplication;
import com.koolew.mars.R;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.utils.ContactUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by jinchangzhu on 6/25/15.
 */
public class ApiWorker {

    private static final String TAG = "ApiWorker";

    private static final int SYNC_REQUEST_TIMEOUT = 8000;
    private static final TimeUnit SYNC_REQUEST_TIME_UNIT = TimeUnit.MILLISECONDS;

    private static ApiWorker sInstance;

    private Context mContext;
    private RequestQueue mRequestQueue;

    public static void init(Context context) {
        sInstance = new ApiWorker(context);
    }

    private ApiWorker(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(mContext);
    }

    public static ApiWorker getInstance() {
        return sInstance;
    }

    public JsonObjectRequest requestContactFriend(List<ContactUtil.SimpleContactInfo> contacts,
            Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        return queuePostRequest(UrlHelper.CONTACT_FRIEND_RECOMMEND_URL,
                buildContactFriendJson(contacts), listener, errorListener);
    }

    private JSONObject buildContactFriendJson(List<ContactUtil.SimpleContactInfo> contacts) {
        JSONObject contactJson = new JSONObject();
        JSONArray jsonArrayContacts = new JSONArray();
        try {
            if (contacts != null) {
                for (ContactUtil.SimpleContactInfo contactInfo : contacts) {
                    if (TextUtils.isEmpty(contactInfo.getName().trim())) {
                        continue;
                    }
                    JSONObject contact = new JSONObject();
                    contact.put("nickname", contactInfo.getName());
                    contact.put("phone", contactInfo.getNumber());
                    jsonArrayContacts.put(contact);
                    if (jsonArrayContacts.length() >= 1000) {
                        break;
                    }
                }
            }
            contactJson.put("contacts", jsonArrayContacts);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return contactJson;
    }

    public JsonObjectRequest updateNickname(String newNickname,
                                            Response.Listener<JSONObject> listener,
                                            Response.ErrorListener errorListener) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("nickname", newNickname);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.USER_INFO_URL, requestJson, listener, errorListener);
    }

    public JsonObjectRequest updatePhoneNumber(String newPhoneNumber, String code,
                                               Response.Listener<JSONObject> listener,
                                               Response.ErrorListener errorListener) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("phone", newPhoneNumber);
            requestJson.put("code", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.USER_INFO_URL, requestJson, listener, errorListener);
    }

    public JsonObjectRequest sendDanmaku(String content, String videoId, float showTime,
                                         float x, float y,
                                         Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("content", content);
            requestJson.put("video_id", videoId);
            requestJson.put("show_time", showTime);
            JSONObject position = new JSONObject();
            position.put("x", x);
            position.put("y", y);
            requestJson.put("position", position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.SEND_DANMAKU_URL, requestJson, listener, errorListener);
    }

    public JsonObjectRequest addTopic(String title, String desc,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("content", title);
            requestJson.put("desc", desc);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.ADD_TOPIC_URL, requestJson, listener, errorListener);
    }

    public JsonObjectRequest sendInvitation(String topicId, List<String> friendIds,
                                            Response.Listener<JSONObject> listener,
                                            Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("topic_id", topicId);
            JSONArray friends = new JSONArray();
            for (String friendId: friendIds) {
                friends.put(friendId);
            }
            requestObject.put("to", friends);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetryPolicy retryPolicy = new DefaultRetryPolicy(
                10000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        return queuePostRequest(UrlHelper.SEND_INVITATION_URL, requestObject,
                listener, errorListener, retryPolicy);
    }

    public JsonObjectRequest kooVideo(String videoId, int count,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("video_id", videoId);
            requestObject.put("count", count);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.KOO_URL, requestObject, listener, errorListener);
    }

    public JsonObjectRequest ignoreInvitation(String topicId,
                                              Response.Listener<JSONObject> listener,
                                              Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("topic_id", topicId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.IGNORE_INVITATION_URL, requestObject,
                listener, errorListener);
    }

    public JsonObjectRequest postRegistrationId(String registrationId,
                                                Response.Listener<JSONObject> listener,
                                                Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("device_id", registrationId);
            requestObject.put("type", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.DEVICE_LOGIN_URL, requestObject, listener, errorListener);
    }

    public JsonObjectRequest postPushBit(int pushBit,
                                         Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("push_bit", pushBit);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.DEVICE_PUSH_URL, requestObject, listener, errorListener);
    }

    public JsonObjectRequest deleteVideo(String videoId,
                                         Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("video_id", videoId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.VIDEO_DELETE_URL, requestObject, listener, errorListener);
    }

    public JsonObjectRequest againstVideo(String videoId,
                                          Response.Listener<JSONObject> listener,
                                          Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("video_id", videoId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.VIDEO_AGAINST_URL, requestObject, listener, errorListener);
    }

    public JsonObjectRequest loginBySns(MyAccountInfo.LOGIN_TYPE type, String openId,
                                        String access_token, String refreshToken, long expiresIn,
                                        String unionId,
                                        Response.Listener<JSONObject> listener,
                                        Response.ErrorListener errorListener) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("device", Build.FINGERPRINT);
            requestJson.put("type", type.ordinal());
            requestJson.put("open_id", openId);
            requestJson.put("access_token", access_token);
            if (refreshToken != null) {
                requestJson.put("refresh_token", refreshToken);
            }
            requestJson.put("expires_in", expiresIn);
            requestJson.put("union_id", unionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.SNS_LOGIN_URL, requestJson, listener, errorListener);
    }

    public JsonObjectRequest logout(Response.Listener<JSONObject> listener,
                                    Response.ErrorListener errorListener) {
        return logout(MyAccountInfo.getRegistrationId(), listener, errorListener);
    }

    public JsonObjectRequest logout(String registrationId,
                                    Response.Listener<JSONObject> listener,
                                    Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("device_id", registrationId);
            requestObject.put("type", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.DEVICE_LOGOUT_URL, requestObject, listener, errorListener);
    }

    public JsonObjectRequest postLocation(double longitude, double latitude,
                                          Response.Listener<JSONObject> listener,
                                          Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("longitude", longitude);
            requestObject.put("latitude", latitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.USER_LOCATION_URL, requestObject, listener, errorListener);
    }

    public JsonObjectRequest postTopicDesc(String topicId, String desc,
                                           Response.Listener<JSONObject> listener,
                                           Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("topic_id", topicId);
            requestObject.put("desc", desc);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.EDIT_TOPIC_DESC_URL, requestObject,
                listener, errorListener);
    }

    public JsonObjectRequest followUser(String uid,
                                        Response.Listener<JSONObject> listener,
                                        Response.ErrorListener errorListener) {
        List<String> uids = new ArrayList<>(1);
        uids.add(uid);
        return followUsers(uids, listener, errorListener);
    }

    public JsonObjectRequest followUsers(List<String> uids,
                                         Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        JSONArray to = new JSONArray();
        for (String uid: uids) {
            to.put(uid);
        }
        try {
            requestObject.put("to", to);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.FRIEND_FOLLOW_URL, requestObject,
                listener, errorListener);
    }

    public JsonObjectRequest unfollowUser(String uid,
                                          Response.Listener<JSONObject> listener,
                                          Response.ErrorListener errorListener) {
        List<String> uids = new ArrayList<>(1);
        uids.add(uid);
        return unfollowUsers(uids, listener, errorListener);
    }

    public JsonObjectRequest unfollowUsers(List<String> uids,
                                           Response.Listener<JSONObject> listener,
                                           Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        JSONArray to = new JSONArray();
        for (String uid: uids) {
            to.put(uid);
        }
        try {
            requestObject.put("to", to);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.FRIEND_UNFOLLOW_URL, requestObject,
                listener, errorListener);
    }

    public JsonObjectRequest bindAlipay(String alipay,
                                        Response.Listener<JSONObject> listener,
                                        Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("alipay", alipay);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.BIND_ALIPAY_URL, requestObject,
                listener, errorListener);
    }

    public JsonObjectRequest cashOut(String alipay, int amount,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("alipay", alipay);
            requestObject.put("amount", amount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queuePostRequest(UrlHelper.CASH_OUT_URL, requestObject,
                listener, errorListener);
    }

    public JsonObjectRequest queueGetRequest(String url,
                                             Response.Listener<JSONObject> listener,
                                             Response.ErrorListener errorListener) {
        return queueGetRequest(url, listener, errorListener, null);
    }

    public JsonObjectRequest queueGetRequest(String url,
                                             Response.Listener<JSONObject> listener,
                                             Response.ErrorListener errorListener,
                                             RetryPolicy retryPolicy) {
        if (TextUtils.isEmpty(url) || listener == null || errorListener == null) {
            if (MarsApplication.DEBUG) {
                throw new IllegalArgumentException("Illegal argument in queueGetRequest!");
            }
            else {
                if (!TextUtils.isEmpty(url)) {
                    if (listener == null) {
                        listener = emptyResponseListener;
                    }
                    if (errorListener == null) {
                        errorListener = emptyErrorListener;
                    }
                }
                else {
                    return null;
                }
            }
        }

        JsonObjectRequest jsonObjectRequest = new CachedJsonObjectRequest(Request.Method.GET, url,
                listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return UrlHelper.getStandardApiHeader();
            }
        };
        if (retryPolicy != null) {
            jsonObjectRequest.setRetryPolicy(retryPolicy);
        }
        mRequestQueue.add(jsonObjectRequest);

        return jsonObjectRequest;
    }

    public JsonObjectRequest queuePostRequest(String url, JSONObject requestJson,
                                              Response.Listener<JSONObject> listener,
                                              Response.ErrorListener errorListener) {
        return queuePostRequest(url, requestJson, listener, errorListener, null);
    }

    public JsonObjectRequest queuePostRequest(String url, JSONObject requestJson,
                                              Response.Listener<JSONObject> listener,
                                              Response.ErrorListener errorListener,
                                              RetryPolicy retryPolicy) {
        if (TextUtils.isEmpty(url) || listener == null || errorListener == null) {
            if (MarsApplication.DEBUG) {
                throw new IllegalArgumentException("Illegal argument in queueGetRequest!");
            }
            else {
                if (!TextUtils.isEmpty(url)) {
                    if (listener == null) {
                        listener = emptyResponseListener;
                    }
                    if (errorListener == null) {
                        errorListener = emptyErrorListener;
                    }
                }
                else {
                    return null;
                }
            }
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, requestJson, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return UrlHelper.getStandardApiHeader();
            }
        };
        if (retryPolicy != null) {
            jsonObjectRequest.setRetryPolicy(retryPolicy);
        }
        mRequestQueue.add(jsonObjectRequest);

        return jsonObjectRequest;
    }

    public JSONObject doGetRequestSync(String url)
            throws InterruptedException, ExecutionException, TimeoutException {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        queueGetRequest(url, future, future);
        return future.get(SYNC_REQUEST_TIMEOUT, SYNC_REQUEST_TIME_UNIT);
    }

    public JSONObject doPostRequestSync(String url, JSONObject requestObject)
            throws InterruptedException, ExecutionException, TimeoutException {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        queuePostRequest(url, requestObject, future, future);
        return future.get(SYNC_REQUEST_TIMEOUT, SYNC_REQUEST_TIME_UNIT);
    }

//    int DEPRECATED_GET_OR_POST = -1;
//    int GET = 0;
//    int POST = 1;
//    int PUT = 2;
//    int DELETE = 3;
//    int HEAD = 4;
//    int OPTIONS = 5;
//    int TRACE = 6;
//    int PATCH = 7;
    public Cache.Entry getApiCache(String url) {
        return mRequestQueue.getCache().get("0:" + url); // A volley's bug
    }

    private Response.Listener<JSONObject> emptyResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
        }
    };

    private Response.ErrorListener emptyErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    };


    private static class CachedJsonObjectRequest extends JsonObjectRequest {
        public CachedJsonObjectRequest(int method, String url, Response.Listener<JSONObject> listener,
                                       Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                return Response.success(new JSONObject(jsonString),
                        parseIgnoreCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }

        /**
         * Extracts a {@link Cache.Entry} from a {@link NetworkResponse}.
         * Cache-control headers are ignored. SoftTtl == 3 mins, ttl == 24 hours.
         * @param response The network response to parse headers from
         * @return a cache entry for the given response, or null if the response is not cacheable.
         */
        public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response) {
            Cache.Entry entry = HttpHeaderParser.parseCacheHeaders(response);

            if (entry.softTtl == 0) {
                entry.softTtl = System.currentTimeMillis();
            }

            return entry;
        }
    }

    public static class ToastErrorListener implements Response.ErrorListener {
        private Context context;
        private String message;

        public ToastErrorListener(Context context) {
            this.context = context;
            message = context.getString(R.string.network_error);
        }

        public ToastErrorListener(Context context, String message) {
            this.context = context;
            this.message = message;
        }

        public ToastErrorListener(Context context, int messageStringId) {
            this.context = context;
            message = context.getString(messageStringId);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
