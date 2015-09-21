package com.koolew.mars;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseCommentInfo;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
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
public class DanmakuTabFragment extends RecyclerListFragmentMould {

    public DanmakuTabFragment() {
        super();
        isNeedLoadMore = true;
    }

    @Override
    protected LoadMoreAdapter useThisAdapter() {
        return new DanmakuTabItemAdapter();
    }

    @Override
    public int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_blue);
    }

    @Override
    protected boolean handleRefresh(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt("code") == 0) {
                JSONArray notifications = jsonObject.
                        getJSONObject("result").getJSONArray("notifications");
                ((DanmakuTabItemAdapter) mAdapter).setData(notifications);
                mAdapter.notifyDataSetChanged();

                return notifications.length() > 0;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected boolean handleLoadMore(JSONObject jsonObject) {
        try {
            if (jsonObject.getInt("code") == 0) {
                JSONArray notifications = jsonObject.
                        getJSONObject("result").getJSONArray("notifications");
                if (notifications != null && notifications.length() > 0) {
                    ((DanmakuTabItemAdapter) mAdapter).addData(notifications);
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
                ((DanmakuTabItemAdapter) mAdapter).getLastUpdateTime(), mLoadMoreListener, null);
    }


    class DanmakuTabItemAdapter extends LoadMoreAdapter {

        private List<DanmakuNotificationItem> mData;

        DanmakuTabItemAdapter() {
            mData = new ArrayList<>();
        }

        public void setData(JSONArray jsonArray) {
            mData.clear();
            addData(jsonArray);
        }

        public void addData(JSONArray jsonArray) {
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                try {
                    mData.add(new DanmakuNotificationItem(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public DanmakuNotificationItem getItem(int position) {
            return mData.get(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.danmaku_tab_item, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder originHolder, int position) {
            ViewHolder holder = (ViewHolder) originHolder;
            DanmakuNotificationItem item = mData.get(position);
            ImageLoader.getInstance().displayImage(item.videoInfo.getVideoThumb(), holder.thumb,
                    ImageLoaderHelper.topicThumbLoadOptions);
            holder.title.setText(item.topicInfo.getTitle());
            holder.lastComment.setText(item.lastComment.getUserInfo().getNickname()
                    + ": " + item.lastComment.getContent());
            if (item.notifyCount == 0) {
                holder.notifyCount.setVisibility(View.INVISIBLE);
            }
            else {
                holder.notifyCount.setVisibility(View.VISIBLE);
                holder.notifyCount.setCount(item.notifyCount);
            }
        }

        @Override
        public int getCustomItemCount() {
            return mData.size();
        }

        public long getLastUpdateTime() {
            if (mData.size() > 0) {
                return mData.get(mData.size() - 1).updateTime;
            }
            return 0;
        }
    }

    class DanmakuNotificationItem {
        BaseVideoInfo videoInfo;
        BaseTopicInfo topicInfo;
        BaseCommentInfo lastComment;
        int notifyCount;
        long updateTime;

        DanmakuNotificationItem(JSONObject jsonObject) {
            try {
                videoInfo = new BaseVideoInfo(jsonObject.getJSONObject("video_info"));
                topicInfo = new BaseTopicInfo(jsonObject.getJSONObject("topic"));
                lastComment = new BaseCommentInfo(jsonObject.getJSONObject("comment"));
                notifyCount = jsonObject.getInt("notify_cnt");
                updateTime = jsonObject.getLong("update_time");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoundedImageView thumb;
        TextView title;
        TextView lastComment;
        NotificationPointView notifyCount;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            thumb = (RoundedImageView) itemView.findViewById(R.id.thumb);
            title = (TextView) itemView.findViewById(R.id.title);
            lastComment = (TextView) itemView.findViewById(R.id.last_comment);
            notifyCount = (NotificationPointView) itemView.findViewById(R.id.notify_count);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            DanmakuNotificationItem item = ((DanmakuTabItemAdapter) mAdapter).getItem(position);
            Intent intent = new Intent(getActivity(), CheckDanmakuActivity.class);
            String videoId = item.videoInfo.getVideoId();
            intent.putExtra(CheckDanmakuActivity.KEY_VIDEO_ID, videoId);
            startActivity(intent);

            if (item.notifyCount > 0) {
                item.notifyCount = 0;
                mAdapter.notifyItemChanged(position);
            }
        }
    }
}
