package com.koolew.mars;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 9/21/15.
 */
public class NotificationTabFragment
        extends RecyclerListFragmentMould<NotificationTabFragment.NotificationTabItemAdapter> {

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
        return null;
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return null;
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        return false;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        return false;
    }


    class NotificationTabItemAdapter extends LoadMoreAdapter {

        private List<NotificationItem> mData;

        public NotificationTabItemAdapter() {
            mData = new ArrayList<>();
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new NotificationHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.notification_tab_item, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getCustomItemCount() {
            return mData.size();
        }
    }

    class NotificationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView icon;
        private TextView message;
        private ImageView arrow;

        public NotificationHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            icon = (ImageView) itemView.findViewById(R.id.icon);
            message = (TextView) itemView.findViewById(R.id.message);
            arrow = (ImageView) itemView.findViewById(R.id.into_arrow);
        }

        @Override
        public void onClick(View v) {
        }
    }

    class NotificationItem {

        private String iconUrl;
        private String message;
        private String url;
    }
}
