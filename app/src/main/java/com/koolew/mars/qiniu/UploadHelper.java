package com.koolew.mars.qiniu;

import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.utils.Mp4ParserUtil;
import com.koolew.mars.webapi.ApiWorker;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by jinchangzhu on 7/22/15.
 */
public class UploadHelper {

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILED = -1;

    public static int uploadVideo(String topicId, String videoPath, String thumbPath) {
        try {
            JSONObject thumbTokenJson = ApiWorker.getInstance().requestQiniuThumbTokenSync();
            String thumbToken = null;
            if (thumbTokenJson.getInt("code") == 0) {
                thumbToken = thumbTokenJson.getJSONObject("result").getString("thumbnail");
            }

            // 重用 uploadManager。一般地，只需要创建一个 uploadManager 对象
            UploadManager uploadManager = new UploadManager();

            UploadFuture thumbFuture = new UploadFuture();
            uploadManager.put(thumbPath, null, thumbToken, thumbFuture,
                    new UploadOptions(null, null, false, null, null));
            UploadFuture.UploadResponse thumbResponse =  thumbFuture.upload();

            JSONObject videoTokenJson = ApiWorker.getInstance().requestQiniuVideoTokenSync();
            String videoToken = null;
            if (videoTokenJson.getInt("code") == 0) {
                videoToken = videoTokenJson.getJSONObject("result").getString("video");
            }

            UploadFuture videoFuture = new UploadFuture();
            Map<String, String> videoOption = new HashMap<String, String>();
            videoOption.put("x:thumb", thumbResponse.getResponse().getString("key"));
            videoOption.put("x:uid", MyAccountInfo.getUid());
            videoOption.put("x:type", "video");
            videoOption.put("x:tid", topicId);
            videoOption.put("x:duration", String.valueOf(Mp4ParserUtil.getDuration(videoPath)));
            uploadManager.put(videoPath, "$(key).mp4", videoToken, videoFuture,
                    new UploadOptions(videoOption, null, false, null, null));
            videoFuture.upload();

            return RESULT_SUCCESS;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return RESULT_FAILED;
    }
}
