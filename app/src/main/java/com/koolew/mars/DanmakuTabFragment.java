package com.koolew.mars;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.view.NotificationPointView;
import com.koolew.mars.webapi.ApiWorker;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 7/17/15.
 */
public class DanmakuTabFragment extends BaseListFragment {

    private DanmakuTabItemAdapter mAdapter;

    public DanmakuTabFragment() {
        super();
        isNeedLoadMore = true;
    }

    @Override
    public String getTitle() {
        return getString(R.string.danmaku);
    }

    @Override
    public int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_orange);
    }

    @Override
    protected void handleRefresh(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt("code") == 0) {
                JSONArray notifications = jsonObject.
                        getJSONObject("result").getJSONArray("notifications");
                if (mAdapter == null) {
                    mAdapter = new DanmakuTabItemAdapter();
                    mListView.setAdapter(mAdapter);
                }
                mAdapter.setData(notifications);
                mAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean handleLoadMore(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt("code") == 0) {
                JSONArray notifications = jsonObject.
                        getJSONObject("result").getJSONArray("notifications");
                if (notifications != null && notifications.length() > 0) {
                    mAdapter.addData(notifications);
                    mAdapter.notifyDataSetChanged();
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestDanmakuTab(mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().requestDanmakuTab(
                mAdapter.getLastUpdateTime(), mLoadMoreListener, null);
    }


    class DanmakuTabItemAdapter extends BaseAdapter {

        private List<DanmakuItemInfo> mData;

        DanmakuTabItemAdapter() {
            mData = new ArrayList<DanmakuItemInfo>();
        }

        public void setData(JSONArray jsonArray) {
            mData.clear();
            addData(jsonArray);
        }

        public void addData(JSONArray jsonArray) {
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                try {
                    addData(jsonArray.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void addData(JSONObject jsonObject) {
            try {
                String thumb = jsonObject.getJSONObject("video_info").getString("thumb_url");
                String title = jsonObject.getJSONObject("topic").getString("content");
                int notifyCount = jsonObject.getInt("notify_cnt");
                long updateTime = jsonObject.getLong("update_time");
                mData.add(new DanmakuItemInfo(thumb, title, notifyCount, updateTime));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public long getLastUpdateTime() {
            if (mData.size() > 0) {
                return mData.get(mData.size() - 1).updateTime;
            }

            return 0;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.danmaku_tab_item, null);
                ViewHolder holder = new ViewHolder();
                holder.thumb = (RoundedImageView) convertView.findViewById(R.id.thumb);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.notifyCount = (NotificationPointView)
                        convertView.findViewById(R.id.notify_count);
                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            DanmakuItemInfo item = (DanmakuItemInfo) getItem(position);
            ImageLoader.getInstance().displayImage(item.thumb, holder.thumb,
                    ImageLoaderHelper.topicThumbLoadOptions);
            holder.title.setText(item.title);
            if (item.notifyCount == 0) {
                holder.notifyCount.setVisibility(View.INVISIBLE);
            }
            else {
                holder.notifyCount.setVisibility(View.VISIBLE);
                holder.notifyCount.setCount(item.notifyCount);
            }

            return convertView;
        }
    }

    class DanmakuItemInfo {
        String thumb;
        String title;
        int notifyCount;
        long updateTime;

        DanmakuItemInfo(String thumb, String title, int notifyCount, long updateTime) {
            this.thumb = thumb;
            this.title = title;
            this.notifyCount = notifyCount;
            this.updateTime = updateTime;
        }
    }

    class ViewHolder {
        RoundedImageView thumb;
        TextView title;
        NotificationPointView notifyCount;
    }
}
