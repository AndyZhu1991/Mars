package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class KooRankActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        Response.Listener<JSONObject>, AdapterView.OnItemClickListener, Response.ErrorListener {

    public static final String KEY_UID = "uid";
    public static final String KEY_KOO_COUNT = "koo count";

    private String mUid;
    private int mKooCount;

    private SwipeRefreshLayout mRefreshLayout;
    private View mNoKooLayout;
    private View mKooRankLayout;
    private ListView mListView;
    private GoodFriendAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_koo_rank);

        Utils.setStatusBarColorBurn(this, 0xFF5888B5);

        Intent intent = getIntent();
        mUid = intent.getStringExtra(KEY_UID);
        mKooCount = intent.getIntExtra(KEY_KOO_COUNT, (int) MyAccountInfo.getKooNum());

        initViews();

        doFirstLoad();
    }

    private void initViews() {
        ((TextView) findViewById(R.id.count_koo)).setText(String.valueOf(mKooCount));

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mNoKooLayout = findViewById(R.id.no_koo_layout);
        mKooRankLayout = findViewById(R.id.koo_rank_layout);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);

        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeColors(0xFF5888B5);
    }

    private void doFirstLoad() {
        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                doRefresh();
            }
        });
    }

    @Override
    public void onResponse(JSONObject jsonObject) {
        mRefreshLayout.setRefreshing(false);
        try {
            if (jsonObject.getInt("code") == 0) {
                JSONArray rank = jsonObject.getJSONObject("result").getJSONArray("rank");
                mAdapter = new GoodFriendAdapter();

                int count = rank.length();
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        mAdapter.add(rank.getJSONObject(i));
                    }
                    mListView.setAdapter(mAdapter);
                    showKooRankLayout();
                }
                else {
                    showNoKooLayout();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // TODO
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    private void doRefresh() {
        if (TextUtils.isEmpty(mUid)) {
            ApiWorker.getInstance().queueGetRequest(
                    UrlHelper.getKooRankUrl(MyAccountInfo.getUid()), this, this);
        }
        else {
            ApiWorker.getInstance().queueGetRequest(UrlHelper.getKooRankUrl(mUid), this, this);
        }
    }

    private void showNoKooLayout() {
        mNoKooLayout.setVisibility(View.VISIBLE);
        mKooRankLayout.setVisibility(View.INVISIBLE);
    }

    private void showKooRankLayout() {
        mKooRankLayout.setVisibility(View.VISIBLE);
        mNoKooLayout.setVisibility(View.INVISIBLE);
    }


    private static final int TYPE_BEST = 0;
    private static final int TYPE_GOOD = 1;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, FriendInfoActivity.class);
        intent.putExtra(FriendInfoActivity.KEY_UID, mAdapter.mData.get(position).getUid());
        startActivity(intent);
    }

    class GoodFriendAdapter extends BaseAdapter {

        private List<GoodFriendItemInfo> mData;
        private LayoutInflater mInflater;

        GoodFriendAdapter() {
            mData = new ArrayList<GoodFriendItemInfo>();
            mInflater = LayoutInflater.from(KooRankActivity.this);
        }

        public void add(JSONObject jsonObject) {
            mData.add(new GoodFriendItemInfo(jsonObject));
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
        public int getItemViewType(int position) {
            return position == 0 ? TYPE_BEST : TYPE_GOOD;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.good_friend_item, null);
                ViewHolder holder = new ViewHolder();
                holder.avatar = (CircleImageView) convertView.findViewById(R.id.avatar);
                holder.nameView = (UserNameView) convertView.findViewById(R.id.name_view);
                holder.topText = (TextView) convertView.findViewById(R.id.top_text);
                convertView.setTag(holder);

                if (getItemViewType(position) == TYPE_BEST) {
                    convertView.findViewById(R.id.crown).setVisibility(View.VISIBLE);
                    convertView.findViewById(R.id.best_fan).setVisibility(View.VISIBLE);
                }
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            GoodFriendItemInfo item = (GoodFriendItemInfo) getItem(position);
            ImageLoader.getInstance().displayImage(item.getAvatar(), holder.avatar,
                    ImageLoaderHelper.avatarLoadOptions);
            holder.nameView.setUser(item);
            holder.topText.setText(getString(R.string.top_num, position + 1));

            return convertView;
        }
    }

    class GoodFriendItemInfo extends BaseUserInfo {
        public GoodFriendItemInfo(JSONObject jsonObject) {
            super(jsonObject);
        }
    }

    class ViewHolder {
        CircleImageView avatar;
        UserNameView nameView;
        TextView topText;
    }
}
