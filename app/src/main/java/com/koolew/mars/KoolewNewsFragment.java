package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseFriendInfo;
import com.koolew.mars.view.LoadMoreFooter;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class KoolewNewsFragment extends Fragment implements AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, LoadMoreFooter.OnLoadListener {

    private static final String TAG = "koolew-KoolewNewsF";

    private SwipeRefreshLayout mRefreshLayout;
    private ListView mListView;
    private LoadMoreFooter mListFooter;
    private TopicAdapter mAdapter;

    private JsonObjectRequest mRefreshRequest;
    private JsonObjectRequest mLoadMoreRequest;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment KoolewNewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KoolewNewsFragment newInstance() {
        KoolewNewsFragment fragment = new KoolewNewsFragment();
        return fragment;
    }

    public KoolewNewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_koolew_news, container, false);
        mListView = (ListView) root.findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);

        mListFooter = (LoadMoreFooter) getActivity().getLayoutInflater()
                .inflate(R.layout.load_more_footer, null);
        mListView.addFooterView(mListFooter);
        mListFooter.setup(mListView);
        mListFooter.setOnLoadListener(this);

        mRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        mRefreshLayout.setColorSchemeResources(R.color.koolew_light_orange);
        mRefreshLayout.setOnRefreshListener(this);

        if (mAdapter == null) {
            mRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mRefreshLayout.setRefreshing(true);
                    doRefresh();
                }
            });
        }
        else {
            mListView.setAdapter(mAdapter);
        }

        return root;
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    private void doRefresh() {
        if (mLoadMoreRequest != null) {
            mLoadMoreRequest.cancel();
        }
        mRefreshRequest = ApiWorker.getInstance().requestFeedsTopic(mRefreshListener, null);
    }

    @Override
    public void onLoad() {
        if (mRefreshRequest != null) {
            mRefreshRequest.cancel();
        }
        mLoadMoreRequest = ApiWorker.getInstance().requestFeedsTopic(
                mAdapter.getOldestCardTime(), mLoadMoreListener, null);
    }

    class FeedsTopicAdapter extends TopicAdapter {
        FeedsTopicAdapter(Context context) {
            super(context);
        }

        @Override
        public TopicItem jsonObject2TopicItem(JSONObject jsonObject) {
            try {
                JSONObject topic = jsonObject.getJSONObject("topic");
                JSONArray parters = jsonObject.getJSONArray("parters");
                int parterCount = parters.length();
                BaseFriendInfo[] parterInfos = new BaseFriendInfo[parterCount];
                for (int i = 0; i < parterCount; i++) {
                    parterInfos[i] = new BaseFriendInfo(parters.getJSONObject(i));
                }

                return new TopicItem(
                        topic.getString("topic_id"),
                        topic.getString("content"),
                        topic.getString("thumb_url"),
                        topic.getInt("video_cnt"),
                        topic.getLong("update_time"),
                        parterInfos);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "JsonObject get field error!");
            }
            return null;
        }
    }

    private Response.Listener<JSONObject> mRefreshListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                mRefreshRequest = null;
                mAdapter = new FeedsTopicAdapter(getActivity());
                JSONArray cards = response.getJSONObject("result").getJSONArray("cards");
                mAdapter.setData(cards);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mListView.setAdapter(mAdapter);
            mRefreshLayout.setRefreshing(false);
            mListFooter.haveMore(true);
        }
    };

    private Response.Listener<JSONObject> mLoadMoreListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                mLoadMoreRequest = null;
                JSONArray cards = response.getJSONObject("result").getJSONArray("cards");
                int loadedCount = mAdapter.addData(cards);
                mAdapter.notifyDataSetChanged();

                mListFooter.loadComplete();
                if (loadedCount == 0) {
                    mListFooter.haveNoMore();
                }
            }
            catch (JSONException jse) {
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TopicAdapter.TopicItem item = (TopicAdapter.TopicItem) mAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), TopicActivity.class);
        intent.putExtra(TopicActivity.KEY_TOPIC_ID, item.topicId);
        intent.putExtra(TopicActivity.KEY_TOPIC_TITLE, item.title);
        startActivity(intent);
    }
}
