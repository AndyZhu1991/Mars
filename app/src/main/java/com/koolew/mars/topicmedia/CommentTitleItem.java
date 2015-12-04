package com.koolew.mars.topicmedia;

import android.view.View;
import android.widget.TextView;

import com.koolew.mars.R;
import com.koolew.mars.infos.BaseVideoInfo;

/**
 * Created by jinchangzhu on 11/27/15.
 */
public class CommentTitleItem extends MediaItem {

    private static final int TYPE = UniversalMediaAdapter.registerGenerator(
            new UniversalMediaAdapter.ItemViewHolderGenerator() {
                @Override
                protected int layoutResId() {
                    return R.layout.check_danmaku_comment_title;
                }

                @Override
                protected Class<?> holderClass() {
                    return ItemViewHolder.class;
                }
            }
    );

    private BaseVideoInfo videoInfo;

    public CommentTitleItem(BaseVideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    @Override
    protected int getType() {
        return TYPE;
    }


    static class ItemViewHolder extends MediaHolder<CommentTitleItem> {
        private TextView kooCountView;
        private BaseVideoInfo videoInfo;

        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);

            kooCountView = (TextView) itemView.findViewById(R.id.title);
        }

        @Override
        protected void onBindItem() {
            setVideoInfo(mItem.videoInfo);
        }

        public void setVideoInfo(BaseVideoInfo videoInfo) {
            this.videoInfo = videoInfo;
            updateKooCountView();
        }

        public void updateKooCountView() {
            kooCountView.setText(kooCountView.getContext()
                    .getString(R.string.kooed_count, videoInfo.getKooTotal()));
        }
    }
}
