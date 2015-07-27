package com.koolew.mars.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.koolew.mars.FeedsTopicActivity;
import com.koolew.mars.FriendInfoActivity;
import com.koolew.mars.PushWrapperActivity;
import com.koolew.mars.TaskActivity;
import com.koolew.mars.WebActivity;

/**
 * Created by jinchangzhu on 7/15/15.
 */
public class UriProcessor {

    private Context mContext;

    public UriProcessor(Context context) {
        mContext = context;
    }

    public void process(String uriString) {
        Uri uri = Uri.parse(uriString);
        String scheme = uri.getScheme();
        if (scheme.equals("koolew")) {
            processKoolewUri(uri);
        }
        else if (scheme.equals("http") || scheme.equals("https")) {
            processUrl(uriString);
        }
        else {
            processOtherUri(uri);
        }
    }

    private void processKoolewUri(Uri uri) {
        String authority = uri.getAuthority();
        if (authority.equals("video")) {
            // TODO:
        }
        else if (authority.equals("topic")) {
            Intent intent = new Intent(mContext, FeedsTopicActivity.class);
            intent.putExtra(FeedsTopicActivity.KEY_TOPIC_ID, uri.getQueryParameter("topic_id"));
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

    private void processUrl(String url) {
        Intent intent = new Intent(mContext, WebActivity.class);
        intent.putExtra(WebActivity.KEY_URL, url);
        mContext.startActivity(intent);
    }

    private void processOtherUri(Uri uri) {
        // TODO
    }

    private void switchToTab(String tabId) {
        // TODO
        if (tabId.equals("feeds") || tabId.equals("suggesstion")) {
            Intent intent = new Intent(mContext, PushWrapperActivity.class);
            intent.putExtra(PushWrapperActivity.KEY_TAB_TYPE, tabId);
            mContext.startActivity(intent);
        }
        else if (tabId.equals("assignment")) {
            mContext.startActivity(new Intent(mContext, TaskActivity.class));
        }
        else if (tabId.equals("comment")) {
        }
        else if (tabId.equals("me")) {
        }
    }
}
