package com.koolew.mars.webapi;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.utils.ContactUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
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

    public JsonObjectRequest requestRecommendFriend(Response.Listener<JSONObject> listener,
                                                    Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.FRIEND_RECOMMEND_URL, listener, errorListener);
    }

    public JsonObjectRequest requestContactFriend(List<ContactUtil.SimpleContactInfo> contacts,
            Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {

        JSONObject requestJson = new JSONObject();
        JSONArray jsonArrayContacts = new JSONArray();
        try {
            for (ContactUtil.SimpleContactInfo contactInfo : contacts) {
                JSONObject contact = new JSONObject();
                contact.put("nickname", contactInfo.getName());
                contact.put("phone", contactInfo.getNumber());
                jsonArrayContacts.put(contact);
            }
            requestJson.put("contacts", jsonArrayContacts);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return standardPostRequest(UrlHelper.CONTACT_FRIEND_RECOMMEND_URL,
                requestJson, listener, errorListener);
    }

    public JsonObjectRequest requestCurrentFriend(Response.Listener<JSONObject> listener,
                                                  Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.CURRENT_FRIEND_URL, listener, errorListener);
    }

    public JsonObjectRequest requestFeedsTopic(Response.Listener<JSONObject> listener,
                                               Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.FEEDS_TOPIC_URL, listener, errorListener);
    }

    public JsonObjectRequest requestFeedsTopic(long before,
                                               Response.Listener<JSONObject> listener,
                                               Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getFeedsTopicUrl(before), listener, errorListener);
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
        return standardPostRequest(UrlHelper.USER_INFO_URL, requestJson, listener, errorListener);
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
        return standardPostRequest(UrlHelper.USER_INFO_URL, requestJson, listener, errorListener);
    }

    public JsonObjectRequest requestPasswordMessage(String phoneNumber,
                                                    Response.Listener<JSONObject> listener,
                                                    Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getRequestPasswordMessageUrl(phoneNumber),
                listener, errorListener);
    }

    public JsonObjectRequest requestPasswordCall(String phoneNumber,
                                                 Response.Listener<JSONObject> listener,
                                                 Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getRequestPasswordCallUrl(phoneNumber),
                listener, errorListener);
    }


    private JsonObjectRequest standardGetRequest(String url,
                                                 Response.Listener<JSONObject> listener,
                                                 Response.ErrorListener errorListener) {
        if (errorListener == null) {
            errorListener = mErrorListener;
        }

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

    private JsonObjectRequest standardPostRequest(String url, JSONObject requestJson,
                                                  Response.Listener<JSONObject> listener,
                                                  Response.ErrorListener errorListener) {
        if (errorListener == null) {
            errorListener = mErrorListener;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, requestJson, listener, errorListener) {
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
