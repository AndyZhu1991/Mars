package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.KooCountUserInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by jinchangzhu on 9/1/15.
 */
public class DetailTitleVideoCardAdapter extends VideoCardAdapter {

    private TopicTitleDetail topicTitleDetail;
    protected String mTopicId;

    public DetailTitleVideoCardAdapter(Context context, String topicId) {
        super(context);
        mTopicId = topicId;
    }

    @Override
    protected View getTitleView(View convertView) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.topic_detail_title, null);
        }

        TextView summary = (TextView) convertView.findViewById(R.id.summary);
        if (topicTitleDetail.type == TYPE_TASK) {
            summary.setTextColor(mContext.getResources().getColor(R.color.koolew_light_green));
            summary.setText(mContext.getString(R.string.invited_label, topicTitleDetail.inviter));
        }
        else {
            summary.setVisibility(View.GONE);
        }

        ((TextView) convertView.findViewById(R.id.title)).setText(topicTitleDetail.title);

        TextView description = ((TextView) convertView.findViewById(R.id.description));
        if (TextUtils.isEmpty(topicTitleDetail.description)) {
            description.setVisibility(View.GONE);
        }
        else {
            description.setText(topicTitleDetail.description);
        }

        ((TextView) convertView.findViewById(R.id.video_count)).setText(
                mContext.getString(R.string.video_count_label, topicTitleDetail.videoCount));

        KooCountUserInfo[] users = topicTitleDetail.kooRankUsers;
        if (users.length == 0) {
            convertView.findViewById(R.id.stars).setVisibility(View.GONE);
        }
        else {
            convertView.findViewById(R.id.stars).setOnClickListener(onStarsClickListener);

            ((TextView) convertView.findViewById(R.id.stars_rank_title)).setText(
                    mContext.getString(R.string.stars_rank, users.length));

            int[] avatarIds = new int[] {
                    R.id.first_koo,
                    R.id.second_koo,
                    R.id.third_koo,
                    R.id.forth_koo,
                    R.id.fifth_koo,
            };
            for (int i = 0; i < avatarIds.length && i < users.length; i++) {
                ImageLoader.getInstance().displayImage(users[i].getAvatar(),
                        (ImageView) convertView.findViewById(avatarIds[i]),
                        ImageLoaderHelper.avatarLoadOptions);
            }

            int avatarCrowns[] = new int[]{
                    R.id.first_crown,
                    R.id.second_crown,
                    R.id.third_crown,
            };
            for (int i = 0; i < avatarCrowns.length; i++) {
                int visibility = (topicTitleDetail.type == TYPE_WORLD && i < users.length)
                        ? View.VISIBLE
                        : View.INVISIBLE;
                convertView.findViewById(avatarCrowns[i]).setVisibility(visibility);
            }

            convertView.findViewById(R.id.topic_manager).setVisibility(
                    topicTitleDetail.type == TYPE_WORLD ? View.VISIBLE : View.INVISIBLE);

            View editTopicDesc = convertView.findViewById(R.id.edit_topic_desc);
            if (topicTitleDetail.type == TYPE_WORLD &&
                    users[0].getUid().equals(MyAccountInfo.getUid())) {
                editTopicDesc.setVisibility(View.VISIBLE);
                editTopicDesc.setOnClickListener(onEditTopicDescListener);
            }
            else {
                editTopicDesc.setVisibility(View.INVISIBLE);
            }
        }

        return convertView;
    }

    private View.OnClickListener onEditTopicDescListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, EditTopicDescActivity.class);
            intent.putExtra(EditTopicDescActivity.KEY_TOPIC_ID, mTopicId);
            mContext.startActivity(intent);
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
}
