package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.player.ScrollPlayer;
import com.koolew.mars.view.LoadMoreFooter;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class KoolewRecommendFragment extends BaseListFragment implements AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, LoadMoreFooter.OnLoadListener {

    private static final String TAG = "koolew-KoolewNewsF";

    private TopicAdapter mAdapter;
    private int page;
    private ScrollPlayer mScrollPlayer;

    public KoolewRecommendFragment() {
        // Required empty public constructor
        isNeedLoadMore = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  super.onCreateView(inflater, container, savedInstanceState);

        mAdapter = new FeedsTopicAdapter(getActivity());
        mScrollPlayer = mAdapter.new TopicScrollPlayer(mListView);
        mScrollPlayer.setNeedDanmaku(false);
        mScrollPlayer.setNeedSound(false);
        if (isNeedLoadMore) {
            mListFooter.setup(mListView, mScrollPlayer);
        }

        return root;
    }

    @Override
    protected void onPageEnd() {
        super.onPageEnd();
        mScrollPlayer.onActivityPause();
    }

    @Override
    protected void onPageStart() {
        super.onPageStart();
        mScrollPlayer.onActivityResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScrollPlayer.onActivityDestroy();
    }

    @Override
    public void onLoad() {
        if (mRefreshRequest != null) {
            mRefreshRequest.cancel();
        }
        mLoadMoreRequest = ApiWorker.getInstance().requestFeedsTopic(
                mAdapter.getOldestCardTime(), mLoadMoreListener, null);
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public int getThemeColor() {
        return getActivity().getResources().getColor(R.color.koolew_light_orange);
    }

    private void setupAdapter() {
        if (mListView.getAdapter() == null) {
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        try {
            setupAdapter();
            mRefreshRequest = null;
            JSONObject result = response.getJSONObject("result");
            JSONArray cards = null;
            if (result.has("hot_cards")) {
                cards = response.getJSONObject("result").getJSONArray("hot_cards");
            }
            mAdapter.setCards(cards);
            mAdapter.notifyDataSetChanged();
            mScrollPlayer.onListRefresh();
            return cards.length() > 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        try {
            mLoadMoreRequest = null;
            JSONArray cards = response.getJSONObject("result").getJSONArray("cards");
            int loadedCount = mAdapter.addCards(cards);
            mAdapter.notifyDataSetChanged();

            return loadedCount > 0;
        }
        catch (JSONException jse) {
        }

        return false;
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        page = 0;
        return ApiWorker.getInstance().requestFeedsHot(page, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        page++;
        return ApiWorker.getInstance().requestFeedsHot(page, mLoadMoreListener, null);
    }

    class FeedsTopicAdapter extends TopicAdapter {
        FeedsTopicAdapter(Context context) {
            super(context);
        }

        @Override
        public TopicItem jsonObject2TopicItem(JSONObject jsonObject) {
            JSONObject topic = null;
            try {
                topic = jsonObject.getJSONObject("topic");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray parters = null;
            try {
                parters = jsonObject.getJSONArray("parters");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new TopicItem(topic, parters);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TopicAdapter.TopicItem item = (TopicAdapter.TopicItem) mAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), FeedsTopicActivity.class);
        intent.putExtra(TopicVideoActivity.KEY_TOPIC_ID, item.getTopicId());
        intent.putExtra(TopicVideoActivity.KEY_TOPIC_TITLE, item.getTitle());
        if (item.isRecommend) {
            intent.putExtra(FeedsTopicActivity.KEY_DEFAULT_SHOW_POSITION,
                    FeedsTopicActivity.POSITION_WORLD);
        }
        startActivity(intent);
    }
}
