package com.koolew.mars;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.redpoint.RedPointManager;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.BigCountView;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendMeetFragment
        extends RecyclerListFragmentMould<FriendMeetFragment.FriendRecommendAdapter>{

    public FriendMeetFragment() {
        isNeedLoadMore = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread() {
            @Override
            public void run() {
                LocationManagerProxy locationManagerProxy =
                        LocationManagerProxy.getInstance(getActivity());
                locationManagerProxy.requestLocationData(
                        LocationProviderProxy.AMapNetwork, -1, 1, mLocationListener);
            }
        }.start();
    }

    @Override
    protected FriendRecommendAdapter useThisAdapter() {
        return new FriendRecommendAdapter();
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_blue);
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestRecommendFriend(mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().requestPoiRecommendFriend(mAdapter.getMaxNearbyDistance(),
                mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        RedPointManager.clearRedPointByPath(RedPointManager.PATH_FRIENDS);
        try {
            JSONObject result = response.getJSONObject("result");
            JSONArray pendings = result.getJSONArray("pendings");
            JSONArray recommends = result.getJSONArray("recommends");
            JSONArray poiRecommends = result.getJSONArray("poi_recommends");
            mAdapter.setData(pendings, recommends, poiRecommends);
            mAdapter.notifyDataSetChanged();
            return poiRecommends.length() > 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        try {
            JSONObject result = response.getJSONObject("result");
            JSONArray poiRecommends = result.getJSONArray("poi_recommends");
            if (poiRecommends.length() > 0) {
                int itemCountBeforeAdd = mAdapter.getCustomItemCount();
                int addedCount = mAdapter.addNearbys(poiRecommends);
                if (addedCount > 0) {
                    mAdapter.notifyItemRangeInserted(itemCountBeforeAdd, addedCount);
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private AMapLocationListener mLocationListener = new AMapLocationListener() {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if(aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0) {
                double longitude = aMapLocation.getLongitude();
                double latitude = aMapLocation.getLatitude();
                ApiWorker.getInstance().postLocation(longitude, latitude,
                        ApiWorker.getInstance().emptyResponseListener, null);
            }
        }

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };


    private static final int TYPE_PADDING   = 0;
    private static final int TYPE_RECOMMEND = 1;
    private static final int TYPE_NEARBY    = 2;

    class FriendRecommendAdapter extends LoadMoreAdapter {

        private Context mContext;

        private List<PendingRecommendItem> mPendings;
        private List<PendingRecommendItem> mRecommends;
        private List<NearbyItem> mNearbys;

        public FriendRecommendAdapter() {
            mContext = getActivity();

            mPendings = new ArrayList<>();
            mRecommends = new ArrayList<>();
            mNearbys = new ArrayList<>();
        }

        public void setData(JSONArray pendings, JSONArray recommends, JSONArray nearbys) {
            JSONArray2List(pendings, mPendings, new ItemGenerator() {
                @Override
                Object JSONObject2Item(JSONObject jsonObject) {
                    return new PendingRecommendItem(jsonObject);
                }
            });
            JSONArray2List(recommends, mRecommends, new ItemGenerator() {
                @Override
                Object JSONObject2Item(JSONObject jsonObject) {
                    return new PendingRecommendItem(jsonObject);
                }
            });
            JSONArray2List(nearbys, mNearbys, new ItemGenerator() {
                @Override
                Object JSONObject2Item(JSONObject jsonObject) {
                    return new NearbyItem(jsonObject);
                }
            });
        }

        private void JSONArray2List(JSONArray jsonArray, List list, ItemGenerator generator) {
            list.clear();
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                try {
                    list.add(generator.JSONObject2Item(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        abstract class ItemGenerator {
            abstract Object JSONObject2Item(JSONObject jsonObject);
        }

        private double getMaxNearbyDistance() {
            return mNearbys.get(mNearbys.size() - 1).distance;
        }

        /**
         *
         * @param nearbys
         * @return added count
         */
        private int addNearbys(JSONArray nearbys) {
            int addedCount = 0;
            int length = nearbys.length();
            for (int i = 0; i < length; i++) {
                try {
                    NearbyItem item = new NearbyItem(nearbys.getJSONObject(i));
                    if (!has(item)) {
                        mNearbys.add(item);
                        addedCount++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return addedCount;
        }

        private boolean has(NearbyItem nearbyItem) {
            for (NearbyItem item: mNearbys) {
                if (nearbyItem.getUid().equals(item.getUid())) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_PADDING:
                    return createPendingHolder(parent);
                case TYPE_RECOMMEND:
                    return createRecommendHolder(parent);
                case TYPE_NEARBY:
                    return createNearbyHolder(parent);
            }
            return null;
        }

        private PendingHolder createPendingHolder(ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.friend_meet_item, parent, false);
            return new PendingHolder(itemView);
        }

        private RecommendHolder createRecommendHolder(ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.friend_meet_item, parent, false);
            return new RecommendHolder(itemView);
        }

        private NearbyHolder createNearbyHolder(ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.friend_near_item, parent, false);
            return new NearbyHolder(itemView);
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case TYPE_PADDING:
                case TYPE_RECOMMEND:
                    bindPendingRecommendHolder((BasePendingRecommendHolder) holder,
                            (PendingRecommendItem) getItem(position));
                    break;
                case TYPE_NEARBY:
                    bindNearbyHolder((NearbyHolder) holder, (NearbyItem) getItem(position));
                    break;
            }
        }

        private void bindPendingRecommendHolder(
                BasePendingRecommendHolder holder, PendingRecommendItem item) {
            bindBaseHolder(holder, item);
            holder.gotKoo.setCount(item.kooCount);
            holder.commonTopic.setCount(item.commonTopic);
            int commonFriendCount = item.commonFriend.size();
            holder.commonFriendLabel.setText(getString(R.string.common_friend, commonFriendCount));
            int i;
            for (i = 0; i < commonFriendCount && i < holder.commonFriendAvatars.length; i++) {
                holder.commonFriendAvatars[i].setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(
                        item.commonFriend.get(i).getAvatar(),
                        holder.commonFriendAvatars[i], ImageLoaderHelper.avatarLoadOptions);
            }
            for (; i < holder.commonFriendAvatars.length; i++) {
                holder.commonFriendAvatars[i].setVisibility(View.GONE);
            }
        }

        private void bindNearbyHolder(NearbyHolder holder, NearbyItem item) {
            bindBaseHolder(holder, item);
            holder.gotKoo.setCount(item.kooCount);
            holder.summaryText.setText(generateReadableDistanceString((float) item.distance));
        }

        private String generateReadableDistanceString(float distance) {
            if (distance < 100) {
                return mContext.getString(R.string.distance_less_than_100);
            }
            else if (distance < 1000) {
                return mContext.getString(R.string.distance_100_to_1000, (int) distance);
            }
            else {
                return mContext.getString(R.string.distance_more_than_1000, distance / 1000);
            }
        }

        private void bindBaseHolder(BaseHolder holder, BaseUserInfo item) {
            ImageLoader.getInstance().displayImage(item.getAvatar(), holder.avatar,
                    ImageLoaderHelper.avatarLoadOptions);
            holder.nameView.setUser(item);
        }

        @Override
        public int getCustomItemCount() {
            return mPendings.size() + mRecommends.size() + mNearbys.size();
        }

        @Override
        public int getCustomItemViewType(int position) {
            return Utils.getPositionType(position,
                    new int[]{TYPE_PADDING, TYPE_RECOMMEND, TYPE_NEARBY},
                    mPendings, mRecommends, mNearbys);
        }

        private BaseUserInfo getItem(int position) {
            return (BaseUserInfo) Utils.getItemFromLists(position, mPendings, mRecommends, mNearbys);
        }

        private void removeItem(int position) {
            Utils.removeItem(position, mPendings, mRecommends, mNearbys);
        }

        abstract class BaseHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                Response.Listener<JSONObject> {
            protected TextView explainLabel;
            protected Button removeButton;
            protected CircleImageView avatar;
            protected UserNameView nameView;
            protected Button acceptAddButton;
            protected BigCountView gotKoo;

            private Dialog progressDialog;

            public BaseHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                explainLabel = (TextView) itemView.findViewById(R.id.explain_label);
                removeButton = (Button) itemView.findViewById(R.id.btn_remove);
                removeButton.setOnClickListener(this);
                avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
                acceptAddButton = (Button) itemView.findViewById(R.id.btn_accept_add);
                acceptAddButton.setOnClickListener(this);
                nameView = (UserNameView) itemView.findViewById(R.id.name_view);
                gotKoo = (BigCountView) itemView.findViewById(R.id.got_koo);

                progressDialog = DialogUtil.getConnectingServerDialog(mContext);
            }

            @Override
            public void onClick(View v) {
                if (v == itemView) {
                    BaseUserInfo info = getItem(getAdapterPosition());
                    Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
                    intent.putExtra(FriendInfoActivity.KEY_UID, info.getUid());
                    intent.putExtra(FriendInfoActivity.KEY_AVATAR, info.getAvatar());
                    intent.putExtra(FriendInfoActivity.KEY_NICKNAME, info.getNickname());
                    startActivity(intent);
                }
                else if (v == removeButton) {
                    progressDialog.show();
                    onRemoveClick();
                }
                else if (v == acceptAddButton) {
                    progressDialog.show();
                    onAcceptAddClick();
                }
            }

            protected abstract void onRemoveClick();

            protected abstract void onAcceptAddClick();

            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    if (response.getInt("code") == 0) {
                        int position = getAdapterPosition();
                        removeItem(position);
                        notifyItemRemoved(position);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        abstract class BasePendingRecommendHolder extends BaseHolder {
            protected BigCountView commonTopic;
            protected TextView commonFriendLabel;
            protected LinearLayout commonFriendAvatarLayout;
            protected CircleImageView[] commonFriendAvatars;

            public BasePendingRecommendHolder(View itemView) {
                super(itemView);

                commonTopic = (BigCountView) itemView.findViewById(R.id.common_topic);
                commonFriendLabel = (TextView) itemView.findViewById(R.id.common_friend_label);
                commonFriendAvatarLayout =
                        (LinearLayout) itemView.findViewById(R.id.common_friend_avatar_layout);

                int maxCommonFriendAvatarCount = getMaxCommonFriendAvatarCount();
                commonFriendAvatars = new CircleImageView[maxCommonFriendAvatarCount];
                for (int i = 0; i < maxCommonFriendAvatarCount; i++) {
                    CircleImageView avatar = new CircleImageView(getActivity());
                    avatar.setBorderColorResource(R.color.avatar_gray_border);
                    avatar.setBorderWidth((int) Utils.dpToPixels(getActivity(), 2));
                    int avatarSize = getResources().getDimensionPixelSize(
                            R.dimen.friend_item_common_friend_avatar_size);
                    int avatarHalfInterval = getResources().getDimensionPixelOffset(
                            R.dimen.friend_item_common_friend_avatar_half_interval);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(avatarSize, avatarSize);
                    lp.setMargins(avatarHalfInterval, 0, avatarHalfInterval, 0);
                    avatar.setLayoutParams(lp);

                    commonFriendAvatars[i] = avatar;
                    commonFriendAvatarLayout.addView(avatar);
                }
            }
        }

        class PendingHolder extends BasePendingRecommendHolder {
            protected View paddingGreenBorder;

            public PendingHolder(View itemView) {
                super(itemView);

                paddingGreenBorder = itemView.findViewById(R.id.green_border_view);

                explainLabel.setText(R.string.padding_friend_label);
                avatar.setBorderColorResource(R.color.koolew_light_green);
                acceptAddButton.setBackground(getResources().
                        getDrawable(R.drawable.btn_bg_accept_solid));
                acceptAddButton.setText(R.string.follow);
                acceptAddButton.setTextColor(getResources().getColor(android.R.color.white));
                paddingGreenBorder.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onRemoveClick() {
                ApiWorker.getInstance().rejectPadding(
                        getItem(getAdapterPosition()).getUid(), this, null);
            }

            @Override
            protected void onAcceptAddClick() {
                ApiWorker.getInstance().agreeFriendAdd(
                        getItem(getAdapterPosition()).getUid(), this, null);
            }
        }

        class RecommendHolder extends BasePendingRecommendHolder {

            public RecommendHolder(View itemView) {
                super(itemView);

                explainLabel.setText(R.string.recommend_friend_label);
                avatar.setBorderColorResource(R.color.avatar_gray_border);
                acceptAddButton.setBackground(getResources().getDrawable(R.drawable.btn_bg_follow));
                acceptAddButton.setText(R.string.follow);
                acceptAddButton.setTextColor(getResources().getColor(R.color.koolew_light_green));
            }

            @Override
            protected void onRemoveClick() {
                ApiWorker.getInstance().ignoreRecommend(
                        getItem(getAdapterPosition()).getUid(), this, null);
            }

            @Override
            protected void onAcceptAddClick() {
                ApiWorker.getInstance().addFriend(
                        getItem(getAdapterPosition()).getUid(), this, null);
            }
        }

        class NearbyHolder extends BaseHolder {
            protected TextView summaryText;

            public NearbyHolder(View itemView) {
                super(itemView);

                removeButton.setVisibility(View.INVISIBLE);
                summaryText = (TextView) itemView.findViewById(R.id.friend_summary_label);
            }

            @Override
            protected void onRemoveClick() {
            }

            @Override
            protected void onAcceptAddClick() {
                ApiWorker.getInstance().addFriend(
                        getItem(getAdapterPosition()).getUid(), this, null);
            }
        }

        class PendingRecommendItem extends BaseUserInfo {

            private int kooCount;
            private int commonTopic;
            private List<BaseUserInfo> commonFriend;

            public PendingRecommendItem(JSONObject jsonObject) {
                super(jsonObject);

                try {
                    kooCount = jsonObject.getInt("koo_num");
                    JSONObject common = jsonObject.getJSONObject("common");
                    commonTopic = common.getInt("common_topic");
                    commonFriend = BaseUserInfo.fromJSONArray(common.getJSONArray("common_friend"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        class NearbyItem extends BaseUserInfo {

            private int kooCount;
            private double distance;

            public NearbyItem(JSONObject jsonObject) {
                super(jsonObject);

                try {
                    kooCount = jsonObject.getInt("koo_num");
                    distance = jsonObject.getDouble("distance");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


        private int getMaxCommonFriendAvatarCount() {
            int screenWidth = Utils.getScreenWidthPixel(getActivity());
            int itemBorder = getResources().getDimensionPixelSize(R.dimen.friend_item_border);
            int commonFriendAvatarSize = getResources().getDimensionPixelSize(
                    R.dimen.friend_item_common_friend_avatar_size);
            int commonFriendAvatarHalfInterval = getResources().getDimensionPixelSize(
                    R.dimen.friend_item_common_friend_avatar_half_interval);

            return (screenWidth - itemBorder * 2) /
                    (commonFriendAvatarSize + commonFriendAvatarHalfInterval * 2);
        }
    }
}
