package com.koolew.mars;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 11/12/15.
 */
public class JoinMovieActivity extends BaseV4FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_movie);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new JoinMovieFragment());
        fragmentTransaction.commit();
    }

    public static class JoinMovieFragment extends RecyclerListFragmentMould<JoinMovieAdapter> {

        private int page = 0;

        public JoinMovieFragment() {
            super();
            mLayoutResId = R.layout.refresh_recycler_without_shadow;
            isNeedLoadMore = true;
        }

        @Override
        protected JoinMovieAdapter useThisAdapter() {
            return new JoinMovieAdapter(getActivity());
        }

        @Override
        protected int getThemeColor() {
            return getResources().getColor(R.color.koolew_light_orange);
        }

        @Override
        protected JsonObjectRequest doRefreshRequest() {
            page = 0;
            return ApiWorker.getInstance().getMovies(page, mRefreshListener, null);
        }

        @Override
        protected JsonObjectRequest doLoadMoreRequest() {
            page++;
            return ApiWorker.getInstance().getMovies(page, mLoadMoreListener, null);
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
        private List<MovieInfo> mMovieInfoList = new ArrayList<>();

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
                    mMovieInfoList.add(new MovieInfo(topics.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                addedCount++;
            }
            return addedCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new JoinMovieHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_movie_item, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            JoinMovieHolder holder = (JoinMovieHolder) viewHolder;
            MovieInfo item = mMovieInfoList.get(position);
            ImageLoader.getInstance().displayImage(item.getThumb(), holder.thumb,
                    ImageLoaderHelper.topicThumbLoadOptions);
            holder.title.setText(item.getTitle());
            holder.videoCount.setText(mContext.getString(R.string.video_count_label,
                    item.getVideoCount()));
        }

        @Override
        public int getCustomItemCount() {
            return mMovieInfoList.size();
        }

        class JoinMovieHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener {

            private ImageView thumb;
            private TextView title;
            private TextView videoCount;

            public JoinMovieHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                thumb = (ImageView) itemView.findViewById(R.id.video_thumb);
                title = (TextView) itemView.findViewById(R.id.title);
                videoCount = (TextView) itemView.findViewById(R.id.video_count);
            }

            @Override
            public void onClick(View v) {
                MovieInfo item = mMovieInfoList.get(getAdapterPosition());
                TopicMediaActivity.startThisActivity(mContext, item.getTopicId(),
                        TopicMediaActivity.TYPE_WORLD);
            }
        }
    }

    public static class MovieInfo extends BaseTopicInfo {
        public MovieInfo(JSONObject jsonObject) {
            super(jsonObject);
            try {
                thumb = jsonObject.getJSONObject("attri").getJSONObject("movie")
                        .getString("thumbnail");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
