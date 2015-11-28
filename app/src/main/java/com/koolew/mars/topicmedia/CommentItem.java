package com.koolew.mars.topicmedia;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.R;
import com.koolew.mars.infos.BaseCommentInfo;

/**
 * Created by jinchangzhu on 11/27/15.
 */
public class CommentItem extends MediaItem {

    private static final int TYPE = UniversalMediaAdapter.registerGenerator(
            new UniversalMediaAdapter.ItemViewHolderGenerator() {
                @Override
                protected int layoutResId() {
                    return R.layout.check_danmaku_comment_item;
                }

                @Override
                protected Class<?> holderClass() {
                    return ItemViewHolder.class;
                }
            }
    );


    private BaseCommentInfo commentInfo;

    CommentItem(BaseCommentInfo commentInfo) {
        this.commentInfo = commentInfo;
    }

    @Override
    protected int getType() {
        return TYPE;
    }


    static class ItemViewHolder extends MediaHolder<CommentItem> {
        private ImageView avatar;
        private TextView nicknameSay;
        private TextView content;
        private TextView date;

        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);

            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            nicknameSay = (TextView) itemView.findViewById(R.id.nickname_say);
            content = (TextView) itemView.findViewById(R.id.comment);
            date = (TextView) itemView.findViewById(R.id.time);
        }

        @Override
        protected void onBindItem() {

        }
    }
}
