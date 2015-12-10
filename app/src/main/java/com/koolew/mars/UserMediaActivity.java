package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;

/**
 * Created by jinchangzhu on 12/8/15.
 */
public class UserMediaActivity extends BaseV4FragmentActivity
        implements TitleBarView.OnRightLayoutClickListener,
        CommonMediaFragment.OnTopicInfoUpdateListener {

    public static final String KEY_TOPIC_ID = BaseTopicInfo.KEY_TOPIC_ID;
    public static final String KEY_UID = BaseUserInfo.KEY_UID;
    public static final String KEY_NICKNAME = BaseUserInfo.KEY_NICKNAME;

    protected TitleBarView mTitleBar;
    protected UserMediaFragment mFragment;

    protected String mTopicId;
    protected String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_topic);

        mTitleBar = (TitleBarView) findViewById(R.id.title_bar);
        mTitleBar.setOnRightLayoutClickListener(this);

        Intent intent = getIntent();
        mTopicId = intent.getStringExtra(KEY_TOPIC_ID);
        mUid = intent.getStringExtra(KEY_UID);
        if (mUid.equals(MyAccountInfo.getUid())) {
            mTitleBar.setTitle(R.string.koolew_involve_title);
            mTitleBar.setBackgroundColor(getResources().getColor(R.color.koolew_deep_orange));
        }
        else {
            String nickname = intent.getStringExtra(KEY_NICKNAME);
            if (!TextUtils.isEmpty(nickname)) {
                mTitleBar.setTitle("@" + nickname);
            }
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mFragment = new UserMediaFragment(mTopicId, mUid);
        fragmentTransaction.add(R.id.fragment_container, mFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onRightLayoutClick() {
        TopicMediaActivity.startThisActivity(this, mTopicId, TopicMediaActivity.TYPE_WORLD);
    }

    @Override
    public void onCategoryDetermined(BaseTopicInfo topicInfo) {
    }

    public static void startThisActivity(Context context, String topicId, String uid, String nickname) {
        Intent intent = new Intent(context, UserMediaActivity.class);
        intent.putExtra(KEY_TOPIC_ID, topicId);
        intent.putExtra(KEY_UID, uid);
        intent.putExtra(KEY_NICKNAME, nickname);
        context.startActivity(intent);
    }

    public static void startMyMediaActivity(Context context, String topicId) {
        startThisActivity(context, topicId, MyAccountInfo.getUid(), "");
    }


    public static class UserMediaFragment extends CommonMediaFragment<UserMediaAdapter> {

        private String mUid;

        public UserMediaFragment(String topicId, String uid) {
            super(topicId);
            isLazyLoad = false;
            isNeedLoadMore = true;

            mUid = uid;
        }

        @Override
        protected UserMediaAdapter useThisAdapter() {
            return new UserMediaAdapter(getActivity());
        }

        @Override
        protected int getThemeColor() {
            return getResources().getColor(R.color.koolew_light_blue);
        }

        @Override
        protected JsonObjectRequest doRefreshRequest() {
            return ApiWorker.getInstance().requestUserTopic(mUid, mTopicId, mRefreshListener, null);
        }

        @Override
        protected JsonObjectRequest doLoadMoreRequest() {
            return ApiWorker.getInstance().requestUserTopic(mUid, mTopicId,
                    mAdapter.getLastUpdateTime(), mLoadMoreListener, null);
        }
    }

    public static class UserMediaAdapter extends CommonMediaFragment.CommonMediaAdapter {

        public UserMediaAdapter(Context context) {
            super(context);
        }
    }
}
