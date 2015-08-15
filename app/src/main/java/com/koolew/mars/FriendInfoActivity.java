package com.koolew.mars;

import android.app.Activity;
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
import com.koolew.mars.blur.DisplayBlurImage;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.TypedUserInfo;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.view.AvatarLinearContainer;
import com.koolew.mars.view.BigCountView;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendInfoActivity extends Activity implements View.OnClickListener,
        AdapterView.OnItemClickListener {

    public static final String KEY_UID = "uid";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_NICKNAME = "nickname";

    private String mUid;
    private int mType;
    private int mKooCount;

    private Dialog mProgressDialog;

    private CircleImageView mAvatar;
    private ImageView mBlurAvatar;
    private TextView mNickname;
    private TextView mSummary;
    private ImageView mOperationImage;
    private TextView mOperationText;
    private ListView mListView;
    private FriendProfileTopicAdapter mAdapter;

    private BigCountView mKooCountView;
    private BigCountView mCommonTopicCountView;
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
            new DisplayBlurImage(mBlurAvatar, avatar).execute();
        }
        mNickname.setText(intent.getStringExtra(KEY_NICKNAME));

        doRefresh();
    }

    private void initViews() {
        View header = getLayoutInflater().inflate(R.layout.friend_info_list_header, null);

        mAvatar = (CircleImageView) header.findViewById(R.id.avatar);
        mBlurAvatar = (ImageView) header.findViewById(R.id.blur_avatar);
        mNickname = (TextView) header.findViewById(R.id.nickname);
        mSummary = (TextView) header.findViewById(R.id.summary);
        mOperationImage = (ImageView) header.findViewById(R.id.operation_image);
        mOperationImage.setOnClickListener(this);
        mOperationText = (TextView) header.findViewById(R.id.operation_text);

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);

        mListView.addHeaderView(header, null, false);
        mKooCountView = (BigCountView) header.findViewById(R.id.count_koo);
        mKooCountView.setOnClickListener(this);
        mCommonTopicCountView = (BigCountView) header.findViewById(R.id.count_common_topic);
        mCommonTopicCountView.setOnClickListener(this);
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
                String avatar = user.getString("avatar");
                ImageLoader.getInstance().displayImage(avatar, mAvatar);
                new DisplayBlurImage(mBlurAvatar, avatar).execute();
                mNickname.setText(user.getString("nickname"));

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
                mAdapter.setData(topic);
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
            case TypedUserInfo.TYPE_INVITED_ME:
                mOperationImage.setImageResource(R.mipmap.friend_info_add_friend);
                mOperationText.setText(R.string.add_friend);
                break;
            case TypedUserInfo.TYPE_SENT_INVITATION:
                mOperationImage.setImageResource(R.mipmap.friend_info_requested);
                mOperationText.setText(R.string.requested_friend);
                break;
            default:
                mOperationImage.setVisibility(View.INVISIBLE);
                mOperationText.setVisibility(View.INVISIBLE);
        }
    }

    private void onOperationLayoutClick(int type) {
        switch (type) {
            case TypedUserInfo.TYPE_STRANGER:
            case TypedUserInfo.TYPE_INVITED_ME:
                mProgressDialog.show();
                ApiWorker.getInstance().addFriend(mUid, mFriendOpListener, null);
                break;
            case TypedUserInfo.TYPE_SENT_INVITATION:
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
                            mType = TypedUserInfo.TYPE_SENT_INVITATION;
                            initTypeView(mType);
                            break;
                        case TypedUserInfo.TYPE_INVITED_ME:
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
        intent.putExtra(CommonTopicActivity.KEY_NICKNAME, mNickname.getText());
        startActivity(intent);
    }

    private void onCommonFriendClick() {
        Intent intent = new Intent(this, CommonFriendActivity.class);
        intent.putExtra(CommonTopicActivity.KEY_UID, mUid);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        position--; // Header
        if (position < 0) {
            return;
        }
        Intent intent = new Intent(this, FeedsTopicActivity.class);
        intent.putExtra(FeedsTopicActivity.KEY_TOPIC_ID, mAdapter.getTopicId(position));
        startActivity(intent);
    }

    class FriendProfileTopicAdapter extends TopicAdapter {

        FriendProfileTopicAdapter(Context context) {
            super(context);
        }

        @Override
        public TopicItem jsonObject2TopicItem(JSONObject jsonObject) {
            try {
                return new TopicItem(
                        jsonObject.getString("topic_id"),
                        jsonObject.getString("content"),
                        jsonObject.getString("thumb_url"),
                        jsonObject.getInt("video_cnt"),
                        0l);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View root = super.getView(position, convertView, parent);

            root.setBackgroundColor(0xFFF5F5F5);

            ((ViewHolder) root.getTag()).videoCount.setText(
                    getString(R.string.part_video_count, ((TopicItem) getItem(position)).videoCount));

            return root;
        }

        public String getTopicId(int position) {
            return mData.get(position).topicId;
        }
    }
}
