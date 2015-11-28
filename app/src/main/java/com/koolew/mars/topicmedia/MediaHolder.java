package com.koolew.mars.topicmedia;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by jinchangzhu on 11/28/15.
 */
abstract class MediaHolder<I extends MediaItem> extends RecyclerView.ViewHolder {
    UniversalMediaAdapter mAdapter;
    Context mContext;
    I mItem;

    public MediaHolder(UniversalMediaAdapter adapter, View itemView) {
        super(itemView);

        mAdapter = adapter;
        mContext = mAdapter.mContext;
    }

    protected void bindItem(I item) {
        mItem = item;
        onBindItem();
    }

    protected abstract void onBindItem();
}
