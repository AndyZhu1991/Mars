package com.koolew.mars;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.statistics.BaseV4Fragment;
import com.koolew.mars.view.LoadMoreFooter;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;


public class KoolewRelatedMeFragment extends BaseV4Fragment
        implements SwipeRefreshLayout.OnRefreshListener, LoadMoreFooter.OnLoadListener,
                   AbsListView.OnItemClickListener {

    private SwipeRefreshLayout mRefreshLayout;
    private ListView mListView;
    private LoadMoreFooter mListFooter;
    private RelatedMeAdapter mAdapter;
    private int mCurrentPage = 0;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment KoolewRelatedMeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KoolewRelatedMeFragment newInstance() {
        KoolewRelatedMeFragment fragment = new KoolewRelatedMeFragment();
        return fragment;
    }

    public KoolewRelatedMeFragment() {
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
        View root = inflater.inflate(R.layout.fragment_koolew_related_me, container, false);

        mListView = (ListView) root.findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);

        mListFooter = (LoadMoreFooter) getActivity().getLayoutInflater()
                .inflate(R.layout.load_more_footer, null);
        mListView.addFooterView(mListFooter, null, false);
        mListFooter.setup(mListView);
        mListFooter.setOnLoadListener(this);

        mRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        mRefreshLayout.setColorSchemeResources(R.color.koolew_deep_orange);
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
    public void onLoad() {
        mCurrentPage++;
        ApiWorker.getInstance().requestInvolve(mCurrentPage, mLoadMoreListener, null);
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    private void doRefresh() {
        mCurrentPage = 0;
        ApiWorker.getInstance().requestInvolve(mCurrentPage, mRefreshListener, null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RelatedMeItem item = (RelatedMeItem) mAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), IJoinedTopicActivity.class);
        intent.putExtra(IJoinedTopicActivity.KEY_TOPIC_ID, item.topicId);
        intent.putExtra(IJoinedTopicActivity.KEY_UID, MyAccountInfo.getUid());
        startActivity(intent);
    }

    private Response.Listener<JSONObject> mRefreshListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            try {
                if (jsonObject.getInt("code") != 0) {
                    return;
                }

                if (mAdapter == null) {
                    mAdapter = new RelatedMeAdapter();
                    mListView.setAdapter(mAdapter);
                }
                JSONArray cards = jsonObject.getJSONObject("result").getJSONArray("cards");
                mAdapter.setData(cards);
                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);
                mListFooter.haveMore(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.Listener<JSONObject> mLoadMoreListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            try {
                if (jsonObject.getInt("code") != 0) {
                    return;
                }

                JSONArray cards = jsonObject.getJSONObject("result").getJSONArray("cards");
                int loadedCount = mAdapter.addData(cards);
                mAdapter.notifyDataSetChanged();

                mListFooter.loadComplete();
                if (loadedCount == 0) {
                    mListFooter.haveNoMore();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    class RelatedMeItem {
        String topicId;
        String title;
        int videoCount;
        boolean isManager;

        RelatedMeItem(String topicId, String title, int videoCount, boolean isManager) {
            this.topicId = topicId;
            this.title = title;
            this.videoCount = videoCount;
            this.isManager = isManager;
        }
    }

    private static final int[] LEFT_LAYOUT_COLORS = {
            0xFFFF5656, 0xFFFC7B7B, 0xFFFFAE82, 0xFFFFC282,
            0xFFF4D288, 0xFFFFC282, 0xFFFFAE82, 0xFFFC7B7B,
    };

    class RelatedMeAdapter extends BaseAdapter {

        List<RelatedMeItem> mData;

        RelatedMeAdapter() {
            mData = new LinkedList<RelatedMeItem>();
        }

        public void setData(JSONArray cards) {
            mData.clear();
            try {
                int count = cards.length();
                for (int i = 0; i < count; i++) {
                    JSONObject topic = cards.getJSONObject(i).getJSONObject("topic");
                    RelatedMeItem item = new RelatedMeItem(topic.getString("topic_id"),
                            topic.getString("content"), topic.getInt("video_cnt"),
                            topic.getInt("is_manager") == 1);
                    mData.add(item);
                }
            }
            catch (JSONException jse) {
            }
        }

        public int addData(JSONArray cards) {
            int addedCount = 0;

            try {
                int count = cards.length();
                for (int i = 0; i < count; i++) {
                    JSONObject topic = cards.getJSONObject(i).getJSONObject("topic");
                    String topicId = topic.getString("topic_id");
                    if (hasTopic(topicId)) {
                        continue;
                    }

                    RelatedMeItem item = new RelatedMeItem(topicId, topic.getString("content"),
                            topic.getInt("video_cnt"), topic.getInt("is_manager") == 1);
                    mData.add(item);
                    addedCount++;
                }
            }
            catch (JSONException jse) {
            }

            return addedCount;
        }

        private boolean hasTopic(String topicId) {
            for (RelatedMeItem item: mData) {
                if (item.topicId.equals(topicId)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.koolew_relative_me_item, null);
                ViewHolder holder = new ViewHolder();
                holder.leftLayout = (LinearLayout) convertView.findViewById(R.id.left_layout);
                holder.videoCount = (TextView) convertView.findViewById(R.id.video_count);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.manager = (TextView) convertView.findViewById(R.id.is_manager);
                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            ((GradientDrawable) holder.leftLayout.getBackground())
                    .setColor(LEFT_LAYOUT_COLORS[position % 8]);
            holder.videoCount.setText("" + mData.get(position).videoCount);
            holder.title.setText(mData.get(position).title);
            holder.manager.setVisibility(mData.get(position).isManager ? View.VISIBLE : View.GONE);

            return convertView;
        }
    }

    class ViewHolder {
        LinearLayout leftLayout;
        TextView videoCount;
        TextView title;
        TextView manager;
    }
}
