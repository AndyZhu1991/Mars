package com.koolew.mars.topicmedia;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.R;
import com.koolew.mars.infos.MovieTopicInfo;
import com.koolew.mars.view.KoolewVideoView;

/**
 * Created by jinchangzhu on 11/27/15.
 */
public class MovieDetailTitleItem extends MediaItem {

    private static final int TYPE = UniversalMediaAdapter.registerGenerator(
            new UniversalMediaAdapter.ItemViewHolderGenerator() {
                @Override
                protected int layoutResId() {
                    return R.layout.movie_topic_title;
                }

                @Override
                protected Class<?> holderClass() {
                    return ItemViewHolder.class;
                }
            }
    );

    private MovieTopicInfo mTopicInfo;

    public MovieDetailTitleItem(MovieTopicInfo topicInfo) {
        mTopicInfo = topicInfo;
    }

    @Override
    protected int getType() {
        return TYPE;
    }


    static class ItemViewHolder extends MediaHolder<MovieDetailTitleItem>
            implements View.OnClickListener {
        private TextView title;
        private KoolewVideoView videoView;
        private ImageView playImage;
        private TextView videoCount;

        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);

            title = (TextView) itemView.findViewById(R.id.title);
            videoView = (KoolewVideoView) itemView.findViewById(R.id.video_view);
            videoView.setOnClickListener(this);
            playImage = (ImageView) itemView.findViewById(R.id.play_image);
            videoCount = (TextView) itemView.findViewById(R.id.video_count);
        }

        @Override
        protected void onBindItem() {
            videoView.setVideoInfo(mItem.mTopicInfo.getVideoUrl(), mItem.mTopicInfo.getThumbnail());

            title.setText(mItem.mTopicInfo.getTitle());
            videoCount.setText(mAdapter.mContext.
                    getString(R.string.video_count_label, mItem.mTopicInfo.getVideoCount()));
        }

        @Override
        public void onClick(View v) {
            if (playImage.getVisibility() == View.VISIBLE) {
                videoView.startPlay();
                playImage.setVisibility(View.INVISIBLE);
            }
            else {
                videoView.stop();
                playImage.setVisibility(View.VISIBLE);
            }
        }
    }
}
