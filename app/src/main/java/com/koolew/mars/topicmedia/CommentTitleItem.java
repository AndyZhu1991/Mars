package com.koolew.mars.topicmedia;

import android.view.View;
import android.widget.TextView;

import com.koolew.mars.R;

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

    private int commentCount;

    public CommentTitleItem(int commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    protected int getType() {
        return TYPE;
    }


    static class ItemViewHolder extends MediaHolder<CommentTitleItem> {
        private TextView commentCount;

        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);

            commentCount = (TextView) itemView.findViewById(R.id.comment_count);
        }

        @Override
        protected void onBindItem() {
            commentCount.setText(mContext.getString(R.string.item_count, mItem.commentCount));
        }
    }
}
