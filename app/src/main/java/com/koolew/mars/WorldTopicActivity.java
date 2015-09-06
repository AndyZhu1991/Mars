package com.koolew.mars;

import android.support.v4.app.FragmentManager;


public class WorldTopicActivity extends TopicVideoActivity{

    @Override
    protected int[] getPageColors() {
        return new int[] {
                getResources().getColor(R.color.koolew_light_blue),
                getResources().getColor(R.color.koolew_light_orange),
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
            fragmentList.add(new WorldVideoListFragment());
            fragmentList.add(new FeedsVideoListFragment());
        }

        @Override
        protected void initTitleList() {
            fragmentTitles.add(getString(R.string.world_title_public));
            fragmentTitles.add(getString(R.string.feeds_title_friend));
        }
    }
}
