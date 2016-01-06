package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiErrorCode;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 10/14/15.
 */
public class SquareDetailFragment extends RecyclerListFragmentMould<SquareDetailFragment.SquareAdapter>
        implements View.OnClickListener {

    public static final String KEY_SQUARE_TITLE = "square title";
    public static final String KEY_SQUARE_ID = "square id";

    private ImageView mLeftThumb;
    private ImageView mRightThumb;
    private View mJudgeLayout;

    private String mSquareTitle;
    private String mSquareId;

    private int mNextPage;
    private int mNextBefore;


    public SquareDetailFragment() {
        super();
        mLayoutResId = R.layout.fragment_tag_square;
        isLazyLoad = false;
        isNeedLoadMore = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();
        mSquareTitle = intent.getStringExtra(KEY_SQUARE_TITLE);
        mSquareId = intent.getStringExtra(KEY_SQUARE_ID);

        TitleBarView titleBarView = ((TitleFragmentActivity) getActivity()).getTitleBar();
        // 这个地方也会 null pointer ?!
        if (titleBarView != null) {
            titleBarView.setBackgroundColor(getResources().getColor(R.color.koolew_black));
            titleBarView.setTitle(mSquareTitle);
        }
    }

    @Override
    public void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        mLeftThumb = (ImageView) findViewById(R.id.left_thumb);
        mRightThumb = (ImageView) findViewById(R.id.right_thumb);
        mJudgeLayout = findViewById(R.id.judge_layout);
        mJudgeLayout.setOnClickListener(this);
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
    protected String getRefreshRequestUrl() {
        return UrlHelper.getSquareDetailUrl(mSquareId, mNextPage, mNextBefore);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        mNextPage = 0;
        mNextBefore = 0;
        requestTopThumbs();
        return super.doRefreshRequest();
    }

    private Request<JSONObject> mTopThumbsRequest = null;
    private void requestTopThumbs() {
        if (mTopThumbsRequest == null) {
            mTopThumbsRequest = ApiWorker.getInstance().queueGetRequest(
                    UrlHelper.getDefaultPlayGroupUrl(mSquareId), mThumbListener, mThumbErrorListener);
        }
    }

    private Response.Listener<JSONObject> mThumbListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mTopThumbsRequest = null;
            try {
                int code = response.getInt("code");
                if (code == 0) {
                    JSONArray videos = response.getJSONObject("result").getJSONObject("next")
                            .getJSONArray("videos");
                    String thumbA = videos.getJSONObject(0).getString(BaseVideoInfo.KEY_THUMB_URL);
                    if (!TextUtils.isEmpty(thumbA)) {
                        ImageLoader.getInstance().displayImage(thumbA, mLeftThumb);
                    }
                    String thumbB = videos.getJSONObject(1).getString(BaseVideoInfo.KEY_THUMB_URL);
                    if (!TextUtils.isEmpty(thumbB)) {
                        ImageLoader.getInstance().displayImage(thumbB, mRightThumb);
                    }
                }
                else if (code == ApiErrorCode.NO_MORE_ITEMS) {
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.ErrorListener mThumbErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mTopThumbsRequest = null;
        }
    };

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getSquareDetailUrl(mSquareId, mNextPage, mNextBefore);
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        JSONArray cards = retrieveSquareCards(result);
        updateNextPageAndBefore(result);
        if (cards.length() > 0) {
            return mAdapter.setItems(cards) > 0;
        }
        return false;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        JSONArray cards = retrieveSquareCards(result);
        updateNextPageAndBefore(result);
        if (cards.length() > 0) {
            return mAdapter.addItems(cards) > 0;
        }
        return false;
    }

    private void updateNextPageAndBefore(JSONObject result) {
        try {
            JSONObject next = result.getJSONObject("next");
            mNextPage = next.getInt("page");
            mNextBefore = next.getInt("before");
        } catch (JSONException e) {
            handleJsonException(result, e);
        }
    }

    private JSONArray retrieveSquareCards(JSONObject result) {
        try {
            return result.getJSONArray("videos");
        } catch (JSONException e) {
            handleJsonException(result, e);
        }
        return new JSONArray();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.judge_layout:
                goToJudge();
                break;
        }
    }

    private void goToJudge() {
        PlayFragment.startThisFragment(getActivity(), mSquareId);
    }

    public static void startThisFragment(Context context, String squareTitle, String squareId) {
        Bundle extras = new Bundle();
        extras.putString(KEY_SQUARE_TITLE, squareTitle);
        extras.putString(KEY_SQUARE_ID, squareId);
        TitleFragmentActivity.launchFragment(context, SquareDetailFragment.class, extras);
    }


    class SquareAdapter extends LoadMoreAdapter {

        private List<SquareItem> mData = new ArrayList<>();

        public int setItems(JSONArray jsonArray) {
            mData.clear();
            int addedCount = addData(jsonArray);
            notifyDataSetChanged();
            return addedCount;
        }

        public int addItems(JSONArray jsonArray) {
            int originCount = mData.size();
            int addedCount = addData(jsonArray);
            if (originCount % 2 == 0) {
                notifyItemRangeInserted(originCount / 2, subItemToLine(addedCount));
            }
            else {
                notifyItemChanged(subItemToLine(originCount) - 1); // Last line
                notifyItemRangeInserted(subItemToLine(originCount), subItemToLine(addedCount - 1));
            }
            return addedCount;
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
            return new SquareItemHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.square_line_item, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position) {
            SquareItem leftItem = mData.get(position * 2);
            SquareItem rightItem = null;
            if (position * 2 + 1 < mData.size()) {
                rightItem = mData.get(position * 2 + 1);
            }
            ((SquareItemHolder) holder).bindSquareLine(leftItem, rightItem);
        }

        @Override
        public int getCustomItemCount() {
            return subItemToLine(mData.size());
        }

        private int subItemToLine(int subItemCount) {
            return (subItemCount + 1) / 2;
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
                    itemView.findViewById(R.id.koo_layout).setOnClickListener(this);
                }

                private int calcThumbHeight() {
                    int screenWidth = Utils.getScreenWidthPixel(getActivity());
                    int itemGap = getResources().getDimensionPixelSize(
                            R.dimen.square_item_padding_half) * 2;
                    int thumbWidth = (screenWidth - itemGap * 3) / 2;
                    return thumbWidth / 4 * 3;
                }

                private SquareItem getItem() {
                    return mData.get(getAdapterPosition() * 2 + position);
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
                            SingleMediaFragment.startThisFragment(getActivity(),
                                    item.videoInfo.getVideoId());
                            break;
                        case R.id.koo_layout:
                            Intent intent2 = new Intent(getActivity(), VideoKooRankActivity.class);
                            intent2.putExtra(VideoKooRankActivity.KEY_VIDEO_ID,
                                    item.videoInfo.getVideoId());
                            startActivity(intent2);
                            break;
                        case R.id.title:
                            TopicMediaActivity.startThisActivity(getActivity(),
                                    item.topicInfo.getTopicId(), TopicMediaActivity.TYPE_WORLD);
                            break;
                    }
                }
            }
        }
    }

    static class SquareItem {
        private BaseVideoInfo videoInfo;
        private BaseTopicInfo topicInfo;
        private BaseUserInfo userInfo;

        public SquareItem(JSONObject jsonObject) {
            videoInfo = new BaseVideoInfo(jsonObject);

            JSONObject topic = JsonUtil.getJSONObjectIfHas(jsonObject, "topic");
            if (topic != null) {
                topicInfo = new BaseTopicInfo(topic);
            }

            JSONObject user = JsonUtil.getJSONObjectIfHas(jsonObject, "user");
            if (user != null) {
                userInfo = new BaseUserInfo(user);
            }
        }
    }
}
