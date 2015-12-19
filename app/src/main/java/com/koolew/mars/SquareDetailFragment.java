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


    public SquareDetailFragment() {
        super();
        mLayoutResId = R.layout.fragment_tag_square;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();
        mSquareTitle = intent.getStringExtra(KEY_SQUARE_TITLE);
        mSquareId = intent.getStringExtra(KEY_SQUARE_ID);

        TitleBarView titleBarView = ((TitleFragmentActivity) getActivity()).getTitleBar();
        titleBarView.setBackgroundColor(getResources().getColor(R.color.koolew_black));
        titleBarView.setTitle(mSquareTitle);
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
    protected JsonObjectRequest doRefreshRequest() {
        requestTopThumbs();
        return ApiWorker.getInstance().standardGetRequest(UrlHelper.getSquareDetailUrl(mSquareId),
                mRefreshListener, mThumbErrorListener);
    }

    private Request<JSONObject> mTopThumbsRequest = null;
    private void requestTopThumbs() {
        if (mTopThumbsRequest == null) {
            mTopThumbsRequest = ApiWorker.getInstance()
                    .requestDefaultPlayGroup(mSquareId, mThumbListener, null);
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
    protected JsonObjectRequest doLoadMoreRequest() {
        return null;
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
                return result.getJSONArray("videos");
            }
        } catch (JSONException e) {
            e.printStackTrace();
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

        public void setItems(JSONArray jsonArray) {
            mData.clear();
            addData(jsonArray);
            notifyDataSetChanged();
        }

        public void addItems(JSONArray jsonArray) {
            int originCount = mData.size();
            int addedCount = addData(jsonArray);
            if (originCount % 2 == 0) {
                notifyItemRangeInserted(originCount / 2, subItemToLine(addedCount));
            }
            else {
                notifyItemChanged(subItemToLine(originCount) - 1); // Last line
                notifyItemRangeInserted(subItemToLine(originCount), subItemToLine(addedCount - 1));
            }
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
