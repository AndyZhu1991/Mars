package com.koolew.mars.qiniu;

import android.text.TextUtils;

import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.android.mp4parserutil.Mp4ParserUtil;
import com.koolew.android.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.utils.Etag;

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

    public static BaseVideoInfo uploadVideo(String topicId, String videoPath, String thumbPath,
                                            String tagId, int privacy) {
        UploadFuture.UploadResponse uploadResponse =
                uploadMp4(topicId, videoPath, thumbPath, privacy, false, null, tagId);
        if (uploadResponse == null) {
            return null;
        }
        try {
            return new BaseVideoInfo(uploadResponse.getResponse()
                    .getJSONObject("result").getJSONObject("video"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BaseVideoInfo uploadMovie(String topicId, String videoPath, String thumbPath,
                                            int privacy, String from) {
        UploadFuture.UploadResponse uploadResponse =
                uploadMp4(topicId, videoPath, thumbPath, privacy, true, from, null);
        if (uploadResponse == null) {
            return null;
        }
        try {
            return new BaseVideoInfo(uploadResponse.getResponse()
                    .getJSONObject("result").getJSONObject("video"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static UploadFuture.UploadResponse uploadMp4(String topicId, String videoPath,
                                                         String thumbPath, int privacy,
                                                         boolean isMovie, String from,
                                                         String tagId) {
        try {
            JSONObject thumbTokenJson = ApiWorker.getInstance()
                    .doGetRequestSync(UrlHelper.REQUEST_QINIU_THUMB_TOKEN_URL);
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

            String videoToken = null;
            if (!isMovie) {
                JSONObject videoTokenJson = ApiWorker.getInstance()
                        .doGetRequestSync(UrlHelper.REQUEST_QINIU_VIDEO_TOKEN_URL);
                if (videoTokenJson.getInt("code") == 0) {
                    videoToken = videoTokenJson.getJSONObject("result").getString("video");
                }
            }
            else {
                JSONObject videoTokenJson = ApiWorker.getInstance()
                        .doGetRequestSync(UrlHelper.REQUEST_QINIU_MOVIE_TOKEN_URL);
                if (videoTokenJson.getInt("code") == 0) {
                    videoToken = videoTokenJson.getJSONObject("result").getString("movie");
                }
            }

            String key = Etag.file(videoPath) + ".mp4";

            UploadFuture videoFuture = new UploadFuture();
            Map<String, String> videoOption = new HashMap<>();
            videoOption.put("x:thumb", thumbResponse.getResponse().getString("key"));
            videoOption.put("x:uid", MyAccountInfo.getUid());
            if (isMovie) {
                videoOption.put("x:type", "movie");
            }
            else {
                videoOption.put("x:type", "video");
            }
            videoOption.put("x:tid", topicId);
            videoOption.put("x:duration", String.valueOf(Mp4ParserUtil.getDuration(videoPath)));
            videoOption.put("x:privacy", String.valueOf(privacy));
            if (isMovie) {
                videoOption.put("x:platform", "android");
                videoOption.put("x:version_code", String.valueOf(Utils.getCurrentVersionCode()));
                videoOption.put("x:from", TextUtils.isEmpty(from) ? "" : from);
            }
            if (!isMovie && !TextUtils.isEmpty(tagId)) {
                videoOption.put("x:tag", tagId);
            }
            uploadManager.put(videoPath, key, videoToken, videoFuture,
                    new UploadOptions(videoOption, null, false, null, null));
            return videoFuture.upload();
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

        return null;
    }
}
