package com.koolew.mars;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseCommentInfo;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.redpoint.RedPointManager;
import com.koolew.mars.redpoint.RedPointView;
import com.koolew.mars.webapi.UrlHelper;
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
public class DanmakuTabFragment extends
        RecyclerListFragmentMould<DanmakuTabFragment.DanmakuTabItemAdapter> {

    public DanmakuTabFragment() {
        super();
        isNeedLoadMore = true;
    }

    @Override
    protected int getNoDataViewResId() {
        return R.layout.no_danmaku_layout;
    }

    @Override
    protected DanmakuTabItemAdapter useThisAdapter() {
        return new DanmakuTabItemAdapter();
    }

    @Override
    public int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_blue);
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        RedPointManager.clearRedPointByPath(RedPointManager.PATH_DANMAKU);
        try {
            JSONArray notifications = result.getJSONArray("notifications");
            mAdapter.setData(notifications);
            mAdapter.notifyDataSetChanged();
            return notifications.length() > 0;
        } catch (JSONException e) {
            handleJsonException(result, e);
        }
        return false;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        try {
            JSONArray notifications = result.getJSONArray("notifications");
            if (notifications != null && notifications.length() > 0) {
                mAdapter.addData(notifications);
                mAdapter.notifyDataSetChanged();
                return true;
            }
        } catch (JSONException e) {
            handleJsonException(result, e);
        }
        return false;
    }

    @Override
    protected String getRefreshRequestUrl() {
        return UrlHelper.DANMAKU_TAB_URL;
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getDanmakuTabUrl(mAdapter.getLastUpdateTime());
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
            holder.notifyCount.setCount(item.notifyCount);
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
                videoInfo = new BaseVideoInfo(jsonObject.getJSONObject("video"));
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
        RedPointView notifyCount;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            thumb = (RoundedImageView) itemView.findViewById(R.id.thumb);
            title = (TextView) itemView.findViewById(R.id.title);
            lastComment = (TextView) itemView.findViewById(R.id.last_comment);
            notifyCount = (RedPointView) itemView.findViewById(R.id.notify_count);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            DanmakuNotificationItem item = mAdapter.getItem(position);
            String videoId = item.videoInfo.getVideoId();
            SingleMediaFragment.startThisFragment(getActivity(), videoId);

            if (item.notifyCount > 0) {
                item.notifyCount = 0;
                mAdapter.notifyItemChanged(position);
            }
        }
    }
}
