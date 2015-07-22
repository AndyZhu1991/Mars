package com.koolew.mars.qiniu;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/22/15.
 */
public class UploadFuture implements UpCompletionHandler {

    private UploadResponse mUploadResponse;

    public synchronized UploadResponse upload() throws InterruptedException {
        wait(0);
        return mUploadResponse;
    }

    @Override
    public synchronized void complete(String key, ResponseInfo info, JSONObject response) {
        mUploadResponse = new UploadResponse(key, info, response);
        notifyAll();
    }

    public static class UploadResponse {
        private String key;
        private ResponseInfo info;
        private JSONObject response;

        public UploadResponse(String key, ResponseInfo info, JSONObject response) {
            this.key = key;
            this.info = info;
            this.response = response;
        }

        public ResponseInfo getInfo() {
            return info;
        }

        public String getKey() {
            return key;
        }

        public JSONObject getResponse() {
            return response;
        }
    }
}
