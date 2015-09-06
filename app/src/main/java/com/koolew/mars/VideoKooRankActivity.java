package com.koolew.mars;

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

    private String videoId;

    private VideoKooRankFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_koo_rank);

        videoId = getIntent().getStringExtra(KEY_VIDEO_ID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mFragment = new VideoKooRankFragment();
        fragmentTransaction.add(R.id.fragment_container, mFragment);
        fragmentTransaction.commit();
    }

    class VideoKooRankFragment extends RecyclerListFragmentMould {

        public VideoKooRankFragment() {
            isNeedLoadMore = false;
        }

        @Override
        protected LoadMoreAdapter useThisAdapter() {
            return new VideoKooRankAdapter();
        }

        private VideoKooRankAdapter getAdapter() {
            return (VideoKooRankAdapter) mAdapter;
        }

        @Override
        protected int getThemeColor() {
            return getResources().getColor(R.color.koolew_light_orange);
        }

        @Override
        protected JsonObjectRequest doRefreshRequest() {
            return ApiWorker.getInstance().requestVideoKooRank(videoId, mRefreshListener, null);
        }

        @Override
        protected JsonObjectRequest doLoadMoreRequest() {
            return null;
        }

        @Override
        protected boolean handleRefresh(JSONObject response) {
            try {
                if (response.getInt("code") == 0) {
                    JSONArray rank = response.getJSONObject("result").getJSONArray("rank");
                    ((VideoKooRankAdapter) mAdapter).setData(rank);
                    mAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected boolean handleLoadMore(JSONObject response) {
            return false;
        }
    }

    class VideoKooRankAdapter extends LoadMoreAdapter {

        private List<KooCountUserInfo> userInfos;

        public VideoKooRankAdapter() {
            userInfos = new ArrayList<>();
        }

        public void setData(JSONArray jsonArray) {
            userInfos.clear();
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                try {
                    userInfos.add(new KooCountUserInfo(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
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
            if (position == 0) {
                videoKooRankItemHolder.kooIcon.setVisibility(View.VISIBLE);
            }
            else {
                videoKooRankItemHolder.kooIcon.setVisibility(View.INVISIBLE);
            }
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
        private ImageView kooIcon;
        private TextView kooCount;

        public VideoKooRankItemHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            nameView = (UserNameView) itemView.findViewById(R.id.name_view);
            kooIcon = (ImageView) itemView.findViewById(R.id.koo_icon);
            kooCount = (TextView) itemView.findViewById(R.id.koo_count);
        }

        @Override
        public void onClick(View v) {
            FriendInfoActivity.startThisActivity(VideoKooRankActivity.this,
                    mFragment.getAdapter().userInfos.get(getAdapterPosition()).getUid());
        }
    }
}
