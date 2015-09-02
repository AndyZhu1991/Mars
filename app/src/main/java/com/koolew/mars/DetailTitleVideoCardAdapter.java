package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by jinchangzhu on 9/1/15.
 */
public class DetailTitleVideoCardAdapter extends VideoCardAdapter {

    private TopicTitleDetail topicTitleDetail;

    public DetailTitleVideoCardAdapter(Context context) {
        super(context);
    }

    @Override
    protected View getTitleView(View convertView) {
        if (convertView != null) {
            return convertView;
        }

        View title = mInflater.inflate(R.layout.topic_detail_title, null);

        TextView summary = (TextView) title.findViewById(R.id.summary);
        if (topicTitleDetail.type == TYPE_TASK) {
            summary.setTextColor(mContext.getResources().getColor(R.color.koolew_light_green));
            summary.setText(mContext.getString(R.string.invited_label, topicTitleDetail.inviter));
        }
        else {
            summary.setVisibility(View.GONE);
        }

        ((TextView) title.findViewById(R.id.title)).setText(topicTitleDetail.title);

        TextView description = ((TextView) title.findViewById(R.id.description));
        if (TextUtils.isEmpty(topicTitleDetail.description)) {
            description.setVisibility(View.GONE);
        }
        else {
            description.setText(topicTitleDetail.description);
        }

        ((TextView) title.findViewById(R.id.video_count)).setText(
                mContext.getString(R.string.video_count_label, topicTitleDetail.videoCount));

        KooCountUserInfo[] users = topicTitleDetail.kooRankUsers;
        if (users.length == 0) {
            title.findViewById(R.id.stars).setVisibility(View.GONE);
        }
        else {
            title.findViewById(R.id.stars).setOnClickListener(onStarsClickListener);

            ((TextView) title.findViewById(R.id.stars_rank_title)).setText(
                    mContext.getString(R.string.stars_rank, users.length));

            int[] avatarUrls = new int[] {
                    R.id.first_koo,
                    R.id.second_koo,
                    R.id.third_koo,
                    R.id.forth_koo,
                    R.id.fifth_koo,
            };
            for (int i = 0; i < avatarUrls.length && i < users.length; i++) {
                ImageLoader.getInstance().displayImage(users[i].getAvatar(),
                        (ImageView) title.findViewById(avatarUrls[i]));
            }

            int avatarCrowns[] = new int[]{
                    R.id.first_crown,
                    R.id.second_crown,
                    R.id.third_crown,
            };
            for (int i = 0; i < avatarCrowns.length && i < users.length; i++) {
                title.findViewById(avatarCrowns[i]).setVisibility(
                        topicTitleDetail.type == TYPE_WORLD ? View.VISIBLE : View.INVISIBLE);
            }

            title.findViewById(R.id.topic_manager).setVisibility(
                    topicTitleDetail.type == TYPE_WORLD ? View.VISIBLE : View.INVISIBLE);

            View editTopicDesc = title.findViewById(R.id.edit_topic_desc);
            if (topicTitleDetail.type == TYPE_WORLD &&
                    users[0].getAvatar().equals(MyAccountInfo.getAvatar())) {
                editTopicDesc.setVisibility(View.VISIBLE);
                editTopicDesc.setOnClickListener(onEditTopicDescListener);
            }
            else {
                editTopicDesc.setVisibility(View.INVISIBLE);
            }
        }

        return title;
    }

    private View.OnClickListener onEditTopicDescListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

    private View.OnClickListener onStarsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, TopicKooRankActivity.class);
            intent.putExtra(TopicKooRankActivity.KEY_KOO_COUNT_USER_INFO,
                    topicTitleDetail.kooRankUsers);
            mContext.startActivity(intent);
        }
    };

    public void setTopicTitleDetail(TopicTitleDetail topicTitleDetail) {
        this.topicTitleDetail = topicTitleDetail;
    }

    public static final int TYPE_FEEDS = 0;
    public static final int TYPE_WORLD = 1;
    public static final int TYPE_TASK  = 2;

    public static class TopicTitleDetail {

        private int type;
        private String inviter;
        private String title;
        private String description;
        private int videoCount;
        private KooCountUserInfo[] kooRankUsers;

        public void setType(int type) {
            this.type = type;
        }

        public void setInviter(String inviter) {
            this.inviter = inviter;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setKooRankUsers(KooCountUserInfo[] kooRankUsers) {
            this.kooRankUsers = kooRankUsers;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setVideoCount(int videoCount) {
            this.videoCount = videoCount;
        }
    }

    public static class KooCountUserInfo extends BaseUserInfo implements Serializable {

        private int kooCount;

        public KooCountUserInfo(JSONObject jsonObject) {
            super(jsonObject);
            if (jsonObject.has("koo_num")) {
                try {
                    kooCount = jsonObject.getInt("koo_num");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public int getKooCount() {
            return kooCount;
        }
    }
}
