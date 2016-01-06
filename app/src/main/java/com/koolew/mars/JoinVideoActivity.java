package com.koolew.mars;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.adapters.TagAdapter;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.Tag;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.remoteconfig.RemoteConfigManager;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JoinVideoActivity extends BaseV4FragmentActivity
        implements TitleBarView.OnRightLayoutClickListener, TagAdapter.OnSelectedTagChangedListener,
        ViewPager.OnPageChangeListener {

    private static final int REQUEST_CODE_CREATE_TOPIC = 1;

    private RecyclerView mTagRecycler;
    private TagAdapter mTagAdapter;
    private ViewPager mTagPager;
    private MovieTagPagerAdapter mPagerAdapter;

    public JoinVideoActivity() {
        isNeedPageStatistics = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_video);

        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);

        mTagRecycler = (RecyclerView) findViewById(R.id.tag_recycler);
        mTagRecycler.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        mTagAdapter = new TagAdapter(this);
        mTagAdapter.initTags(TagAdapter.TAGS_VIDEO, true);
        mTagAdapter.setTextColorSelected(0xFFD3AB64);
        mTagAdapter.setTagChangedListener(this);
        mTagRecycler.setAdapter(mTagAdapter);

        mTagPager = (ViewPager) findViewById(R.id.tag_pager);
        mPagerAdapter = new MovieTagPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.fragments.add(new JoinVideoFragment(null));
        List<Tag> tags = RemoteConfigManager.getInstance().getVideoTagsConfig().getConfig();
        for (Tag tag: tags) {
            mPagerAdapter.fragments.add(new JoinVideoFragment(tag));
        }
        mTagPager.setOffscreenPageLimit(mPagerAdapter.getCount());
        mTagPager.setAdapter(mPagerAdapter);
        mTagPager.addOnPageChangeListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CREATE_TOPIC:
                if (resultCode == Activity.RESULT_OK) {
                    finish();
                }
                break;
        }
    }

    @Override
    public void onRightLayoutClick() {
        startActivityForResult(new Intent(this, CreateTopicActivity.class),
                REQUEST_CODE_CREATE_TOPIC);
    }

    @Override
    public void onSelectedTagChanged(Tag tag) {
        int currentPosition = mTagPager.getCurrentItem();
        int newPosition = 0;
        for (int i = 0; i < mPagerAdapter.fragments.size(); i++) {
            Tag tagi = mPagerAdapter.fragments.get(i).mTag;
            if (tag == tagi) {
                newPosition = i;
            }
        }

        if (currentPosition == newPosition) {
            // Do nothing
        }
        else if (Math.abs(currentPosition - newPosition) == 1) {
            mTagPager.setCurrentItem(newPosition, true);
        }
        else {
            mTagPager.setCurrentItem(newPosition, false);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mTagAdapter.setSelectedPosition(position);
        mTagRecycler.scrollToPosition(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public static class MovieTagPagerAdapter extends FragmentPagerAdapter {

        private List<JoinVideoFragment> fragments = new ArrayList<>();

        public MovieTagPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }


    public static class JoinVideoFragment
            extends RecyclerListFragmentMould<JoinVideoFragment.JoinVideoTopicAdapter> {

        private int page = 0;
        private Tag mTag = null;

        public JoinVideoFragment(Tag tag) {
            mTag = tag;
            isNeedPageStatistics = false;
            mLayoutResId = R.layout.refresh_recycler_without_shadow;
            isLazyLoad = true;
            isNeedLoadMore = true;
        }

        @Override
        protected View createDefaultView() {
            return null;
        }

        @Override
        protected JoinVideoTopicAdapter useThisAdapter() {
            return new JoinVideoTopicAdapter();
        }

        @Override
        protected int getThemeColor() {
            return getResources().getColor(R.color.koolew_light_orange);
        }

        @Override
        protected String getRefreshRequestUrl() {
            if (mTag == null) {
                return UrlHelper.getRecommendTopicUrl(BaseTopicInfo.CATEGORY_VIDEO, page);
            }
            else {
                return UrlHelper.getTopicUrl(BaseTopicInfo.CATEGORY_VIDEO, mTag.getId(), page);
            }
        }

        @Override
        protected JsonObjectRequest doRefreshRequest() {
            page = 0;
            return super.doRefreshRequest();
        }

        @Override
        protected String getLoadMoreRequestUrl() {
            if (mTag == null) {
                return UrlHelper.getRecommendTopicUrl(BaseTopicInfo.CATEGORY_VIDEO, page);
            }
            else {
                return UrlHelper.getTopicUrl(BaseTopicInfo.CATEGORY_VIDEO, mTag.getId(), page);
            }
        }

        @Override
        protected JsonObjectRequest doLoadMoreRequest() {
            page++;
            return super.doLoadMoreRequest();
        }

        @Override
        protected boolean handleRefreshResult(JSONObject result) {
            try {
                JSONArray topics = result.getJSONArray("topics");
                mAdapter.topicInfos.clear();
                int addedCount = mAdapter.addTopics(topics);
                mAdapter.notifyDataSetChanged();
                return addedCount > 0;
            } catch (JSONException e) {
                handleJsonException(result, e);
            }
            return false;
        }

        @Override
        protected boolean handleLoadMoreResult(JSONObject result) {
            try {
                JSONArray topics = result.getJSONArray("topics");
                int originCount = mAdapter.topicInfos.size();
                int addedCount = mAdapter.addTopics(topics);
                if (addedCount > 0) {
                    mAdapter.notifyItemRangeInserted(originCount, addedCount);
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }


        class JoinVideoTopicAdapter extends LoadMoreAdapter {
            private List<BaseTopicInfo> topicInfos = new ArrayList<>();

            private int addTopics(JSONArray topics) {
                int length = topics.length();
                int addedCount = 0;
                for (int i = 0; i < length; i++) {
                    try {
                        topicInfos.add(new BaseTopicInfo(topics.getJSONObject(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    addedCount++;
                }
                return addedCount;
            }

            @Override
            public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
                return new JoinVideoTopicHolder(LayoutInflater.from(getActivity())
                        .inflate(R.layout.join_video_topic_item, parent, false));
            }

            @Override
            public void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position) {
                JoinVideoTopicHolder jh = (JoinVideoTopicHolder) holder;
                BaseTopicInfo topicInfo = topicInfos.get(position);
                ImageLoader.getInstance().displayImage(topicInfo.getThumb(), jh.thumb,
                        ImageLoaderHelper.topicThumbLoadOptions);
                jh.title.setText(topicInfo.getTitle());
            }

            @Override
            public int getCustomItemCount() {
                return topicInfos.size();
            }

            class JoinVideoTopicHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
                private ImageView thumb;
                private TextView title;
                private View captureBtn;

                public JoinVideoTopicHolder(View itemView) {
                    super(itemView);
                    itemView.setOnClickListener(this);

                    thumb = (ImageView) itemView.findViewById(R.id.thumb);
                    title = (TextView) itemView.findViewById(R.id.title);
                    captureBtn = itemView.findViewById(R.id.capture_btn);
                    captureBtn.setOnClickListener(this);
                }

                @Override
                public void onClick(View v) {
                    BaseTopicInfo topicInfo = topicInfos.get(getAdapterPosition());
                    if (v == itemView) {
                        TopicMediaActivity.startThisActivity(getActivity(), topicInfo.getTopicId(),
                                TopicMediaActivity.TYPE_WORLD);
                    }
                    else if (v == captureBtn) {
                        VideoShootActivity.startThisActivity(getActivity(), topicInfo);
                    }
                }
            }
        }
    }
}
