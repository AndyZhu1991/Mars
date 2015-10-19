package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;

public class FeedsTopicActivity extends TopicVideoActivity {

    @Override
    protected int[] getPageColors() {
        return new int[] {
                getResources().getColor(R.color.koolew_light_orange),
                getResources().getColor(R.color.koolew_light_blue),
        };
    }

    @Override
    protected TopicVideoPagerAdapter getPagerAdapter() {
        return new FeedsTopicAdapter(getSupportFragmentManager());
    }

    public static void startTopicFollowed(Context context, String topicId, String title) {
        Intent intent = new Intent(context, FeedsTopicActivity.class);
        intent.putExtra(KEY_TOPIC_ID, topicId);
        intent.putExtra(KEY_TOPIC_TITLE, title);
        context.startActivity(intent);
    }

    public static void startTopicWorld(Context context, String topicId, String title) {
        Intent intent = new Intent(context, FeedsTopicActivity.class);
        intent.putExtra(KEY_TOPIC_ID, topicId);
        intent.putExtra(KEY_TOPIC_TITLE, title);
        intent.putExtra(KEY_DEFAULT_SHOW_POSITION, POSITION_WORLD);
        context.startActivity(intent);
    }

    class FeedsTopicAdapter extends TopicVideoPagerAdapter {

        public FeedsTopicAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        protected void initFragmentList() {
            fragmentList.add(new FeedsVideoListFragment());
            fragmentList.add(new WorldVideoListFragment());
        }

        @Override
        protected void initTitleList() {
            fragmentTitles.add(getString(R.string.feeds_title_friend));
            fragmentTitles.add(getString(R.string.world_title_public));
        }
    }
}
