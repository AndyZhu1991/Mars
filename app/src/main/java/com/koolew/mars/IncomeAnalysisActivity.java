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
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IncomeAnalysisActivity extends BaseV4FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_analysis);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new IncomeAnalysisFragment());
        fragmentTransaction.commit();
    }

    public static class IncomeAnalysisFragment
            extends RecyclerListFragmentMould<IncomeAnalysisAdapter> {

        private int mPage;

        public IncomeAnalysisFragment() {
            isNeedLoadMore = true;
        }

        @Override
        protected int getNoDataViewResId() {
            return R.layout.no_income_layout;
        }

        @Override
        protected IncomeAnalysisAdapter useThisAdapter() {
            return new IncomeAnalysisAdapter(getActivity());
        }

        @Override
        protected int getThemeColor() {
            return getResources().getColor(R.color.koolew_red);
        }

        @Override
        protected String getRefreshRequestUrl() {
            return UrlHelper.getIncomeAnalysisUrl(mPage);
        }

        @Override
        protected JsonObjectRequest doRefreshRequest() {
            mPage = 0;
            return super.doRefreshRequest();
        }

        @Override
        protected String getLoadMoreRequestUrl() {
            return UrlHelper.getIncomeAnalysisUrl(mPage);
        }

        @Override
        protected JsonObjectRequest doLoadMoreRequest() {
            mPage++;
            return super.doLoadMoreRequest();
        }

        @Override
        protected boolean handleRefreshResult(JSONObject result) {
            JSONArray videos = getVideos(result);
            mAdapter.setData(videos);
            return videos.length() > 0;
        }

        @Override
        protected boolean handleLoadMoreResult(JSONObject result) {
            JSONArray videos = getVideos(result);
            mAdapter.addData(videos);
            return videos.length() > 0;
        }

        private JSONArray getVideos(JSONObject result) {
            try {
                return result.getJSONArray("videos");
            } catch (JSONException e) {
                handleJsonException(result, e);
            }
            return new JSONArray();
        }
    }

    static class IncomeAnalysisAdapter extends LoadMoreAdapter {

        private Context mContext;
        private List<IncomeVideoInfo> mData;

        public IncomeAnalysisAdapter(Context context) {
            mData = new ArrayList<>();
            mContext = context;
        }

        public void setData(JSONArray jsonArray) {
            mData.clear();
            addInfo(jsonArray);
            notifyDataSetChanged();
        }

        public void addData(JSONArray jsonArray) {
            int originSize = mData.size();
            addInfo(jsonArray);
            notifyItemRangeInserted(originSize, mData.size() - originSize);
        }

        // Just change mData
        private void addInfo(JSONArray jsonArray) {
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                try {
                    mData.add(new IncomeVideoInfo(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new IncomeAnalysisHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.danmaku_tab_item, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position) {
            IncomeAnalysisHolder incomeHolder = (IncomeAnalysisHolder) holder;
            IncomeVideoInfo info = mData.get(position);
            ImageLoader.getInstance().displayImage(info.getVideoThumb(), incomeHolder.mVideoThumb,
                    ImageLoaderHelper.topicThumbLoadOptions);
            incomeHolder.mTopicTitle.setText(info.getTopicInfo().getTitle());
            incomeHolder.mIncomeText.setText(mContext.getString(R.string.income_rmb) +
                    mContext.getString(R.string.colon) +
                    TodayIncomeActivity.toIncomeString(info.income));
        }

        @Override
        public int getCustomItemCount() {
            return mData.size();
        }

        class IncomeAnalysisHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private ImageView mVideoThumb;
            private TextView mTopicTitle;
            private TextView mIncomeText;

            public IncomeAnalysisHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                mVideoThumb = (ImageView) itemView.findViewById(R.id.thumb);
                mTopicTitle = (TextView) itemView.findViewById(R.id.title);
                mIncomeText = (TextView) itemView.findViewById(R.id.last_comment);
                mIncomeText.setTextColor(mContext.getResources().getColor(R.color.koolew_red));
            }

            @Override
            public void onClick(View v) {
                SingleMediaFragment.startThisFragment(mContext, mData.get(getAdapterPosition()).getVideoId());
            }
        }
    }

    static class IncomeVideoInfo extends BaseVideoInfo {

        private double income;

        public IncomeVideoInfo(JSONObject jsonObject) {
            super(jsonObject);
            income = JsonUtil.getDoubleIfHas(jsonObject, "profit");
        }
    }
}
