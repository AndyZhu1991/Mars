package com.koolew.mars;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.utils.WebApiUtil;
import com.koolew.mars.view.DrawerToggleView;
import com.koolew.mars.view.PhoneNumberView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class MainActivity extends FragmentActivity
        implements MainBaseFragment.OnFragmentInteractionListener,
                   KoolewFragment.MainColorChangedListener{

    private static final String TAG = "koolew-MainActivity";

    private DrawerLayout mDrawerLayout;
    private DrawerToggleView mToggleView;
    private View mMyToolbar;
    private FrameLayout mContentFrame;
    private LinearLayout mLeftDrawer;
    private ListView mDrawerList;
    private DrawerListAdapter mAdapter;
    private ImageView mAvatar;
    private TextView mNickname;
    private PhoneNumberView mPhoneNumber;
    private TextView mCountKoo;
    private TextView mCountCoin;

    private Fragment[] fragments = new Fragment[4];

    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestQueue = Volley.newRequestQueue(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mMyToolbar = findViewById(R.id.my_toolbar);
        mToggleView = (DrawerToggleView) findViewById(R.id.my_drawer_toggle);
        mContentFrame = (FrameLayout) findViewById(R.id.content_frame);
        mLeftDrawer = (LinearLayout) findViewById(R.id.left_drawer);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mNickname = (TextView) findViewById(R.id.nickname);
        mPhoneNumber = (PhoneNumberView) findViewById(R.id.phone_number);
        mCountKoo = (TextView) findViewById(R.id.count_koo);
        mCountCoin = (TextView) findViewById(R.id.count_coin);

        mAdapter = new DrawerListAdapter();
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 4) {
                    // 添加话题卡
                    // Test
                    startActivity(new Intent(MainActivity.this, VideoShootActivity.class));
                } else {
                    switchFragment(position);
                }
            }
        });
        mNickname.setText(MyAccountInfo.getNickname());
        mPhoneNumber.setNumber(MyAccountInfo.getPhoneNumber());

        getUserInfo();

        fragments[0] = KoolewFragment.newInstance();
        fragments[1] = MessageFragment.newInstance();
        fragments[2] = FriendFragment.newInstance();
        fragments[3] = SettingsFragment.newInstance();

        switchFragment(0);
        configureDrawer();
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
        String url = WebApiUtil.USER_INFO_URL;
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
                return WebApiUtil.getStandardPostHeaders();
            }
        };
        mRequestQueue.add(jsonRequest);
    }

    private void switchFragment(int position) {
        mAdapter.checkedPosition = position;
        mAdapter.notifyDataSetChanged();
        Fragment fragment = fragments[position];
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment).commit();

        mDrawerLayout.closeDrawer(mLeftDrawer);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onMainColorChanged(int color) {
        mMyToolbar.setBackgroundColor(color);
    }

    class DrawerListAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        private int checkedPosition;

        private int[] listIcons = {
                R.mipmap.ic_drawer_list_koolew,
                R.mipmap.ic_drawer_list_msg,
                R.mipmap.ic_drawer_list_friend,
                R.mipmap.ic_drawer_list_settings,
                R.mipmap.ic_drawer_list_add };
        private int[] listIconsSelected = {
                R.mipmap.ic_drawer_list_koolew_selected,
                R.mipmap.ic_drawer_list_msg_selected,
                R.mipmap.ic_drawer_list_friend_selected,
                R.mipmap.ic_drawer_list_settings_selected,
                R.mipmap.ic_drawer_list_add_selected };
        private int[] listTexts = {
                R.string.drawer_item_koolew,
                R.string.drawer_item_msg,
                R.string.drawer_item_friend,
                R.string.drawer_item_settings,
                R.string.drawer_item_add };

        DrawerListAdapter() {
            inflater = LayoutInflater.from(MainActivity.this);
        }

        @Override
        public int getCount() {
            return 5;
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
                holder.text.setTextColor(getResources().getColor(R.color.koolew_light_orange));
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
