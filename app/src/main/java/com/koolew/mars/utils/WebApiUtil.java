package com.koolew.mars.utils;

import com.koolew.mars.infos.MyAccountInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by jinchangzhu on 5/27/15.
 */
public class WebApiUtil {

    private static final String BASE_URL = "http://test.koolew.com/v1/";

    public static final String FEEDS_TOPIC_URL = BASE_URL + "feeds/topic";

    public static final long REQUEST_TIMEOUT = 10;
    public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    public static Map<String, String> getStandardPostHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", MyAccountInfo.getToken());
        return headers;
    }
}
