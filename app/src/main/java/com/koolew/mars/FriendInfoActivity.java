package com.koolew.mars;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.koolew.mars.blur.DisplayBlurImageAndStatusBar;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.infos.TypedUserInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.view.AvatarLinearContainer;
import com.koolew.mars.view.BigCountView;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendInfoActivity extends BaseActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener {

    public static final String KEY_UID = "uid";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_NICKNAME = "nickname";

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
    private ListView mListView;
    private FriendProfileTopicAdapter mAdapter;

    private View mKooCommonTopicLayout;
    private BigCountView mKooCountView;
    private BigCountView mCommonTopicCountView;
    private View mCommonFriendLayout;
    private TextView mCommonFriendTitle;
    private TextView mJoinedTopicTitle;
    private AvatarLinearContainer mAvatarContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);

        initViews();

        mProgressDialog = DialogUtil.getConnectingServerDialog(this);

        Intent intent = getIntent();
        mUid = intent.getStringExtra(KEY_UID);
        String avatar = intent.getStringExtra(KEY_AVATAR);
        if (avatar != null && !avatar.equals("")) {
            ImageLoader.getInstance().displayImage(avatar, mAvatar,
                    ImageLoaderHelper.avatarLoadOptions);
            new DisplayBlurImageAndStatusBar(this, mBlurAvatar, avatar).execute();
        }
        mNameView.setUserInfo(intent.getStringExtra(KEY_NICKNAME), BaseUserInfo.VIP_TYPE_NO_VIP);
        if (mUid.equals(MyAccountInfo.getUid())) {
            mKooCommonTopicLayout.setVisibility(View.GONE);
            mCommonFriendLayout.setVisibility(View.GONE);
            mJoinedTopicTitle.setText(R.string.joined_topic);
        }

        doRefresh();
    }

    private void initViews() {
        View header = getLayoutInflater().inflate(R.layout.friend_info_list_header, null);

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

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);

        mListView.addHeaderView(header, null, false);
        mKooCommonTopicLayout = header.findViewById(R.id.koo_common_topic_layout);
        mKooCountView = (BigCountView) header.findViewById(R.id.count_koo);
        mKooCountView.setOnClickListener(this);
        mCommonTopicCountView = (BigCountView) header.findViewById(R.id.count_common_topic);
        mCommonTopicCountView.setOnClickListener(this);
        mCommonFriendLayout = header.findViewById(R.id.common_friend_layout);
        mCommonFriendTitle = (TextView) header.findViewById(R.id.common_friend_title);
        mJoinedTopicTitle = (TextView) header.findViewById(R.id.joined_topic_title);
        mAvatarContainer = (AvatarLinearContainer) header.findViewById(R.id.avatar_container);

        findViewById(R.id.common_friend_title_layout).setOnClickListener(this);
    }

    private void doRefresh() {
        ApiWorker.getInstance().requestFriendProfile(mUid, mResponseListener, null);
    }

    private Response.Listener<JSONObject> mResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            try {
                JSONObject result = jsonObject.getJSONObject("result");

                mType = result.getInt("type");
                initTypeView(mType);

                JSONObject user = result.getJSONObject("user");
                mUserInfo = new BaseUserInfo(user);
                ImageLoader.getInstance().displayImage(mUserInfo.getAvatar(), mAvatar);
                new DisplayBlurImageAndStatusBar(FriendInfoActivity.this, mBlurAvatar,
                        mUserInfo.getAvatar()).execute();
                mNameView.setUser(mUserInfo);
                mFansCountText.setText(getString(R.string.fans_count, mUserInfo.getFansCount()));
                mFollowsCountText.setText(getString(
                        R.string.follows_count, mUserInfo.getFollowsCount()));

                mKooCount = user.getInt("koo_num");
                mKooCountView.setCount(mKooCount);

                JSONArray topic;
                if (result.has("topic")) {
                    topic = result.getJSONArray("topic");
                }
                else {
                    topic = new JSONArray();
                }
                if (topic.length() == 0) {
                    mJoinedTopicTitle.setText(R.string.he_or_she_no_topic_joined);
                }
                mAdapter = new FriendProfileTopicAdapter(FriendInfoActivity.this);
                mAdapter.setCards(topic);
                mListView.setAdapter(mAdapter);

                JSONObject common = user.getJSONObject("common");
                mCommonTopicCountView.setCount(common.getInt("common_topic"));
                JSONArray commonFriend = common.getJSONArray("common_friend");
                int commonFriendCount = commonFriend.length();
                mCommonFriendTitle.setText(getString(R.string.common_friend, commonFriendCount));
                for (int i = 0; i < commonFriendCount; i++) {
                    mAvatarContainer.addAvatar(
                            new AvatarLinearContainer.PersonInfo(commonFriend.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void initTypeView(int type) {
        switch (type) {
            case TypedUserInfo.TYPE_STRANGER:
            case TypedUserInfo.TYPE_FAN:
                mOperationImage.setImageResource(R.mipmap.friend_info_add_friend);
                mOperationText.setText(R.string.follow);
                break;
            case TypedUserInfo.TYPE_FOLLOWED:
                mOperationImage.setImageResource(R.mipmap.friend_info_requested);
                mOperationText.setText(R.string.followed);
                break;
            default:
                mOperationImage.setVisibility(View.INVISIBLE);
                mOperationText.setVisibility(View.INVISIBLE);
        }
    }

    private void onOperationLayoutClick(int type) {
        switch (type) {
            case TypedUserInfo.TYPE_STRANGER:
            case TypedUserInfo.TYPE_FAN:
                mProgressDialog.show();
                ApiWorker.getInstance().addFriend(mUid, mFriendOpListener, null);
                break;
            case TypedUserInfo.TYPE_FOLLOWED:
                break;
            default:
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
                            initTypeView(mType);
                            break;
                        case TypedUserInfo.TYPE_FAN:
                            mType = TypedUserInfo.TYPE_FRIEND;
                            initTypeView(mType);
                            break;
                    }
                }
                else {
                    Toast.makeText(FriendInfoActivity.this,
                            R.string.connect_server_failed, Toast.LENGTH_SHORT).show();
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
            case R.id.common_friend_title_layout:
                onCommonFriendClick();
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
        Intent intent = new Intent(this, KooRankActivity.class);
        intent.putExtra(KooRankActivity.KEY_UID, mUid);
        intent.putExtra(KooRankActivity.KEY_KOO_COUNT, mKooCount);
        startActivity(intent);
    }

    private void onCommonTopicClick() {
        Intent intent = new Intent(this, CommonTopicActivity.class);
        intent.putExtra(CommonTopicActivity.KEY_UID, mUid);
        intent.putExtra(CommonTopicActivity.KEY_NICKNAME, mNameView.getNickname());
        startActivity(intent);
    }

    private void onCommonFriendClick() {
        Intent intent = new Intent(this, CommonFriendActivity.class);
        intent.putExtra(CommonTopicActivity.KEY_UID, mUid);
        startActivity(intent);
    }

    private void onFansCountTextClick() {
        Bundle extras = new Bundle();
        extras.putString(FansFragment.KEY_UID, mUid);
        TitleFragmentActivity.launchFragment(this, FansFragment.class, extras);
    }

    private void onFollowsCountTextClick() {
        Bundle extras = new Bundle();
        extras.putString(FollowsFragment.KEY_UID, mUid);
        TitleFragmentActivity.launchFragment(this, FollowsFragment.class, extras);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        position--; // Header
        if (position < 0) {
            return;
        }
        Intent intent = new Intent(this, UserTopicActivity.class);
        intent.putExtra(UserTopicActivity.KEY_TOPIC_ID, mAdapter.getTopicId(position));
        intent.putExtra(UserTopicActivity.KEY_UID, mUid);
        intent.putExtra(UserTopicActivity.KEY_NICKNAME, mNameView.getNickname());
        startActivity(intent);
    }

    class FriendProfileTopicAdapter extends TopicAdapter {

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

            ((ViewHolder) root.getTag()).videoCount.setText(getString(R.string.part_video_count,
                    ((TopicItem) getItem(position)).getVideoCount()));

            return root;
        }

        public String getTopicId(int position) {
            return mData.get(position).getTopicId();
        }
    }

    public static void startThisActivity(Context context, String uid) {
        Intent intent = new Intent(context, FriendInfoActivity.class);
        intent.putExtra(KEY_UID, uid);
        context.startActivity(intent);
    }
}
