package com.koolew.mars;

import android.support.v4.app.FragmentManager;

public class TaskTopicActivity extends TopicVideoActivity {

    public static final String KEY_INVITER = TaskVideoListFragment.KEY_INVITER;

    public static final int RESULT_IGNORE = RESULT_FIRST_USER + 1;

    @Override
    protected int[] getPageColors() {
        return new int[] {
                getResources().getColor(R.color.koolew_light_green),
                getResources().getColor(R.color.koolew_light_blue),
        };
    }

    @Override
    protected TopicVideoPagerAdapter getPagerAdapter() {
        return new TaskTopicAdapter(getSupportFragmentManager());
    }

    class TaskTopicAdapter extends TopicVideoPagerAdapter {

        public TaskTopicAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        protected void initFragmentList() {
            fragmentList.add(new TaskVideoListFragment());
            fragmentList.add(new WorldVideoListFragment());
        }

        @Override
        protected void initTitleList() {
            fragmentTitles.add(getString(R.string.feeds_title_friend));
            fragmentTitles.add(getString(R.string.world_title_public));
        }
    }
}
