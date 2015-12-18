package com.koolew.mars.webapi;

import android.net.Uri;
import android.text.TextUtils;

import com.koolew.mars.infos.MyAccountInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by jinchangzhu on 5/27/15.
 */
public class UrlHelper {

    private static final String TEST_API_URL = "http://test.koolew.cn/";
    private static final String REAL_API_URL = "https://api.koolew.com/";

    private static final String BASE_URL = TEST_API_URL;

    private static final String V1_URL = BASE_URL + "v1/";
    private static final String V2_URL = BASE_URL + "v2/";
    private static final String V3_URL = BASE_URL + "v3/";
    private static final String V4_URL = BASE_URL + "v4/";
    private static final String V5_URL = BASE_URL + "v5/";

    // Account
    public static final String SNS_LOGIN_URL = V4_URL + "account/login/sns";
    public static final String USER_INFO_URL = V4_URL + "account/profile";
    private static final String REQUEST_PASSWORD_URL = V4_URL + "account/login/validate";
    public static final String LOGIN_URL = V4_URL + "account/login/mobile";
    public static final String SNS_SIGNUP_URL = V4_URL + "account/signup/sns";
    public static final String DEVICE_LOGIN_URL = V4_URL + "account/device/bind";
    public static final String DEVICE_PUSH_URL = V4_URL + "account/push";
    public static final String DEVICE_LOGOUT_URL = V4_URL + "account/device/unbind";
    public static final String BIND_ALIPAY_URL = V4_URL + "account/alipay";
    public static final String USER_LOCATION_URL = V4_URL + "account/location";

    // Upload
    public static final String REQUEST_QINIU_AVATAR_TOKEN_URL = V4_URL + "upload/token/qiniu?type=avatar";
    public static final String REQUEST_QINIU_THUMB_TOKEN_URL = V4_URL + "upload/token/qiniu?type=thumbnail";
    public static final String REQUEST_QINIU_VIDEO_TOKEN_URL = V4_URL + "upload/token/qiniu?type=video";
    public static final String REQUEST_QINIU_MOVIE_TOKEN_URL = V4_URL + "upload/token/qiniu?type=movie";

    // Search
    private static final String SEARCH_USER_URL = V4_URL + "search/user";
    private static final String SEARCH_TOPIC_URL = V4_URL + "search/topic";

    // Users
    public static final String FRIEND_PROFILE_URL = V4_URL + "users/show";
    private static final String TIMELINE_URL = V4_URL + "users/timeline";
    private static final String USER_TOPIC_URL = V4_URL + "users/media";

    // Friendships
    public static final String CURRENT_FRIEND_URL = V4_URL + "friendships/friends/bilateral";
    public static final String FRIEND_RECOMMEND_URL = V4_URL + "friendships/friends/recommend";
    public static final String CONTACT_FRIEND_RECOMMEND_URL = V4_URL + "friendships/contact";
    public static final String FRIEND_FOLLOW_URL = V4_URL + "friendships/create";
    public static final String FRIEND_UNFOLLOW_URL = V4_URL + "friendships/destroy";
    public static final String FRIEND_FOLLOWS_URL = V4_URL + "friendships/friends";
    public static final String FRIEND_FANS_URL = V4_URL + "friendships/followers";
    public static final String KOO_RANK_URL = V4_URL + "friendships/show/koo_rank";
    private static final String COMMON_TOPIC_URL = V4_URL + "friendships/show/topic_common";

    // Profit
    public static final String INCOME_DESC_URL = V4_URL + "profit";
    private static final String INCOME_ANALYSIS_URL = V4_URL + "profit/video";
    public static final String CASH_OUT_URL = V4_URL + "profit/withdraw";
    public static final String CASH_OUT_RECORD_URL = V4_URL + "profit/withdraw/history";

    // Feeds
    public static final String FEEDS_TOPIC_URL = V5_URL + "feeds/topics";
    private static final String TOPIC_VIDEO_FRIEND_URL = V4_URL + "feeds/media";
    public static final String TASK_URL = V4_URL + "task";
    private static final String TASK_DETAIL_URL = V4_URL + "task/detail";
    public static final String SEND_INVITATION_URL = V4_URL + "task";
    public static final String IGNORE_INVITATION_URL = V4_URL + "task/ignore";

    // Public
    public static final String REQUEST_WORLD_HOT_URL = V4_URL + "discovery/recommend";
    public static final String BANNER_URL = V4_URL + "discovery/banner";
    public static final String SQUARE_URL = V5_URL + "discovery/square";
    private static final String FEEDS_HOT_URL = V4_URL + "discovery/hot";
    private static final String GUESS_JUDGE_URL = V5_URL + "discovery/judge";
    private static final String SQUARE_DETAIL_URL = V5_URL + "discovery/square/detail";

    public static final String CHECK_VERSION_URL = BASE_URL + "version";

    // Notification
    public static final String NOTIFICATION_BRIEF_URL = V4_URL + "notification/unread";
    public static final String DANMAKU_TAB_URL = V4_URL + "notification/comment";
    public static final String NOTIFICATION_URL = V4_URL + "notification/activity";
    public static final String NOTIFICATION_KOO_URL = V4_URL + "notification/koo";

    // Topic
    public static final String TOPIC_URL = V5_URL + "topic"; // New Api category=movie/video
    public static final String TOPIC_VIDEO_WORLD_URL = V4_URL + "topic/media";
    public static final String ADD_TOPIC_URL = V4_URL + "topic/create";
    public static final String EDIT_TOPIC_DESC_URL = V4_URL + "topic";
    private static final String RECOMMEND_TOPIC_URL = V5_URL + "topic/recommend";

    // Video
    private static final String SINGLE_VIDEO_URL = V4_URL + "video";
    private static final String VIDEO_COMMENT_URL = V4_URL + "video/comment";
    public static final String SEND_DANMAKU_URL = V4_URL + "video/comment";
    private static final String VIDEO_KOO_RANK_URL = V4_URL + "video/koo";
    public static final String VIDEO_AGAINST_URL = V4_URL + "video/downvote";
    public static final String KOO_URL = V4_URL + "video/koo";
    public static final String VIDEO_DELETE_URL = V4_URL + "video/delete";

    // Tag
    public static final String VIDEO_TAG_URL = V5_URL + "tag?category=video";
    public static final String MOVIE_TAG_URL = V5_URL + "tag?category=movie";

    public static final long REQUEST_TIMEOUT = 10;
    public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;



    public static String getTopicVideoFriendUrl(String topicId) {
        return Uri.parse(TOPIC_VIDEO_FRIEND_URL)
                .buildUpon()
                .appendQueryParameter("topic_id", topicId)
                .build().toString();
    }
    public static String getTopicVideoFriendUrl(String topicId, long beforeTime) {
        return Uri.parse(getTopicVideoFriendUrl(topicId))
                .buildUpon()
                .appendQueryParameter("before", String.valueOf(beforeTime))
                .build().toString();
    }

    public static String getRequestPasswordMessageUrl(String phoneNum) {
        return Uri.parse(REQUEST_PASSWORD_URL)
                .buildUpon()
                .appendQueryParameter("phone", phoneNum)
                .build().toString();
    }

    public static String getRequestPasswordCallUrl(String phoneNum) {
        return Uri.parse(REQUEST_PASSWORD_URL)
                .buildUpon()
                .appendQueryParameter("type", phoneNum)
                .build().toString();
    }

    public static String getFeedsTopicUrl(long before) {
        return Uri.parse(FEEDS_TOPIC_URL)
                .buildUpon()
                .appendQueryParameter("before", String.valueOf(before))
                .build().toString();
    }

    public static String getCommonTopicUrl(String uid) {
        return Uri.parse(COMMON_TOPIC_URL)
                .buildUpon()
                .appendQueryParameter("uid", uid)
                .build().toString();
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
        return Uri.parse(KOO_RANK_URL)
                .buildUpon()
                .appendQueryParameter("uid", uid)
                .build().toString();
    }

    public static String getVideoKooRankUrl(String videoId, int page) {
        return Uri.parse(VIDEO_KOO_RANK_URL)
                .buildUpon()
                .appendQueryParameter("video_id", videoId)
                .appendQueryParameter("page", String.valueOf(page))
                .build().toString();
    }

    public static String getInvolveUrl(int page) {
        return Uri.parse(TIMELINE_URL)
                .buildUpon()
                .appendQueryParameter("page", String.valueOf(page))
                .appendQueryParameter("sort_by", "count")
                .build().toString();
    }

    public static String getUserTimelineUrl(String uid, long before) {
        return Uri.parse(getUserTimelineUrl(uid))
                .buildUpon()
                .appendQueryParameter("before", String.valueOf(before))
                .build().toString();
    }

    public static String getUserTimelineUrl(String uid) {
        return Uri.parse(TIMELINE_URL)
                .buildUpon()
                .appendQueryParameter("sort_by", "time")
                .appendQueryParameter("uid", uid)
                .build().toString();
    }

    public static String getFriendProfileUrl(String uid) {
        return Uri.parse(FRIEND_PROFILE_URL)
                .buildUpon()
                .appendQueryParameter("uid", uid)
                .build().toString();
    }

    public static String getFriendProfileUrl(String uid, long before) {
        return Uri.parse(FRIEND_PROFILE_URL)
                .buildUpon()
                .appendQueryParameter("uid", uid)
                .appendQueryParameter("before", String.valueOf(before))
                .build().toString();
    }

    public static String getTaskUrl(long before) {
        return Uri.parse(TASK_URL)
                .buildUpon()
                .appendQueryParameter("before", String.valueOf(before))
                .build().toString();
    }

    public static String getTaskDetailUrl(String uid) {
        return Uri.parse(TASK_DETAIL_URL)
                .buildUpon()
                .appendQueryParameter("uid", uid)
                .build().toString();
    }

    public static String getTaskDetailUrl(String uid, long before) {
        return Uri.parse(TASK_DETAIL_URL)
                .buildUpon()
                .appendQueryParameter("uid", uid)
                .appendQueryParameter("before", String.valueOf(before))
                .build().toString();
    }

    public static String getDanmakuTabUrl(long before) {
        return Uri.parse(DANMAKU_TAB_URL)
                .buildUpon()
                .appendQueryParameter("before", String.valueOf(before))
                .build().toString();
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
                .appendQueryParameter("before", String.valueOf(time))
                .build().toString();
    }

    public static String getVideoCommentUrl(String videoId, long before) {
        Uri.Builder builder = Uri.parse(VIDEO_COMMENT_URL)
                .buildUpon()
                .appendQueryParameter("video_id", videoId);
        if (before != 0 && before != Long.MAX_VALUE) {
            builder.appendQueryParameter("before", String.valueOf(before));
        }
        return builder.build().toString();
    }

    public static String getCurrentFriendUrl(long before) {
        return Uri.parse(CURRENT_FRIEND_URL)
                .buildUpon()
                .appendQueryParameter("before", String.valueOf(before))
                .build().toString();
    }

    public static String getFriendFansUrl(String uid) {
        return getFriendFansUrl(uid, Long.MAX_VALUE);
    }

    public static String getFriendFansUrl(long before) {
        return getFriendFansUrl(null, before);
    }

    public static String getFriendFansUrl(String uid, long before) {
        Uri.Builder builder = Uri.parse(FRIEND_FANS_URL).buildUpon();
        if (!TextUtils.isEmpty(uid)) {
            builder.appendQueryParameter("uid", uid);
        }
        if (before != 0 && before != Long.MAX_VALUE) {
            builder.appendQueryParameter("before", String.valueOf(before));
        }
        return builder.build().toString();
    }

    public static String getFriendFollowsUrl(String uid) {
        return getFriendFollowsUrl(uid, Long.MAX_VALUE);
    }

    public static String getFriendFollowsUrl(long before) {
        return getFriendFollowsUrl(null, before);
    }

    public static String getFriendFollowsUrl(String uid, long before) {
        Uri.Builder builder = Uri.parse(FRIEND_FOLLOWS_URL).buildUpon();
        if (!TextUtils.isEmpty(uid)) {
            builder.appendQueryParameter("uid", uid);
        }
        if (before != 0 && before != Long.MAX_VALUE) {
            builder.appendQueryParameter("before", String.valueOf(before));
        }
        return builder.build().toString();
    }

    public static String getFriendRecommendUrl(double min) {
        return Uri.parse(FRIEND_RECOMMEND_URL)
                .buildUpon()
                .appendQueryParameter("min", String.valueOf(min))
                .build().toString();
    }

    public static String getDefaultPlayGroupUrl(String squareId) {
        return Uri.parse(GUESS_JUDGE_URL)
                .buildUpon()
                .appendQueryParameter("square_id", squareId)
                .build()
                .toString();
    }

    public static String getJudgeUrl(String squareId, String videoId) {
        return Uri.parse(GUESS_JUDGE_URL)
                .buildUpon()
                .appendQueryParameter("square_id", squareId)
                .appendQueryParameter("video_id", videoId)
                .build()
                .toString();
    }

    public static String getPayPlayUrl() {
        return Uri.parse(GUESS_JUDGE_URL)
                .buildUpon()
                .appendQueryParameter("pay", String.valueOf(1))
                .build()
                .toString();
    }

    public static String getFeedsHotUrl(int page) {
        return Uri.parse(FEEDS_HOT_URL)
                .buildUpon()
                .appendQueryParameter("page", String.valueOf(page))
                .build().toString();
    }

    public static String getNotificationUrl(long before) {
        return Uri.parse(NOTIFICATION_URL)
                .buildUpon()
                .appendQueryParameter("before", String.valueOf(before))
                .build().toString();
    }

    public static String getIncomeAnalysisUrl(int page) {
        return Uri.parse(INCOME_ANALYSIS_URL)
                .buildUpon()
                .appendQueryParameter("page", String.valueOf(page))
                .build().toString();
    }

    public static String getCashOutRecordUrl(long before) {
        return Uri.parse(CASH_OUT_RECORD_URL)
                .buildUpon()
                .appendQueryParameter("before", String.valueOf(before))
                .build().toString();
    }

    public static String getNotificationKooUrl(long before) {
        return Uri.parse(NOTIFICATION_KOO_URL)
                .buildUpon()
                .appendQueryParameter("before", String.valueOf(before))
                .build().toString();
    }

    public static String getMovieUrl(int page) {
        return Uri.parse(TOPIC_URL)
                .buildUpon()
                .appendQueryParameter("category", "movie")
                .appendQueryParameter("page", String.valueOf(page))
                .build().toString();
    }

    public static String getRecommendTopicUrl(String category, int page) {
        return Uri.parse(RECOMMEND_TOPIC_URL)
                .buildUpon()
                .appendQueryParameter("category", category)
                .appendQueryParameter("page", String.valueOf(page))
                .build().toString();
    }

    public static String getTopicUrl(String category, String tag, int page) {
        return Uri.parse(TOPIC_URL)
                .buildUpon()
                .appendQueryParameter("category", category)
                .appendQueryParameter("tag", tag)
                .appendQueryParameter("page", String.valueOf(page))
                .build().toString();
    }

    public static String getSquareDetailUrl(String squareId) {
        return Uri.parse(SQUARE_DETAIL_URL)
                .buildUpon()
                .appendQueryParameter("square_id", squareId)
                .build().toString();
    }


    public static Map<String, String> getStandardPostHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", MyAccountInfo.getToken());
        return headers;
    }
}
