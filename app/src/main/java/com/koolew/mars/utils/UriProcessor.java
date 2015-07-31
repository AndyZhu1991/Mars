package com.koolew.mars.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.koolew.mars.CheckDanmakuActivity;
import com.koolew.mars.DanmakuTabActivity;
import com.koolew.mars.FriendInfoActivity;
import com.koolew.mars.KoolewWebActivity;
import com.koolew.mars.PushWrapperActivity;
import com.koolew.mars.TaskActivity;
import com.koolew.mars.WorldTopicActivity;

/**
 * Created by jinchangzhu on 7/15/15.
 */
public class UriProcessor {

    protected static final String TAB_FEEDS = "feeds";
    protected static final String TAB_SUGGESTION = "suggestion";
    protected static final String TAB_ASSIGNMENT = "assignment";
    protected static final String TAB_COMMENT = "comment";
    protected static final String TAB_ME = "me";

    private Context mContext;

    public UriProcessor(Context context) {
        mContext = context;
    }

    public boolean process(String uriString) {
        if (TextUtils.isEmpty(uriString)) {
            return false;
        }

        Uri uri = Uri.parse(uriString);
        String scheme = uri.getScheme();
        if (scheme.equals("koolew")) {
            processKoolewUri(uri);
            return true;
        }
        else if (scheme.equals("http") || scheme.equals("https")) {
            return processUrl(uriString);
        }
        else {
            processOtherUri(uri);
        }

        return false;
    }

    private void processKoolewUri(Uri uri) {
        String authority = uri.getAuthority();
        if (authority.equals("video")) {
            String videoId = uri.getQueryParameter("video_id");
            startSingleVideoActivity(videoId);
        }
        else if (authority.equals("topic")) {
            Intent intent = new Intent(mContext, WorldTopicActivity.class);
            intent.putExtra(WorldTopicActivity.KEY_TOPIC_ID, uri.getQueryParameter("topic_id"));
            mContext.startActivity(intent);
        }
        else if (authority.equals("user")) {
            Intent intent = new Intent(mContext, FriendInfoActivity.class);
            intent.putExtra(FriendInfoActivity.KEY_UID, uri.getQueryParameter("user_id"));
            mContext.startActivity(intent);
        }
        else if (authority.equals("tab")) {
            switchToTab(uri.getQueryParameter("tab_id"));
        }
    }

    protected boolean processUrl(String url) {
        Intent intent = new Intent(mContext, KoolewWebActivity.class);
        intent.putExtra(KoolewWebActivity.KEY_URL, url);
        mContext.startActivity(intent);
        return true;
    }

    private void processOtherUri(Uri uri) {
        // TODO
    }

    protected void switchToTab(String tabId) {
        // TODO
        if (tabId.equals(TAB_FEEDS) || tabId.equals(TAB_SUGGESTION)) {
            Intent intent = new Intent(mContext, PushWrapperActivity.class);
            intent.putExtra(PushWrapperActivity.KEY_TAB_TYPE, tabId);
            mContext.startActivity(intent);
        }
        else if (tabId.equals(TAB_ASSIGNMENT)) {
            mContext.startActivity(new Intent(mContext, TaskActivity.class));
        }
        else if (tabId.equals(TAB_COMMENT)) {
            Intent intent = new Intent(mContext, DanmakuTabActivity.class);
            mContext.startActivity(intent);
        }
        else if (tabId.equals(TAB_ME)) {
        }
    }

    private void startSingleVideoActivity(String videoId) {
        Intent intent = new Intent(mContext, CheckDanmakuActivity.class);
        intent.putExtra(CheckDanmakuActivity.KEY_VIDEO_ID, videoId);
        mContext.startActivity(intent);
    }
}
