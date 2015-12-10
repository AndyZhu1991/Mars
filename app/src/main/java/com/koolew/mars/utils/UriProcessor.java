package com.koolew.mars.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.koolew.mars.FriendInfoActivity;
import com.koolew.mars.KoolewWebActivity;
import com.koolew.mars.MessagesActivity;
import com.koolew.mars.PushWrapperActivity;
import com.koolew.mars.SingleMediaFragment;
import com.koolew.mars.TitleFragmentActivity;
import com.koolew.mars.TodayIncomeActivity;
import com.koolew.mars.TopicMediaActivity;

/**
 * Created by jinchangzhu on 7/15/15.
 */
public class UriProcessor {

    protected static final String AUTH_VIDEO = "video";
    protected static final String AUTH_TOPIC = "topic";
    protected static final String AUTH_USER = "user";
    protected static final String AUTH_TAB = "tab";

    protected static final String KEY_VIDEO_ID = "video_id";
    protected static final String KEY_TOPIC_ID = "topic_id";
    protected static final String KEY_USER_ID = "user_id";
    protected static final String KEY_TAB_ID = "tab_id";

    protected static final String TAB_FEEDS = "feeds";
    protected static final String TAB_SUGGESTION = "suggestion";
    protected static final String TAB_ASSIGNMENT = "assignment";
    protected static final String TAB_COMMENT = "comment";
    protected static final String TAB_ME = "me";
    protected static final String TAB_KOO = "koo";
    protected static final String TAB_PROFIT = "profit";

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
        if (authority.equals(AUTH_VIDEO)) {
            String videoId = uri.getQueryParameter(KEY_VIDEO_ID);
            startSingleVideoActivity(videoId);
        }
        else if (authority.equals(AUTH_TOPIC)) {
            Intent intent = newIntent(mContext, TopicMediaActivity.class);
            intent.putExtra(TopicMediaActivity.KEY_TOPIC_ID, uri.getQueryParameter(KEY_TOPIC_ID));
            intent.putExtra(TopicMediaActivity.KEY_TYPE, TopicMediaActivity.TYPE_WORLD);
            mContext.startActivity(intent);
        }
        else if (authority.equals(AUTH_USER)) {
            Intent intent = newIntent(mContext, FriendInfoActivity.class);
            intent.putExtra(FriendInfoActivity.KEY_UID, uri.getQueryParameter(KEY_USER_ID));
            mContext.startActivity(intent);
        }
        else if (authority.equals(AUTH_TAB)) {
            switchToTab(uri.getQueryParameter(KEY_TAB_ID));
        }
    }

    protected boolean processUrl(String url) {
        Intent intent = newIntent(mContext, KoolewWebActivity.class);
        intent.putExtra(KoolewWebActivity.KEY_URL, url);
        mContext.startActivity(intent);
        return true;
    }

    private void processOtherUri(Uri uri) {
        // TODO
    }

    protected void switchToTab(String tabId) {
        if (tabId.equals(TAB_FEEDS) || tabId.equals(TAB_SUGGESTION)) {
            Intent intent = newIntent(mContext, PushWrapperActivity.class);
            intent.putExtra(PushWrapperActivity.KEY_TAB_TYPE, tabId);
            mContext.startActivity(intent);
        }
        else if (tabId.equals(TAB_ASSIGNMENT)) {
            Intent intent = newIntent(mContext, MessagesActivity.class);
            intent.putExtra(MessagesActivity.KEY_WHICH_TAB, MessagesActivity.TASK_TAB);
            mContext.startActivity(intent);
        }
        else if (tabId.equals(TAB_COMMENT)) {
            Intent intent = newIntent(mContext, MessagesActivity.class);
            mContext.startActivity(intent);
        }
        else if (tabId.equals(TAB_ME)) {
        }
        else if (tabId.equals(TAB_KOO)) {
            Intent intent = newIntent(mContext, MessagesActivity.class);
            intent.putExtra(MessagesActivity.KEY_WHICH_TAB, MessagesActivity.KOO_TAB);
            mContext.startActivity(intent);
        }
        else if (tabId.equals(TAB_PROFIT)) {
            mContext.startActivity(newIntent(mContext, TodayIncomeActivity.class));
        }
    }

    private void startSingleVideoActivity(String videoId) {
        Intent intent = newIntent(mContext, TitleFragmentActivity.class);
        intent.putExtra(TitleFragmentActivity.KEY_FRAGMENT_CLASS, SingleMediaFragment.class);
        intent.putExtra(SingleMediaFragment.KEY_VIDEO_ID, videoId);
        mContext.startActivity(intent);
    }

    private Intent newIntent(Context context, Class<?> activity) {
        if (context instanceof Activity) {
            return new Intent(context, activity);
        }
        else {
            Intent intent = new Intent(context, activity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }
    }
}
