package com.koolew.mars;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.statistics.BaseLazyV4Fragment;
import com.koolew.mars.view.LoadMoreFooter;

import org.json.JSONObject;

/**
 * Created by jinchangzhu on 10/9/15.
 */
public abstract class BaseLazyListFragment extends BaseLazyV4Fragment
        implements SwipeRefreshLayout.OnRefreshListener, LoadMoreFooter.OnLoadListener,
        AbsListView.OnItemClickListener {

    protected static final int DEFAULT_LAYOUT = R.layout.general_refresh_list_layout;


    protected int mLayoutResId;

    protected boolean isNeedLoadMore;

    protected SwipeRefreshLayout mRefreshLayout;
    protected ListView mListView;
    protected LoadMoreFooter mListFooter;

    protected JsonObjectRequest mRefreshRequest;
    protected JsonObjectRequest mLoadMoreRequest;

    protected BaseLazyListFragment() {
        mLayoutResId = DEFAULT_LAYOUT;
        isNeedLoadMore = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateViewLazy(Bundle savedInstanceState) {
        setContentView(mLayoutResId);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeColors(getThemeColor());
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        if (isNeedLoadMore) {
            mListFooter = (LoadMoreFooter) LayoutInflater.from(getActivity())
                    .inflate(R.layout.load_more_footer, null);
            mListFooter.haveNoMore();
            mListFooter.setup(mListView);
            mListFooter.setOnLoadListener(this);
            mListView.addFooterView(mListFooter, null, false);
            mListFooter.setVisibility(View.INVISIBLE);
        }

        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });
    }

    @Override
    protected View createDefaultView() {
        return inflater.inflate(R.layout.shadow, null);
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
    public void onLoad() {
        if (mRefreshRequest != null) {
            mRefreshRequest.cancel();
            mRefreshRequest = null;
        }
        mLoadMoreRequest = doLoadMoreRequest();
    }

    public abstract String getTitle();

    public abstract int getThemeColor();

    protected Response.Listener<JSONObject> mRefreshListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            mRefreshLayout.setRefreshing(false);
            mRefreshRequest = null;

            if (handleRefresh(jsonObject)) {
                if (isNeedLoadMore) {
                    mListFooter.haveMore(true);
                    mListFooter.setVisibility(View.VISIBLE);
                }
            }
            else {
                if (isNeedLoadMore) {
                    mListFooter.haveMore(false);
                    mListFooter.setVisibility(View.INVISIBLE);
                }
            }
        }
    };

    protected Response.Listener<JSONObject> mLoadMoreListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            mLoadMoreRequest = null;

            if (isNeedLoadMore) {
                mListFooter.loadComplete();
                mListFooter.haveMore(handleLoadMore(jsonObject));
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

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

    protected abstract JsonObjectRequest doRefreshRequest();

    protected abstract JsonObjectRequest doLoadMoreRequest();
}
