package com.koolew.mars;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.KooCountUserInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoKooRankActivity extends BaseV4FragmentActivity {

    public static final String KEY_VIDEO_ID = "video id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_koo_rank);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new VideoKooRankFragment());
        fragmentTransaction.commit();
    }

    public static class VideoKooRankFragment
            extends RecyclerListFragmentMould<VideoKooRankFragment.VideoKooRankAdapter>{

        private String videoId;
        private int page;

        public VideoKooRankFragment() {
            isNeedLoadMore = true;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            videoId = getActivity().getIntent().getStringExtra(KEY_VIDEO_ID);
        }

        @Override
        protected VideoKooRankAdapter useThisAdapter() {
            return new VideoKooRankAdapter();
        }

        private VideoKooRankAdapter getAdapter() {
            return mAdapter;
        }

        @Override
        protected int getThemeColor() {
            return getResources().getColor(R.color.koolew_light_orange);
        }

        @Override
        protected JsonObjectRequest doRefreshRequest() {
            page = 0;
            return ApiWorker.getInstance().requestVideoKooRank(videoId, page, mRefreshListener, null);
        }

        @Override
        protected JsonObjectRequest doLoadMoreRequest() {
            page++;
            return ApiWorker.getInstance().requestVideoKooRank(videoId, page, mLoadMoreListener, null);
        }

        @Override
        protected boolean handleRefresh(JSONObject response) {
            try {
                if (response.getInt("code") == 0) {
                    JSONArray rank = response.getJSONObject("result").getJSONArray("rank");
                    return mAdapter.setData(rank) > 0;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected boolean handleLoadMore(JSONObject response) {
            try {
                if (response.getInt("code") == 0) {
                    JSONArray rank = response.getJSONObject("result").getJSONArray("rank");
                    return mAdapter.addData(rank) > 0;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }


        class VideoKooRankAdapter extends LoadMoreAdapter {

            private List<KooCountUserInfo> userInfos;

            public VideoKooRankAdapter() {
                userInfos = new ArrayList<>();
            }

            public int addData(JSONArray jsonArray) {
                int originCount = userInfos.size();
                int addedCount = add(jsonArray);
                notifyItemRangeInserted(originCount, addedCount);
                return addedCount;
            }

            public int setData(JSONArray jsonArray) {
                userInfos.clear();
                int addedCount = add(jsonArray);
                notifyDataSetChanged();
                return addedCount;
            }

            private int add(JSONArray jsonArray) {
                int addedCount = 0;
                int length = jsonArray.length();
                for (int i = 0; i < length; i++) {
                    try {
                        userInfos.add(new KooCountUserInfo(jsonArray.getJSONObject(i)));
                        addedCount++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return addedCount;
            }

            @Override
            public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.topic_koo_rank_item, parent, false);
                return new VideoKooRankItemHolder(itemView);
            }

            @Override
            public void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position) {
                VideoKooRankItemHolder videoKooRankItemHolder = (VideoKooRankItemHolder) holder;
                KooCountUserInfo userInfo = userInfos.get(position);

                ImageLoader.getInstance().displayImage(userInfo.getAvatar(),
                        videoKooRankItemHolder.avatar, ImageLoaderHelper.avatarLoadOptions);
                videoKooRankItemHolder.nameView.setUser(userInfo);
                videoKooRankItemHolder.kooCount.setText(String.valueOf(userInfo.getKooCount()));
            }

            @Override
            public int getCustomItemCount() {
                return userInfos.size();
            }
        }

        class VideoKooRankItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private CircleImageView avatar;
            private UserNameView nameView;
            private TextView kooCount;

            public VideoKooRankItemHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
                nameView = (UserNameView) itemView.findViewById(R.id.name_view);
                kooCount = (TextView) itemView.findViewById(R.id.koo_count);
            }

            @Override
            public void onClick(View v) {
                FriendInfoActivity.startThisActivity(getActivity(),
                        getAdapter().userInfos.get(getAdapterPosition()).getUid());
            }
        }
    }
}
