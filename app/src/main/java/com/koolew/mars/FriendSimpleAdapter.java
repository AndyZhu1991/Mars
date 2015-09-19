package com.koolew.mars;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.TypedUserInfo;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.utils.ContactUtil;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.UserNameView;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.koolew.mars.infos.TypedUserInfo.TYPE_FRIEND;
import static com.koolew.mars.infos.TypedUserInfo.TYPE_FAN;
import static com.koolew.mars.infos.TypedUserInfo.TYPE_NO_REGISTER;
import static com.koolew.mars.infos.TypedUserInfo.TYPE_SELF;
import static com.koolew.mars.infos.TypedUserInfo.TYPE_FOLLOWED;
import static com.koolew.mars.infos.TypedUserInfo.TYPE_STRANGER;
import static com.koolew.mars.infos.TypedUserInfo.TYPE_UNKNOWN;

/**
 * Created by jinchangzhu on 6/29/15.
 */
public class FriendSimpleAdapter extends LoadMoreAdapter {

    private static final int[] OPERATE_BTN_BG = new int[] {
            0, // Not used
            R.drawable.btn_bg_follow,
            R.drawable.btn_bg_followed,
            R.drawable.btn_bg_follow,
            R.drawable.btn_bg_followed_each_other,
            R.drawable.btn_bg_invite,
    };
    private static final int[] OPERATE_BTN_COLOR = new int[] {
            0, // Not used
            0xFF80DFA6,
            0xFF6ED4E4,
            0xFF80DFA6,
            0xFF9EADB7,
            0xFF9EADB7,
    };
    private static final int[] OPERATE_BTN_TEXT_RES = new int[] {
            0, // Not used
            R.string.follow,
            R.string.followed,
            R.string.follow,
            R.string.followed_each_other,
            R.string.invite,
    };


    protected Context mContext;
    protected List<FriendInfo> mData;
    protected ProgressDialog mProgressDialog;

    public FriendSimpleAdapter(Context context) {
        mContext = context;
        mData = new ArrayList<>();
        mProgressDialog = DialogUtil.getConnectingServerDialog(mContext);
    }

    public void setData(JSONArray relations) {
        mData.clear();
        add(relations);
    }

    public void add(JSONArray relations) {
        try {
            int count = relations.length();
            for (int i = 0; i < count; i++) {
                JSONObject friend = (JSONObject) relations.get(i);
                int type;
                if (friend.has("type")) {
                    type = friend.getInt("type");
                }
                else {
                    type = TYPE_UNKNOWN;
                }
                if (friendTypeFilter(type)) {
                    add(friend);
                }
            }
        }
        catch (JSONException jse) {
            jse.printStackTrace();
        }
    }

    // Override it in subclass
    protected boolean friendTypeFilter(int type) {
        return true;
    }

    public void add(JSONObject jsonObject) {
        FriendInfo info = new FriendInfo(jsonObject);
        retrievalContactName(info);
        generateSummary(info);
        mData.add(info);
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        return new FriendSimpleViewHolder(LayoutInflater.from(mContext).
                inflate(R.layout.friend_item_simple, parent, false), viewType);
    }

    @Override
    public void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position) {
        FriendSimpleViewHolder vh = (FriendSimpleViewHolder) holder;

        if (vh.getItemViewType() == TYPE_NO_REGISTER) {
            ContactUtil.SimpleContactInfo info = (ContactUtil.SimpleContactInfo) getItem(position);
            vh.nameView.setUserInfo(info.getName(), BaseUserInfo.VIP_TYPE_NO_VIP);
        }
        else {
            FriendInfo info = (FriendInfo) getItem(position);
            ImageLoader.getInstance().displayImage(
                    info.getAvatar(), vh.avatar, ImageLoaderHelper.avatarLoadOptions);
            vh.nameView.setUser(info);
            if (info.summary == null || info.summary.length() == 0) {
                vh.summary.setVisibility(View.GONE);
            }
            else {
                vh.summary.setText(info.summary);
            }
        }
    }

    @Override
    public int getCustomItemCount() {
        return mData.size();
    }

    @Override
    public int getCustomItemViewType(int position) {
        return mData.get(position).getType();
    }

    // Override it in subclass
    protected void retrievalContactName(FriendInfo info) {
    }

    protected void onFriendClick(int position) {
        FriendInfoActivity.startThisActivity(mContext, mData.get(position).getUid());
    }

    protected void onOperate(int position) {
        String uid = null;
        if (mData.size() > position) {
            uid = mData.get(position).getUid();
        }
        switch (getItemViewType(position)) {
            case TYPE_SELF:
                break;
            case TYPE_STRANGER:
                followUser(uid);
                break;
            case TYPE_FOLLOWED:
                unfollowUser(uid);
                break;
            case TYPE_FAN:
                followUser(uid);
                break;
            case TYPE_FRIEND:
                unfollowUser(uid);
                break;
            case TYPE_NO_REGISTER:
                break;
        }
    }

    protected void followUser(String uid) {
        mProgressDialog.show();
        ApiWorker.getInstance().followUser(uid, new FollowResponseListener(uid), null);
    }

    protected void unfollowUser(final String uid) {
        new AlertDialog.Builder(mContext)
                .setMessage(R.string.unfollow_confirm)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.show();
                        ApiWorker.getInstance().unfollowUser(uid, new UnfollowResponseListener(uid), null);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    protected void inviteContact(String phoneNum) {
        Uri smsToUri = Uri.parse("smsto:" + phoneNum);
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", mContext.getString(R.string.sms_invitation_message,
                AppProperty.APP_DOWNLOAD_URL));
        mContext.startActivity(intent);
    }

    class UnfollowResponseListener extends FriendOpResponseListener {

        public UnfollowResponseListener(String uid) {
            super(uid);
        }

        @Override
        protected void onFriendOperated(int position) {
            int type = mData.get(position).getType();
            if (type == TYPE_FOLLOWED) {
                onFollowedUnfollow(position);
            }
            else if (type == TYPE_FRIEND) {
                onFriendUnfollow(position);
            }
        }
    }

    protected void onFollowedUnfollow(int position) {
        FriendInfo info = mData.get(position);
        info.setType(TYPE_STRANGER);
        notifyItemChanged(position);
    }

    protected void onFriendUnfollow(int position) {
        notifyItemRemoved(position);
    }

    class FollowResponseListener extends FriendOpResponseListener {

        public FollowResponseListener(String uid) {
            super(uid);
        }

        @Override
        protected void onFriendOperated(int position) {
            FriendInfo info = mData.get(position);
            int type = info.getType();
            if (type == TYPE_STRANGER) {
                info.setType(TYPE_FOLLOWED);
            }
            else if (type == TYPE_FAN) {
                info.setType(TYPE_FRIEND);
            }
            notifyItemChanged(position);
        }
    }

    abstract class FriendOpResponseListener implements Response.Listener<JSONObject> {
        protected String uid;

        public FriendOpResponseListener(String uid) {
            this.uid = uid;
        }

        @Override
        public void onResponse(JSONObject response) {
            mProgressDialog.dismiss();
            int count = mData.size();
            for (int i = 0; i < count; i++) {
                if (mData.get(i).getUid().equals(uid)) {
                    onFriendOperated(i);
                    break;
                }
            }
        }

        protected abstract void onFriendOperated(int position);
    }

    protected void generateSummary(FriendInfo info) {
        if (info.getType() == TYPE_FAN) {
            if (info.contactName != null) {
                info.summary = new StringBuilder()
                        .append(mContext.getString(R.string.phone_contact_name, info.contactName))
                        .append(mContext.getString(R.string.comma))
                        .append(mContext.getString(R.string.request_for_friend))
                        .toString();
            }
            else {
                info.summary = mContext.getString(R.string.request_for_friend);
            }
        }
        else if (info.getType() != TYPE_NO_REGISTER && info.contactName != null) {
            info.summary = mContext.getString(R.string.phone_contact_name, info.contactName);
        }
    }

    public class FriendInfo extends TypedUserInfo {
        protected String phoneNumber;
        protected String contactName;
        protected String summary;

        public FriendInfo(JSONObject jsonObject) {
            super(jsonObject);

            try {
                if (jsonObject.has("phone")) {
                    phoneNumber = jsonObject.getString("phone");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected class FriendSimpleViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        protected CircleImageView avatar;
        protected UserNameView nameView;
        protected TextView summary;
        protected TextView operateBtn;

        public FriendSimpleViewHolder(View itemView, int itemType) {
            super(itemView);
            itemView.setOnClickListener(this);

            avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            nameView = (UserNameView) itemView.findViewById(R.id.name_view);
            summary = (TextView) itemView.findViewById(R.id.summary);
            operateBtn = (TextView) itemView.findViewById(R.id.operation_btn);
            operateBtn.setOnClickListener(this);

            operateBtn.setBackgroundResource(OPERATE_BTN_BG[itemType]);
            operateBtn.setText(OPERATE_BTN_TEXT_RES[itemType]);
            operateBtn.setTextColor(OPERATE_BTN_COLOR[itemType]);

            if (itemType == TYPE_FAN) {
                int color = mContext.getResources().getColor(R.color.koolew_light_green);
                avatar.setBorderColor(color);
                nameView.setTextColor(color);
            }
            else if (itemType == TYPE_FRIEND) {
                LinearLayout.LayoutParams lp =
                        (LinearLayout.LayoutParams) operateBtn.getLayoutParams();
                lp.width = (int) Utils.dpToPixels(mContext, 70);
                operateBtn.setLayoutParams(lp);
            }
            else if (itemType == TYPE_NO_REGISTER) {
                avatar.setImageResource(R.mipmap.default_avatar);
                summary.setVisibility(View.GONE);
                operateBtn.setTextSize(14);
            }
        }

        @Override
        public void onClick(View v) {
            if (v == itemView) {
                onFriendClick(getAdapterPosition());
            }
            else if (v == operateBtn) {
                onOperate(getAdapterPosition());
            }
        }
    }
}
