package com.koolew.mars;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 11/7/15.
 */
public class KooTabFragment extends RecyclerListFragmentMould<KooTabFragment.KooTabItemAdapter> {

    public KooTabFragment() {
        super();
        mLayoutResId = R.layout.koo_notification_fragment;
        isNeedLoadMore = true;
        isLazyLoad = true;
    }

    @Override
    protected KooTabItemAdapter useThisAdapter() {
        return new KooTabItemAdapter();
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_red);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().getKooNotification(mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().getKooNotification(mAdapter.getLastUpdateTime(),
                mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        try {
            if (response.getInt("code") == 0) {
                return mAdapter.setData(response.getJSONObject("result")
                        .getJSONArray("notifications"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        try {
            if (response.getInt("code") == 0) {
                return mAdapter.addData(response.getJSONObject("result")
                        .getJSONArray("notifications"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    class KooTabItemAdapter extends LoadMoreAdapter {

        private List<KooItem> mKooItemList = new ArrayList<>();

        public boolean setData(JSONArray notifications) {
            mKooItemList.clear();
            int addedCount = addDataInner(notifications);
            notifyDataSetChanged();
            return addedCount > 0;
        }

        public boolean addData(JSONArray notifications) {
            int originalCount = mKooItemList.size();
            int addedCount = addDataInner(notifications);
            if (addedCount > 0) {
                notifyItemRangeInserted(originalCount, addedCount);
                return true;
            }
            else {
                return false;
            }
        }

        private int addDataInner(JSONArray notifications) {
            int length = notifications.length();
            if (length == 0) {
                return 0;
            }
            int addedCount = 0;
            for (int i = 0; i < length; i++) {
                try {
                    mKooItemList.add(new KooItem(notifications.getJSONObject(i)));
                    addedCount++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return addedCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new KooHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.koo_tab_item, null));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            KooHolder holder = (KooHolder) viewHolder;
            KooItem item = mKooItemList.get(position);

            ImageLoader.getInstance().displayImage(item.userInfo.getAvatar(), holder.avatar,
                    ImageLoaderHelper.avatarLoadOptions);
            ImageLoader.getInstance().displayImage(item.videoInfo.getVideoThumb(),
                    holder.videoThumb, ImageLoaderHelper.topicThumbLoadOptions);

            ForegroundColorSpan nicknameSpan = new ForegroundColorSpan(0xFFDB5E5F);
            ForegroundColorSpan remainSpan = new ForegroundColorSpan(0xFF3E5467);
            SpannableStringBuilder ssBuilder = new SpannableStringBuilder();
            ssBuilder.append(item.userInfo.getNickname())
                    .append(getString(R.string.supported_this_video));
            ssBuilder.setSpan(nicknameSpan, 0, item.userInfo.getNickname().length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssBuilder.setSpan(remainSpan, item.userInfo.getNickname().length(),
                    ssBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.whoKoo.setText(ssBuilder);

            holder.topicTitle.setText(item.topicInfo.getTitle());
            holder.kooCount.setText(String.format("+%d", item.kooCount));
        }

        @Override
        public int getCustomItemCount() {
            return mKooItemList.size();
        }

        private long getLastUpdateTime() {
            if (mKooItemList.size() == 0) {
                return Long.MAX_VALUE;
            }
            else {
                return mKooItemList.get(mKooItemList.size() - 1).updateTime;
            }
        }
    }

    private class KooHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView avatar;
        private ImageView videoThumb;
        private TextView whoKoo;
        private TextView topicTitle;
        private TextView kooCount;

        public KooHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            avatar.setOnClickListener(this);
            videoThumb = (ImageView) itemView.findViewById(R.id.video_thumb);
            whoKoo = (TextView) itemView.findViewById(R.id.who_koo);
            topicTitle = (TextView) itemView.findViewById(R.id.topic_title);
            kooCount = (TextView) itemView.findViewById(R.id.koo_count);
        }

        private KooItem getKooItem() {
            return mAdapter.mKooItemList.get(getAdapterPosition());
        }

        @Override
        public void onClick(View v) {
            if (v == itemView) {
                Intent intent = new Intent(getActivity(), CheckDanmakuActivity.class);
                intent.putExtra(CheckDanmakuActivity.KEY_VIDEO_ID,
                        getKooItem().videoInfo.getVideoId());
                startActivity(intent);
            }
            else if (v == avatar) {
                FriendInfoActivity.startThisActivity(getActivity(), getKooItem().userInfo.getUid());
            }
        }
    }

    private static class KooItem {
        private BaseUserInfo userInfo;
        private BaseVideoInfo videoInfo;
        private BaseTopicInfo topicInfo;
        private int kooCount;
        private long updateTime;

        private KooItem(JSONObject jsonObject) {
            try {
                userInfo = new BaseUserInfo(jsonObject.getJSONObject("user"));
                videoInfo = new BaseVideoInfo(jsonObject.getJSONObject("video"));
                topicInfo = new BaseTopicInfo(jsonObject.getJSONObject("topic"));
                kooCount = jsonObject.getInt("koo_cnt");
                updateTime = jsonObject.getLong("update_time");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
