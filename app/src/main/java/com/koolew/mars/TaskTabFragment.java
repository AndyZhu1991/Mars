package com.koolew.mars;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.redpoint.RedPointManager;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jinchangzhu on 11/5/15.
 */
public class TaskTabFragment extends RecyclerListFragmentMould<TaskTabFragment.TaskAdapter> {

    public TaskTabFragment() {
        super();
        isNeedLoadMore = true;
        isLazyLoad = true;
    }

    @Override
    protected int getNoDataViewResId() {
        return R.layout.no_task_layout;
    }

    @Override
    protected TaskAdapter useThisAdapter() {
        return new TaskAdapter();
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_green);
    }

    @Override
    protected String getRefreshRequestUrl() {
        return UrlHelper.TASK_URL;
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getTaskUrl(mAdapter.getLastCardTime());
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        RedPointManager.clearRedPointByPath(RedPointManager.PATH_TASK);
        try {
            JSONArray cards = result.getJSONArray("cards");
            return mAdapter.setData(cards);
        } catch (JSONException e) {
            handleJsonException(result, e);
        }
        return false;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        try {
            JSONArray cards = result.getJSONArray("cards");
            return mAdapter.addData(cards);
        } catch (JSONException e) {
            handleJsonException(result, e);
        }
        return false;
    }

    class TaskAdapter extends LoadMoreAdapter {

        private List<TaskItem> taskItems = new ArrayList<>();

        public boolean setData(JSONArray jsonArray) {
            taskItems.clear();
            int addedCount = addToList(jsonArray);
            notifyDataSetChanged();
            return addedCount > 0;
        }

        public boolean addData(JSONArray jsonArray) {
            int countBeforeAdd = taskItems.size();
            int addedCount = addToList(jsonArray);
            if (addedCount > 0) {
                notifyItemRangeInserted(countBeforeAdd, addedCount);
                return true;
            }
            else {
                return false;
            }
        }

        private int addToList(JSONArray jsonArray) {
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                try {
                    taskItems.add(new TaskItem(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return count;
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new TaskItemHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.task_item, null));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            TaskItem item = taskItems.get(position);
            TaskItemHolder holder = (TaskItemHolder) viewHolder;
            if (item.isNew) {
                holder.newTaskFlag.setVisibility(View.VISIBLE);
                holder.avatar.setBorderColor(getResources().getColor(R.color.koolew_light_green));
            }
            else {
                holder.newTaskFlag.setVisibility(View.INVISIBLE);
                holder.avatar.setBorderColor(getResources().getColor(R.color.avatar_gray_border));
            }

            ImageLoader.getInstance().displayImage(item.user.getAvatar(), holder.avatar,
                    ImageLoaderHelper.avatarLoadOptions);
            holder.nameView.setUser(item.user);
            holder.topicCount.setText(getString(R.string.counter_ge, item.taskCount));

            holder.avatar.setTag(item.user.getUid());


            for (int i = 0; i < holder.topicLayout.getChildCount(); i++) {
                holder.topicLayout.getChildAt(i).setVisibility(View.GONE);
            }
            int count = item.topics.length;
            int topicContainerWidthRemaining = getTopicContainerWidth();
            int topicItemMinWidth = getResources().getDimensionPixelSize(
                    R.dimen.task_item_topic_item_min_width);
            int topicItemRightMargin = getResources().getDimensionPixelSize(
                    R.dimen.task_item_topic_item_right_margin);
            for (int i = 0; i < count ; i++) {
                TextView topicText;
                if (holder.topicLayout.getChildCount() <= i) {
                    topicText = generateTopicTextView();
                    holder.topicLayout.addView(topicText);
                    topicText.setOnClickListener(holder);
                } else {
                    topicText = (TextView) holder.topicLayout.getChildAt(i);
                }

                String content = item.topics[i].getTitle();
                topicText.setText(content);
                topicText.setTag(i);

                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) topicText.getLayoutParams();
                int width = getTopicItemWidth(content);
                if (width + topicItemRightMargin <= topicContainerWidthRemaining) {
                    lp.width = width;
                } else {
                    lp.width = topicContainerWidthRemaining - topicItemRightMargin;
                }
                topicText.setLayoutParams(lp);

                topicText.setVisibility(View.VISIBLE);

                topicContainerWidthRemaining -= (lp.width + topicItemRightMargin);
                if (topicContainerWidthRemaining < topicItemMinWidth) {
                    break;
                }
            }
        }

        @Override
        public int getCustomItemCount() {
            return taskItems.size();
        }

        private int getTopicContainerWidth() {
            return Utils.getScreenWidthPixel(getActivity())
                    - getResources().getDimensionPixelSize(R.dimen.task_item_margin) * 2
                    - getResources().getDimensionPixelSize(R.dimen.task_item_bottom_left_padding)
                    - getResources().getDimensionPixelSize(R.dimen.task_item_bottom_right_padding);
        }

        private TextView generateTopicTextView() {
            TextView textView = new TextView(getActivity());

            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setBackgroundResource(R.drawable.task_item_topic_item_bg);
            textView.setSingleLine();

            textView.setTextSize(Utils.pixelsToSp(getActivity(),
                    getResources().getDimension(R.dimen.task_item_topic_item_text_size)));
            textView.setTextColor(0xFF9B9B9B);

            textView.setPadding(getResources().getDimensionPixelOffset(R.dimen.task_item_topic_item_lr_padding), 0,
                    getResources().getDimensionPixelOffset(R.dimen.task_item_topic_item_lr_padding), 0);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, getResources().getDimensionPixelOffset(R.dimen.task_item_topic_item_right_margin), 0);
            textView.setLayoutParams(lp);

            return textView;
        }

        private int getTopicItemWidth(String text) {
            int fullWidth = (int) (Utils.getTextWidth(text, getResources().getDimension(R.dimen.task_item_topic_item_text_size))
                    + getResources().getDimensionPixelSize(R.dimen.task_item_topic_item_lr_padding) * 2) + 2;
            int minWidth = getResources().getDimensionPixelOffset(R.dimen.task_item_topic_item_min_width);

            return Math.max(fullWidth, minWidth);
        }

        public long getLastCardTime() {
            if (taskItems.size() > 0) {
                return taskItems.get(taskItems.size() - 1).updateTime;
            }
            else {
                return 0;
            }
        }
    }

    class TaskItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView newTaskFlag;
        private CircleImageView avatar;
        private UserNameView nameView;
        private TextView topicCount;
        private LinearLayout topicLayout;

        public TaskItemHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            newTaskFlag = (ImageView) itemView.findViewById(R.id.new_task_flag);
            avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            avatar.setOnClickListener(this);
            nameView = (UserNameView) itemView.findViewById(R.id.name_view);
            topicCount = (TextView) itemView.findViewById(R.id.topic_count);
            topicLayout = (LinearLayout) itemView.findViewById(R.id.topic_layout);
        }

        @Override
        public void onClick(View v) {
            if (v == itemView) {
                onTaskItemClick();
            }
            else if (v == avatar) {
                onAvatarClick();
            }
            else {
                onTopicItemClick(v);
            }
        }

        private void onTaskItemClick() {
            Intent intent = new Intent(getActivity(), FriendTaskActivity.class);
            BaseUserInfo user = mAdapter.taskItems.get(getAdapterPosition()).user;
            intent.putExtra(FriendTaskActivity.KEY_UID, user.getUid());
            intent.putExtra(FriendTaskActivity.KEY_NICKNAME, user.getNickname());
            startActivity(intent);
        }

        private void onAvatarClick() {
            FriendInfoActivity.startThisActivity(getActivity(),
                    mAdapter.taskItems.get(getAdapterPosition()).user.getUid());
        }

        private void onTopicItemClick(View v) {
            TaskItem item = mAdapter.taskItems.get(getAdapterPosition());
            int topicIndex = (int) v.getTag();
            BaseTopicInfo currentTopic = item.topics[topicIndex];

            TopicMediaActivity.startThisActivity(getActivity(), currentTopic.getTopicId(),
                    TopicMediaActivity.TYPE_TASK);
        }
    }

    class TaskItem {
        private BaseTopicInfo[] topics;
        private int taskCount;
        private BaseUserInfo user;
        private long updateTime;
        private boolean isNew;

        private TaskItem(JSONObject jsonObject) {
            try {
                JSONArray jsonTopics = jsonObject.getJSONArray("topics");
                int topicCount = jsonTopics.length();
                this.topics = new BaseTopicInfo[topicCount];
                for (int i = 0; i < topicCount; i++) {
                    this.topics[i] = new BaseTopicInfo(jsonTopics.getJSONObject(i));
                }
                taskCount = jsonObject.getInt("task_cnt");
                user = new BaseUserInfo(jsonObject.getJSONObject("user"));
                updateTime = jsonObject.getLong("update_time");
                isNew = jsonObject.getInt("new") == 1;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
