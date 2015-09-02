package com.koolew.mars.mould;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 9/2/15.
 */

public abstract class LoadMoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private boolean isNeedLoadMore;
    private boolean isLoading;
    private LoadMoreViewHolder mLoadMoreViewHolder;
    private LoadMoreListener mLoadMoreListener;

    public LoadMoreAdapter() {
        isNeedLoadMore = false;
        isLoading = false;
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (isCustomViewType(viewType)) {
            return onCreateCustomViewHolder(parent, viewType);
        }
        else {
            return createLoadMoreHolder(parent, viewType);
        }
    }

    private LoadMoreViewHolder createLoadMoreHolder(ViewGroup parent, int viewType) {
        View loadMoreView =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.load_more_footer, parent, false);
        mLoadMoreViewHolder = new LoadMoreViewHolder(loadMoreView);
        return mLoadMoreViewHolder;
    }

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isCustomViewPosition(position)) {
            onBindCustomViewHolder(holder, position);
        }
        else {
            bindLoadMoreViewHolder((LoadMoreViewHolder) holder, position);
        }
    }

    private void bindLoadMoreViewHolder(LoadMoreViewHolder holder, int position) {
    }

    @Override
    public final int getItemCount() {
        if (getCustomItemCount() == 0) {
            return 0;
        }
        else {
            return getCustomItemCount() + getLoadMoreViewCount();
        }
    }

    @Override
    public final int getItemViewType(int position) {
        if (isCustomViewPosition(position)) {
            return getCustomItemViewType(position);
        }
        else {
            return getLoadMoreViewType();
        }
    }

    private int getLoadMoreViewType() {
        return Integer.MAX_VALUE;
    }

    private int getLoadMoreViewCount() {
        return isNeedLoadMore ? 1 : 0;
    }

    private boolean isCustomViewPosition(int position) {
        return position < getCustomItemCount();
    }

    private boolean isCustomViewType(int type) {
        return type != getLoadMoreViewType();
    }

    public void setNeedLoadMore(boolean isNeedLoadMore) {
        this.isNeedLoadMore = isNeedLoadMore;
    }

    public void setLoadMoreListener(LoadMoreListener listener) {
        this.mLoadMoreListener = listener;
    }

    public void setupScrollListener(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(mScrollListener);
    }

    public void afterRefresh(boolean hasSomething) {
        if (isNeedLoadMore) {
            if (mLoadMoreViewHolder != null) {
                mLoadMoreViewHolder.setLoading();
            }
            isLoading = false;
        }
    }

    public void afterLoad(boolean loadedSomething) {
        if (isNeedLoadMore) {
            isLoading = false;
            if (!loadedSomething) {
                mLoadMoreViewHolder.setNoMore();
            }
        }
    }

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (!isLoading && mLoadMoreViewHolder != null && mLoadMoreViewHolder.hasMore &&
                    getItemViewType(manager.findLastVisibleItemPosition()) == getLoadMoreViewType()) {
                if (mLoadMoreListener != null) {
                    isLoading = true;
                    mLoadMoreListener.onLoadMore();
                }
            }
        }
    };


    public int getCustomItemViewType(int position) {
        return 0;
    }

    public abstract RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position);

    public abstract int getCustomItemCount();

    interface LoadMoreListener {
        void onLoadMore();
    }

    class LoadMoreViewHolder extends RecyclerView.ViewHolder {

        private View loadingFrame;
        private View noMoreFrame;

        private boolean hasMore;

        public LoadMoreViewHolder(View itemView) {
            super(itemView);

            loadingFrame = itemView.findViewById(R.id.loading_frame);
            noMoreFrame = itemView.findViewById(R.id.no_more_frame);

            hasMore = true;
        }

        public void setLoading() {
            loadingFrame.setVisibility(View.VISIBLE);
            noMoreFrame.setVisibility(View.INVISIBLE);
            hasMore = true;
        }

        public void setNoMore() {
            loadingFrame.setVisibility(View.INVISIBLE);
            noMoreFrame.setVisibility(View.VISIBLE);
            hasMore = false;
        }
    }
}
