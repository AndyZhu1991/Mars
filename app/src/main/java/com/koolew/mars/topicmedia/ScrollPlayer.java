package com.koolew.mars.topicmedia;

import android.support.v7.widget.RecyclerView;

import com.koolew.mars.view.KoolewVideoView;

/**
 * Created by jinchangzhu on 12/5/15.
 */
public class ScrollPlayer extends KoolewVideoView.ScrollPlayer {

    public ScrollPlayer(RecyclerView recyclerView) {
        super(recyclerView);
    }

    @Override
    protected KoolewVideoView getVideoView(RecyclerView.ViewHolder holder) {
        if (holder instanceof VideoItem.ItemViewHolder) {
            return ((VideoItem.ItemViewHolder) holder).videoView;
        }

        return null;
    }
}
