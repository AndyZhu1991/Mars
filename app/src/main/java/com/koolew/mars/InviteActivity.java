package com.koolew.mars;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class InviteActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener,
        TitleBarView.OnRightLayoutClickListener {

    public static final String KEY_TOPIC_ID = "topic_id";
    public static final String KEY_TITLE = "title";

    private String mTopicId;
    private String mTitle;

    private TextView mTitleView;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private InviteAdapter mAdapter;

    private ProgressDialog mConnectingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        Intent intent = getIntent();
        mTopicId = intent.getStringExtra(KEY_TOPIC_ID);
        mTitle = intent.getStringExtra(KEY_TITLE);

        initViews();

        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                refreshFriendList();
            }
        });
    }

    private void initViews() {
        ((TitleBarView) findViewById(R.id.title_bar)).setOnRightLayoutClickListener(this);

        mTitleView = (TextView) findViewById(R.id.title);
        mTitleView.setText(mTitle);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,calcInviteItemCountHorizontal()));
        mAdapter = new InviteAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mConnectingDialog = DialogUtil.getConnectingServerDialog(this);
    }

    private int calcInviteItemCountHorizontal() {
        int screenWidth = Utils.getScreenWidthPixel(this);
        int itemWidth = getResources().getDimensionPixelSize(R.dimen.invite_friend_item_width);
        return screenWidth / itemWidth;
    }

    private void refreshFriendList() {
        ApiWorker.getInstance().requestCurrentFriend(mRefreshListener, null);
    }

    private Response.Listener<JSONObject> mRefreshListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mRefreshLayout.setRefreshing(false);
            try {
                if (response.getInt("code") == 0) {
                    JSONArray friends = response.getJSONObject("result").getJSONArray("users");
                    mAdapter.setData(friends);
                    mAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.Listener<JSONObject> mInviteListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mConnectingDialog.dismiss();
            try {
                if (response.getInt("code") == 0) {
                    onBackPressed();
                }
                else {
                    Toast.makeText(InviteActivity.this,
                            R.string.connect_server_failed, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onRefresh() {
        refreshFriendList();
    }

    @Override
    public void onRightLayoutClick() {
        List<String> friendIdList = mAdapter.getSelectedFriendIdList();
        if (friendIdList.size() == 0) {
            Toast.makeText(this, R.string.please_invite_some_friends, Toast.LENGTH_SHORT).show();
            return;
        }
        ApiWorker.getInstance().sendInvitation(mTopicId, friendIdList, mInviteListener, null);
    }

    class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.ViewHolder> {

        private List<FriendItem> mData;
        private int selectedCount;

        public InviteAdapter() {
            mData = new ArrayList<>();
            selectedCount = 0;
        }

        public void setData(JSONArray friends) {
            mData.clear();
            int count = friends.length();
            for (int i = 0; i <count; i++) {
                try {
                    mData.add(new FriendItem(friends.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public List<String> getSelectedFriendIdList() {
            List<String> list = new LinkedList<>();
            for (FriendItem item: mData) {
                if (item.isSelected) {
                    list.add(item.getUid());
                }
            }

            return list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(InviteActivity.this)
                    .inflate(R.layout.invite_friend_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position == 0) {
                holder.mAvatar.setImageResource(R.mipmap.ic_select_all);
                holder.mCheckIndicator.setVisibility(View.INVISIBLE);
                if (selectedCount == mData.size()) {
                    holder.mNickname.setText(R.string.all_selected);
                }
                else {
                    holder.mNickname.setText(R.string.select_all);
                }
            }
            else {
                FriendItem friendItem = getItemAt(position);
                ImageLoader.getInstance().displayImage(friendItem.getAvatar(), holder.mAvatar,
                        ImageLoaderHelper.topicThumbLoadOptions);
                holder.mCheckIndicator.setVisibility(
                        friendItem.isSelected ? View.VISIBLE : View.INVISIBLE);
                holder.mNickname.setText(friendItem.getNickname());
            }
        }

        @Override
        public int getItemCount() {
            if (mData.size() == 0) {
                return 0;
            }
            else {
                return 1 + mData.size();
            }
        }

        private FriendItem getItemAt(int position) {
            return mData.get(position - 1);
        }

        class FriendItem extends BaseUserInfo {
            private boolean isSelected;

            public FriendItem(JSONObject jsonObject) {
                super(jsonObject);
                isSelected = false;
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private CircleImageView mAvatar;
            private ImageView mCheckIndicator;
            private TextView mNickname;

            public ViewHolder(View itemView) {
                super(itemView);

                mAvatar = (CircleImageView) itemView.findViewById(R.id.avatar);
                mCheckIndicator = (ImageView) itemView.findViewById(R.id.check_indicator);
                mNickname = (TextView) itemView.findViewById(R.id.nickname);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();

                if (position == 0) {
                    boolean itemOperation; // true: select , false: deselect
                    if (selectedCount == mData.size()) {
                        itemOperation = false;
                    }
                    else {
                        itemOperation = true;
                    }

                    for (FriendItem item: mData) {
                        item.isSelected = itemOperation;
                    }

                    selectedCount = itemOperation ? mData.size() : 0;

                    notifyDataSetChanged();
                }
                else {
                    FriendItem friendItem = getItemAt(position);
                    friendItem.isSelected = !friendItem.isSelected;
                    selectedCount += friendItem.isSelected ? 1 : -1;

                    notifyItemChanged(position);
                    if (selectedCount == mData.size() ||
                            (selectedCount == mData.size() - 1 && !friendItem.isSelected)) {
                        notifyItemChanged(0);
                    }
                }
            }
        }
    }
}
