package com.koolew.mars;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.blur.DisplayBlurImage;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.view.DrawerToggleView;
import com.koolew.mars.view.PhoneNumberView;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class MainActivity extends FragmentActivity
        implements MainBaseFragment.OnFragmentInteractionListener,
                   MainBaseFragment.ToolbarOperateInterface, View.OnClickListener{

    private static final String TAG = "koolew-MainActivity";

    public static final int REQUEST_CODE_CHANGE_INFO = 1;

    private DrawerLayout mDrawerLayout;
    private DrawerToggleView mToggleView;
    private View mMyToolbar;
    private TextView mTitleView;
    private FrameLayout mContentFrame;
    private LinearLayout mLeftDrawer;
    private ImageView mInfoBackground;
    private ListView mDrawerList;
    private DrawerListAdapter mAdapter;
    private ImageView mAvatar;
    private TextView mNickname;
    private PhoneNumberView mPhoneNumber;
    private TextView mCountKoo;
    private TextView mCountCoin;

    private MainBaseFragment[] fragments = new MainBaseFragment[3];
    private MainBaseFragment mCurFragment;

    private FrameLayout[] mTopIconLayouts = new FrameLayout[2];
    private ImageView[] mTopIcons = new ImageView[2];
    private View[] mTopNotifications = new View[2];

    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestQueue = Volley.newRequestQueue(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mMyToolbar = findViewById(R.id.my_toolbar);
        mTitleView = (TextView) findViewById(R.id.title);
        mToggleView = (DrawerToggleView) findViewById(R.id.my_drawer_toggle);
        mContentFrame = (FrameLayout) findViewById(R.id.content_frame);
        mLeftDrawer = (LinearLayout) findViewById(R.id.left_drawer);
        mInfoBackground = (ImageView) findViewById(R.id.info_background);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mAvatar.setOnClickListener(this);
        mNickname = (TextView) findViewById(R.id.nickname);
        mPhoneNumber = (PhoneNumberView) findViewById(R.id.phone_number);
        mCountKoo = (TextView) findViewById(R.id.count_koo);
        mCountCoin = (TextView) findViewById(R.id.count_coin);
        findViewById(R.id.coin_layout).setOnClickListener(this);
        findViewById(R.id.koo_layout).setOnClickListener(this);

        mAdapter = new DrawerListAdapter();
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 3) {
                    // TODO 添加话题卡
                } else {
                    switchFragment(position);
                }
            }
        });
        mNickname.setText(MyAccountInfo.getNickname());
        mPhoneNumber.setNumber(MyAccountInfo.getPhoneNumber());

        fragments[0] = KoolewFragment.newInstance();
        fragments[1] = FriendFragment.newInstance();
        fragments[2] = SettingsFragment.newInstance();
        mCurFragment = fragments[0];

        mTopIconLayouts[0]   = (FrameLayout) findViewById(R.id.top_icon_layout1);
        mTopIcons[0]         = (ImageView) findViewById(R.id.top_icon1);
        mTopNotifications[0] = findViewById(R.id.top_notification1);
        mTopIconLayouts[1]   = (FrameLayout) findViewById(R.id.top_icon_layout2);
        mTopIcons[1]         = (ImageView) findViewById(R.id.top_icon2);
        mTopNotifications[1] = findViewById(R.id.top_notification2);

        mTopIconLayouts[0].setOnClickListener(this);
        mTopIconLayouts[1].setOnClickListener(this);

        switchFragment(0);
        configureDrawer();

        getUserInfo();
    }

    private void configureDrawer() {
        // Configure drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerLayout.setDrawerListener(mToggleView);
        mToggleView.setDrawer(mDrawerLayout);
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
        else {
            super.onBackPressed();
        }
    }

    private void getUserInfo() {
        String url = UrlHelper.USER_INFO_URL;
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response -> " + response.toString());
                        try {
                            if (response.getInt("code") == 0) {
                                Log.d(TAG, "code == 0");
                                JSONObject user = response.getJSONObject("result").getJSONObject("user");
                                MyAccountInfo.setUid(user.getString("uid"));
                                MyAccountInfo.setPhoneNumber(user.getString("phone"));
                                MyAccountInfo.setCoinNum(user.getLong("coin_num"));
                                MyAccountInfo.setAvatar(user.getString("avatar"));
                                MyAccountInfo.setNickname(user.getString("nickname"));
                                MyAccountInfo.setKooNum(user.getLong("koo_num"));

                                mPhoneNumber.setNumber(MyAccountInfo.getPhoneNumber());
                                mCountCoin.setText("" + MyAccountInfo.getCoinNum());
                                ImageLoader.getInstance().displayImage(MyAccountInfo.getAvatar(), mAvatar);
                                new DisplayBlurImage(mInfoBackground, MyAccountInfo.getAvatar()).execute();
                                mNickname.setText(MyAccountInfo.getNickname());
                                mCountKoo.setText("" + MyAccountInfo.getKooNum());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage(), error);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return UrlHelper.getStandardPostHeaders();
            }
        };
        mRequestQueue.add(jsonRequest);
    }

    private void switchFragment(int position) {
        mAdapter.checkedPosition = position;
        mAdapter.notifyDataSetChanged();
        mCurFragment = fragments[position];
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, mCurFragment).commit();

        mDrawerLayout.closeDrawer(mLeftDrawer);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void setToolbarColor(int color) {
        mMyToolbar.setBackgroundColor(color);
    }

    @Override
    public void setToolbarTitle(String title) {
        mTitleView.setText(title);
    }

    @Override
    public void setToolbarTitle(int titleResId) {
        mTitleView.setText(titleResId);
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
    public void notifyTopIcon(int position) {
        mTopNotifications[position].setVisibility(View.VISIBLE);
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
                    mNickname.setText(MyAccountInfo.getNickname());
                    ImageLoader.getInstance().displayImage(MyAccountInfo.getAvatar(), mAvatar);
                    new DisplayBlurImage(mInfoBackground, MyAccountInfo.getAvatar()).execute();
                }
                break;
        }
    }

    class DrawerListAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        private int checkedPosition;

        private int[] listIcons = {
                R.mipmap.ic_drawer_list_koolew,
                R.mipmap.ic_drawer_list_friend,
                R.mipmap.ic_drawer_list_settings,
                R.mipmap.ic_drawer_list_add };
        private int[] listIconsSelected = {
                R.mipmap.ic_drawer_list_koolew_selected,
                R.mipmap.ic_drawer_list_friend_selected,
                R.mipmap.ic_drawer_list_settings_selected,
                R.mipmap.ic_drawer_list_add_selected };
        private int[] listTexts = {
                R.string.title_koolew,
                R.string.title_friend,
                R.string.title_settings,
                R.string.title_add };
        private int[] selectedColor = {
                R.color.drawer_list_koolew_select,
                R.color.drawer_list_friend_select,
                R.color.drawer_list_settings_select,
                R.color.drawer_list_add_select,
        };

        DrawerListAdapter() {
            inflater = LayoutInflater.from(MainActivity.this);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "getView position: " + position);
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.drawer_list_item, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.icon = (ImageView) view.findViewById(R.id.icon);
                holder.text = (TextView) view.findViewById(R.id.text);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            if (position == checkedPosition) {
                holder.icon.setImageDrawable(getResources().getDrawable(listIconsSelected[position]));
                holder.text.setTextColor(getResources().getColor(selectedColor[position]));
            }
            else {
                holder.icon.setImageDrawable(getResources().getDrawable(listIcons[position]));
                holder.text.setTextColor(getResources().getColor(R.color.koolew_gray));
            }
            holder.text.setText(listTexts[position]);

            return view;
        }
    }

    class ViewHolder {
        ImageView icon;
        TextView text;
    }
}
