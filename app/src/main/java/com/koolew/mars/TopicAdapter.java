package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koolew.mars.danmaku.DanmakuItemInfo;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.player.ScrollPlayer;
import com.koolew.mars.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jinchangzhu on 6/2/15.
 */
public abstract class TopicAdapter extends BaseAdapter implements View.OnClickListener {

    private static final String TAG = "koolew-TopicAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    protected List<TopicItem> mData;

    TopicAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);

        mData = new ArrayList<TopicItem>();
    }

    public void setData(JSONArray cards) {
        mData.clear();
        addData(cards);
    }

    public int addData(JSONArray cards) {
        int length = cards.length();
        try {
            for (int i = 0; i < length; i++) {
                mData.add(jsonObject2TopicItem(cards.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return length;
    }

    public long getOldestCardTime() {
        return mData.get(mData.size() - 1).updateTime;
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
            convertView = mInflater.inflate(R.layout.topic_card_item, null);
            ViewHolder holder = new ViewHolder();
            convertView.setTag(holder);

            Resources res = mContext.getResources();

            holder.videoFrame = (FrameLayout) convertView.findViewById(R.id.video_frame);
            ViewGroup.LayoutParams lp = holder.videoFrame.getLayoutParams();
            int cardWidth = Utils.getScreenWidthPixel(mContext)
                    - res.getDimensionPixelSize(R.dimen.topic_invitation_card_padding) * 2;
            lp.height = cardWidth / 4 * 3;
            holder.videoFrame.setLayoutParams(lp);

            convertView.findViewById(R.id.info_layout).getLayoutParams().height = lp.height / 2;

            holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
            holder.topicTitle = (TextView) convertView.findViewById(R.id.topic_title);
            holder.videoCount = (TextView) convertView.findViewById(R.id.video_count);

            holder.partersLayout = (LinearLayout) convertView.findViewById(R.id.parters_layout);
            holder.partersArrow = (ImageView) convertView.findViewById(R.id.parters_arrow);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress);
            holder.parters = new CircleImageView[getMaxShowTopicParterCount()];
            for (int i = 0; i < holder.parters.length; i++) {
                CircleImageView avatar = new CircleImageView(mContext);
                avatar.setOnClickListener(this);
                int avatarSize = res.getDimensionPixelSize(R.dimen.topic_parter_size);
                int avatarMarginLr = res.getDimensionPixelOffset(R.dimen.topic_parter_half_interval);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(avatarSize, avatarSize);
                params.setMargins(avatarMarginLr, 0, avatarMarginLr, 0);
                avatar.setLayoutParams(params);
                avatar.setBorderWidth(mContext.getResources().getDimensionPixelSize(R.dimen.avatar_border_width));
                holder.parters[i] = avatar;
                holder.partersLayout.addView(avatar);
            }
        }

        TopicItem topicItem = mData.get(position);
        ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.position = position;

        ImageLoader.getInstance().displayImage(topicItem.thumb, holder.thumb,
                ImageLoaderHelper.topicThumbLoadOptions);
        holder.topicTitle.setText(topicItem.title);
        holder.videoCount.setText(
                mContext.getString(R.string.video_count_label, topicItem.videoCount));
        holder.progressBar.setVisibility(View.INVISIBLE);

        if (holder.parters != null && holder.parters.length != 0 &&
                topicItem.parters != null && topicItem.parters.length != 0) {

            holder.partersLayout.setVisibility(View.VISIBLE);
            holder.partersArrow.setVisibility(View.VISIBLE);

            for (int i = 0; i < holder.parters.length; i++) {
                holder.parters[i].setVisibility(View.GONE);
            }

            for (int i = 0; i < topicItem.parters.length && i < holder.parters.length; i++) {
                if (topicItem.parters[i].isSpecial) {
                    holder.parters[i].setBorderColorResource(R.color.koolew_light_green);
                }
                else {
                    holder.parters[i].setBorderColorResource(R.color.avatar_gray_border);
                }

                ImageLoader.getInstance().displayImage(topicItem.parters[i].getAvatar(),
                        holder.parters[i], ImageLoaderHelper.avatarLoadOptions);
                holder.parters[i].setTag(topicItem.parters[i].getUid());

                holder.parters[i].setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.partersLayout.setVisibility(View.GONE);
            holder.partersArrow.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof CircleImageView) {
            Intent intent = new Intent(mContext, FriendInfoActivity.class);
            intent.putExtra(FriendInfoActivity.KEY_UID, v.getTag().toString());
            mContext.startActivity(intent);
        }
    }

    public abstract TopicItem jsonObject2TopicItem(JSONObject jsonObject);

    private static int maxShowTopicParterCount = -1;
    private int getMaxShowTopicParterCount() {
        if (maxShowTopicParterCount > 0) {
            return maxShowTopicParterCount;
        }

        Resources res = mContext.getResources();
        int topicPadding = res.getDimensionPixelSize(R.dimen.topic_invitation_card_padding);
        int topicParterSize = res.getDimensionPixelSize(R.dimen.topic_parter_size);
        int topicParterHalfInterval = res.getDimensionPixelSize(R.dimen.topic_parter_half_interval);
        int containerPadding = res.getDimensionPixelSize(R.dimen.topic_parter_container_lr_padding);
        int screenWidth = Utils.getScreenWidthPixel(mContext);

        int count = (screenWidth - topicPadding * 2 - containerPadding * 2)
                         / (topicParterSize + topicParterHalfInterval * 2);

        maxShowTopicParterCount = Math.min(count, AppProperty.getTopicMaxReturnParterCount());
        return maxShowTopicParterCount;
    }

    public class TopicItem {
        public String topicId;
        public String title;
        public String thumb;
        public String videoUrl;
        public int videoCount;
        public long updateTime;
        public UserInfo[] parters;

        public TopicItem() {
        }

        public TopicItem(String topicId, String title, String thumb, int videoCount, long updateTime) {
            this(topicId, title, thumb, videoCount, updateTime, null);
        }

        public TopicItem(String topicId, String title, String thumb, int videoCount, long updateTime,
                         UserInfo[] parters) {
            this.topicId = topicId;
            this.title = title;
            this.thumb = thumb;
            this.videoCount = videoCount;
            this.updateTime = updateTime;
            this.parters = parters;
        }
    }

    public static class UserInfo extends BaseUserInfo {
        public boolean isSpecial;

        public UserInfo(JSONObject jsonObject) {
            super(jsonObject);
        }
    }

    class ViewHolder {
        public int position;

        FrameLayout videoFrame;
        ImageView thumb;
        TextView topicTitle;
        TextView videoCount;
        LinearLayout partersLayout;
        CircleImageView[] parters;
        ImageView partersArrow;
        ProgressBar progressBar;
    }

    public class TopicScrollPlayer extends ScrollPlayer {

        public TopicScrollPlayer(ListView listView) {
            super(listView);
        }

        @Override
        public boolean isItemView(View childView) {
            return childView.getTag() instanceof ViewHolder;
        }

        @Override
        public ViewGroup getSurfaceContainer(View itemView) {
            return ((ViewHolder) itemView.getTag()).videoFrame;
        }

        @Override
        public ViewGroup getDanmakuContainer(View itemView) {
            return null;
        }

        @Override
        public ArrayList<DanmakuItemInfo> getDanmakuList(View itemView) {
            return null;
        }

        @Override
        public ImageView getThumbImage(View itemView) {
            return ((ViewHolder) itemView.getTag()).thumb;
        }

        @Override
        public View getProgressView(View itemView) {
            return ((ViewHolder) itemView.getTag()).progressBar;
        }

        @Override
        public String getVideoUrl(View itemView) {
            return mData.get(((ViewHolder) itemView.getTag()).position).videoUrl;
        }
    }
}
