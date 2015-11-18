package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.koolew.mars.blur.DisplayBlurImage;
import com.koolew.mars.blur.DisplayBlurImageAndPalette;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.preference.PreferenceHelper;
import com.koolew.mars.redpoint.RedPointManager;
import com.koolew.mars.redpoint.RedPointView;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.update.Updater;
import com.koolew.mars.utils.ColorUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.DrawerToggleView;
import com.koolew.mars.view.PhoneNumberView;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends BaseV4FragmentActivity
        implements MainBaseFragment.OnFragmentInteractionListener,
        MainBaseFragment.ToolbarOperateInterface, View.OnClickListener,
        DrawerLayout.DrawerListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "koolew-MainActivity";

    public static final String KEY_PUSH_URI = "push_uri";

    public static final int REQUEST_CODE_CHANGE_INFO = 1;

    private DrawerLayout mDrawerLayout;
    private DrawerToggleView mToggleView;
    private RedPointView mToggleRedPoint;
    private View mMyToolbar;
    private int mTitleBarColor;
    private int mAvatarPaletteColor;
    private String mTitle;
    private TextView mTitleView;
    private TextView mMiddleTitle;
    private FrameLayout mContentFrame;
    private SwipeRefreshLayout mLeftDrawer;
    private ImageView mInfoBackground;
    private RecyclerView mDrawerRecycler;
    private DrawerAdapter mAdapter;
    private ImageView mAvatar;
    private UserNameView mNameView;
    private PhoneNumberView mPhoneNumber;
    private TextView mFansCountText;
    private TextView mFollowsCountText;
    private TextView mCountKoo;
    private TextView mCountCoin;
    private TextView mIncomeNum;

    private MainBaseFragment mCurFragment;

    private FrameLayout[] mTopIconLayouts = new FrameLayout[2];
    private ImageView[] mTopIcons = new ImageView[2];
    private RedPointView[] mTopRedPoints = new RedPointView[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);
        mMyToolbar = findViewById(R.id.my_toolbar);
        mTitleView = (TextView) findViewById(R.id.title);
        if (!TextUtils.isEmpty(mTitle)) {
            mTitleView.setText(mTitle);
        }
        mMiddleTitle = (TextView) findViewById(R.id.middle_title);
        mToggleView = (DrawerToggleView) findViewById(R.id.my_drawer_toggle);
        mToggleView.setDrawer(mDrawerLayout);
        mToggleRedPoint = (RedPointView) findViewById(R.id.toggle_red_point);
        mToggleRedPoint.registerPath(RedPointManager.PATH_DRAWER_TOGGLE);
        mContentFrame = (FrameLayout) findViewById(R.id.content_frame);
        mLeftDrawer = (SwipeRefreshLayout) findViewById(R.id.left_drawer);
        mLeftDrawer.setColorSchemeColors(Color.WHITE);
        mLeftDrawer.setOnRefreshListener(this);
        mInfoBackground = (ImageView) findViewById(R.id.info_background);
        mDrawerRecycler = (RecyclerView) findViewById(R.id.drawer_recycler);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mAvatar.setOnClickListener(this);
        mNameView = (UserNameView) findViewById(R.id.name_view);
        mPhoneNumber = (PhoneNumberView) findViewById(R.id.phone_number);
        mFansCountText = (TextView) findViewById(R.id.fans_count_text);
        mFansCountText.setOnClickListener(this);
        mFollowsCountText = (TextView) findViewById(R.id.follows_count_text);
        mFollowsCountText.setOnClickListener(this);
        mCountKoo = (TextView) findViewById(R.id.count_koo);
        mCountCoin = (TextView) findViewById(R.id.count_coin);
        mIncomeNum = (TextView) findViewById(R.id.income_text);
        findViewById(R.id.coin_layout).setOnClickListener(this);
        findViewById(R.id.koo_layout).setOnClickListener(this);
        findViewById(R.id.income_layout).setOnClickListener(this);

        mAdapter = new DrawerAdapter();
        mDrawerRecycler.setLayoutManager(new LinearLayoutManager(this));
        mDrawerRecycler.setAdapter(mAdapter);
        mNameView.setUserInfo(MyAccountInfo.getNickname(), MyAccountInfo.getVip());
        mPhoneNumber.setNumber(MyAccountInfo.getPhoneNumber());

        switchFragment(0);

        mTopIconLayouts[0]   = (FrameLayout) findViewById(R.id.top_icon_layout1);
        mTopIcons[0]         = (ImageView) findViewById(R.id.top_icon1);
        mTopRedPoints[0]     = (RedPointView) findViewById(R.id.top_red_point1);
        mTopIconLayouts[1]   = (FrameLayout) findViewById(R.id.top_icon_layout2);
        mTopIcons[1]         = (ImageView) findViewById(R.id.top_icon2);
        mTopRedPoints[1]     = (RedPointView) findViewById(R.id.top_red_point2);

        mTopIconLayouts[0].setOnClickListener(this);
        mTopIconLayouts[1].setOnClickListener(this);

        switchFragment(0);

        mLeftDrawer.post(new Runnable() {
            @Override
            public void run() {
                doRequestSelfInfo();
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String pushUri = bundle.getString(KEY_PUSH_URI);
            if (!TextUtils.isEmpty(pushUri)) {
                new UriProcessor(this).process(pushUri);
            }
        }

        RedPointManager.refreshRedPoint();

        Updater updater = Updater.newInstance(this);
        if (updater != null) {
            updater.checkUpdateAutomatic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncLocalMyInfo();
    }

    private void syncLocalMyInfo() {
        ImageLoader.getInstance().displayImage(MyAccountInfo.getAvatar(), mAvatar);
        mNameView.setUserInfo(MyAccountInfo.getNickname(), MyAccountInfo.getVip());
        mFansCountText.setText(getString(R.string.fans_count, MyAccountInfo.getFansCount()));
        mFollowsCountText.setText(getString(R.string.follows_count, MyAccountInfo.getFollowsCount()));
        mCountKoo.setText(String.valueOf(MyAccountInfo.getKooNum()));
        long localCoinCount = MyAccountInfo.getCoinNum();
        mCountCoin.setText(String.valueOf(localCoinCount >= 0 ? localCoinCount : 0));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else if(mAdapter.checkedPosition != 0) {
            switchFragment(0);
        }
        else {
            super.onBackPressed();
        }
    }

    class SelfInfoListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            mLeftDrawer.setRefreshing(false);
            try {
                if (response.getInt("code") == 0) {
                    Log.d(TAG, "code == 0");
                    JSONObject user = response.getJSONObject("result").getJSONObject("user");
                    MyAccountInfo.setUid(user.getString("uid"));
                    MyAccountInfo.setPhoneNumber(user.getString("phone"));
                    MyAccountInfo.setCoinNum(user.getLong("coin_num"));
                    MyAccountInfo.setAvatar(user.getString("avatar"));
                    MyAccountInfo.setNickname(user.getString("nickname"));
                    MyAccountInfo.setVip(user.getInt("vip"));
                    MyAccountInfo.setKooNum(user.getLong("koo_num"));
                    MyAccountInfo.setFansCount(user.getInt("fans"));
                    MyAccountInfo.setFollowsCount(user.getInt("follows"));
                    mIncomeNum.setText(getString(R.string.today_income_renminbi,
                            user.getDouble("today_profit")));
                    new PreferenceHelper(MainActivity.this).setPushBit(user.getInt("push_bit"));

                    mPhoneNumber.setNumber(MyAccountInfo.getPhoneNumber());
                    mCountCoin.setText(String.valueOf(MyAccountInfo.getCoinNum()));
                    ImageLoader.getInstance().displayImage(MyAccountInfo.getAvatar(), mAvatar);
                    new DisplayBlurImageAndPalette(mInfoBackground, MyAccountInfo.getAvatar()) {
                        @Override
                        protected void onPalette(Palette palette) {
                            mAvatarPaletteColor = Utils.getStatusBarColorFromPalette(palette);
                            mLeftDrawer.setColorSchemeColors(mAvatarPaletteColor);
                        }
                    }.execute();
                    mNameView.setUserInfo(MyAccountInfo.getNickname(), MyAccountInfo.getVip());
                    mCountKoo.setText(String.valueOf(MyAccountInfo.getKooNum()));
                    mFansCountText.setText(getString(R.string.fans_count, MyAccountInfo.getFansCount()));
                    mFollowsCountText.setText(getString(R.string.follows_count, MyAccountInfo.getFollowsCount()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        }
    }

    private void doRequestSelfInfo() {
        mLeftDrawer.setRefreshing(true);
        ApiWorker.getInstance().requestSelfInfo(new SelfInfoListener(), null);
    }

    private void switchFragment(int position) {
        MainBaseFragment fragment = mAdapter.drawerItems[position].getFragment();
        if (fragment != null) {
            mAdapter.checkedPosition = position;
            mAdapter.notifyDataSetChanged();
            mCurFragment = fragment;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, mCurFragment).commit();
        }

        mDrawerLayout.closeDrawer(mLeftDrawer);
    }

    private void onItemClick(int position) {
        switchFragment(position);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void setToolbarColor(int color) {
        mTitleBarColor = color;
        Utils.setStatusBarColorBurn(this, color);
        mMyToolbar.setBackgroundColor(color);
    }

    @Override
    public void setToolbarTitle(String title) {
        mTitle = title;
        if (!TextUtils.isEmpty(title) && mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    @Override
    public void setToolbarMiddleTitle(String middleTitle) {
        if (mMiddleTitle != null) {
            mMiddleTitle.setText(middleTitle);
        }
    }

    @Override
    public void setTopIconCount(int count) {
        for (int i = 0; i < mTopIconLayouts.length; i++) {
            if (i < count) {
                mTopIconLayouts[i].setVisibility(View.VISIBLE);
            }
            else {
                mTopIconLayouts[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void setTopIconImageResource(int position, int resource) {
        mTopIcons[position].setImageResource(resource);
    }

    @Override
    public void setTopRedPointPath(int position, String path) {
        mTopRedPoints[position].registerPath(path);
    }

    @Override
    public void unregisterAllTopRedPoint() {
        if (mTopRedPoints[0] != null) {
            mTopRedPoints[0].unregisterPath();
        }
        if (mTopRedPoints[1] != null) {
            mTopRedPoints[1].unregisterPath();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatar:
                Intent intent = new Intent(MainActivity.this, ChangeInfoActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CHANGE_INFO);
                break;
            case R.id.coin_layout:
                startCoinRuleActivity();
                break;
            case R.id.koo_layout:
                startKooRankActivity();
                break;
            case R.id.top_icon_layout1:
                mCurFragment.onTopIconClick(0);
                break;
            case R.id.top_icon_layout2:
                mCurFragment.onTopIconClick(1);
                break;
            case R.id.fans_count_text:
                TitleFragmentActivity.launchFragment(this, FansFragment.class);
                break;
            case R.id.follows_count_text:
                TitleFragmentActivity.launchFragment(this, FollowsFragment.class);
                break;
            case R.id.income_layout:
                startActivity(new Intent(this, TodayIncomeActivity.class));
                break;
        }
    }

    private void startCoinRuleActivity() {
        Intent intent = new Intent(this, CoinRuleActivity.class);
        startActivity(intent);
    }

    private void startKooRankActivity() {
        Intent intent = new Intent(this, KooRankActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CHANGE_INFO:
                if (resultCode == RESULT_OK) {
                    mPhoneNumber.setNumber(MyAccountInfo.getPhoneNumber());
                    mNameView.setUserInfo(MyAccountInfo.getNickname(), MyAccountInfo.getVip());
                    ImageLoader.getInstance().displayImage(MyAccountInfo.getAvatar(), mAvatar);
                    new DisplayBlurImage(mInfoBackground, MyAccountInfo.getAvatar()).execute();
                }
                break;
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        Utils.setStatusBarColorBurn(this, ColorUtil.getTransitionColor
                (mTitleBarColor, mAvatarPaletteColor, slideOffset));
    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }

    @Override
    public void onDrawerClosed(View drawerView) {
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }

    @Override
    public void onRefresh() {
        doRequestSelfInfo();
    }


    private class UriProcessor extends com.koolew.mars.utils.UriProcessor {
        public UriProcessor(Context context) {
            super(context);
        }

        @Override
        protected void switchToTab(String tabId) {
            if (tabId.equals(TAB_FEEDS)) {
            }
            else if (tabId.equals(TAB_SUGGESTION)) {
                switchFragment(1);
            }
            else {
                super.switchToTab(tabId);
            }
        }
    }


    private static final int UNSELECT_COLOR_RES = R.color.koolew_gray;
    class DrawerItem {
        private int icon;
        private int iconSelected;
        private int titleRes;
        private int selectedColorRes;
        private Class fragmentClass;

        public DrawerItem(int icon, int iconSelected, int titleRes, int selectedColorRes,
                          Class fragmentClass) {
            this.icon = icon;
            this.iconSelected = iconSelected;
            this.titleRes = titleRes;
            this.selectedColorRes = selectedColorRes;
            this.fragmentClass = fragmentClass;
        }

        public MainBaseFragment getFragment() {
            try {
                return (MainBaseFragment) fragmentClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class DrawerHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView icon;
        private TextView title;
        private RedPointView redPoint;

        public DrawerHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            icon = (ImageView) itemView.findViewById(R.id.icon);
            title = (TextView) itemView.findViewById(R.id.text);
            redPoint = (RedPointView) itemView
                    .findViewById(R.id.red_point);
        }

        @Override
        public void onClick(View v) {
            onItemClick(getAdapterPosition());
        }
    }

    class DrawerAdapter extends RecyclerView.Adapter<DrawerHolder> {
        private int checkedPosition;

        private DrawerItem[] drawerItems = new DrawerItem[] {
                new DrawerItem(R.mipmap.ic_drawer_list_koolew,
                        R.mipmap.ic_drawer_list_koolew_selected,
                        R.string.title_koolew,
                        R.color.drawer_list_koolew_select,
                        KoolewFragment.class),

                new DrawerItem(R.mipmap.ic_drawer_list_play,
                        R.mipmap.ic_drawer_list_play_selected,
                        R.string.title_play,
                        R.color.drawer_list_play_select,
                        PlayFragment.class),

                new DrawerItem(R.mipmap.ic_drawer_list_message,
                        R.mipmap.ic_drawer_list_message_selected,
                        R.string.title_message,
                        R.color.drawer_list_message_select,
                        null) {
                    @Override
                    public MainBaseFragment getFragment() {
                        Intent intent = new Intent(MainActivity.this, MessagesActivity.class);
                        startActivity(intent);
                        return null;
                    }
                },

                new DrawerItem(R.mipmap.ic_drawer_list_friend,
                        R.mipmap.ic_drawer_list_friend_selected,
                        R.string.title_friend,
                        R.color.drawer_list_friend_select,
                        FriendFragment.class),

                new DrawerItem(R.mipmap.ic_drawer_list_settings,
                        R.mipmap.ic_drawer_list_settings_selected,
                        R.string.title_settings,
                        R.color.drawer_list_settings_select,
                        SettingsFragment.class),
        };

        @Override
        public DrawerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DrawerHolder(LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.drawer_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(DrawerHolder holder, int position) {
            if (position == checkedPosition) {
                holder.icon.setImageDrawable(getResources()
                        .getDrawable(drawerItems[position].iconSelected));
                holder.title.setTextColor(getResources()
                        .getColor(drawerItems[position].selectedColorRes));
            }
            else {
                holder.icon.setImageDrawable(getResources().getDrawable(drawerItems[position].icon));
                holder.title.setTextColor(getResources().getColor(UNSELECT_COLOR_RES));
            }
            holder.title.setText(drawerItems[position].titleRes);
            if (position == 2) { // Message
                holder.redPoint.registerPath(RedPointManager.PATH_MESSAGE);
            }
            if (position == 3) { // Friends
                holder.redPoint.registerPath(RedPointManager.PATH_FRIENDS);
            }
        }

        @Override
        public int getItemCount() {
            return drawerItems.length;
        }
    }
}
