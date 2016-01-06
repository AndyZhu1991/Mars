package com.koolew.mars.mould;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.MarsApplication;
import com.koolew.mars.R;
import com.koolew.mars.statistics.BaseLazyV4Fragment;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 9/2/15.
 */
public abstract class RecyclerListFragmentMould<A extends LoadMoreAdapter> extends BaseLazyV4Fragment
        implements SwipeRefreshLayout.OnRefreshListener, LoadMoreAdapter.LoadMoreListener {

    //                                                         millis   second   minute
    protected static final long MAX_API_CACHE_AVALIABLE_TIME =  1000  *   60   *   3   ; // 1 minute

    protected static final int DEFAULT_LAYOUT = R.layout.general_refresh_recycler_layout;

    protected int mLayoutResId;

    protected A mAdapter;

    protected boolean isNeedApiCache;
    protected boolean isNeedLoadMore;

    protected SwipeRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected FrameLayout mSpecialContainer;
    protected View mNoDataView;

    protected JsonObjectRequest mRefreshRequest;
    protected JsonObjectRequest mLoadMoreRequest;


    public RecyclerListFragmentMould() {
        mLayoutResId = DEFAULT_LAYOUT;
        isNeedApiCache = false;
        isNeedLoadMore = false;
        isLazyLoad = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected abstract A useThisAdapter();

    @Override
    public void onCreateViewLazy(Bundle savedInstanceState) {
        setContentView(mLayoutResId);

        initViews();

        setupAdapter();

        initApiData();
    }

    protected void initViews() {
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeColors(getThemeColor());
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSpecialContainer = (FrameLayout) findViewById(R.id.special_container);
    }

    protected void initApiData() {
        Cache.Entry apiCache = null;
        if (isNeedApiCache) {
            apiCache = ApiWorker.getInstance().getApiCache(getRefreshRequestUrl());
            if (apiCache != null) {
                try {
                    mRefreshListener.onResponse(new JSONObject(new String(apiCache.data)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (apiCache == null ||
                System.currentTimeMillis() - apiCache.softTtl > MAX_API_CACHE_AVALIABLE_TIME) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mRefreshLayout.setRefreshing(true);
                    onRefresh();
                }
            });
        }
    }

    @Override
    protected View createDefaultView() {
        return inflater.inflate(R.layout.shadow, null);
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

            boolean hasData = handleRefresh(jsonObject);
            if (mSpecialContainer != null) {
                mSpecialContainer.removeAllViews();
                mNoDataView = null;
                if (!hasData) {
                    mNoDataView = createNoDataView();
                    if (mNoDataView != null) {
                        mSpecialContainer.addView(mNoDataView);
                    }
                }
            }
            mAdapter.afterRefresh(hasData);
            mAdapter.notifyRecyclerScrolled(mRecyclerView);
        }
    };

    protected Response.ErrorListener mRefreshErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mRefreshLayout.setRefreshing(false);
            mRefreshRequest = null;

            handleVolleyError(error);
        }
    };

    protected Response.Listener<JSONObject> mLoadMoreListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            mLoadMoreRequest = null;

            mAdapter.afterLoad(handleLoadMore(jsonObject));
        }
    };

    protected Response.ErrorListener mLoadMoreErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mLoadMoreRequest = null;

            handleVolleyError(error);
        }
    };

    protected void handleVolleyError(VolleyError error) {
        Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
    }

    protected View createNoDataView() {
        int noDataViewResId = getNoDataViewResId();
        if (noDataViewResId != 0) {
            return inflater.inflate(noDataViewResId, null);
        }
        else {
            return null;
        }
    }

    protected int getNoDataViewResId() {
        return 0;
    }


    protected abstract int getThemeColor();

    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().queueGetRequest(
                getRefreshRequestUrl(), mRefreshListener, mRefreshErrorListener);
    }

    protected abstract String getRefreshRequestUrl();

    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().queueGetRequest(
                getLoadMoreRequestUrl(), mLoadMoreListener, mLoadMoreErrorListener);
    }

    protected abstract String getLoadMoreRequestUrl();


    /**
     *
     * @param response
     * @return is something loaded
     */
    protected final boolean handleRefresh(JSONObject response) {
        try {
            int code = response.getInt("code");
            if (code == 0) {
                JSONObject result = response.getJSONObject("result");
                return handleRefreshResult(result);
            }
            else {
                String msg = response.getString("msg");
                handleRefreshFailed(code, msg);
            }
        } catch (JSONException e) {
            handleJsonException(response, e);
        }
        return false;
    }

    protected abstract boolean handleRefreshResult(JSONObject result);

    protected void handleRefreshFailed(int code, String msg) {
        defaultHandleException("Api: " + getRefreshRequestUrl() + "\ncode: " + code + "\n msg: " + msg);
    }

    /**
     *
     * @param response
     * @return is something loaded
     */
    protected final boolean handleLoadMore(JSONObject response) {
        try {
            int code = response.getInt("code");
            if (code == 0) {
                JSONObject result = response.getJSONObject("result");
                return handleLoadMoreResult(result);
            }
            else {
                String msg = response.getString("msg");
                handleLoadMoreFailed(code, msg);
            }
        } catch (JSONException e) {
            handleJsonException(response, e);
        }
        return false;
    }

    protected abstract boolean handleLoadMoreResult(JSONObject result);

    protected void handleLoadMoreFailed(int code, String msg) {
        defaultHandleException("Api: " + getLoadMoreRequestUrl() + "\ncode: " + code + "\n msg: " + msg);
    }


    protected void handleJsonException(JSONObject jsonObject, JSONException exception) {
        defaultHandleException("Exception: " + exception + "\njsonObject: " + jsonObject);
    }

    protected void defaultHandleException(String message) {
        if (MarsApplication.DEBUG) {
            throw new RuntimeException("Should process this exception: " + message);
        }
    }
}
