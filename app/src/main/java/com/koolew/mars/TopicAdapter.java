package com.koolew.mars;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koolew.mars.infos.BaseFriendInfo;
import com.koolew.mars.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
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
public abstract class TopicAdapter extends BaseAdapter {

    private static final String TAG = "koolew-TopicAdapter";

    static DisplayImageOptions imgDisplayOptions = new DisplayImageOptions.Builder()
            //.showStubImage(R.drawable.stub_image)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            //.imageScaleType(ImageScaleType.EXACT)
            .build();

    private Context mContext;
    private LayoutInflater mInflater;
    private List<TopicItem> mData;

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
            holder.parters = new CircleImageView[getMaxShowTopicParterCount()];
            for (int i = 0; i < holder.parters.length; i++) {
                CircleImageView avatar = new CircleImageView(mContext);
                int avatarSize = res.getDimensionPixelSize(R.dimen.topic_parter_size);
                int avatarMarginLr = res.getDimensionPixelOffset(R.dimen.topic_parter_half_interval);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(avatarSize, avatarSize);
                params.setMargins(avatarMarginLr, 0, avatarMarginLr, 0);
                avatar.setLayoutParams(params);
                avatar.setBorderWidth(mContext.getResources().getDimensionPixelSize(R.dimen.avatar_border_width));
                if (i == 0) {
                    avatar.setBorderColorResource(R.color.koolew_light_green);
                }
                else {
                    avatar.setBorderColorResource(R.color.avatar_gray_border);
                }
                holder.parters[i] = avatar;
                holder.partersLayout.addView(avatar);
            }
        }

        TopicItem topicItem = mData.get(position);
        ViewHolder holder = (ViewHolder) convertView.getTag();

        ImageLoader.getInstance().displayImage(topicItem.thumb, holder.thumb, imgDisplayOptions);
        holder.topicTitle.setText(topicItem.title);
        holder.videoCount.setText(
                mContext.getString(R.string.video_count_label, topicItem.videoCount));

        if (holder.parters != null && holder.parters.length != 0 &&
                topicItem.parters != null && topicItem.parters.length != 0) {

            holder.partersLayout.setVisibility(View.VISIBLE);
            holder.partersArrow.setVisibility(View.VISIBLE);

            for (int i = 0; i < holder.parters.length; i++) {
                holder.parters[i].setVisibility(View.GONE);
            }

            for (int i = 0; i < topicItem.parters.length && i < holder.parters.length; i++) {
                ImageLoader.getInstance().displayImage(topicItem.parters[i].getAvatar(),
                        holder.parters[i], imgDisplayOptions);
                holder.parters[i].setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.partersLayout.setVisibility(View.GONE);
            holder.partersArrow.setVisibility(View.GONE);
        }

        return convertView;
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
        int screenWidth = Utils.getScreenWidthPixel(mContext);

        int count = (screenWidth - topicPadding * 2)
                         / (topicParterSize + topicParterHalfInterval * 2);

        maxShowTopicParterCount = Math.min(count, AppProperty.getTopicMaxReturnParterCount());
        return maxShowTopicParterCount;
    }

    public class TopicItem {
        public String topicId;
        public String title;
        public String thumb;
        public int videoCount;
        public long updateTime;
        public BaseFriendInfo[] parters;

        public TopicItem() {
        }

        public TopicItem(String topicId, String title, String thumb, int videoCount, long updateTime) {
            this(topicId, title, thumb, videoCount, updateTime, null);
        }

        public TopicItem(String topicId, String title, String thumb, int videoCount, long updateTime,
                         BaseFriendInfo[] parters) {
            this.topicId = topicId;
            this.title = title;
            this.thumb = thumb;
            this.videoCount = videoCount;
            this.updateTime = updateTime;
            this.parters = parters;
        }
    }

    class ViewHolder {
        FrameLayout videoFrame;
        ImageView thumb;
        TextView topicTitle;
        TextView videoCount;
        LinearLayout partersLayout;
        CircleImageView[] parters;
        ImageView partersArrow;
    }
}
