package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendMeetFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

    private ListView mListView;
    private FriendMeetAdapter mAdapter;

    private SwipeRefreshLayout mRefreshLayout;

    private JSONObject mResult;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment FriendMeetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendMeetFragment newInstance() {
        FriendMeetFragment fragment = new FriendMeetFragment();
        return fragment;
    }

    public FriendMeetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_friend_meet, container, false);

        mListView = (ListView) root.findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        mRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        mRefreshLayout.setColorSchemeResources(R.color.koolew_light_blue);
        mRefreshLayout.setOnRefreshListener(this);

        initListView();

        return root;
    }

    private void initListView() {
        // Only request data at first onCreateView
        if (mResult == null) {
            mRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mRefreshLayout.setRefreshing(true);
                    doRefresh();
                }
            });
        }
        else {
            setResultToListView();
        }
    }

    private void setResultToListView() {
        mAdapter = new FriendMeetAdapter();
        try {
            mAdapter.setData(mResult.getJSONArray("pendings"), mResult.getJSONArray("recommends"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    private void doRefresh() {
        ApiWorker.getInstance().requestRecommendFriend(new RefreshListener(), null);
    }

    class RefreshListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject jsonObject) {
            try {
                mResult = jsonObject.getJSONObject("result");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            setResultToListView();

            mRefreshLayout.setRefreshing(false);
        }
    }

    private View.OnClickListener mOnRemoveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

    private View.OnClickListener mOnAcceptListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

    private View.OnClickListener mOnAddListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("stdzhu", "item click");
        JSONObject jsonObject = (JSONObject) mAdapter.getItem(position);
        try {
            Intent intent = new Intent(getActivity(), FriendInfoActivity.class);
            intent.putExtra(FriendInfoActivity.KEY_UID, jsonObject.getString("uid"));
            intent.putExtra(FriendInfoActivity.KEY_AVATAR, jsonObject.getString("avatar"));
            intent.putExtra(FriendInfoActivity.KEY_NICKNAME, jsonObject.getString("nickname"));
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(jsonObject + " not contiant uid, avatar and nickname");
        }
    }


    private static final int TYPE_PADDING = 0;
    private static final int TYPE_RECOMMEND = 1;

    class FriendMeetAdapter extends BaseAdapter {

        private List<JSONObject> mPendings;
        private List<JSONObject> mRecommends;

        FriendMeetAdapter() {
            mPendings = new ArrayList<JSONObject>();
            mRecommends = new ArrayList<JSONObject>();
        }

        public void setData(JSONArray pendings, JSONArray recommends) throws JSONException {
            int paddingCount = pendings.length();
            for (int i = 0; i < paddingCount; i++) {
                mPendings.add((JSONObject) pendings.get(i));
            }

            int recommendCount = recommends.length();
            for (int i = 0; i < recommendCount; i++) {
                mRecommends.add((JSONObject) recommends.get(i));
            }
        }

        @Override
        public int getCount() {
            return mPendings.size() + mRecommends.size();
        }

        @Override
        public Object getItem(int position) {
            return position < mPendings.size() ? mPendings.get(position) :
                    mRecommends.get(position - mPendings.size());
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView ==  null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.friend_meet_item, null);

                ViewHolder holder = new ViewHolder();

                holder.explainLabel = (TextView) convertView.findViewById(R.id.explain_label);
                holder.removeButton = (Button) convertView.findViewById(R.id.btn_remove);
                holder.removeButton.setOnClickListener(mOnRemoveListener);
                holder.avatar = (CircleImageView) convertView.findViewById(R.id.avatar);
                holder.acceptAddButton = (Button) convertView.findViewById(R.id.btn_accept_add);
                holder.nickname = (TextView) convertView.findViewById(R.id.nickname);
                holder.kooCount = (TextView) convertView.findViewById(R.id.count_koo);
                holder.commonTopicCount = (TextView) convertView.findViewById(R.id.count_common_topic);
                holder.commonFriendLabel = (TextView) convertView.findViewById(R.id.common_friend_label);
                holder.commonFriendAvatarLayout =
                        (LinearLayout) convertView.findViewById(R.id.common_friend_avatar_layout);
                holder.paddingGreenBorder = convertView.findViewById(R.id.green_border_view);

                int maxCommonFriendAvatarCount = getMaxCommonFriendAvatarCount();
                holder.commonFriendAvatars = new CircleImageView[maxCommonFriendAvatarCount];
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

                    holder.commonFriendAvatars[i] = avatar;
                    holder.commonFriendAvatarLayout.addView(avatar);
                }

                if (getItemViewType(position) == TYPE_PADDING) {
                    holder.explainLabel.setText(R.string.padding_friend_label);
                    holder.avatar.setBorderColorResource(R.color.koolew_light_green);
                    holder.acceptAddButton.setBackground(getResources().
                            getDrawable(R.drawable.btn_bg_accept_solid));
                    holder.acceptAddButton.setText(R.string.accept);
                    holder.acceptAddButton.setTextColor(getResources().
                            getColor(android.R.color.white));
                    holder.acceptAddButton.setOnClickListener(mOnAcceptListener);
                    holder.paddingGreenBorder.setVisibility(View.VISIBLE);
                }
                else {
                    holder.explainLabel.setText(R.string.recommend_friend_label);
                    holder.avatar.setBorderColorResource(R.color.avatar_gray_border);
                    holder.acceptAddButton.setBackground(getResources().
                            getDrawable(R.drawable.btn_bg_add));
                    holder.acceptAddButton.setText(R.string.add);
                    holder.acceptAddButton.setTextColor(getResources().
                            getColor(R.color.koolew_light_blue));
                    holder.acceptAddButton.setOnClickListener(mOnAddListener);
                    holder.paddingGreenBorder.setVisibility(View.GONE);
                }

                convertView.setTag(holder);
            }

            JSONObject item = (JSONObject) getItem(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();
            try {
                ImageLoader.getInstance().displayImage(item.getString("avatar"), holder.avatar,
                        ImageLoaderHelper.avatarLoadOptions);
                holder.nickname.setText(item.getString("nickname"));
                holder.kooCount.setText(String.valueOf(item.getLong("koo_num")));
                holder.commonTopicCount.setText(String.valueOf(
                        item.getJSONObject("common").getLong("common_topic")));
                JSONArray commonFriend = item.getJSONObject("common").getJSONArray("common_friend");
                int commonFriendCount = commonFriend.length();
                holder.commonFriendLabel.setText(getString(R.string.common_friend, commonFriendCount));
                int i;
                for (i = 0; i < commonFriendCount && i < holder.commonFriendAvatars.length; i++) {
                    holder.commonFriendAvatars[i].setVisibility(View.VISIBLE);
                    ImageLoader.getInstance().displayImage(
                            ((JSONObject) commonFriend.get(i)).getString("avatar"),
                            holder.commonFriendAvatars[i], ImageLoaderHelper.avatarLoadOptions);
                }
                for (; i < holder.commonFriendAvatars.length; i++) {
                    holder.commonFriendAvatars[i].setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            return position < mPendings.size() ? TYPE_PADDING : TYPE_RECOMMEND;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
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

    class ViewHolder {
        TextView explainLabel;
        Button removeButton;
        CircleImageView avatar;
        TextView nickname;
        Button acceptAddButton;
        TextView kooCount;
        TextView commonTopicCount;
        TextView commonFriendLabel;
        LinearLayout commonFriendAvatarLayout;
        CircleImageView[] commonFriendAvatars;
        View paddingGreenBorder;
    }
}
