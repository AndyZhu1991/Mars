package com.koolew.mars;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class CommonFriendActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    public static final String KEY_UID = "uid";

    private String mUid;

    private ListView mListView;
    private CommonFriendAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_friend);

        mUid = getIntent().getStringExtra(KEY_UID);

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);

        doLoad();
    }

    private void doLoad() {
        ApiWorker.getInstance().requestCommonFriend(mUid, mResponseListener, null);
    }

    private Response.Listener<JSONObject> mResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            try {
                if (jsonObject.getInt("code") == 0) {
                    JSONArray friends = jsonObject.getJSONObject("result").getJSONArray("friends");
                    mAdapter = new CommonFriendAdapter();
                    mAdapter.add(friends);
                    mListView.setAdapter(mAdapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, FriendInfoActivity.class);
        intent.putExtra(FriendInfoActivity.KEY_UID, mAdapter.mData.get(position).getUid());
        startActivity(intent);
    }


    class CommonFriendAdapter extends BaseAdapter {

        private List<BaseUserInfo> mData = new ArrayList<BaseUserInfo>();

        public void add(JSONArray jsonArray) {
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                try {
                    mData.add(new BaseUserInfo(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(CommonFriendActivity.this)
                        .inflate(R.layout.friend_item_simple, null);
                ViewHolder holder = new ViewHolder();
                holder.avatar = (CircleImageView) convertView.findViewById(R.id.avatar);
                holder.nameView = (UserNameView) convertView.findViewById(R.id.name_view);
                convertView.setTag(holder);

                TextView operateBtn = (TextView) convertView.findViewById(R.id.operation_btn);
                operateBtn.setText(R.string.title_friend);
                operateBtn.setTextSize(14);
                operateBtn.setTextColor(getResources().getColor(R.color.koolew_gray));
                operateBtn.setBackgroundResource(R.drawable.btn_bg_friend);

                convertView.findViewById(R.id.summary).setVisibility(View.GONE);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            BaseUserInfo info = (BaseUserInfo) getItem(position);
            ImageLoader.getInstance().displayImage(info.getAvatar(), holder.avatar,
                    ImageLoaderHelper.avatarLoadOptions);
            holder.nameView.setUser(info);

            return convertView;
        }

        class ViewHolder {
            CircleImageView avatar;
            UserNameView nameView;
        }
    }
}
