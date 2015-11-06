package com.koolew.mars;

import android.content.Intent;
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
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.utils.UriProcessor;
import com.koolew.mars.utils.Utils;
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
 * Created by jinchangzhu on 10/14/15.
 */
public class KoolewHotsFragment/*KoolewSquareFragment*/ extends
        RecyclerListFragmentMould<KoolewHotsFragment.SquareAdapter> {

    private int before = 0;
    private int page = 0;

    private UriProcessor mUriProcessor;
    private SquareAdapter.HeaderHolder mHeaderHolder;

    public KoolewHotsFragment() {
        super();
        isNeedLoadMore = true;
        isLazyLoad = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUriProcessor = new UriProcessor(getActivity());
    }

    @Override
    public void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        mRecyclerView.setPadding(0, 0, 0, 0);
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
        before = 0;
        page = 0;
        requestBanner();
        return ApiWorker.getInstance().requestSquare(before, page, mRefreshListener, null);
    }

    private JsonObjectRequest mBannerRequest;
    private void requestBanner() {
        if (mBannerRequest == null) {
            mBannerRequest = ApiWorker.getInstance().getBanner(mBannerListener, null);
        }
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().requestSquare(before, page, mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        JSONArray cards = retrieveSquareCards(response);
        if (cards.length() > 0) {
            mAdapter.setItems(cards);
        }
        return true;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        JSONArray cards = retrieveSquareCards(response);
        if (cards.length() > 0) {
            mAdapter.addItems(cards);
        }
        return true;
    }

    private JSONArray retrieveSquareCards(JSONObject response) {
        try {
            int code = response.getInt("code");
            if (code == 0) {
                JSONObject result = response.getJSONObject("result");
                JSONObject next = result.getJSONObject("next");
                page = next.getInt("page");
                before = next.getInt("before");
                return result.getJSONArray("cards");
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
                    mHeaderHolder.mViewPager.setAdapter(adapter);
                    mHeaderHolder.mIndicator.setViewPager(mHeaderHolder.mViewPager);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    private static final int TYPE_HEADER = 0;
    private static final int TYPE_LINE_ITEM = 1;

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
            if (originCount % 2 == 0) {
                notifyItemRangeInserted(linePositionToAdapter(originCount / 2),
                        subItemToLine(addedCount));
            }
            else {
                notifyItemChanged(linePositionToAdapter(subItemToLine(originCount) - 1)); // Last line
                notifyItemRangeInserted(linePositionToAdapter(subItemToLine(originCount)),
                        subItemToLine(addedCount - 1));
            }
        }

        private int linePositionToAdapter(int linePosition) {
            return linePosition + 1;
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
            switch (viewType) {
                case TYPE_HEADER:
                    return new HeaderHolder(LayoutInflater.from(getActivity())
                            .inflate(R.layout.square_header_layout, parent, false));
                case TYPE_LINE_ITEM:
                    return new SquareItemHolder(LayoutInflater.from(getActivity())
                            .inflate(R.layout.square_line_item, parent, false));
            }
            return null;
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (getCustomItemViewType(position)) {
                case TYPE_HEADER:
                    break;
                case TYPE_LINE_ITEM:
                    position--;
                    SquareItem leftItem = mData.get(position * 2);
                    SquareItem rightItem = null;
                    if (position * 2 + 1 < mData.size()) {
                        rightItem = mData.get(position * 2 + 1);
                    }
                    ((SquareItemHolder) holder).bindSquareLine(leftItem, rightItem);
                    break;
            }
        }

        @Override
        public int getCustomItemCount() {
            return 1 + subItemToLine(mData.size());
        }

        private int subItemToLine(int subItemCount) {
            return (subItemCount + 1) / 2;
        }

        @Override
        public int getCustomItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            }
            else {
                return TYPE_LINE_ITEM;
            }
        }

        class SquareItemHolder extends RecyclerView.ViewHolder {

            private SubItemHolder[] subItemHolders = new SubItemHolder[2];

            public SquareItemHolder(View itemView) {
                super(itemView);

                subItemHolders[0] = new SubItemHolder(itemView.findViewById(R.id.left_item), 0);
                subItemHolders[1] = new SubItemHolder(itemView.findViewById(R.id.right_item), 1);
            }

            public void bindSquareLine(SquareItem leftItem, SquareItem rightItem) {
                subItemHolders[0].bindSquareItem(leftItem);
                subItemHolders[1].bindSquareItem(rightItem);
            }

            class SubItemHolder implements View.OnClickListener {
                private int position;

                private View itemView;
                private TextView title;
                private ImageView avatar;
                private ImageView thumb;
                private TextView kooTotal;
                private TextView firstNickname;
                private TextView firstKooCount;
                private TextView secondNickname;
                private TextView secondKooCount;

                public SubItemHolder(View itemView, int position) {
                    this.position = position;

                    this.itemView = itemView;
                    title = (TextView) itemView.findViewById(R.id.title);
                    title.setOnClickListener(this);
                    avatar = (ImageView) itemView.findViewById(R.id.avatar);
                    avatar.setOnClickListener(this);
                    View thumbLayout = itemView.findViewById(R.id.thumb_layout);
                    thumbLayout.getLayoutParams().height = calcThumbHeight();
                    thumb = (ImageView) itemView.findViewById(R.id.video_thumb);
                    thumb.setOnClickListener(this);
                    kooTotal = (TextView) itemView.findViewById(R.id.koo_total);
                    kooTotal.setOnClickListener(this);
                    firstNickname = (TextView) itemView.findViewById(R.id.first_nickname);
                    firstKooCount = (TextView) itemView.findViewById(R.id.first_koo_count);
                    secondNickname = (TextView) itemView.findViewById(R.id.second_nickname);
                    secondKooCount = (TextView) itemView.findViewById(R.id.second_koo_count);
                    itemView.findViewById(R.id.fans_layout).setOnClickListener(this);
                }

                private int calcThumbHeight() {
                    int screenWidth = Utils.getScreenWidthPixel(getActivity());
                    int itemGap = getResources().getDimensionPixelSize(
                            R.dimen.square_item_padding_half) * 2;
                    int thumbWidth = (screenWidth - itemGap * 3) / 2;
                    return thumbWidth / 4 * 3;
                }

                private SquareItem getItem() {
                    int positionInSquareLine = getAdapterPosition() - 1;
                    return mData.get(positionInSquareLine * 2 + position);
                }

                public void bindSquareItem(SquareItem item) {
                    if (item == null) {
                        itemView.setVisibility(View.INVISIBLE);
                    }
                    else {
                        if (itemView.getVisibility() == View.INVISIBLE) {
                            itemView.setVisibility(View.VISIBLE);
                        }
                        title.setText(item.topicInfo.getTitle());
                        ImageLoader.getInstance().displayImage(item.userInfo.getAvatar(), avatar,
                                ImageLoaderHelper.avatarLoadOptions);
                        ImageLoader.getInstance().displayImage(item.videoInfo.getVideoThumb(),
                                thumb, ImageLoaderHelper.topicThumbLoadOptions);
                        kooTotal.setText(String.valueOf(item.videoInfo.getKooTotal()));
                        if (item.supporters.length >= 1) {
                            firstNickname.setText(item.supporters[0].getNickname());
                            firstKooCount.setText(String.valueOf(item.supporters[0].kooTotal));
                        }
                        else {
                            firstNickname.setText("");
                            firstKooCount.setText("0");
                        }
                        if (item.supporters.length >= 2) {
                            secondNickname.setText(item.supporters[1].getNickname());
                            secondKooCount.setText(String.valueOf(item.supporters[1].kooTotal));
                        }
                        else {
                            secondNickname.setText("");
                            secondKooCount.setText("0");
                        }
                    }
                }

                @Override
                public void onClick(View v) {
                    SquareItem item = getItem();
                    switch (v.getId()) {
                        case R.id.avatar:
                            FriendInfoActivity.startThisActivity(getActivity(),
                                    item.userInfo.getUid());
                            break;
                        case R.id.video_thumb:
                            Intent intent = new Intent(getActivity(), CheckDanmakuActivity.class);
                            intent.putExtra(CheckDanmakuActivity.KEY_VIDEO_ID,
                                    item.videoInfo.getVideoId());
                            startActivity(intent);
                            break;
                        case R.id.koo_total:
                        case R.id.fans_layout:
                            Intent intent2 = new Intent(getActivity(), VideoKooRankActivity.class);
                            intent2.putExtra(VideoKooRankActivity.KEY_VIDEO_ID,
                                    item.videoInfo.getVideoId());
                            startActivity(intent2);
                            break;
                        case R.id.title:
                            FeedsTopicActivity.startTopicWorld(getActivity(),
                                    item.topicInfo.getTopicId(), item.topicInfo.getTitle());
                            break;
                    }
                }
            }
        }

        class HeaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private ViewPager mViewPager;
            private BannerPagerIndicator mIndicator;

            public HeaderHolder(View itemView) {
                super(itemView);
                mHeaderHolder = this;

                mViewPager = (ViewPager) itemView.findViewById(R.id.view_pager);
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
                mIndicator = (BannerPagerIndicator) itemView.findViewById(R.id.indicator);
                itemView.findViewById(R.id.clock_in).setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                KoolewWebActivity.startThisActivityWithoutTitleBar(
                        getActivity(), AppProperty.CLOCK_IN_URL);
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
                    if (!bannerPressed && mHeaderHolder.mViewPager.getAdapter() != null
                            && mHeaderHolder.mViewPager.getAdapter().getCount() != 0) {
                        bannerChangePassedSecond++;
                        if (bannerChangePassedSecond == BANNER_AUTO_CHANGE_SECONDS) {
                            bannerChangePassedSecond = 0;
                            mHeaderHolder.mViewPager.setCurrentItem(
                                    (mHeaderHolder.mViewPager.getCurrentItem() + 1)
                                    % mHeaderHolder.mViewPager.getAdapter().getCount(), true);
                        }
                    }
                }
            });
        }
    }

    static class SquareItem {
        private BaseVideoInfo videoInfo;
        private BaseTopicInfo topicInfo;
        private BaseUserInfo userInfo;
        private Supporter[] supporters;

        public SquareItem(JSONObject jsonObject) {
            JSONObject video = JsonUtil.getJSONObjectIfHas(jsonObject, "video");
            if (video != null) {
                videoInfo = new BaseVideoInfo(video);
            }

            JSONObject topic = JsonUtil.getJSONObjectIfHas(jsonObject, "topic");
            if (topic != null) {
                topicInfo = new BaseTopicInfo(topic);
            }

            JSONObject user = JsonUtil.getJSONObjectIfHas(jsonObject, "user");
            if (user != null) {
                userInfo = new BaseUserInfo(user);
            }

            JSONArray supportersArray = JsonUtil.getJSONArrayIfHas(jsonObject, "supporters");
            if (supportersArray != null) {
                int length = supportersArray.length();
                supporters = new Supporter[length];
                for (int i = 0; i < length; i++) {
                    try {
                        supporters[i] = new Supporter(supportersArray.getJSONObject(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    static class Supporter extends BaseUserInfo {
        private int kooTotal;

        public Supporter(JSONObject jsonObject) {
            super(jsonObject);

            kooTotal = JsonUtil.getIntIfHas(jsonObject, "koo_total");
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
