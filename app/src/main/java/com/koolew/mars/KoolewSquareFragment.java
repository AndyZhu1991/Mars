package com.koolew.mars;

import android.app.Activity;
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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.utils.Downloader;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.utils.UriProcessor;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.BannerPagerIndicator;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by jinchangzhu on 12/14/15.
 */
public class KoolewSquareFragment extends RecyclerListFragmentMould<KoolewSquareFragment.SquareAdapter> {

    private UriProcessor mUriProcessor;
    private ViewPager mViewPager;
    private BannerPagerIndicator mIndicator;

    private int mPage;

    public KoolewSquareFragment() {
        super();
        isLazyLoad = true;
        isNeedApiCache = true;
        mLayoutResId = R.layout.fragment_square;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUriProcessor = new UriProcessor(getActivity());
    }

    @Override
    public void onCreateViewLazy(Bundle savedInstanceState) {
        mPage = 0;
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
    protected void requestInitApiFromNetwork() {
        mPage = 0;
        super.requestInitApiFromNetwork();
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

    private JsonObjectRequest mBannerRequest;
    private void requestBanner() {
        if (mBannerRequest == null) {
            mBannerRequest = ApiWorker.getInstance().queueGetRequest(
                    UrlHelper.BANNER_URL, mBannerListener, mBannerErrorListener);
        }
    }

    @Override
    protected String getRefreshRequestUrl() {
        return UrlHelper.getSquareUrl(mPage);
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return null;
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        JSONArray cards = null;
        try {
            cards = result.getJSONArray("cards");
            mPage = result.getJSONObject("next").getInt("page");
        } catch (JSONException e) {
            handleJsonException(result, e);
        }
        if (cards != null && cards.length() > 0) {
            mAdapter.setItems(cards);
        }
        return true;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        return false;
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

    private Response.ErrorListener mBannerErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mBannerRequest = null;
        }
    };


    class SquareAdapter extends LoadMoreAdapter {

        private List<SquareItem> mData = new ArrayList<>();

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
                    mData.add(new SquareItem(jsonArray.getJSONObject(i)));
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
            SquareItem item = mData.get(position);
            SquareTagHolder holder = (SquareTagHolder) viewHolder;
            ImageLoader.getInstance().displayImage(item.icon, holder.tagIcon);
            holder.tagName.setText(item.name);
            holder.tagName.setTextColor(item.color);

            for (int i = 0; i < holder.thumbs.length; i++) {
                if (item.videoInfos.length > i && item.videoInfos[i] != null) {
                    ImageLoader.getInstance().displayImage(
                            item.videoInfos[i].getVideoThumb(), holder.thumbs[i]);
                }
                else {
                    holder.thumbs[i].setImageResource(R.mipmap.topic_default_thumb);
                }
            }

            File localGifFile = new File("/sdcard/test.gif"); //Downloader.getInstance().tryToGetLocalFile(item.gifUrl);
            if (localGifFile == null) {
                try {
                    Downloader.getInstance().download(holder, item.gifUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                holder.gifImageView.setImageResource(R.mipmap.default_pk_entry);
            }
            else {
                holder.setGifFile(localGifFile);
            }
        }

        @Override
        public int getCustomItemCount() {
            return mData.size();
        }

        class SquareTagHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                Downloader.LoadListener {
            ImageView tagIcon;
            TextView tagName;
            ImageView[] thumbs = new ImageView[SUB_ITEM_IDS.length];
            GifImageView gifImageView;

            public SquareTagHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                tagIcon = (ImageView) itemView.findViewById(R.id.tag_icon);
                tagName = (TextView) itemView.findViewById(R.id.tag_name);

                for (int i = 0; i < SUB_ITEM_IDS.length; i++) {
                    thumbs[i] = (ImageView) itemView.findViewById(SUB_ITEM_IDS[i])
                            .findViewById(R.id.thumb);
                    thumbs[i].setOnClickListener(this);
                }

                gifImageView = (GifImageView) itemView.findViewById(R.id.gif_image);
            }

            public void setGifFile(File gifFile) {
                try {
                    GifDrawable gifDrawable = new GifDrawable(gifFile);
                    gifDrawable.setCornerRadius(Utils.dpToPixels(getContext(), 4));
                    gifImageView.setImageDrawable(gifDrawable);
                } catch (IOException e) {
                    gifImageView.setImageResource(R.mipmap.default_pk_entry);
                }
            }

            @Override
            public void onClick(View v) {
                SquareItem item = mData.get(getAdapterPosition());
                if (v == itemView) {
                    // Go to square tab activity
                    SquareDetailFragment.startThisFragment(getActivity(), item.name, item.id);
                }
                else {
                    for (int i = 0; i < SUB_ITEM_IDS.length; i++) {
                        if (v == thumbs[i]) {
                            if (item.videoInfos.length > i && item.videoInfos[i] != null) {
                                SingleMediaFragment.startThisFragment(getActivity(),
                                        item.videoInfos[i].getVideoId());
                            }
                            return;
                        }
                    }
                }
            }

            @Override
            public void onDownloadComplete(String url, String filePath) {
                if (mData.get(getAdapterPosition()).gifUrl.equals(url)) {
                    setGifFile(new File(filePath));
                }
            }

            @Override
            public void onDownloadProgress(long totalBytes, long downloadedBytes, int progress) {
            }

            @Override
            public void onDownloadFailed(int errorCode, String errorMessage) {
            }
        }
    }

    private static final int[] SUB_ITEM_IDS = new int[] {
            R.id.item0,
            R.id.item1,
            R.id.item2,
            R.id.item3,
            R.id.item4,
    };

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

    static class SquareItem {
        String id;
        String name;
        String icon;
        int color;
        String gifUrl;
        BaseVideoInfo[] videoInfos;

        public SquareItem(JSONObject itemObject) {
            id = JsonUtil.getStringIfHas(itemObject, "id");
            name = JsonUtil.getStringIfHas(itemObject, "name");
            icon = JsonUtil.getStringIfHas(itemObject, "icon");
            color = JsonUtil.getIntIfHas(itemObject, "color", Color.BLACK);

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
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            int count = jsonArray.length();
            mImageViews = new ImageView[count];
            for (int i = 0; i < count; i++) {
                mImageViews[i] = new ImageView(activity);
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
            if (mImageViews == null) {
                return 0;
            }
            else {
                return mImageViews.length;
            }
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
