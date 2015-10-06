package com.koolew.mars;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.utils.UriProcessor;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 9/21/15.
 */
public class NotificationTabFragment
        extends RecyclerListFragmentMould<NotificationTabFragment.NotificationTabItemAdapter> {

    public NotificationTabFragment() {
        super();
        isNeedLoadMore = true;
    }

    @Override
    protected NotificationTabItemAdapter useThisAdapter() {
        return new NotificationTabItemAdapter();
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_deep_blue);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestNotification(mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().requestNotification(mAdapter.getLastUpdateTime(),
                mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        JSONArray activities = queryActivities(response);
        if (activities == null || activities.length() == 0) {
            return false;
        }
        else {
            mAdapter.setData(activities);
            mAdapter.notifyDataSetChanged();
            return true;
        }
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        JSONArray activities = queryActivities(response);
        int length = activities.length();
        if (activities == null || length == 0) {
            return false;
        }
        else {
            mAdapter.addData(activities);
            mAdapter.notifyItemRangeInserted(mAdapter.mData.size() - length, length);
            return true;
        }
    }

    private JSONArray queryActivities(JSONObject response) {
        try {
            if (response.getInt("code") == 0) {
                return response.getJSONObject("result").getJSONArray("activities");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    class NotificationTabItemAdapter extends LoadMoreAdapter {

        private List<NotificationItem> mData;

        public NotificationTabItemAdapter() {
            mData = new ArrayList<>();
        }

        public void setData(JSONArray activities) {
            mData.clear();
            addData(activities);
        }

        public void addData(JSONArray activities) {
            int length = activities.length();
            if (length == 0) {
                return;
            }
            for (int i = 0; i < length; i++) {
                try {
                    mData.add(new NotificationItem(activities.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public long getLastUpdateTime() {
            if (mData.size() != 0) {
                return mData.get(mData.size() - 1).updateTime;
            }
            else {
                return 0;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new NotificationHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.notification_tab_item, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            NotificationHolder holder = (NotificationHolder) viewHolder;
            NotificationItem item = mData.get(position);

            ImageLoader.getInstance().displayImage(item.imageUrl, holder.image);
            holder.title.setText(item.title);
            holder.arrow.setVisibility(TextUtils.isEmpty(item.url) ? View.INVISIBLE : View.VISIBLE);
        }

        @Override
        public int getCustomItemCount() {
            return mData.size();
        }
    }

    class NotificationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView image;
        private TextView title;
        private ImageView arrow;

        public NotificationHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            image = (ImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
            arrow = (ImageView) itemView.findViewById(R.id.into_arrow);
        }

        @Override
        public void onClick(View v) {
            NotificationItem item = mAdapter.mData.get(getAdapterPosition());
            if (!TextUtils.isEmpty(item.url)) {
                new UriProcessor(getActivity()).process(item.url);
            }
        }
    }

    class NotificationItem {
        private String imageUrl;
        private String title;
        private long updateTime;
        private String url;

        public NotificationItem(JSONObject jsonObject) {
            try {
                imageUrl = jsonObject.getString("image_url");
                title = jsonObject.getString("title");
                updateTime = jsonObject.getLong("update_time");
                url = jsonObject.getString("redirect_url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
