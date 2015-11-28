package com.koolew.mars.topicmedia;

import android.view.View;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 11/27/15.
 */
public class UnknownCategoryItem extends MediaItem {

    private static final int TYPE = UniversalMediaAdapter.registerGenerator(
            new UniversalMediaAdapter.ItemViewHolderGenerator() {
                @Override
                protected int layoutResId() {
                    return R.layout.unknown_category_hint;
                }

                @Override
                protected Class<?> holderClass() {
                    return ItemViewHolder.class;
                }
            }
    );

    @Override
    protected int getType() {
        return TYPE;
    }


    static class ItemViewHolder extends MediaHolder<UnknownCategoryItem> {

        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        protected void onBindItem() {
        }
    }
}
