package com.koolew.mars;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.view.LoadMoreFooter;

import org.json.JSONObject;


public abstract class BaseListFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, LoadMoreFooter.OnLoadListener {

    protected boolean isNeedLoadMore;

    protected SwipeRefreshLayout mRefreshLayout;
    protected ListView mListView;
    protected LoadMoreFooter mListFooter;

    protected JsonObjectRequest mRefreshRequest;
    protected JsonObjectRequest mLoadMoreRequest;

    protected BaseListFragment() {
        isNeedLoadMore = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_base_list, container, false);

        mRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeColors(getThemeColor());
        mListView = (ListView) root.findViewById(R.id.list_view);
        if (isNeedLoadMore) {
            mListFooter = (LoadMoreFooter) LayoutInflater.from(getActivity())
                    .inflate(R.layout.load_more_footer, null);
            mListFooter.setup(mListView);
            mListFooter.setOnLoadListener(this);
            mListView.addFooterView(mListFooter);
        }

        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });

        return root;
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

            handleRefresh(jsonObject);

            mListFooter.haveMore(true);
        }
    };

    protected Response.Listener<JSONObject> mLoadMoreListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            mLoadMoreRequest = null;
            mListFooter.loadComplete();

            mListFooter.haveMore(handleLoadMore(jsonObject));
        }
    };

    protected abstract void handleRefresh(JSONObject jsonObject);

    /**
     *
     * @param jsonObject
     * @return is something loaded
     */
    protected abstract boolean handleLoadMore(JSONObject jsonObject);

    protected abstract JsonObjectRequest doRefreshRequest();

    protected abstract JsonObjectRequest doLoadMoreRequest();
}
