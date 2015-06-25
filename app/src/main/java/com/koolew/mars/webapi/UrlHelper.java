package com.koolew.mars.webapi;

import com.koolew.mars.infos.MyAccountInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by jinchangzhu on 5/27/15.
 */
public class UrlHelper {

    private static final String BASE_URL = "http://test.koolew.cn/v1/";

    public static final String FEEDS_TOPIC_URL = BASE_URL + "feeds/topic";
    private static final String TOPIC_VIDEO_FRIEND_URL = BASE_URL + "feeds";
    public static final String SNS_LOGIN_URL = BASE_URL + "user/login/sns";
    public static final String ADD_FRIEND_URL = BASE_URL + "friend/apply";
    public static final String FRIEND_RECOMMEND_URL = BASE_URL + "contact/address";
    public static final String USER_INFO_URL = BASE_URL + "user/info";
    private static final String REQUEST_PASSWORD_URL = BASE_URL + "user/code";
    public static final String LOGIN_URL = BASE_URL + "user/login";
    public static final String SNS_SIGNUP_URL = BASE_URL + "user/signup/sns";
    public static final String REQUEST_QINIU_TOKEN_URL = BASE_URL + "qiniu/uptoken?type=avatar";

    public static final long REQUEST_TIMEOUT = 10;
    public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    public static String getTopicVideoFriendUrl(String topicId) {
        return TOPIC_VIDEO_FRIEND_URL + "?topic_id=" + topicId;
    }

    public static String getRequestPasswordUrl(String phoneNum) {
        return REQUEST_PASSWORD_URL + "?phone=" + phoneNum;
    }

    public static Map<String, String> getStandardPostHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", MyAccountInfo.getToken());
        return headers;
    }
}
