package com.koolew.mars;

import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class KoolewInvolveFragment
        extends RecyclerListFragmentMould<KoolewInvolveFragment.InvolveAdapter> {

    private int mCurrentPage = 0;


    public KoolewInvolveFragment() {
        super();
        isNeedLoadMore = true;
        isLazyLoad = true;
    }

    @Override
    protected InvolveAdapter useThisAdapter() {
        return new InvolveAdapter();
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_deep_orange);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        mCurrentPage = 0;
        return ApiWorker.getInstance().requestInvolve(mCurrentPage, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        mCurrentPage++;
        return ApiWorker.getInstance().requestInvolve(mCurrentPage, mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        return mAdapter.setItems(getInvolveTopics(response)) > 0;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        return mAdapter.addItems(getInvolveTopics(response)) > 0;
    }

    private JSONArray getInvolveTopics(JSONObject response) {
        try {
            return response.getJSONObject("result").getJSONArray("topics");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }


    class InvolveItem extends BaseTopicInfo {
        boolean isManager;

        InvolveItem(JSONObject jsonObject) {
            super(jsonObject);
            this.isManager = JsonUtil.getIntIfHas(jsonObject, "is_manager") != 0;
        }
    }

    private static final int[] LEFT_LAYOUT_COLORS = {
            0xFFFF5656, 0xFFFC7B7B, 0xFFFFAE82, 0xFFFFC282,
            0xFFF4D288, 0xFFFFC282, 0xFFFFAE82, 0xFFFC7B7B,
    };

    class InvolveAdapter extends LoadMoreAdapter {

        List<InvolveItem> mData = new ArrayList<>();


        public int setItems(JSONArray cards) {
            mData.clear();
            int addedCount = addData(cards);
            notifyDataSetChanged();
            return addedCount;
        }

        public int addItems(JSONArray cards) {
            int originCount = mData.size();
            int addedCount = addData(cards);
            notifyItemRangeInserted(originCount, addedCount);

            return addedCount;
        }

        private int addData(JSONArray cards) {
            int addedCount = 0;

            try {
                int count = cards.length();
                for (int i = 0; i < count; i++) {
                    JSONObject topic = cards.getJSONObject(i);
                    InvolveItem item = new InvolveItem(topic);
                    String topicId = item.getTopicId();
                    if (hasTopic(topicId)) {
                        continue;
                    }

                    mData.add(item);
                    addedCount++;
                }
            }
            catch (JSONException jse) {
            }

            return addedCount;
        }

        private boolean hasTopic(String topicId) {
            for (InvolveItem item: mData) {
                if (item.getTopicId().equals(topicId)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new InvolveHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.koolew_involve_item, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            InvolveHolder holder = (InvolveHolder) viewHolder;

            ((GradientDrawable) holder.leftLayout.getBackground())
                    .setColor(LEFT_LAYOUT_COLORS[position % 8]);
            holder.videoCount.setText("" + mData.get(position).getVideoCount());
            holder.title.setText(mData.get(position).getTitle());
            holder.manager.setVisibility(mData.get(position).isManager ? View.VISIBLE : View.GONE);
        }

        @Override
        public int getCustomItemCount() {
            return mData.size();
        }


        class InvolveHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            LinearLayout leftLayout;
            TextView videoCount;
            TextView title;
            TextView manager;

            public InvolveHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                leftLayout = (LinearLayout) itemView.findViewById(R.id.left_layout);
                videoCount = (TextView) itemView.findViewById(R.id.video_count);
                title = (TextView) itemView.findViewById(R.id.title);
                manager = (TextView) itemView.findViewById(R.id.is_manager);
            }

            @Override
            public void onClick(View v) {
                InvolveItem item = mData.get(getAdapterPosition());
                UserMediaActivity.startMyMediaActivity(getActivity(), item.getTopicId());
            }
        }
    }
}
