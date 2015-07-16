package com.koolew.mars;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.utils.UriProcessor;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.BannerPagerIndicator;
import com.koolew.mars.view.LoadMoreFooter;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class TaskActivity extends Activity
        implements SwipeRefreshLayout.OnRefreshListener, LoadMoreFooter.OnLoadListener {

    private View mHeaderView;
    private LoadMoreFooter mListFooter;
    private ViewPager mViewPager;
    private BannerPagerIndicator mIndicator;
    private BannerAdapter mPagerAdapter;
    private TextView mHeaderText;

    private SwipeRefreshLayout mRefreshLayout;
    private ListView mListView;
    private TaskAdapter mListAdapter;

    private JsonObjectRequest mRefreshRequest;
    private JsonObjectRequest mLoadMoreRequest;

    private UriProcessor mUriProcesser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        initView();

        mUriProcesser = new UriProcessor(this);

        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                doRefresh();
            }
        });
    }

    private void initView() {
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setColorSchemeResources(R.color.koolew_light_green);
        mRefreshLayout.setOnRefreshListener(this);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(mOnFriendTasksClickListener);

        mHeaderView = LayoutInflater.from(this).inflate(R.layout.task_activity_list_header, null);
        mViewPager = (ViewPager) mHeaderView.findViewById(R.id.view_pager);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        mRefreshLayout.setEnabled(false);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mRefreshLayout.setEnabled(true);
                        break;
                }
                return false;
            }
        });
        mIndicator = (BannerPagerIndicator) mHeaderView.findViewById(R.id.indicator);
        mHeaderText = (TextView) mHeaderView.findViewById(R.id.header_text);
        mListView.addHeaderView(mHeaderView);

        mListFooter = (LoadMoreFooter) LayoutInflater.from(this).inflate(R.layout.load_more_footer, null);
        mListFooter.setTextColor(getResources().getColor(R.color.koolew_light_gray));
        mListView.addFooterView(mListFooter);
        mListFooter.setup(mListView);
        mListFooter.setOnLoadListener(this);
    }

    private void doRefresh() {
        if (mLoadMoreRequest != null) {
            mLoadMoreRequest.cancel();
            mLoadMoreRequest = null;
        }
        mRefreshRequest = ApiWorker.getInstance().requestTask(mRefreshListener, null);
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    @Override
    public void onLoad() {
        if (mRefreshRequest != null) {
            mRefreshRequest.cancel();
            mRefreshRequest = null;
        }
        mLoadMoreRequest = ApiWorker.getInstance().
                requestTask(mListAdapter.getLastCardTime(), mLoadMoreListener, null);
    }

    private Response.Listener<JSONObject> mRefreshListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            if (mRefreshRequest != null) {
                mRefreshLayout.setRefreshing(false);
                mRefreshRequest = null;
            }

            try {
                if (jsonObject.getInt("code") == 0) {
                    JSONObject result = jsonObject.getJSONObject("result");

                    JSONArray banners = result.getJSONArray("banners");
                    mPagerAdapter = new BannerAdapter();
                    mPagerAdapter.setData(banners);
                    mViewPager.setAdapter(mPagerAdapter);
                    mIndicator.setViewPager(mViewPager);

                    JSONArray cards = result.getJSONArray("cards");
                    int count = cards.length();
                    if (count == 0) {
                        mHeaderText.setText(R.string.you_have_no_task);
                        mListFooter.haveNoMore();
                        mListFooter.setVisibility(View.INVISIBLE);
                    }
                    else {
                        mListFooter.haveMore(true);
                        mListFooter.setVisibility(View.VISIBLE);
                    }
                    mListAdapter = new TaskAdapter();
                    mListAdapter.setData(cards);
                    mListView.setAdapter(mListAdapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.Listener<JSONObject> mLoadMoreListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            if (mLoadMoreRequest != null) {
                mListFooter.loadComplete();
                mLoadMoreRequest = null;
            }

            try {
                if (jsonObject.getInt("code") == 0) {
                    JSONObject result = jsonObject.getJSONObject("result");

                    JSONArray cards = result.getJSONArray("cards");
                    int count = cards.length();
                    if (count != 0) {
                        mListAdapter.addData(cards);
                        mListAdapter.notifyDataSetChanged();
                    }
                    else {
                        mListFooter.haveNoMore();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener mOnAvatarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(TaskActivity.this, FriendInfoActivity.class);
            intent.putExtra(FriendInfoActivity.KEY_UID, v.getTag().toString());
            startActivity(intent);
        }
    };

    private AbsListView.OnItemClickListener mOnFriendTasksClickListener = new AbsListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            position--; // Header?
            if (position < mListAdapter.getCount()) {
                Intent intent = new Intent(TaskActivity.this, FriendTaskActivity.class);
                try {
                    JSONObject user = ((JSONObject) mListAdapter.getItem(position)).getJSONObject("user");
                    intent.putExtra(FriendTaskActivity.KEY_UID, user.getString("uid"));
                    intent.putExtra(FriendTaskActivity.KEY_NICKNAME, user.getString("nickname"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        }
    };

    private View.OnClickListener mOnTaskClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

    private View.OnClickListener mOnBannerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mUriProcesser.process(v.getTag().toString());
        }
    };


    class BannerAdapter extends PagerAdapter {

        private ImageView[] mImageViews;

        public void setData(JSONArray jsonArray) {
            int count = jsonArray.length();
            mImageViews = new ImageView[count];
            for (int i = 0; i < count; i++) {
                mImageViews[i] = new ImageView(TaskActivity.this);
                mImageViews[i].setOnClickListener(mOnBannerClickListener);
                mImageViews[i].setScaleType(ImageView.ScaleType.CENTER_CROP);
                ViewPager.LayoutParams lp = new ViewPager.LayoutParams();
                mImageViews[i].setLayoutParams(lp);

                try {
                    JSONObject banner = jsonArray.getJSONObject(i);
                    ImageLoader.getInstance().displayImage(banner.getString("image_url"), mImageViews[i]);
                    mImageViews[i].setTag(banner.getString("content_url"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getCount() {
            return mImageViews.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mImageViews[position]);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mImageViews[position]);
            return mImageViews[position];
        }
    }


    class TaskAdapter extends BaseAdapter {

        private List<JSONObject> mData = new ArrayList<JSONObject>();

        public void setData(JSONArray jsonArray) {
            mData.clear();
            addData(jsonArray);
        }

        public void addData(JSONArray jsonArray) {
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                try {
                    mData.add(jsonArray.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public long getLastCardTime() {
            try {
                if (mData.size() > 0) {
                    return mData.get(mData.size() - 1).getLong("update_time");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;
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
                convertView = LayoutInflater.from(TaskActivity.this).inflate(R.layout.task_item, null);
                ViewHolder holder = new ViewHolder();
                holder.newTaskFlag = (ImageView) convertView.findViewById(R.id.new_task_flag);
                holder.avatar = (CircleImageView) convertView.findViewById(R.id.avatar);
                holder.nickname = (TextView) convertView.findViewById(R.id.nickname);
                holder.topicCount = (TextView) convertView.findViewById(R.id.topic_count);
                holder.topicLayout = (LinearLayout) convertView.findViewById(R.id.topic_layout);
                convertView.setTag(holder);

                holder.avatar.setOnClickListener(mOnAvatarClickListener);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            JSONObject item = (JSONObject) getItem(position);
            try {
                if (item.getInt("new") == 0) {
                    holder.newTaskFlag.setVisibility(View.INVISIBLE);
                    holder.avatar.setBorderColor(getResources().getColor(R.color.avatar_gray_border));
                }
                else {
                    holder.newTaskFlag.setVisibility(View.VISIBLE);
                    holder.avatar.setBorderColor(getResources().getColor(R.color.koolew_light_green));
                }

                JSONObject user = item.getJSONObject("user");
                ImageLoader.getInstance().displayImage(user.getString("avatar"), holder.avatar);
                holder.nickname.setText(user.getString("nickname"));
                holder.topicCount.setText(getString(R.string.counter_ge, item.getInt("task_cnt")));

                holder.avatar.setTag(user.getString("uid"));


                for (int i = 0; i < holder.topicLayout.getChildCount(); i++) {
                    holder.topicLayout.getChildAt(i).setVisibility(View.GONE);
                }
                JSONArray topics = item.getJSONArray("topics");
                int count = topics.length();
                int topicContainerWidthRemaining = getTopicContainerWidth();
                int topicItemMinWidth = getResources().getDimensionPixelSize(
                        R.dimen.task_item_topic_item_min_width);
                int topicItemRightMargin = getResources().getDimensionPixelSize(
                        R.dimen.task_item_topic_item_right_margin);
                for (int i = 0; i < count ; i++) {
                    TextView topicText;
                    if (holder.topicLayout.getChildCount() <= i) {
                        topicText = generateTopicTextView();
                        holder.topicLayout.addView(topicText);
                        topicText.setOnClickListener(mOnTaskClickListener);
                    }
                    else {
                        topicText = (TextView) holder.topicLayout.getChildAt(i);
                    }

                    String content = topics.getJSONObject(i).getString("content");
                    String topic_id = topics.getJSONObject(i).getString("topic_id");
                    topicText.setText(content);
                    topicText.setTag(topic_id);

                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) topicText.getLayoutParams();
                    int width = getTopicItemWidth(content);
                    if (width + topicItemRightMargin <= topicContainerWidthRemaining) {
                        lp.width = width;
                    }
                    else {
                        lp.width = topicContainerWidthRemaining - topicItemRightMargin;
                    }
                    topicText.setLayoutParams(lp);

                    topicText.setVisibility(View.VISIBLE);

                    topicContainerWidthRemaining -= (lp.width + topicItemRightMargin);
                    if (topicContainerWidthRemaining < topicItemMinWidth) {
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertView;
        }

        private int getTopicContainerWidth() {
            return Utils.getScreenWidthPixel(TaskActivity.this)
                    - getResources().getDimensionPixelSize(R.dimen.task_item_margin) * 2
                    - getResources().getDimensionPixelSize(R.dimen.task_item_bottom_left_padding)
                    - getResources().getDimensionPixelSize(R.dimen.task_item_bottom_right_padding);
        }

        private int getTopicItemWidth(String text) {
            int fullWidth = (int) (Utils.getTextWidth(text, getResources().getDimension(R.dimen.task_item_topic_item_text_size))
                                + getResources().getDimensionPixelSize(R.dimen.task_item_topic_item_lr_padding) * 2) + 2;
            int minWidth = getResources().getDimensionPixelOffset(R.dimen.task_item_topic_item_min_width);

            return Math.max(fullWidth, minWidth);
        }

        private TextView generateTopicTextView() {
            TextView textView = new TextView(TaskActivity.this);

            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setBackgroundResource(R.drawable.task_item_topic_item_bg);
            textView.setSingleLine();

            textView.setTextSize(Utils.pixelsToSp(TaskActivity.this,
                    getResources().getDimension(R.dimen.task_item_topic_item_text_size)));
            textView.setTextColor(0xFF9B9B9B);

            textView.setPadding(getResources().getDimensionPixelOffset(R.dimen.task_item_topic_item_lr_padding), 0,
                    getResources().getDimensionPixelOffset(R.dimen.task_item_topic_item_lr_padding), 0);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, getResources().getDimensionPixelOffset(R.dimen.task_item_topic_item_right_margin), 0);
            textView.setLayoutParams(lp);

            return textView;
        }
    }

    class ViewHolder {
        ImageView newTaskFlag;
        CircleImageView avatar;
        TextView nickname;
        TextView topicCount;
        LinearLayout topicLayout;
    }
}
