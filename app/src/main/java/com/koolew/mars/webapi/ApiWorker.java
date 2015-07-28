package com.koolew.mars.webapi;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.utils.ContactUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by jinchangzhu on 6/25/15.
 */
public class ApiWorker {

    private static final int SYNC_REQUEST_TIMEOUT = 8000;
    private static final TimeUnit SYNC_REQUEST_TIME_UNIT = TimeUnit.MILLISECONDS;

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

    public JsonObjectRequest requestFeedsTopicVideo(String topicId,
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

    public JsonObjectRequest requestFeedsTopicVideo(String topicId, long beforeTime,
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

    public JsonObjectRequest requestKooRank(Response.Listener<JSONObject> listener,
                                            Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.KOO_RANK_URL, listener, errorListener);
    }

    public JsonObjectRequest requestFriendProfile(String uid,
                                                  Response.Listener<JSONObject> listener,
                                                  Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getFriendProfileUrl(uid), listener, errorListener);
    }

    public JsonObjectRequest requestCommonTopic(String uid,
                                                Response.Listener<JSONObject> listener,
                                                Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getCommonTopicUrl(uid), listener, errorListener);
    }

    public JsonObjectRequest requestCommonFriend(String uid,
                                                 Response.Listener<JSONObject> listener,
                                                 Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getCommonFriendUrl(uid), listener, errorListener);
    }

    public JsonObjectRequest requestTask(Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.TASK_URL, listener, errorListener);
    }

    public JsonObjectRequest requestTask(long before,
                                         Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getTaskUrl(before), listener, errorListener);
    }

    public JsonObjectRequest requestTaskDetail(String uid,
                                               Response.Listener<JSONObject> listener,
                                               Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getTaskDetailUrl(uid), listener, errorListener);
    }

    public JsonObjectRequest requestTaskDetail(String uid, long before,
                                               Response.Listener<JSONObject> listener,
                                               Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getTaskDetailUrl(uid, before), listener, errorListener);
    }

    public JsonObjectRequest requestDanmakuTab(Response.Listener<JSONObject> listener,
                                               Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.DANMAKU_TAB_URL, listener, errorListener);
    }

    public JsonObjectRequest requestDanmakuTab(long before,
                                               Response.Listener<JSONObject> listener,
                                               Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getDanmakuTabUrl(before), listener, errorListener);
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
        return standardPostRequest(UrlHelper.SEND_DANMAKU_URL, requestJson, listener, errorListener);
    }

    public JSONObject requestQiniuThumbTokenSync()
            throws InterruptedException, ExecutionException, TimeoutException {
        return standardGetRequestSync(UrlHelper.REQUEST_QINIU_THUMB_TOKEN_URL);
    }

    public JSONObject requestQiniuVideoTokenSync()
            throws InterruptedException, ExecutionException, TimeoutException {
        return standardGetRequestSync(UrlHelper.REQUEST_QINIU_VIDEO_TOKEN_URL);
    }

    public JsonObjectRequest requestWorldHotTopic(Response.Listener<JSONObject> listener,
                                                  Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.REQUEST_WORLD_HOT_URL, listener, errorListener);
    }

    public JsonObjectRequest searchTopic(String keyWord,
                                         Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getSearchTopicUrl(keyWord), listener, errorListener);
    }

    public JsonObjectRequest addTopic(String title,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("content", title);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return standardPostRequest(UrlHelper.ADD_TOPIC_URL, requestJson, listener, errorListener);
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
        return standardPostRequest(UrlHelper.SEND_INVITATION_URL, requestObject, listener, errorListener);
    }

    public JsonObjectRequest requestWorldTopicVideo(String topicId, int page,
                                                    Response.Listener<JSONObject> listener,
                                                    Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getWorldTopicVideoUrl(topicId, page),
                listener, errorListener);
    }

    public JsonObjectRequest requestUserTopic(String uid, String topicId,
                                              Response.Listener<JSONObject> listener,
                                              Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getUserTopicUrl(uid, topicId), listener, errorListener);
    }

    public JsonObjectRequest requestUserTopic(String uid, String topicId, long time,
                                              Response.Listener<JSONObject> listener,
                                              Response.ErrorListener errorListener) {
        return standardGetRequest(UrlHelper.getUserTopicUrl(uid, topicId, time),
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

    private JSONObject standardGetRequestSync(String url)
            throws InterruptedException, ExecutionException, TimeoutException {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        standardGetRequest(url, future, future);
        return future.get(SYNC_REQUEST_TIMEOUT, SYNC_REQUEST_TIME_UNIT);
    }


    class StdErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
        }
    }
}
