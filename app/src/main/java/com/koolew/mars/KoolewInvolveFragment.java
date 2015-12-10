package com.koolew.mars;

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
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

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


    static class InvolveItem extends BaseTopicInfo {
        boolean isManager;

        InvolveItem(JSONObject jsonObject) {
            super(jsonObject);
            this.isManager = JsonUtil.getIntIfHas(jsonObject, "is_manager") != 0;
        }
    }

    static class InvolveLine {
        private InvolveItem leftItem;
        private InvolveItem rightItem;

        private InvolveLine(InvolveItem leftItem, InvolveItem rightItem) {
            this.leftItem = leftItem;
            this.rightItem = rightItem;
        }
    }


    static class InvolveData {
        List<InvolveItem> involveItems = new ArrayList<>();

        private void clear() {
            involveItems.clear();
        }

        private int size() {
            return (involveItems.size() + 1) / 2;
        }

        private void add(InvolveItem involveItem) {
            involveItems.add(involveItem);
        }

        private InvolveLine get(int position) {
            InvolveItem leftItem = involveItems.get(position * 2);
            int rightPosition = position * 2 + 1;
            InvolveItem rightItem = rightPosition < involveItems.size()
                    ? involveItems.get(rightPosition) : null;
            return new InvolveLine(leftItem, rightItem);
        }

        private boolean hasTopic(String topicId) {
            for (InvolveItem item: involveItems) {
                if (item.getTopicId().equals(topicId)) {
                    return true;
                }
            }
            return false;
        }
    }

    class InvolveAdapter extends LoadMoreAdapter {

        InvolveData mData = new InvolveData();


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
                    if (mData.hasTopic(topicId)) {
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

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new InvolveLineHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.involve_line, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            ((InvolveLineHolder) viewHolder).bindInvolveLine(position);
        }

        @Override
        public int getCustomItemCount() {
            return mData.size();
        }


        class InvolveLineHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private InvolveItemHolder leftHolder;
            private InvolveItemHolder rightHolder;

            public InvolveLineHolder(View itemView) {
                super(itemView);

                leftHolder = new InvolveItemHolder(itemView.findViewById(R.id.left_item));
                leftHolder.itemView.setOnClickListener(this);
                rightHolder = new InvolveItemHolder(itemView.findViewById(R.id.right_item));
                rightHolder.itemView.setOnClickListener(this);
            }

            private void bindInvolveLine(int position) {
                InvolveLine involveLine = mData.get(getAdapterPosition());
                leftHolder.bindInvolveItem(involveLine.leftItem);
                rightHolder.bindInvolveItem(involveLine.rightItem);
            }

            @Override
            public void onClick(View v) {
                InvolveLine line = mData.get(getAdapterPosition());
                InvolveItem item;
                if (v == leftHolder.itemView) {
                    item = line.leftItem;
                }
                else {
                    item = line.rightItem;
                }
                UserMediaActivity.startMyMediaActivity(getActivity(), item.getTopicId());
            }
        }

        class InvolveItemHolder {
            View itemView;
            ImageView thumb;
            TextView videoCount;
            TextView title;
            //TextView manager;

            public InvolveItemHolder(View itemView) {
                this.itemView = itemView;
                thumb = (ImageView) itemView.findViewById(R.id.thumb);
                videoCount = (TextView) itemView.findViewById(R.id.video_count);
                title = (TextView) itemView.findViewById(R.id.title);
                //manager = (TextView) itemView.findViewById(R.id.is_manager);
            }

            private void bindInvolveItem(InvolveItem involveItem) {
                if (involveItem == null) {
                    itemView.setVisibility(View.INVISIBLE);
                }
                else {
                    if (itemView.getVisibility() == View.INVISIBLE) {
                        itemView.setVisibility(View.VISIBLE);
                    }
                    ImageLoader.getInstance().displayImage(involveItem.getThumb(), thumb,
                            ImageLoaderHelper.topicThumbLoadOptions);
                    videoCount.setText(String.valueOf(involveItem.getVideoCount()));
                    title.setText(involveItem.getTitle());
                }
            }
        }
    }
}
