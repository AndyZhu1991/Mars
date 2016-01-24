package com.koolew.mars;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.redpoint.RedPointManager;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.view.BigCountView;
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
        return getResources().getColor(R.color.koolew_deep_blue);
    }

    @Override
    protected String getRefreshRequestUrl() {
        return UrlHelper.FRIEND_RECOMMEND_URL;
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getFriendRecommendUrl(mAdapter.getMaxNearbyDistance());
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        RedPointManager.clearRedPointByPath(RedPointManager.PATH_FRIENDS);
        JSONArray poiRecommends;
        try {
            poiRecommends = result.getJSONArray("poi_recommends");
        } catch (JSONException e) {
            handleJsonException(result, e);
            poiRecommends = new JSONArray();
        }
        mAdapter.setData(poiRecommends);
        mAdapter.notifyDataSetChanged();
        return poiRecommends.length() > 0;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        try {
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
            handleJsonException(result, e);
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
                        postLocationListener, postLocationErrorListener);
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

    private Response.Listener<JSONObject> postLocationListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            // TODO
        }
    };

    private Response.ErrorListener postLocationErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO
        }
    };


    private static final int TYPE_NEARBY = 0;

    class FriendRecommendAdapter extends LoadMoreAdapter {

        private Context mContext;

        private List<NearbyItem> mNearbys;

        public FriendRecommendAdapter() {
            mContext = getActivity();
            mNearbys = new ArrayList<>();
        }

        public void setData(JSONArray nearbys) {
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
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.friend_near_item, parent, false);
            return new NearbyHolder(itemView);
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            NearbyHolder holder = (NearbyHolder) viewHolder;
            NearbyItem item = getItem(position);
            ImageLoader.getInstance().displayImage(item.getAvatar(), holder.avatar,
                    ImageLoaderHelper.avatarLoadOptions);
            holder.nameView.setUser(item);
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

        @Override
        public int getCustomItemCount() {
            return mNearbys.size();
        }

        @Override
        public int getCustomItemViewType(int position) {
            return TYPE_NEARBY;
        }

        private NearbyItem getItem(int position) {
            return mNearbys.get(position);
        }

        private void removeItem(int position) {
            mNearbys.remove(position);
        }

        class NearbyHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                Response.Listener<JSONObject> {
            protected TextView explainLabel;
            protected CircleImageView avatar;
            protected UserNameView nameView;
            protected TextView summaryText;
            protected Button acceptAddButton;
            protected BigCountView gotKoo;

            private Dialog progressDialog;

            public NearbyHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                explainLabel = (TextView) itemView.findViewById(R.id.explain_label);
                avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
                acceptAddButton = (Button) itemView.findViewById(R.id.btn_accept_add);
                acceptAddButton.setOnClickListener(this);
                nameView = (UserNameView) itemView.findViewById(R.id.name_view);
                summaryText = (TextView) itemView.findViewById(R.id.friend_summary_label);
                gotKoo = (BigCountView) itemView.findViewById(R.id.got_koo);

                progressDialog = DialogUtil.getConnectingServerDialog(mContext);
            }

            @Override
            public void onClick(View v) {
                if (v == itemView) {
                    BaseUserInfo info = getItem(getAdapterPosition());
                    FriendInfoActivity.startThisActivity(getActivity(), info.getUid());
                }
                else if (v == acceptAddButton) {
                    progressDialog.show();
                    onAcceptAddClick();
                }
            }

            protected void onAcceptAddClick() {
                ApiWorker.getInstance().followUser(
                        getItem(getAdapterPosition()).getUid(), this, null);
            }

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
    }
}
