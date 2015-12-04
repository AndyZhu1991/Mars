package com.koolew.mars.topicmedia;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.koolew.mars.EditTopicDescActivity;
import com.koolew.mars.R;
import com.koolew.mars.infos.BaseTopicInfo;

/**
 * Created by jinchangzhu on 11/27/15.
 */
public class VideoDetailTitleItem extends MediaItem {

    private static final int TYPE = UniversalMediaAdapter.registerGenerator(
            new UniversalMediaAdapter.ItemViewHolderGenerator() {
                @Override
                protected int layoutResId() {
                    return R.layout.video_topic_title;
                }

                @Override
                protected Class<?> holderClass() {
                    return ItemViewHolder.class;
                }
            }
    );

    public enum TitleType {
        FEEDS, WORLD, TASK
    }

    private BaseTopicInfo mTopicInfo;
    private TitleType titleType = TitleType.FEEDS;
    private String inviter; // Only for TitleType.TASK
    private boolean isManager = false;

    public VideoDetailTitleItem(BaseTopicInfo topicInfo) {
        mTopicInfo = topicInfo;
    }

    @Override
    protected int getType() {
        return TYPE;
    }

    public void setInviter(String inviter) {
        this.inviter = inviter;
    }

    public void setTitleType(TitleType titleType) {
        this.titleType = titleType;
    }

    public void setIsManager(boolean isManager) {
        this.isManager = isManager;
    }

    static class ItemViewHolder extends MediaHolder<VideoDetailTitleItem>
            implements View.OnClickListener {

        private View editTopicDesc;
        private TextView summary;
        private TextView title;
        private TextView description;
        private TextView videoCount;

        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);

            editTopicDesc = itemView.findViewById(R.id.edit_topic_desc);
            summary = (TextView) itemView.findViewById(R.id.summary);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            videoCount = (TextView) itemView.findViewById(R.id.video_count);
        }

        @Override
        protected void onBindItem() {
            if (mItem.titleType == TitleType.TASK) {
                summary.setTextColor(mContext.getResources().getColor(R.color.koolew_light_green));
                summary.setText(mContext.getString(R.string.invited_label, mItem.inviter));
            }
            else {
                summary.setVisibility(View.GONE);
            }

            title.setText(mItem.mTopicInfo.getTitle());

            if (TextUtils.isEmpty(mItem.mTopicInfo.getDesc())) {
                description.setVisibility(View.GONE);
            }
            else {
                description.setText(mItem.mTopicInfo.getDesc());
                description.setVisibility(View.VISIBLE);
            }

            videoCount.setText(
                    mContext.getString(R.string.video_count_label, mItem.mTopicInfo.getVideoCount()));

            if (mItem.titleType == TitleType.WORLD && mItem.isManager) {
                editTopicDesc.setVisibility(View.VISIBLE);
                editTopicDesc.setOnClickListener(this);
            }
            else {
                editTopicDesc.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, EditTopicDescActivity.class);
            intent.putExtra(EditTopicDescActivity.KEY_TOPIC_ID, mItem.mTopicInfo.getTopicId());
            mContext.startActivity(intent);
        }
    }
}
