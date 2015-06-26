package com.koolew.mars.webapi;

import com.koolew.mars.infos.MyAccountInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by jinchangzhu on 5/27/15.
 */
public class UrlHelper {

    private static final String BASE_URL = "http://test.koolew.cn/";

    private static final String V1_URL = BASE_URL + "v1/";
    private static final String V2_URL = BASE_URL + "v2/";

    // v1 api
    public static final String FEEDS_TOPIC_URL = V1_URL + "feeds/topic";
    private static final String TOPIC_VIDEO_FRIEND_URL = V1_URL + "feeds";
    public static final String SNS_LOGIN_URL = V1_URL + "user/login/sns";
    public static final String ADD_FRIEND_URL = V1_URL + "friend/apply";
    public static final String FRIEND_RECOMMEND_URL = V1_URL + "contact/address";
    public static final String USER_INFO_URL = V1_URL + "user/info";
    private static final String REQUEST_PASSWORD_URL = V1_URL + "user/code";
    public static final String LOGIN_URL = V1_URL + "user/login";
    public static final String SNS_SIGNUP_URL = V1_URL + "user/signup/sns";
    public static final String REQUEST_QINIU_TOKEN_URL = V1_URL + "qiniu/uptoken?type=avatar";

    // v2 api
    public static final String INVOLVE_URL = V2_URL + "feeds/involve";

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

    public static String getRequestPasswordUrl(String phoneNum) {
        return REQUEST_PASSWORD_URL + "?phone=" + phoneNum;
    }


    // v2 api
    public static String getInvolveUrl(int page) {
        return INVOLVE_URL + "?page=" + page;
    }


    public static Map<String, String> getStandardPostHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", MyAccountInfo.getToken());
        return headers;
    }
}
