package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.utils.JsonUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 12/11/15.
 */
public class TimelineAdapter extends LoadMoreAdapter {

    private static final int TYPE_CAPTURE_LINE = 1;
    private static final int TYPE_TIMELINE_LINE = 2;

    protected Context mContext;
    protected InvolveData mData;

    protected boolean isSelf = false;
    protected BaseUserInfo mUserInfo;


    public TimelineAdapter(Context context) {
        mContext = context;
        mData = new InvolveData();
    }

    public void setIsSelf() {
        isSelf = true;
    }

    public void setUserInfo(BaseUserInfo userInfo) {
        mUserInfo = userInfo;
    }

    public long getLastUpdateTime() {
        return mData.getLastUpdateTime();
    }

    public int setItems(JSONArray cards) {
        mData.clear();
        int addedCount = addData(cards);
        notifyDataSetChanged();
        return addedCount;
    }

    public int addItems(JSONArray cards) {
        int originCount = mData.size();
        boolean lastLineFull = mData.isLastLineFull();
        int addedCount = addData(cards);
        if (addedCount > 0) {
            if (!lastLineFull) {
                notifyItemChanged(originCount - 1);
            }
            int addedLine = mData.size() - originCount;
            if (addedLine > 0) {
                notifyItemRangeInserted(originCount, addedLine);
            }
        }

        return addedCount;
    }

    private int addData(JSONArray cards) {
        int addedCount = 0;

        try {
            int count = cards.length();
            for (int i = 0; i < count; i++) {
                JSONObject topic = cards.getJSONObject(i);
                TimelineItem item = new TimelineItem(topic);
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
    public int getCustomItemViewType(int position) {
        Object item = mData.get(position);
        if (item instanceof TimelineItem) {
            return TYPE_CAPTURE_LINE;
        }
        else {
            return TYPE_TIMELINE_LINE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_CAPTURE_LINE) {
            return new InvolveFirstLineHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.involve_first_line, parent, false));
        }
        else {
            return new InvolveLineHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.involve_line, parent, false));
        }
    }

    @Override
    public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (getCustomItemViewType(position) == TYPE_CAPTURE_LINE) {
            ((InvolveFirstLineHolder) viewHolder).bindTimelineItem((TimelineItem) mData.get(position));
        }
        else {
            ((InvolveLineHolder) viewHolder).bindTimelineLine((TimelineLine) mData.get(position));
        }
    }

    @Override
    public int getCustomItemCount() {
        return mData.size();
    }


    static class TimelineItem extends BaseTopicInfo {
        boolean isManager;

        TimelineItem(JSONObject jsonObject) {
            super(jsonObject);
            this.isManager = JsonUtil.getIntIfHas(jsonObject, "is_manager") != 0;
        }
    }

    static class TimelineLine {
        private TimelineItem leftItem;
        private TimelineItem rightItem;

        private TimelineLine(TimelineItem leftItem, TimelineItem rightItem) {
            this.leftItem = leftItem;
            this.rightItem = rightItem;
        }
    }

    class InvolveData {
        List<TimelineItem> timelineItems = new ArrayList<>();

        private void clear() {
            timelineItems.clear();
        }

        private int size() {
            if (timelineItems.size() == 0) {
                return 0;
            }
            if (isSelf) {
                return (timelineItems.size() + 2) / 2;
            }
            else {
                return (timelineItems.size() + 1) / 2;
            }
        }

        private void add(TimelineItem timelineItem) {
            timelineItems.add(timelineItem);
        }

        private Object get(int position) {
            int leftPosition;
            int rightPosition;
            if (isSelf) {
                leftPosition = position * 2 - 1;
                rightPosition = position * 2;
            }
            else {
                leftPosition = position * 2;
                rightPosition = position * 2 + 1;
            }

            TimelineItem rightItem = rightPosition < timelineItems.size()
                    ? timelineItems.get(rightPosition) : null;
            if (isSelf && position == 0) {
                return rightItem;
            }
            TimelineItem leftItem = timelineItems.get(leftPosition);

            return new TimelineLine(leftItem, rightItem);
        }

        private boolean hasTopic(String topicId) {
            for (TimelineItem item: timelineItems) {
                if (item.getTopicId().equals(topicId)) {
                    return true;
                }
            }
            return false;
        }

        private long getLastUpdateTime() {
            if (timelineItems.size() == 0) {
                return Long.MAX_VALUE;
            }
            else {
                return timelineItems.get(timelineItems.size() - 1).getUpdateTime();
            }
        }

        private boolean isLastLineFull() {
            if (isSelf) {
                return timelineItems.size() % 2 == 1;
            }
            else {
                return timelineItems.size() % 2 == 0;
            }
        }
    }

    class InvolveFirstLineHolder extends RecyclerView.ViewHolder {
        private InvolveCaptureHolder captureHolder;
        private InvolveItemHolder itemHolder;

        public InvolveFirstLineHolder(View itemView) {
            super(itemView);

            captureHolder = new InvolveCaptureHolder(itemView.findViewById(R.id.capture_layout));
            itemHolder = new InvolveItemHolder(itemView.findViewById(R.id.item_layout));
        }

        public void bindTimelineItem(TimelineItem item) {
            itemHolder.bindInvolveItem(item);
        }
    }

    class InvolveLineHolder extends RecyclerView.ViewHolder {
        private InvolveItemHolder leftHolder;
        private InvolveItemHolder rightHolder;

        public InvolveLineHolder(View itemView) {
            super(itemView);

            leftHolder = new InvolveItemHolder(itemView.findViewById(R.id.left_item));
            rightHolder = new InvolveItemHolder(itemView.findViewById(R.id.right_item));
        }

        private void bindTimelineLine(TimelineLine timelineLine) {
            leftHolder.bindInvolveItem(timelineLine.leftItem);
            rightHolder.bindInvolveItem(timelineLine.rightItem);
        }
    }

    class InvolveItemHolder implements View.OnClickListener {
        TimelineItem timelineItem;

        View itemView;
        ImageView thumb;
        TextView videoCount;
        TextView title;
        TextView manager;

        public InvolveItemHolder(View itemView) {
            this.itemView = itemView;
            itemView.setOnClickListener(this);
            thumb = (ImageView) itemView.findViewById(R.id.thumb);
            videoCount = (TextView) itemView.findViewById(R.id.video_count);
            title = (TextView) itemView.findViewById(R.id.title);
            manager = (TextView) itemView.findViewById(R.id.is_manager);
        }

        private void bindInvolveItem(TimelineItem timelineItem) {
            this.timelineItem = timelineItem;
            if (timelineItem == null) {
                itemView.setVisibility(View.INVISIBLE);
            }
            else {
                if (itemView.getVisibility() == View.INVISIBLE) {
                    itemView.setVisibility(View.VISIBLE);
                }
                ImageLoader.getInstance().displayImage(timelineItem.getThumb(), thumb,
                        ImageLoaderHelper.topicThumbLoadOptions);
                videoCount.setText(String.valueOf(timelineItem.getVideoCount()));
                title.setText(timelineItem.getTitle());
                if (timelineItem.isManager) {
                    manager.setVisibility(View.VISIBLE);
                }
                else {
                    manager.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (isSelf) {
                UserMediaActivity.startMyMediaActivity(v.getContext(), timelineItem.getTopicId());
            }
            else {
                UserMediaActivity.startThisActivity(mContext, timelineItem.getTopicId(),
                        mUserInfo.getUid(), mUserInfo.getNickname());
            }
        }
    }

    static class InvolveCaptureHolder implements View.OnClickListener {
        View captureTopicVideo;
        View captureMovie;

        public InvolveCaptureHolder(View itemView) {
            captureTopicVideo = itemView.findViewById(R.id.capture_topic_video);
            captureTopicVideo.setOnClickListener(this);
            captureMovie = itemView.findViewById(R.id.capture_movie);
            captureMovie.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.capture_topic_video:
                    v.getContext().startActivity(new Intent(v.getContext(), JoinVideoActivity.class));
                    break;
                case R.id.capture_movie:
                    v.getContext().startActivity(new Intent(v.getContext(), JoinMovieActivity.class));
                    break;
            }
        }
    }
}
