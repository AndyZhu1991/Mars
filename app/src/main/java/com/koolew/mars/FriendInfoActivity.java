package com.koolew.mars;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.blur.DisplayBlurImageAndStatusBar;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.infos.TypedUserInfo;
import com.koolew.mars.player.ScrollPlayer;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.view.BigCountView;
import com.koolew.mars.view.LoadMoreFooter;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendInfoActivity extends BaseV4FragmentActivity {

    public static final String KEY_UID = "uid";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_NICKNAME = "nickname";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new FriendInfoFragment());
        fragmentTransaction.commit();
    }


    static class FriendProfileTopicAdapter extends TopicAdapter {

        FriendProfileTopicAdapter(Context context) {
            super(context);
        }

        @Override
        public TopicItem jsonObject2TopicItem(JSONObject jsonObject) {
            return new TopicItem(jsonObject);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View root = super.getView(position, convertView, parent);

            root.setBackgroundColor(0xFFF5F5F5);

            ((ViewHolder) root.getTag()).videoCount.setText(
                    mContext.getString(R.string.part_video_count,
                    ((TopicItem) getItem(position)).getVideoCount()));

            return root;
        }

        public String getTopicId(int position) {
            return mData.get(position).getTopicId();
        }
    }

    public static class FriendInfoFragment extends BaseListFragment
            implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener,
            LoadMoreFooter.OnLoadListener, View.OnClickListener {

        private ScrollPlayer mScrollPlayer;
        private FriendProfileTopicAdapter mAdapter;

        private BaseUserInfo mUserInfo;
        private String mUid;
        private int mType;
        private int mKooCount;

        private Dialog mProgressDialog;

        private CircleImageView mAvatar;
        private ImageView mBlurAvatar;
        private UserNameView mNameView;
        private TextView mSummary;
        private ImageView mOperationImage;
        private TextView mOperationText;
        private TextView mFansCountText;
        private TextView mFollowsCountText;

        private View mBottomLayout;
        private BigCountView mKooCountView;
        private BigCountView mCommonTopicCountView;


        public FriendInfoFragment() {
            isNeedLoadMore = true;
            mLayoutResId = R.layout.no_shader_refresh_list;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View root =  super.onCreateView(inflater, container, savedInstanceState);

            mAdapter = new FriendProfileTopicAdapter(getActivity());
            mScrollPlayer = mAdapter.new TopicScrollPlayer(mListView);
            mScrollPlayer.setNeedDanmaku(false);
            mScrollPlayer.setNeedSound(false);
            if (isNeedLoadMore) {
                mListFooter.setup(mListView, mScrollPlayer);
            }

            Intent intent = getActivity().getIntent();
            mUid = intent.getStringExtra(KEY_UID);

            initViews();

            mProgressDialog = DialogUtil.getConnectingServerDialog(getActivity());
            String avatar = intent.getStringExtra(KEY_AVATAR);
            if (avatar != null && !avatar.equals("")) {
                ImageLoader.getInstance().displayImage(avatar, mAvatar,
                        ImageLoaderHelper.avatarLoadOptions);
                new DisplayBlurImageAndStatusBar(getActivity(), mBlurAvatar, avatar).execute();
            }
            mNameView.setUserInfo(intent.getStringExtra(KEY_NICKNAME), BaseUserInfo.VIP_TYPE_NO_VIP);

            return root;
        }

        private void initViews() {
            View header = getActivity().getLayoutInflater().
                    inflate(R.layout.friend_info_list_header, null);

            mAvatar = (CircleImageView) header.findViewById(R.id.avatar);
            mBlurAvatar = (ImageView) header.findViewById(R.id.blur_avatar);
            mNameView = (UserNameView) header.findViewById(R.id.name_view);
            mSummary = (TextView) header.findViewById(R.id.summary);
            mOperationImage = (ImageView) header.findViewById(R.id.operation_image);
            mOperationImage.setOnClickListener(this);
            mOperationText = (TextView) header.findViewById(R.id.operation_text);
            mFansCountText = (TextView) header.findViewById(R.id.fans_count_text);
            mFansCountText.setOnClickListener(this);
            mFollowsCountText = (TextView) header.findViewById(R.id.follows_count_text);
            mFollowsCountText.setOnClickListener(this);
            mBottomLayout = header.findViewById(R.id.koo_common_topic_layout);
            if (mUid.equals(MyAccountInfo.getUid())) {
                mBottomLayout.setVisibility(View.GONE);
            }

            mListView.setOnItemClickListener(this);

            mListView.addHeaderView(header, null, false);
            mKooCountView = (BigCountView) header.findViewById(R.id.count_koo);
            mKooCountView.setOnClickListener(this);
            mCommonTopicCountView = (BigCountView) header.findViewById(R.id.count_common_topic);
            mCommonTopicCountView.setOnClickListener(this);
        }

        @Override
        protected void onPageEnd() {
            super.onPageEnd();
            mScrollPlayer.onActivityPause();
        }

        @Override
        protected void onPageStart() {
            super.onPageStart();
            mScrollPlayer.onActivityResume();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mScrollPlayer.onActivityDestroy();
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public int getThemeColor() {
            return getActivity().getResources().getColor(R.color.koolew_light_orange);
        }

        private void setupAdapter() {
            if (mListView.getAdapter() == null) {
                mListView.setAdapter(mAdapter);
            }
        }

        private void onOperationLayoutClick(int type) {
            switch (type) {
                case TypedUserInfo.TYPE_STRANGER:
                case TypedUserInfo.TYPE_FAN:
                    mProgressDialog.show();
                    ApiWorker.getInstance().followUser(mUid, mFriendOpListener, null);
                    break;
                case TypedUserInfo.TYPE_FOLLOWED:
                case TypedUserInfo.TYPE_FRIEND:
                    unfollowWithConfirm(mUid);
                    break;
                default:
            }
        }

        private void unfollowWithConfirm(final String uid) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.unfollow_confirm)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ApiWorker.getInstance().unfollowUser(uid, mFriendOpListener, null);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }

        private void initTypeView(int type) {
            switch (type) {
                case TypedUserInfo.TYPE_STRANGER:
                case TypedUserInfo.TYPE_FAN:
                    mOperationImage.setImageResource(R.mipmap.friend_info_follow);
                    mOperationText.setText(R.string.follow);
                    break;
                case TypedUserInfo.TYPE_FOLLOWED:
                    mOperationImage.setImageResource(R.mipmap.friend_info_followed);
                    mOperationText.setText(R.string.followed);
                    break;
                case TypedUserInfo.TYPE_FRIEND:
                    mOperationImage.setImageResource(R.mipmap.friend_info_followed_each_other);
                    mOperationText.setText(R.string.followed_each_other);
                    break;
                default:
                    mOperationImage.setVisibility(View.INVISIBLE);
                    mOperationText.setVisibility(View.INVISIBLE);
            }
        }

        private Response.Listener<JSONObject> mFriendOpListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mProgressDialog.dismiss();
                try {
                    if (response.getInt("code") == 0) {
                        switch (mType) {
                            case TypedUserInfo.TYPE_STRANGER:
                                mType = TypedUserInfo.TYPE_FOLLOWED;
                                break;
                            case TypedUserInfo.TYPE_FAN:
                                mType = TypedUserInfo.TYPE_FRIEND;
                                break;
                            case TypedUserInfo.TYPE_FOLLOWED:
                                mType = TypedUserInfo.TYPE_STRANGER;
                                break;
                            case TypedUserInfo.TYPE_FRIEND:
                                mType = TypedUserInfo.TYPE_FAN;
                                break;
                        }
                        initTypeView(mType);
                    }
                    else {
                        Toast.makeText(getActivity(), R.string.connect_server_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.count_koo:
                    onCountKooClick();
                    break;
                case R.id.count_common_topic:
                    onCommonTopicClick();
                    break;
                case R.id.operation_image:
                    onOperationLayoutClick(mType);
                    break;
                case R.id.fans_count_text:
                    onFansCountTextClick();
                    break;
                case R.id.follows_count_text:
                    onFollowsCountTextClick();
                    break;
            }
        }

        private void onCountKooClick() {
            Intent intent = new Intent(getActivity(), KooRankActivity.class);
            intent.putExtra(KooRankActivity.KEY_UID, mUid);
            intent.putExtra(KooRankActivity.KEY_KOO_COUNT, mKooCount);
            startActivity(intent);
        }

        private void onCommonTopicClick() {
            Intent intent = new Intent(getActivity(), CommonTopicActivity.class);
            intent.putExtra(CommonTopicActivity.KEY_UID, mUid);
            intent.putExtra(CommonTopicActivity.KEY_NICKNAME, mNameView.getNickname());
            startActivity(intent);
        }

        private void onFansCountTextClick() {
            Bundle extras = new Bundle();
            extras.putString(FansFragment.KEY_UID, mUid);
            TitleFragmentActivity.launchFragment(getActivity(), FansFragment.class, extras);
        }

        private void onFollowsCountTextClick() {
            Bundle extras = new Bundle();
            extras.putString(FollowsFragment.KEY_UID, mUid);
            TitleFragmentActivity.launchFragment(getActivity(), FollowsFragment.class, extras);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            position--; // Header
            if (position < 0) {
                return;
            }
            Intent intent = new Intent(getActivity(), UserTopicActivity.class);
            intent.putExtra(UserTopicActivity.KEY_TOPIC_ID, mAdapter.getTopicId(position));
            intent.putExtra(UserTopicActivity.KEY_UID, mUid);
            intent.putExtra(UserTopicActivity.KEY_NICKNAME, mNameView.getNickname());
            startActivity(intent);
        }

        @Override
        protected boolean handleRefresh(JSONObject response) {
            try {
                setupAdapter();
                JSONObject result = response.getJSONObject("result");

                mType = result.getInt("type");
                initTypeView(mType);

                JSONObject user = result.getJSONObject("user");
                mUserInfo = new BaseUserInfo(user);
                ImageLoader.getInstance().displayImage(mUserInfo.getAvatar(), mAvatar);
                new DisplayBlurImageAndStatusBar(getActivity(), mBlurAvatar, mUserInfo.getAvatar())
                        .execute();
                mNameView.setUser(mUserInfo);
                mFansCountText.setText(getString(R.string.fans_count, mUserInfo.getFansCount()));
                mFollowsCountText.setText(getString(
                        R.string.follows_count, mUserInfo.getFollowsCount()));

                if (!mUid.equals(MyAccountInfo.getUid())) {
                    mKooCount = user.getInt("koo_num");
                    mKooCountView.setCount(mKooCount);

                    JSONObject common = user.getJSONObject("common");
                    mCommonTopicCountView.setCount(common.getInt("common_topic"));
                }

                JSONArray topic;
                if (result.has("topic")) {
                    topic = result.getJSONArray("topic");
                }
                else {
                    topic = new JSONArray();
                }
                mAdapter.setCards(topic);
                mAdapter.notifyDataSetChanged();

                return topic.length() > 0;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected boolean handleLoadMore(JSONObject response) {
            try {
                JSONObject result = response.getJSONObject("result");
                JSONArray topic;
                if (result.has("topic")) {
                    topic = result.getJSONArray("topic");
                }
                else {
                    topic = new JSONArray();
                }
                mAdapter.addCards(topic);
                mAdapter.notifyDataSetChanged();

                return topic.length() > 0;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected JsonObjectRequest doRefreshRequest() {
            return ApiWorker.getInstance().requestFriendProfile(mUid, mRefreshListener, null);
        }

        @Override
        protected JsonObjectRequest doLoadMoreRequest() {
            return ApiWorker.getInstance().requestFriendProfile(mUid,
                    mAdapter.getOldestCardTime(), mLoadMoreListener, null);
        }

    }

    public static void startThisActivity(Context context, String uid) {
        Intent intent = new Intent(context, FriendInfoActivity.class);
        intent.putExtra(KEY_UID, uid);
        context.startActivity(intent);
    }
}
