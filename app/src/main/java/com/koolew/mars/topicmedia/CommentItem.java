package com.koolew.mars.topicmedia;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.FriendInfoActivity;
import com.koolew.mars.R;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseCommentInfo;
import com.koolew.mars.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

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

    public CommentItem(BaseCommentInfo commentInfo) {
        this.commentInfo = commentInfo;
    }

    @Override
    protected int getType() {
        return TYPE;
    }

    @Override
    protected long getUpdateTime() {
        return commentInfo.getCreateTime();
    }

    static class ItemViewHolder extends MediaHolder<CommentItem> implements View.OnClickListener {
        private ImageView avatar;
        private TextView nicknameSay;
        private TextView content;
        private TextView date;

        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);
            itemView.setOnClickListener(this);

            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            nicknameSay = (TextView) itemView.findViewById(R.id.nickname_say);
            content = (TextView) itemView.findViewById(R.id.comment);
            date = (TextView) itemView.findViewById(R.id.time);
        }

        @Override
        protected void onBindItem() {
            ImageLoader.getInstance().displayImage(mItem.commentInfo.getUserInfo().getAvatar(),
                    avatar, ImageLoaderHelper.avatarLoadOptions);
            date.setText(Utils.buildTimeSummary(mContext, mItem.commentInfo.getCreateTime() * 1000));

            ForegroundColorSpan nicknameSpan = new ForegroundColorSpan(0xFFDB5E5F);
            ForegroundColorSpan remainSpan = new ForegroundColorSpan(0xFF4E677A);

            SpannableStringBuilder ssBuilder = new SpannableStringBuilder();
            ssBuilder.append(mItem.commentInfo.getUserInfo().getNickname())
                    .append(mContext.getString(R.string.say))
                    .append(mContext.getString(R.string.colon));
            ssBuilder.setSpan(nicknameSpan, 0, mItem.commentInfo.getUserInfo().getNickname().length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssBuilder.setSpan(remainSpan, mItem.commentInfo.getUserInfo().getNickname().length(),
                    ssBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            nicknameSay.setText(ssBuilder);

            content.setText(mItem.commentInfo.getContent());
        }

        @Override
        public void onClick(View v) {
            FriendInfoActivity.startThisActivity(mContext, mItem.commentInfo.getUserInfo().getUid());
        }
    }
}
