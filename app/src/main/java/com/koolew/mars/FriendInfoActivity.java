package com.koolew.mars;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.koolew.mars.blur.DisplayBlurImage;
import com.koolew.mars.view.AvatarLinearContainer;
import com.koolew.mars.view.BigCountView;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendInfoActivity extends Activity implements View.OnClickListener {

    public static final String KEY_UID = "uid";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_NICKNAME = "nickname";

    private String mUid;

    private CircleImageView mAvatar;
    private ImageView mBlurAvatar;
    private TextView mNickname;
    private TextView mSummary;
    private ImageView mOperationImage;
    private TextView mOperationText;
    private ListView mListView;
    private TopicAdapter mAdapter;

    private BigCountView mKooCountView;
    private BigCountView mCommonTopicCountView;
    private TextView mCommonFriendTitle;
    private AvatarLinearContainer mAvatarContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);

        initViews();

        Intent intent = getIntent();
        mUid = intent.getStringExtra(KEY_UID);
        String avatar = intent.getStringExtra(KEY_AVATAR);
        if (avatar != null && !avatar.equals("")) {
            ImageLoader.getInstance().displayImage(avatar, mAvatar);
            new DisplayBlurImage(mBlurAvatar, avatar).execute();
        }
        mNickname.setText(intent.getStringExtra(KEY_NICKNAME));

        doRefresh();
    }

    private void initViews() {
        mAvatar = (CircleImageView) findViewById(R.id.avatar);
        mBlurAvatar = (ImageView) findViewById(R.id.blur_avatar);
        mNickname = (TextView) findViewById(R.id.nickname);
        mSummary = (TextView) findViewById(R.id.summary);
        mOperationImage = (ImageView) findViewById(R.id.operation_image);
        mOperationText = (TextView) findViewById(R.id.operation_text);
        mListView = (ListView) findViewById(R.id.list_view);

        View header = getLayoutInflater().inflate(R.layout.friend_info_list_header, null);
        mListView.addHeaderView(header);
        mKooCountView = (BigCountView) header.findViewById(R.id.count_koo);
        mCommonTopicCountView = (BigCountView) header.findViewById(R.id.count_common_topic);
        mCommonTopicCountView.setOnClickListener(this);
        mCommonFriendTitle = (TextView) header.findViewById(R.id.common_friend_title);
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

                JSONObject user = result.getJSONObject("user");
                mKooCountView.setCount(user.getInt("koo_num"));

                JSONArray topic = result.getJSONArray("topic");
                mAdapter = new FriendProfileTopicAdapter(FriendInfoActivity.this);
                mAdapter.setData(topic);
                mListView.setAdapter(mAdapter);

                JSONObject common = result.getJSONObject("common");
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.count_common_topic:
                onCommonTopicClick();
                break;
            case R.id.common_friend_title_layout:
                onCommonFriendClick();
                break;
        }
    }

    private void onCommonTopicClick() {
        Intent intent = new Intent(this, CommonTopicActivity.class);
        intent.putExtra(CommonTopicActivity.KEY_UID, mUid);
        startActivity(intent);
    }

    private void onCommonFriendClick() {
        Intent intent = new Intent(this, CommonFriendActivity.class);
        intent.putExtra(CommonTopicActivity.KEY_UID, mUid);
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
    }
}
