package com.koolew.mars.topicmedia;

import android.view.View;
import android.widget.TextView;

import com.koolew.mars.R;
import com.koolew.mars.infos.BaseTopicInfo;

/**
 * Created by jinchangzhu on 11/26/15.
 */
public class BasicTitleItem extends MediaItem {

    private static final int TYPE = UniversalMediaAdapter.registerGenerator(
            new UniversalMediaAdapter.ItemViewHolderGenerator() {
                @Override
                protected int layoutResId() {
                    return R.layout.topic_title_layout;
                }

                @Override
                protected Class<?> holderClass() {
                    return ItemViewHolder.class;
                }
            }
    );

    private String mTitle;

    public BasicTitleItem(BaseTopicInfo topicInfo) {
        mTitle = topicInfo.getTitle();
    }

    @Override
    protected int getType() {
        return TYPE;
    }


    static class ItemViewHolder extends MediaHolder<BasicTitleItem> {
        private TextView title;

        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);

            title = (TextView) itemView.findViewById(R.id.title);
        }

        @Override
        protected void onBindItem() {
            title.setText(mItem.mTitle);
        }
    }
}
