package com.koolew.mars.mould;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.R;
import com.koolew.mars.statistics.BaseV4Fragment;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 9/2/15.
 */
public abstract class RecyclerListFragmentMould<A extends LoadMoreAdapter> extends BaseV4Fragment
        implements SwipeRefreshLayout.OnRefreshListener, LoadMoreAdapter.LoadMoreListener {

    protected static final int DEFAULT_LAYOUT = R.layout.general_refresh_recycler_layout;

    protected int mLayoutResId;

    protected A mAdapter;

    protected boolean isNeedLoadMore;

    protected SwipeRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;

    protected JsonObjectRequest mRefreshRequest;
    protected JsonObjectRequest mLoadMoreRequest;


    public RecyclerListFragmentMould() {
        mLayoutResId = DEFAULT_LAYOUT;
        isNeedLoadMore = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected abstract A useThisAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(mLayoutResId, container, false);

        mRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeColors(getThemeColor());
        mRecyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();

        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });

        return root;
    }

    protected void setupAdapter() {
        mAdapter = useThisAdapter();
        mAdapter.setNeedLoadMore(isNeedLoadMore);
        mAdapter.setLoadMoreListener(this);
        mAdapter.setupScrollListener(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRefreshRequest != null) {
            mRefreshRequest.cancel();
            mRefreshRequest = null;
        }
        if (mLoadMoreRequest != null) {
            mLoadMoreRequest.cancel();
            mLoadMoreRequest = null;
        }
    }

    @Override
    public void onRefresh() {
        if (mLoadMoreRequest != null) {
            mLoadMoreRequest.cancel();
            mLoadMoreRequest = null;
        }
        mRefreshRequest = doRefreshRequest();
    }

    @Override
    public void onLoadMore() {
        if (mRefreshRequest != null) {
            mRefreshRequest.cancel();
            mRefreshRequest = null;
        }
        mLoadMoreRequest = doLoadMoreRequest();
    }

    protected Response.Listener<JSONObject> mRefreshListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            mRefreshLayout.setRefreshing(false);
            mRefreshRequest = null;

            mAdapter.afterRefresh(handleRefresh(jsonObject));
            mAdapter.notifyRecyclerScrolled(mRecyclerView);
        }
    };

    protected Response.Listener<JSONObject> mLoadMoreListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            mLoadMoreRequest = null;

            mAdapter.afterLoad(handleLoadMore(jsonObject));
        }
    };


    protected abstract int getThemeColor();

    protected abstract JsonObjectRequest doRefreshRequest();

    protected abstract JsonObjectRequest doLoadMoreRequest();

    /**
     *
     * @param response
     * @return is something loaded
     */
    protected abstract boolean handleRefresh(JSONObject response);

    /**
     *
     * @param response
     * @return is something loaded
     */
    protected abstract boolean handleLoadMore(JSONObject response);
}
