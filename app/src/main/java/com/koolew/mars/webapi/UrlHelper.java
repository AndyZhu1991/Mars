package com.koolew.mars.webapi;

import android.net.Uri;

import com.koolew.mars.infos.MyAccountInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by jinchangzhu on 5/27/15.
 */
public class UrlHelper {

    private static final String TEST_API_URL = "http://test.koolew.cn/";
    private static final String REAL_API_URL = "http://api.koolew.com/";

    private static final String BASE_URL = REAL_API_URL;

    private static final String V1_URL = BASE_URL + "v1/";
    private static final String V2_URL = BASE_URL + "v2/";

    // v1 api
    public static final String FEEDS_TOPIC_URL = V1_URL + "feeds/topic";
    private static final String TOPIC_VIDEO_FRIEND_URL = V1_URL + "feeds";
    public static final String SNS_LOGIN_URL = V1_URL + "user/login/sns";
    public static final String ADD_FRIEND_URL = V1_URL + "friend/apply";
    public static final String CONTACT_FRIEND_RECOMMEND_URL = V1_URL + "contact/address";
    public static final String USER_INFO_URL = V1_URL + "user/info";
    private static final String REQUEST_PASSWORD_URL = V1_URL + "user/code";
    public static final String LOGIN_URL = V1_URL + "user/login";
    public static final String SNS_SIGNUP_URL = V1_URL + "user/signup/sns";
    public static final String REQUEST_QINIU_AVATAR_TOKEN_URL = V1_URL + "qiniu/uptoken?type=avatar";
    public static final String CURRENT_FRIEND_URL = V1_URL + "friend";
    public static final String KOO_RANK_URL = V1_URL + "koo/rank";
    private static final String COMMON_TOPIC_URL = V1_URL + "profile/topic/common";
    private static final String COMMON_FRIEND_URL = V1_URL + "profile/friend/common";
    public static final String SEND_DANMAKU_URL = V1_URL + "comment";
    public static final String REQUEST_QINIU_THUMB_TOKEN_URL = V1_URL + "qiniu/uptoken?type=thumbnail";
    public static final String REQUEST_QINIU_VIDEO_TOKEN_URL = V1_URL + "qiniu/uptoken?type=video";
    public static final String REQUEST_WORLD_HOT_URL = V1_URL + "world/hot";
    private static final String SEARCH_TOPIC_URL = V1_URL + "topic/search";
    public static final String ADD_TOPIC_URL = V1_URL + "topic";
    public static final String SEND_INVITATION_URL = V1_URL + "call";
    private static final String SEARCH_USER_URL = V1_URL + "user/search";
    public static final String REJECT_FRIEND_PADDING_URL = V1_URL + "friend/reject";
    public static final String AGREE_FRIEND_ADD_URL = V1_URL + "friend/agree";
    public static final String DELETE_FRIEND_URL = V1_URL + "friend/delete";
    private static final String SINGLE_VIDEO_URL = V1_URL + "video";
    public static final String KOO_URL = V1_URL + "koo";
    public static final String IGNORE_INVITATION_URL = V1_URL + "call/ignore";
    public static final String DEVICE_LOGIN_URL = V1_URL + "device/login";
    public static final String DEVICE_PUSH_URL = V1_URL + "device/push";
    public static final String VIDEO_DELETE_URL = V1_URL + "video/delete";
    public static final String VIDEO_AGAINST_URL = V1_URL + "video/against";
    public static final String DEVICE_LOGOUT_URL = V1_URL + "device/logout";

    // v2 api
    public static final String INVOLVE_URL = V2_URL + "feeds/involve";
    public static final String FRIEND_RECOMMEND_URL = V2_URL + "friend/recommend";
    private static final String FRIEND_PROFILE_URL = V2_URL + "profile";
    public static final String TASK_URL = V2_URL + "task";
    private static final String TASK_DETAIL_URL = V2_URL + "task/detail";
    public static final String DANMAKU_TAB_URL = V2_URL + "activity/comment";
    public static final String TOPIC_VIDEO_WORLD_URL = V2_URL + "world/topic";
    private static final String USER_TOPIC_URL = V2_URL + "user/topic/info";
    public static final String IGNORE_RECOMMEND_URL = V2_URL + "friend/recommend/ignore";
    public static final String KOO_RANK_V2_URL = V2_URL + "koo/rank";
    public static final String CONTACT_FRIEND_RECOMMEND_V2_URL = V2_URL + "contact/recommend";

    public static final long REQUEST_TIMEOUT = 10;
    public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;


    // v1 api
    public static String getTopicVideoFriendUrl(String topicId) {
        return new StringBuilder().append(TOPIC_VIDEO_FRIEND_URL)
                .append("?topic_id=").append(topicId).toString();
    }
    public static String getTopicVideoFriendUrl(String topicId, long beforeTime) {
        return new StringBuilder().append(getTopicVideoFriendUrl(topicId))
                .append("&time=").append(beforeTime).toString();
    }

    public static String getRequestPasswordMessageUrl(String phoneNum) {
        return new StringBuilder(REQUEST_PASSWORD_URL).append("?phone=").append(phoneNum).toString();
    }

    public static String getRequestPasswordCallUrl(String phoneNum) {
        return new StringBuilder(REQUEST_PASSWORD_URL)
                .append("?phone=").append(phoneNum).append("&type=1").toString();
    }

    public static String getFeedsTopicUrl(long before) {
        return new StringBuilder(FEEDS_TOPIC_URL).append("?before=").append(before).toString();
    }

    public static String getCommonTopicUrl(String uid) {
        return new GetUrlBuilder(COMMON_TOPIC_URL)
                .addParameter("uid", uid)
                .build();
    }

    public static String getCommonFriendUrl(String uid) {
        return new GetUrlBuilder(COMMON_FRIEND_URL)
                .addParameter("uid", uid)
                .build();
    }

    public static String getSearchTopicUrl(String keyWord) {
        return Uri.parse(SEARCH_TOPIC_URL)
                .buildUpon()
                .appendQueryParameter("query", keyWord)
                .build().toString();
    }

    public static String getSearchUserUrl(String keyWord) {
        return Uri.parse(SEARCH_USER_URL)
                .buildUpon()
                .appendQueryParameter("query", keyWord)
                .build().toString();
    }

    public static String getSingleVideoUrl(String videoId) {
        return Uri.parse(SINGLE_VIDEO_URL)
                .buildUpon()
                .appendQueryParameter("video_id", videoId)
                .build().toString();
    }

    public static String getKooRankUrl(String uid) {
        return Uri.parse(KOO_RANK_V2_URL)
                .buildUpon()
                .appendQueryParameter("uid", uid)
                .build().toString();
    }


    // v2 api
    public static String getInvolveUrl(int page) {
        return INVOLVE_URL + "?page=" + page;
    }

    public static String getFriendProfileUrl(String uid) {
        return new StringBuilder(FRIEND_PROFILE_URL).append("?uid=").append(uid).toString();
    }

    public static String getTaskUrl(long before) {
        return new GetUrlBuilder(TASK_URL)
                .addParameter("before", before)
                .build();
    }

    public static String getTaskDetailUrl(String uid) {
        return new GetUrlBuilder(TASK_DETAIL_URL)
                .addParameter("uid", uid)
                .build();
    }

    public static String getTaskDetailUrl(String uid, long before) {
        return new GetUrlBuilder(TASK_DETAIL_URL)
                .addParameter("uid", uid)
                .addParameter("before", before)
                .build();
    }

    public static String getDanmakuTabUrl(long before) {
        return new GetUrlBuilder(DANMAKU_TAB_URL)
                .addParameter("before", before)
                .build();
    }

    public static String getWorldTopicVideoUrl(String topicId, int page) {
        return Uri.parse(TOPIC_VIDEO_WORLD_URL)
                .buildUpon()
                .appendQueryParameter("topic_id", topicId)
                .appendQueryParameter("page", String.valueOf(page))
                .build().toString();
    }

    public static String getUserTopicUrl(String uid, String topicId) {
        return Uri.parse(USER_TOPIC_URL)
                .buildUpon()
                .appendQueryParameter("uid", uid)
                .appendQueryParameter("topic_id", topicId)
                .build().toString();
    }

    public static String getUserTopicUrl(String uid, String topicId, long time) {
        return Uri.parse(getUserTopicUrl(uid, topicId))
                .buildUpon()
                .appendQueryParameter("time", String.valueOf(time))
                .build().toString();
    }


    public static Map<String, String> getStandardPostHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", MyAccountInfo.getToken());
        return headers;
    }
}
