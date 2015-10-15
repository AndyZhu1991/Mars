package com.koolew.mars;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
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
        return mAdapter.setItems(getInvolveCards(response)) > 0;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        return mAdapter.addItems(getInvolveCards(response)) > 0;
    }

    private JSONArray getInvolveCards(JSONObject response) {
        try {
            return response.getJSONObject("result").getJSONArray("cards");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }


    class InvolveItem {
        String topicId;
        String title;
        int videoCount;
        boolean isManager;

        InvolveItem(String topicId, String title, int videoCount, boolean isManager) {
            this.topicId = topicId;
            this.title = title;
            this.videoCount = videoCount;
            this.isManager = isManager;
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
                    JSONObject topic = cards.getJSONObject(i).getJSONObject("topic");
                    String topicId = topic.getString("topic_id");
                    if (hasTopic(topicId)) {
                        continue;
                    }

                    InvolveItem item = new InvolveItem(topicId, topic.getString("content"),
                            topic.getInt("video_cnt"), topic.getInt("is_manager") == 1);
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
                if (item.topicId.equals(topicId)) {
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
            holder.videoCount.setText("" + mData.get(position).videoCount);
            holder.title.setText(mData.get(position).title);
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
                Intent intent = new Intent(getActivity(), IJoinedTopicActivity.class);
                intent.putExtra(IJoinedTopicActivity.KEY_TOPIC_ID, item.topicId);
                intent.putExtra(IJoinedTopicActivity.KEY_UID, MyAccountInfo.getUid());
                startActivity(intent);
            }
        }
    }
}
