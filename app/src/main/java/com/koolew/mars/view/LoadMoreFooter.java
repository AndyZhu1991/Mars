package com.koolew.mars.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 6/26/15.
 */
public class LoadMoreFooter extends FrameLayout implements AbsListView.OnScrollListener {

    private AbsListView.OnScrollListener mOriginScrollListener;
    private OnLoadListener mLoadListener;

    private boolean mIsLoading = false;
    private boolean mHaveMore = true;

    private View mProgressFrame;
    private View mNoMoreHintFrame;

    public LoadMoreFooter(Context context) {
        this(context, null);
    }

    public LoadMoreFooter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadMoreFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProgressFrame = findViewById(R.id.progress);
        mNoMoreHintFrame = findViewById(R.id.no_more_hint_label);
    }


    public void setup(ListView listView) {
        setup(listView, null);
    }

    public void setup(ListView listView, AbsListView.OnScrollListener originScrollListener) {
        listView.setOnScrollListener(this);
        mOriginScrollListener = originScrollListener;
    }

    public void setOnLoadListener(OnLoadListener listener) {
        mLoadListener = listener;
    }

    public void loadComplete() {
        mIsLoading = false;
    }

    public void haveNoMore() {
        haveMore(false);
    }

    public void haveMore(boolean haveMore) {
        if (mHaveMore != haveMore) {
            mHaveMore = haveMore;

            if (mHaveMore) {
                mProgressFrame.setVisibility(VISIBLE);
                mNoMoreHintFrame.setVisibility(INVISIBLE);
            }
            else {
                mProgressFrame.setVisibility(INVISIBLE);
                mNoMoreHintFrame.setVisibility(VISIBLE);
            }
        }
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mOriginScrollListener != null) {
            mOriginScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int count = listView.getChildCount();
        for (int i = 0; i < count; i++) {
            if (listView.getChildAt(i) == this && listView.getAdapter().getCount() > 1) {
                if (!mIsLoading && mHaveMore && getTop() < listView.getHeight()) {
                    mIsLoading = true;
                    if (mLoadListener != null) {
                        mLoadListener.onLoad();
                    }
                }
            }
        }

        if (mOriginScrollListener != null) {
            mOriginScrollListener.onScroll(listView, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public interface OnLoadListener {
        void onLoad();
    }
}
