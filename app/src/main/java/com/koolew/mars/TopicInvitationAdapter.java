package com.koolew.mars;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
public class TopicInvitationAdapter extends BaseAdapter {

    private static final String TAG = "koolew-TopicInvitAdpt";

    public static final int TYPE_TOPIC = 0;
    public static final int TYPE_INVITATION = 1;
    public static final int TYPE_COUNT = 2; // This is COUNT

    private Context mContext;
    private LayoutInflater mInflater;
    public List<JSONObject> mData;

    TopicInvitationAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);

        mData = new ArrayList<JSONObject>();
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

        int type = getItemViewType(position);

        if (type == TYPE_TOPIC) {
            return getTopicView(position, convertView, parent);
        }
        else if (type == TYPE_INVITATION) {
            return getInvitationView(position, convertView, parent);
        }
        return null;
    }

    private View getTopicView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.topic_card_item, null);
            ViewHolderTopic holder = new ViewHolderTopic();
            convertView.setTag(holder);
            holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
            holder.topicTitle = (TextView) convertView.findViewById(R.id.topic_title);
            holder.videoCount = (TextView) convertView.findViewById(R.id.video_count);
            holder.partersLayout = (LinearLayout) convertView.findViewById(R.id.parters_layout);
            holder.parters = new CircleImageView[getMaxShowTopicParterCount()];
            for (int i = 0; i < holder.parters.length; i++) {
                CircleImageView avatar = new CircleImageView(mContext);
                Resources res = mContext.getResources();
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
                    avatar.setBorderColorResource(R.color.koolew_gray);
                }
                holder.parters[i] = avatar;
                holder.partersLayout.addView(avatar);
            }
        }

        try {
            JSONObject itemJson = mData.get(position);
            JSONObject topic = itemJson.getJSONObject("topic");
            JSONArray parters = itemJson.getJSONArray("parters");
            ViewHolderTopic holder = (ViewHolderTopic) convertView.getTag();

            ImageLoader.getInstance().displayImage(topic.getString("thumb_url"), holder.thumb);
            holder.topicTitle.setText(topic.getString("content"));
            holder.videoCount.setText(
                    mContext.getString(R.string.video_count_label, topic.getInt("video_cnt")));

            for (int i = 0; i < holder.parters.length; i++) {
                holder.parters[i].setVisibility(View.GONE);
            }
            for (int i = 0; i < parters.length(); i++) {
                ImageLoader.getInstance().
                        displayImage(((JSONObject) parters.get(i)).getString("avatar"), holder.parters[i]);
                holder.parters[i].setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "");
        }

        return convertView;
    }

    private View getInvitationView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.invitation_card_item, null);
            ViewHolderInvitation holder = new ViewHolderInvitation();
            convertView.setTag(holder);
            holder.invitationTitle = (TextView) convertView.findViewById(R.id.invitation_title);
            holder.topicTitle = (TextView) convertView.findViewById(R.id.topic_title);
            holder.videoCount = (TextView) convertView.findViewById(R.id.video_count);
            holder.acceptBtn = (ImageButton) convertView.findViewById(R.id.btn_accept);
        }

        try {
            JSONObject itemJson = mData.get(position);
            JSONObject topic = itemJson.getJSONObject("topic");
            JSONArray parters = itemJson.getJSONArray("parters");
            ViewHolderInvitation holder = (ViewHolderInvitation) convertView.getTag();

            holder.topicTitle.setText(topic.getString("content"));
            holder.videoCount.setText(
                    mContext.getString(R.string.video_count_label, topic.getInt("video_cnt")));

            String inviter = ((JSONObject) parters.get(0)).getString("nickname");
            int partersLength = parters.length();
            int i = 1;
            for (; i < partersLength && i < 3; i++) {
                inviter += "、" +((JSONObject) parters.get(i)).getString("nickname");
            }
            if (i < partersLength) {
                inviter += "……";
            }
            holder.invitationTitle.setText(mContext.getString(R.string.invited_label, inviter));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        int type = TYPE_TOPIC;
        JSONObject jsonObject = mData.get(position);
        try {
            switch (jsonObject.getInt("type")) {
                case 0:
                    type = TYPE_INVITATION;
                    break;
                case 1:
                    type = TYPE_TOPIC;
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return type;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    private static int maxShowTopicParterCount = -1;
    private int getMaxShowTopicParterCount() {
        if (maxShowTopicParterCount > 0) {
            return maxShowTopicParterCount;
        }

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        Resources res = mContext.getResources();
        int topicMargin = res.getDimensionPixelSize(R.dimen.topic_invitation_card_margin);
        int topicPadding = res.getDimensionPixelSize(R.dimen.topic_invitation_card_padding);
        int topicParterSize = res.getDimensionPixelSize(R.dimen.topic_parter_size);
        int topicParterHalfInterval = res.getDimensionPixelSize(R.dimen.topic_parter_half_interval);
        int screenWidth = outMetrics.widthPixels;

        int count = (screenWidth - topicMargin * 2 - topicPadding * 2)
                         / (topicParterSize + topicParterHalfInterval * 2);

        maxShowTopicParterCount = Math.min(count, AppProperty.getTopicMaxReturnParterCount());
        return maxShowTopicParterCount;
    }

    class ViewHolderTopic {
        ImageView thumb;
        TextView topicTitle;
        TextView videoCount;
        LinearLayout partersLayout;
        CircleImageView[] parters;
    }

    class ViewHolderInvitation {
        TextView invitationTitle;
        TextView topicTitle;
        TextView videoCount;
        ImageButton acceptBtn;
    }
}
