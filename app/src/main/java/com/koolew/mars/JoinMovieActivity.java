package com.koolew.mars;

import android.content.Context;
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
import com.koolew.mars.infos.MovieTopicInfo;
import com.koolew.mars.infos.Tag;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.remoteconfig.RemoteConfigManager;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 11/12/15.
 */
public class JoinMovieActivity extends BaseV4FragmentActivity
        implements TitleBarView.OnRightLayoutClickListener, TagAdapter.OnSelectedTagChangedListener,
        ViewPager.OnPageChangeListener {

    private static final int MOVIE_THEME_COLOR = 0xFF462762;

    private RecyclerView mTagRecycler;
    private TagAdapter mTagAdapter;
    private ViewPager mTagPager;
    private MovieTagPagerAdapter mPagerAdapter;

    public JoinMovieActivity() {
        isNeedPageStatistics = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_movie);

        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);

        mTagRecycler = (RecyclerView) findViewById(R.id.tag_recycler);
        mTagRecycler.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        mTagAdapter = new TagAdapter(this);
        mTagAdapter.initTags(TagAdapter.TAGS_MOVIE, true);
        mTagAdapter.setTextColorSelected(MOVIE_THEME_COLOR);
        mTagAdapter.setTagChangedListener(this);
        mTagRecycler.setAdapter(mTagAdapter);

        mTagPager = (ViewPager) findViewById(R.id.tag_pager);
        mPagerAdapter = new MovieTagPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.fragments.add(new JoinMovieFragment(null));
        List<Tag> tags = RemoteConfigManager.getInstance().getMovieTagsConfig().getConfig();
        for (Tag tag: tags) {
            mPagerAdapter.fragments.add(new JoinMovieFragment(tag));
        }
        mTagPager.setOffscreenPageLimit(mPagerAdapter.getCount());
        mTagPager.setAdapter(mPagerAdapter);
        mTagPager.addOnPageChangeListener(this);
    }

    @Override
    public void onRightLayoutClick() {
        // Go to movie add explain
        KoolewWebActivity.startThisActivity(this, "http://www.koolew.com/movie.html",
                getString(R.string.explain), MOVIE_THEME_COLOR);
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

        private List<JoinMovieFragment> fragments = new ArrayList<>();

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

    public static class JoinMovieFragment extends RecyclerListFragmentMould<JoinMovieAdapter> {

        private Tag mTag = null;
        private int page = 0;

        public JoinMovieFragment(Tag tag) {
            super();
            isNeedLoadMore = false;
            mLayoutResId = R.layout.refresh_recycler_without_shadow;
            mTag = tag;
            isNeedLoadMore = true;
            isLazyLoad = true;
        }

        @Override
        protected View createDefaultView() {
            return null;
        }

        @Override
        protected JoinMovieAdapter useThisAdapter() {
            return new JoinMovieAdapter(getActivity());
        }

        @Override
        protected int getThemeColor() {
            return MOVIE_THEME_COLOR;
        }

        @Override
        protected JsonObjectRequest doRefreshRequest() {
            page = 0;
            if (mTag == null) {
                return ApiWorker.getInstance().standardGetRequest(
                        UrlHelper.getRecommendTopicUrl(BaseTopicInfo.CATEGORY_MOVIE, page),
                        mRefreshListener, null);
            }
            else {
                return ApiWorker.getInstance().standardGetRequest(
                        UrlHelper.getTopicUrl(BaseTopicInfo.CATEGORY_MOVIE, mTag.getId(), page),
                        mRefreshListener, null);
            }
        }

        @Override
        protected JsonObjectRequest doLoadMoreRequest() {
            page++;
            if (mTag == null) {
                return ApiWorker.getInstance().standardGetRequest(
                        UrlHelper.getRecommendTopicUrl(BaseTopicInfo.CATEGORY_MOVIE, page),
                        mLoadMoreListener, null);
            }
            else {
                return ApiWorker.getInstance().standardGetRequest(
                        UrlHelper.getTopicUrl(BaseTopicInfo.CATEGORY_MOVIE, mTag.getId(), page),
                        mLoadMoreListener, null);
            }
        }

        @Override
        protected boolean handleRefresh(JSONObject response) {
            try {
                return mAdapter.set(response.getJSONObject("result"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected boolean handleLoadMore(JSONObject response) {
            try {
                return mAdapter.add(response.getJSONObject("result"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static class JoinMovieAdapter extends LoadMoreAdapter {

        private Context mContext;
        private List<MovieTopicInfo> mMovieInfoList = new ArrayList<>();

        public JoinMovieAdapter(Context context) {
            mContext = context;
        }

        private boolean add(JSONObject result) {
            int originCount = mMovieInfoList.size();
            int addedCount = addData(getTopicsFromResult(result));
            if (addedCount > 0) {
                notifyItemRangeInserted(originCount, addedCount);
                return true;
            }
            return false;
        }

        private boolean set(JSONObject result) {
            mMovieInfoList.clear();
            int addedCount = addData(getTopicsFromResult(result));
            if (addedCount > 0) {
                notifyDataSetChanged();
                return true;
            }
            return false;
        }

        private JSONArray getTopicsFromResult(JSONObject result) {
            try {
                return result.getJSONArray("topics");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new JSONArray();
        }

        private int addData(JSONArray topics) {
            int addedCount = 0;
            int length = topics.length();
            for (int i = 0; i < length; i++) {
                try {
                    MovieTopicInfo movieInfo = new MovieTopicInfo(topics.getJSONObject(i));
                    if (!has(movieInfo)) {
                        mMovieInfoList.add(movieInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                addedCount++;
            }
            return addedCount;
        }

        private boolean has(MovieTopicInfo movieInfo) {
            for (MovieTopicInfo m: mMovieInfoList) {
                if (m.getTopicId().equals(movieInfo.getTopicId())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new JoinMovieHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_movie_item, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            JoinMovieHolder holder = (JoinMovieHolder) viewHolder;
            MovieTopicInfo item = mMovieInfoList.get(position);
            ImageLoader.getInstance().displayImage(item.getThumbnail(), holder.thumb,
                    ImageLoaderHelper.topicThumbLoadOptions);
            holder.title.setText(item.getTitle());
        }

        @Override
        public int getCustomItemCount() {
            return mMovieInfoList.size();
        }

        class JoinMovieHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener {

            private ImageView thumb;
            private TextView title;
            private View capture;

            public JoinMovieHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                thumb = (ImageView) itemView.findViewById(R.id.video_thumb);
                title = (TextView) itemView.findViewById(R.id.title);
                capture = itemView.findViewById(R.id.capture);
                capture.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                MovieTopicInfo item = mMovieInfoList.get(getAdapterPosition());
                if (v == itemView) {
                    TopicMediaActivity.startThisActivity(mContext, item.getTopicId(),
                            TopicMediaActivity.TYPE_WORLD);
                }
                else if (v == capture) {
                    MovieStudioActivity.startThisActivity(mContext, item);
                }
            }
        }
    }
}
