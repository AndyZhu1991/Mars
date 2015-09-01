package com.koolew.mars;

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
