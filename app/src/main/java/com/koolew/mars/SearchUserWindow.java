package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jinchangzhu on 7/29/15.
 */
public class SearchUserWindow extends PopupWindow implements TextWatcher,
        Response.Listener<JSONObject>, View.OnClickListener {

    private Context mContext;

    private View mContentView;
    private EditText mEditText;
    private RecyclerView mRecyclerView;

    private UserAdapter mAdapter;

    private JsonObjectRequest mUserSearchRequest;

    public SearchUserWindow(Context context) {
        mContext = context;

        mContentView = LayoutInflater.from(mContext).inflate(R.layout.search_user_layout, null);
        mContentView.setOnClickListener(this);
        setContentView(mContentView);

        Resources resources = mContext.getResources();

        View editFrame = mContentView.findViewById(R.id.edit_frame);
        LinearLayout.LayoutParams eflp = (LinearLayout.LayoutParams) editFrame.getLayoutParams();
        eflp.topMargin = Utils.getStatusBarHeightPixel(mContext) +
                resources.getDimensionPixelSize(R.dimen.search_user_window_search_margin);
        eflp.bottomMargin = (int) (Utils.getActionBarHeightPixel(mContext)
                        + Utils.dpToPixels(mContext, 48) // PageIndicator height: 48dp
                        - resources.getDimensionPixelSize(R.dimen.search_user_window_search_margin)
                        - resources.getDimensionPixelSize(R.dimen.search_user_window_search_height));

        mEditText = (EditText) mContentView.findViewById(R.id.edit_text);
        mEditText.addTextChangedListener(this);

        mRecyclerView = (RecyclerView) mContentView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new UserAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setPadding(0, 0, 0, Utils.getNavigationBarHeightPixel(mContext));

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(0xB0000000));
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);

        Utils.showSoftKeyInput(mEditText, 200);
    }

    private void refreshUserSearch(String keyWord) {
        if (mUserSearchRequest != null) {
            mUserSearchRequest.cancel();
            mUserSearchRequest = null;
        }
        if (keyWord.length() > 0) {
            mUserSearchRequest = ApiWorker.getInstance().searchUser(keyWord, this, null);
        }
        else {
            mAdapter.setData(new JSONArray());
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            if (response.getInt("code") == 0) {
                JSONArray users = response.getJSONObject("result").getJSONArray("users");
                mAdapter.setData(users);
                mAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        refreshUserSearch(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }


    class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

        private List<BaseUserInfo> mData;

        public UserAdapter() {
            mData = new ArrayList<>();
        }

        public void setData(JSONArray jsonArray) {
            mData.clear();
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                try {
                    mData.add(new BaseUserInfo(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (count == 0) {
                mRecyclerView.setVisibility(View.INVISIBLE);
            }
            else {
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.friend_item_simple, parent, false);
            itemView.findViewById(R.id.summary).setVisibility(View.GONE);
            itemView.findViewById(R.id.operation_btn).setVisibility(View.GONE);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ImageLoader.getInstance().displayImage(mData.get(position).getAvatar(), holder.avatar);
            holder.nickname.setText(mData.get(position).getNickname());
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private CircleImageView avatar;
            private TextView nickname;

            public ViewHolder(View itemView) {
                super(itemView);

                avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
                nickname = (TextView) itemView.findViewById(R.id.nickname);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v == itemView) {
                    Intent intent = new Intent(mContext, FriendInfoActivity.class);
                    String uid = mData.get(getAdapterPosition()).getUid();
                    intent.putExtra(FriendInfoActivity.KEY_UID, uid);
                    mContext.startActivity(intent);
                }
            }
        }
    }
}
