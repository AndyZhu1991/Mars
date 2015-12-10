package com.koolew.mars.topicmedia;

import android.view.View;
import android.widget.TextView;

import com.koolew.mars.MovieStudioActivity;
import com.koolew.mars.R;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MovieTopicInfo;

/**
 * Created by jinchangzhu on 11/27/15.
 */
public class MovieItem extends VideoItem {

    private static final int TYPE = UniversalMediaAdapter.registerGenerator(
            new UniversalMediaAdapter.ItemViewHolderGenerator() {
                @Override
                protected int layoutResId() {
                    return R.layout.video_item;
                }

                @Override
                protected Class<?> holderClass() {
                    return ItemViewHolder.class;
                }
            }
    );

    public MovieItem(BaseVideoInfo videoInfo) {
        super(videoInfo);
    }

    @Override
    protected int getType() {
        return TYPE;
    }


    static class ItemViewHolder extends VideoItem.ItemViewHolder {
        protected View actButton;
        protected MovieTopicInfo movieInfo;

        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);
            
            videoDate.setVisibility(View.INVISIBLE);
            videoDate = (TextView) itemView.findViewById(R.id.video_date2);
            actButton = itemView.findViewById(R.id.btn_act);
            actButton.setVisibility(View.VISIBLE);
            actButton.setOnClickListener(this);
        }

        @Override
        protected void onBindItem() {
            if (!(mItem.videoInfo.getTopicInfo() instanceof MovieTopicInfo)) {
                throw new RuntimeException("There is no movie info in a movie item");
            }
            movieInfo = (MovieTopicInfo) mItem.videoInfo.getTopicInfo();
            super.onBindItem();
        }

        @Override
        public void onClick(View v) {
            if (v == actButton) {
                onAct();
            }
            else {
                super.onClick(v);
            }
        }

        protected void onAct() {
            BaseVideoInfo videoInfo = mItem.videoInfo;
            MovieStudioActivity.startThisActivity(mContext, movieInfo, videoInfo.getVideoUrl(),
                    videoInfo.getUserInfo().getUid());
        }
    }
}
