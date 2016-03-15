package com.koolew.mars;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.blur.DisplayBlurImageAndPalette;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.infos.TypedUserInfo;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.android.utils.Utils;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 12/12/15.
 */
public class FriendInfoActivity extends BaseV4FragmentActivity implements View.OnClickListener,
        AppBarLayout.OnOffsetChangedListener, ViewTreeObserver.OnGlobalLayoutListener {

    public static final String KEY_UID = BaseUserInfo.KEY_UID;

    private String mUid;
    private BaseUserInfo mUserInfo;
    private int mType;

    private UserInvolveFragment mFragment;

    private TitleBarView mTitleBar;
    private ImageView mBlurAvatar;
    private ImageView mAvatar;
    private UserNameView mUserName;
    private TextView mOperationView;

    private TextView mSupportCount;
    private TextView mFansCount;
    private TextView mFollowingCount;

    private AppBarLayout mAppBar;
    private View mTitleNameLayout;

    private int mThemeColor = Color.BLACK;

    private int titleBarCenterX;
    private int titleBarCenterY;
    private int nameViewLeft;
    private int nameViewTop;

    private int collapsbleHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        mUid = getIntent().getStringExtra(KEY_UID);

        ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar)).setStatusBarScrimColor(0);

        mAppBar = (AppBarLayout) findViewById(R.id.appbar);
        mAppBar.addOnOffsetChangedListener(this);
        mAppBar.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mTitleNameLayout = findViewById(R.id.title_name_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarHeight = Utils.getStatusBarHeight();
            mAppBar.getLayoutParams().height += statusBarHeight;
            findViewById(R.id.user_info_layout).setPadding(0, statusBarHeight, 0, 0);
            mTitleNameLayout.setPadding(0, statusBarHeight, 0, 0);
        }

        mTitleBar = (TitleBarView) findViewById(R.id.title_bar);
        mBlurAvatar = (ImageView) findViewById(R.id.blur_avatar);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mAvatar.setOnClickListener(this);
        mUserName = (UserNameView) findViewById(R.id.user_name);
        mOperationView = (TextView) findViewById(R.id.operation_btn);
        mOperationView.setOnClickListener(this);
        mSupportCount = (TextView) findViewById(R.id.support_count);
        mSupportCount.setOnClickListener(this);
        mFansCount = (TextView) findViewById(R.id.fans_count);
        mFansCount.setOnClickListener(this);
        mFollowingCount = (TextView) findViewById(R.id.following_count);
        mFollowingCount.setOnClickListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mFragment = new UserInvolveFragment();
        fragmentTransaction.add(R.id.fragment_container, mFragment);
        fragmentTransaction.commit();
    }

    private JsonObjectRequest mUserInfoRequest;
    private void refreshUserInfo() {
        if (mUserInfoRequest == null) {
            mAppBar.getViewTreeObserver().addOnGlobalLayoutListener(this);
            mUserInfoRequest = ApiWorker.getInstance().queueGetRequest(
                    UrlHelper.getFriendProfileUrl(mUid), userInfoListener, userInfoErrorListener);
        }
    }

    private Response.Listener<JSONObject> userInfoListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mUserInfoRequest = null;
            try {
                if (response.getInt("code") == 0) {
                    JSONObject result = response.getJSONObject("result");
                    mType = result.getInt("type");
                    initTypeView(mType);

                    JSONObject user = result.getJSONObject("user");
                    mUserInfo = new BaseUserInfo(user);
                    mFragment.setupUserInfo();

                    ImageLoader.getInstance().displayImage(mUserInfo.getAvatar(), mAvatar);
                    new DisplayBlurImageAndPalette(mBlurAvatar, mUserInfo.getAvatar()) {
                        @Override
                        protected void onPalette(Palette palette) {
                            mThemeColor = palette.getDarkVibrantColor(palette.getMutedColor(Color.BLACK));
                            mFragment.setupThemeColor();
                        }
                    }.execute();
                    mUserName.setUser(mUserInfo);
                    mSupportCount.setText(getString(R.string.support_count, mUserInfo.getKooCount()));
                    mFansCount.setText(getString(R.string.fans_count, mUserInfo.getFansCount()));
                    mFollowingCount.setText(getString(
                            R.string.follows_count, mUserInfo.getFollowsCount()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.ErrorListener userInfoErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO
        }
    };

    private void initTypeView(int type) {
        switch (type) {
            case TypedUserInfo.TYPE_STRANGER:
            case TypedUserInfo.TYPE_FAN:
                Utils.setTextViewDrawableTop(mOperationView, R.mipmap.friend_info_follow);
                mOperationView.setText(R.string.follow);
                break;
            case TypedUserInfo.TYPE_FOLLOWED:
                Utils.setTextViewDrawableTop(mOperationView, R.mipmap.friend_info_followed);
                mOperationView.setText(R.string.followed);
                break;
            case TypedUserInfo.TYPE_FRIEND:
                Utils.setTextViewDrawableTop(mOperationView, R.mipmap.friend_info_followed_each_other);
                mOperationView.setText(R.string.followed_each_other);
                break;
            default:
                mOperationView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        float progress = 1.0f * -i / collapsbleHeight;
        mTitleBar.setBackgroundColor(Color.argb((int) (255 * progress), Color.red(mThemeColor),
                Color.green(mThemeColor), Color.blue(mThemeColor)));

        float diffX = titleBarCenterX - (nameViewLeft + mUserName.getWidth() / 2);
        float diffY = titleBarCenterY - (nameViewTop + mUserName.getHeight() / 2);
        mUserName.setX(nameViewLeft + diffX * progress);
        mUserName.setY(nameViewTop + diffY * progress);
    }

    @Override
    public void onGlobalLayout() {
        mAppBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        titleBarCenterX = (mTitleBar.getLeft() + mTitleBar.getRight()) / 2;
        titleBarCenterY = (mTitleBar.getTop() + mTitleBar.getBottom()) / 2;
        nameViewLeft = mUserName.getLeft();
        nameViewTop = mUserName.getTop();
        collapsbleHeight = mTitleNameLayout.getHeight() - mTitleBar.getBottom();
    }

    @Override
    public void onClick(View v) {
        if (mUserName == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.operation_btn:
                onOperationLayoutClick(mType);
                break;
            case R.id.support_count:
                onCountKooClick();
                break;
            case R.id.fans_count:
                onFansCountTextClick();
                break;
            case R.id.following_count:
                onFollowsCountTextClick();
                break;
        }
    }

    private void onCountKooClick() {
        Intent intent = new Intent(this, KooRankActivity.class);
        intent.putExtra(KooRankActivity.KEY_UID, mUid);
        intent.putExtra(KooRankActivity.KEY_KOO_COUNT, mUserInfo.getKooCount());
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

    private void onOperationLayoutClick(int type) {
        switch (type) {
            case TypedUserInfo.TYPE_STRANGER:
            case TypedUserInfo.TYPE_FAN:
                ApiWorker.getInstance().followUser(mUid, mFriendOpListener, mFriendOpListener);
                break;
            case TypedUserInfo.TYPE_FOLLOWED:
            case TypedUserInfo.TYPE_FRIEND:
                unfollowWithConfirm(mUid);
                break;
            default:
        }
    }

    private void unfollowWithConfirm(final String uid) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.unfollow_confirm)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ApiWorker.getInstance().unfollowUser(uid, mFriendOpListener, mFriendOpListener);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private FriendOpListener mFriendOpListener = new FriendOpListener();

    class FriendOpListener implements Response.Listener<JSONObject>, Response.ErrorListener {
        @Override
        public void onResponse(JSONObject response) {
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
                    Toast.makeText(FriendInfoActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(FriendInfoActivity.this, R.string.connect_server_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void startThisActivity(Context context, String uid) {
        Intent intent = new Intent(context, FriendInfoActivity.class);
        intent.putExtra(KEY_UID, uid);
        context.startActivity(intent);
    }


    public static class UserTitleBar extends TitleBarView {
        public UserTitleBar(Context context) {
            super(context);
        }

        public UserTitleBar(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void setStatusBarColor(int color) {
            if (getContext() instanceof Activity) {
                Utils.setStatusBarColorBurnWithAlpha((Activity) getContext(), color);
            }
        }
    }

    public class UserInvolveFragment extends RecyclerListFragmentMould<TimelineAdapter> {

        public UserInvolveFragment() {
            super();
            isNeedLoadMore = true;
        }

        @Override
        protected TimelineAdapter useThisAdapter() {
            TimelineAdapter adapter = new TimelineAdapter(getActivity());
            // 这TM也会NP，逗我的吧？
            if (mUid.equals(MyAccountInfo.getUid())) {
                adapter.setIsSelf();
            }
            return adapter;
        }

        public void setupUserInfo() {
            mAdapter.setUserInfo(mUserInfo);
        }

        @Override
        protected int getThemeColor() {
            return mThemeColor;
        }

        public void setupThemeColor() {
            mRefreshLayout.setColorSchemeColors(mThemeColor);
        }

        @Override
        protected String getRefreshRequestUrl() {
            return UrlHelper.getUserTimelineUrl(mUid);
        }

        @Override
        protected JsonObjectRequest doRefreshRequest() {
            refreshUserInfo();
            return super.doRefreshRequest();
        }

        @Override
        protected String getLoadMoreRequestUrl() {
            return UrlHelper.getUserTimelineUrl(mUid, mAdapter.getLastUpdateTime());
        }

        @Override
        protected boolean handleRefreshResult(JSONObject result) {
            return mAdapter.setItems(getInvolveTopics(result)) > 0;
        }

        @Override
        protected boolean handleLoadMoreResult(JSONObject result) {
            return mAdapter.addItems(getInvolveTopics(result)) > 0;
        }

        @Override
        protected int getNoDataViewResId() {
            return R.layout.timeline_no_data;
        }

        private JSONArray getInvolveTopics(JSONObject result) {
            try {
                return result.getJSONArray("topics");
            } catch (JSONException e) {
                handleJsonException(result, e);
            }
            return new JSONArray();
        }
    }
}
