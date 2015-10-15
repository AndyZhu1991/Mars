package com.koolew.mars;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 10/14/15.
 */
public class KoolewHotsFragment/*KoolewSquareFragment*/ extends
        RecyclerListFragmentMould<KoolewHotsFragment.SquareAdapter> {

    private int before = 0;
    private int page = 0;

    public KoolewHotsFragment() {
        super();
        isNeedLoadMore = true;
        isLazyLoad = true;
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
        return ApiWorker.getInstance().requestSquare(before, page, mRefreshListener, null);
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
                private ImageView avatar;
                private ImageView thumb;
                private View bottomLayout;
                private TextView kooTotal;
                private TextView firstNickname;
                private TextView firstKooCount;
                private TextView secondNickname;
                private TextView secondKooCount;

                public SubItemHolder(View itemView, int position) {
                    this.position = position;

                    this.itemView = itemView;
                    avatar = (ImageView) itemView.findViewById(R.id.avatar);
                    avatar.setOnClickListener(this);
                    thumb = (ImageView) itemView.findViewById(R.id.video_thumb);
                    thumb.getLayoutParams().height = calcThumbHeight();
                    thumb.setOnClickListener(this);
                    bottomLayout = itemView.findViewById(R.id.bottom_layout);
                    bottomLayout.setOnClickListener(this);
                    kooTotal = (TextView) itemView.findViewById(R.id.koo_total);
                    firstNickname = (TextView) itemView.findViewById(R.id.first_nickname);
                    firstKooCount = (TextView) itemView.findViewById(R.id.first_koo_count);
                    secondNickname = (TextView) itemView.findViewById(R.id.second_nickname);
                    secondKooCount = (TextView) itemView.findViewById(R.id.second_koo_count);
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
                        case R.id.bottom_layout:
                            Intent intent2 = new Intent(getActivity(), VideoKooRankActivity.class);
                            intent2.putExtra(VideoKooRankActivity.KEY_VIDEO_ID,
                                    item.videoInfo.getVideoId());
                            startActivity(intent2);
                            break;
                    }
                }
            }
        }
    }

    static class SquareItem {
        private BaseVideoInfo videoInfo;
        private BaseUserInfo userInfo;
        private Supporter[] supporters;

        public SquareItem(JSONObject jsonObject) {
            JSONObject video = JsonUtil.getJSONObjectIfHas(jsonObject, "video");
            if (video != null) {
                videoInfo = new BaseVideoInfo(video);
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
}
