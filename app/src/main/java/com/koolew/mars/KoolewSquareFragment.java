package com.koolew.mars;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.blur.DisplayBlurImage;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.utils.UriProcessor;
import com.koolew.mars.view.BannerPagerIndicator;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jinchangzhu on 12/14/15.
 */
public class KoolewSquareFragment extends RecyclerListFragmentMould<KoolewSquareFragment.SquareAdapter> {

    private UriProcessor mUriProcessor;
    private ViewPager mViewPager;
    private BannerPagerIndicator mIndicator;

    public KoolewSquareFragment() {
        super();
        isLazyLoad = true;
        mLayoutResId = R.layout.fragment_square;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUriProcessor = new UriProcessor(getActivity());
    }

    @Override
    public void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        bannerPressed = true;
                        mRefreshLayout.setEnabled(false);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        bannerPressed = false;
                        bannerChangePassedSecond = 0;
                        mRefreshLayout.setEnabled(true);
                        break;
                }
                return false;
            }
        });
        mIndicator = (BannerPagerIndicator) findViewById(R.id.indicator);
        requestBanner();
    }

    @Override
    protected void onPageEnd() {
        super.onPageEnd();
        if (mBannerAutoChangeTask != null) {
            mBannerAutoChangeTask.cancel();
            mBannerAutoChangeTask = null;
        }
    }

    @Override
    protected void onPageStart() {
        super.onPageStart();
        if (mBannerAutoChangeTask == null) {
            mBannerAutoChangeTask = new Timer();
            mBannerAutoChangeTask.schedule(new BannerAutoChangeTask(), 1000, 1000);
        }
    }

    @Override
    protected SquareAdapter useThisAdapter() {
        return new SquareAdapter();
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_black);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestSquare(mRefreshListener, null);
    }

    private JsonObjectRequest mBannerRequest;
    private void requestBanner() {
        if (mBannerRequest == null) {
            mBannerRequest = ApiWorker.getInstance().getBanner(mBannerListener, null);
        }
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().requestSquare(mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        JSONArray cards = retrieveSquareTags(response);
        if (cards.length() > 0) {
            mAdapter.setItems(cards);
        }
        return true;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        JSONArray cards = retrieveSquareTags(response);
        if (cards.length() > 0) {
            mAdapter.addItems(cards);
        }
        return true;
    }

    private JSONArray retrieveSquareTags(JSONObject response) {
        try {
            int code = response.getInt("code");
            if (code == 0) {
                JSONObject result = response.getJSONObject("result");
                return result.getJSONArray("tags");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    private Response.Listener<JSONObject> mBannerListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mBannerRequest = null;
            try {
                if (response.getInt("code") == 0) {
                    JSONObject result = response.getJSONObject("result");

                    JSONArray banners = result.getJSONArray("banners");
                    BannerAdapter adapter = new BannerAdapter();
                    adapter.setData(banners);
                    mViewPager.setAdapter(adapter);
                    mIndicator.setViewPager(mViewPager);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    class SquareAdapter extends LoadMoreAdapter {

        private List<SquareTagItem> mData = new ArrayList<>();

        public void setItems(JSONArray jsonArray) {
            mData.clear();
            addData(jsonArray);
            notifyDataSetChanged();
        }

        public void addItems(JSONArray jsonArray) {
            int originCount = mData.size();
            int addedCount = addData(jsonArray);
            notifyItemRangeInserted(originCount, addedCount);
        }

        private int addData(JSONArray jsonArray) {
            int addedCount = 0;
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                try {
                    mData.add(new SquareTagItem(jsonArray.getJSONObject(i)));
                    addedCount++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return addedCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new SquareTagHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.square_tag_item, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            SquareTagItem item = mData.get(position);
            SquareTagHolder holder = (SquareTagHolder) viewHolder;
            ImageLoader.getInstance().displayImage(item.tagIcon, holder.tagIcon);
            holder.tagName.setText(item.tagName);
            holder.tagName.setTextColor(item.tagColor);
            if (item.videoInfos.length > 0) {
                ImageLoader.getInstance().displayImage(item.videoInfos[0].getVideoThumb(), holder.thumbs[0]);
            }
            if (item.videoInfos.length > 1) {
                ImageLoader.getInstance().displayImage(item.videoInfos[1].getVideoThumb(), holder.thumbs[1]);
            }
            if (item.videoInfos.length > 2) {
                DisplayBlurImage displayBlurImageTask = new DisplayBlurImage(
                        holder.thumbs[2], item.videoInfos[2].getVideoThumb());
                displayBlurImageTask.setScaleBeforeBlur(15);
                displayBlurImageTask.execute();
            }
        }

        @Override
        public int getCustomItemCount() {
            return mData.size();
        }

        class SquareTagHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView tagIcon;
            TextView tagName;
            ImageView[] thumbs = new ImageView[3];

            public SquareTagHolder(View itemView) {
                super(itemView);

                tagIcon = (ImageView) itemView.findViewById(R.id.tag_icon);
                tagName = (TextView) itemView.findViewById(R.id.tag_name);
                thumbs[0] = (ImageView) itemView.findViewById(R.id.first_thumb);
                thumbs[0].setOnClickListener(this);
                thumbs[1] = (ImageView) itemView.findViewById(R.id.second_thumb);
                thumbs[1].setOnClickListener(this);
                thumbs[2] = (ImageView) itemView.findViewById(R.id.third_thumb);
                thumbs[2].setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                SquareTagItem item = mData.get(getAdapterPosition());
                if (v == thumbs[0] && item.videoInfos.length > 0) {
                    SingleMediaFragment.startThisFragment(getActivity(), item.videoInfos[0].getVideoId());
                }
                else if (v == thumbs[1] && item.videoInfos.length > 1) {
                    SingleMediaFragment.startThisFragment(getActivity(), item.videoInfos[1].getVideoId());
                }
                else if (v == thumbs[2]) {
                    // Go to square tab activity
                }
            }
        }
    }

    private Timer mBannerAutoChangeTask;
    private boolean bannerPressed = false;
    private static final int BANNER_AUTO_CHANGE_SECONDS = 3;
    private int bannerChangePassedSecond = 0;

    class BannerAutoChangeTask extends TimerTask {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!bannerPressed && mViewPager.getAdapter() != null
                            && mViewPager.getAdapter().getCount() != 0) {
                        bannerChangePassedSecond++;
                        if (bannerChangePassedSecond == BANNER_AUTO_CHANGE_SECONDS) {
                            bannerChangePassedSecond = 0;
                            mViewPager.setCurrentItem((mViewPager.getCurrentItem() + 1)
                                    % mViewPager.getAdapter().getCount(), true);
                        }
                    }
                }
            });
        }
    }

    static class SquareTagItem {
        String tagId;
        String tagName;
        String tagIcon;
        int tagColor;
        BaseVideoInfo[] videoInfos;

        public SquareTagItem(JSONObject itemObject) {
            JSONObject tag = JsonUtil.getJSONObjectIfHas(itemObject, "tag");
            if (tag == null) {
                return;
            }
            tagId = JsonUtil.getStringIfHas(tag, "tag_id");
            tagName = JsonUtil.getStringIfHas(tag, "tag_name");
            tagIcon = JsonUtil.getStringIfHas(tag, "tag_icon");
            tagColor = JsonUtil.getIntIfHas(tag, "tag_color", Color.BLACK);

            JSONArray videosJson = JsonUtil.getJSONArrayIfHas(itemObject, "videos", new JSONArray());
            int videoCount = videosJson.length();
            videoInfos = new BaseVideoInfo[videoCount];
            for (int i = 0; i < videoCount; i++) {
                try {
                    videoInfos[i] = new BaseVideoInfo(videosJson.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private View.OnClickListener mOnBannerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mUriProcessor.process(v.getTag().toString());
        }
    };

    class BannerAdapter extends PagerAdapter {

        private ImageView[] mImageViews;

        public void setData(JSONArray jsonArray) {
            int count = jsonArray.length();
            mImageViews = new ImageView[count];
            for (int i = 0; i < count; i++) {
                mImageViews[i] = new ImageView(getActivity());
                mImageViews[i].setOnClickListener(mOnBannerClickListener);
                mImageViews[i].setScaleType(ImageView.ScaleType.CENTER_CROP);
                ViewPager.LayoutParams lp = new ViewPager.LayoutParams();
                mImageViews[i].setLayoutParams(lp);

                try {
                    JSONObject banner = jsonArray.getJSONObject(i);
                    ImageLoader.getInstance().displayImage(banner.getString("image_url"),
                            mImageViews[i]);
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
}
