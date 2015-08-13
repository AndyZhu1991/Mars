package com.koolew.mars;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.TypedUserInfo;
import com.koolew.mars.utils.ContactUtil;
import com.koolew.mars.utils.DialogUtil;
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
 * Created by jinchangzhu on 6/29/15.
 */
public class FriendSimpleAdapter extends BaseAdapter {

    public static final int TYPE_UNKNOWN         = TypedUserInfo.TYPE_UNKNOWN;
    public static final int TYPE_SELF            = TypedUserInfo.TYPE_SELF;
    public static final int TYPE_STRANGER        = TypedUserInfo.TYPE_STRANGER;
    public static final int TYPE_SENT_INVITATION = TypedUserInfo.TYPE_SENT_INVITATION;
    public static final int TYPE_INVITED_ME      = TypedUserInfo.TYPE_INVITED_ME;
    public static final int TYPE_FRIEND          = TypedUserInfo.TYPE_FRIEND;
    public static final int TYPE_NO_REGISTER     = TypedUserInfo.TYPE_NO_REGISTER;

    private static final int[] OPERATE_BTN_BG = new int[] {
            0, // Not used
            R.drawable.btn_bg_add,
            R.drawable.btn_bg_waiting,
            R.drawable.btn_bg_accept,
            R.drawable.btn_bg_remove_no_border,
            R.drawable.btn_bg_invite,
    };
    private static final int[] OPERATE_BTN_COLOR = new int[] {
            0, // Not used
            0xFF6ED4E4,
            0xFF7D8B97,
            0xFF80DFA6,
            0xFF9EADB7,
            0xFF9EADB7,
    };
    private static final int[] OPERATE_BTN_TEXT_RES = new int[] {
            0, // Not used
            R.string.add,
            R.string.waiting_for_accept,
            R.string.accept,
            R.string.remove,
            R.string.invite,
    };


    protected Context mContext;
    protected List<FriendInfo> mData;
    protected ProgressDialog mProgressDialog;

    public FriendSimpleAdapter(Context context) {
        mContext = context;
        mData = new ArrayList<FriendInfo>();
        mProgressDialog = DialogUtil.getConnectingServerDialog(mContext);
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
        FriendInfo info = new FriendInfo();
        try {
            info.uid = jsonObject.getString("uid");
            info.nickname = jsonObject.getString("nickname");
            info.avatar = jsonObject.getString("avatar");
            info.phoneNumber = jsonObject.getString("phone");
            info.type = jsonObject.getInt("type");
            retrievalContactName(info);
            generateSummary(info);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mData.add(info);
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
        int itemType = getItemViewType(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.friend_item_simple, null);
            ViewHolder holder = new ViewHolder();
            holder.avatar = (CircleImageView) convertView.findViewById(R.id.avatar);
            holder.nickname = (TextView) convertView.findViewById(R.id.nickname);
            holder.summary = (TextView) convertView.findViewById(R.id.summary);
            holder.operateBtn = (Button) convertView.findViewById(R.id.operation_btn);
            holder.operateBtn.setOnClickListener(mOperateListener);
            convertView.setTag(holder);

            holder.operateBtn.setBackgroundResource(OPERATE_BTN_BG[itemType]);
            holder.operateBtn.setText(OPERATE_BTN_TEXT_RES[itemType]);
            holder.operateBtn.setTextColor(OPERATE_BTN_COLOR[itemType]);

            if (itemType == TYPE_SENT_INVITATION) {
                LinearLayout.LayoutParams lp =
                        (LinearLayout.LayoutParams) holder.operateBtn.getLayoutParams();
                lp.width = (int) Utils.dpToPixels(mContext, 70);
                holder.operateBtn.setLayoutParams(lp);
            }
            else if (itemType == TYPE_INVITED_ME) {
                int color = mContext.getResources().getColor(R.color.koolew_light_green);
                holder.avatar.setBorderColor(color);
                holder.nickname.setTextColor(color);
            }
            else if (itemType == TYPE_FRIEND) {
                holder.operateBtn.setTextSize(14);
            }
            else if (itemType == TYPE_NO_REGISTER) {
                holder.avatar.setImageResource(R.mipmap.default_avatar);
                holder.summary.setVisibility(View.GONE);
                holder.operateBtn.setTextSize(14);
            }
        }


        ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.operateBtn.setTag(position);

        if (itemType == TYPE_NO_REGISTER) {
            ContactUtil.SimpleContactInfo info = (ContactUtil.SimpleContactInfo) getItem(position);
            holder.nickname.setText(info.getName());
        }
        else {
            FriendInfo info = (FriendInfo) getItem(position);
            ImageLoader.getInstance().displayImage(info.avatar, holder.avatar,
                    ImageLoaderHelper.avatarLoadOptions);
            holder.nickname.setText(info.nickname);
            if (info.summary == null || info.summary.length() == 0) {
                holder.summary.setVisibility(View.GONE);
            }
            else {
                holder.summary.setText(info.summary);
            }
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).type;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    private View.OnClickListener mOperateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onOperate((Integer) v.getTag());
        }
    };

    // Override it in subclass
    protected void retrievalContactName(FriendInfo info) {
    }

    protected void onOperate(int position) {
        String uid = null;
        if (mData.size() > position) {
            uid = mData.get(position).uid;
        }
        switch (getItemViewType(position)) {
            case TYPE_SELF:
                break;
            case TYPE_STRANGER:
                requestForFriend(uid);
                break;
            case TYPE_SENT_INVITATION:
                break;
            case TYPE_INVITED_ME:
                agreeAddFriend(uid);
                break;
            case TYPE_FRIEND:
                removeFriend(uid);
                break;
            case TYPE_NO_REGISTER:
                break;
        }
    }

    protected void requestForFriend(String uid) {
        mProgressDialog.show();
        ApiWorker.getInstance().addFriend(uid, new RemoveResponseListener(uid), null);
    }

    protected void agreeAddFriend(String uid) {
        mProgressDialog.show();
        ApiWorker.getInstance().agreeFriendAdd(uid, new RemoveResponseListener(uid), null);
    }

    protected void removeFriend(final String uid) {
        new AlertDialog.Builder(mContext)
                .setMessage(R.string.delete_friend_confirm)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.show();
                        ApiWorker.getInstance().deleteFriend(uid, new RemoveResponseListener(uid), null);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    protected void inviteContact(String phoneNum) {
    }

    class RemoveResponseListener implements Response.Listener<JSONObject> {

        private String uid;

        public RemoveResponseListener(String uid) {
            this.uid = uid;
        }

        @Override
        public void onResponse(JSONObject response) {
            mProgressDialog.dismiss();
            int count = mData.size();
            for (int i = 0; i < count; i++) {
                if (mData.get(i).uid.equals(uid)) {
                    mData.remove(i);
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    protected void generateSummary(FriendInfo info) {
        if (info.type == TYPE_INVITED_ME) {
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
        else if (info.type == TYPE_STRANGER && info.contactName != null) {
            info.summary = mContext.getString(R.string.phone_contact_name, info.contactName);
        }
        else if (info.type == TYPE_SENT_INVITATION && info.contactName != null) {
            info.summary = mContext.getString(R.string.sent_invitation);
        }
    }

    public class FriendInfo {
        protected int type;
        protected String uid;
        protected String nickname;
        protected String avatar;
        protected String phoneNumber;
        protected String contactName;
        protected String summary;

        public String getUid() {
            return uid;
        }
    }

    protected class ViewHolder {
        protected CircleImageView avatar;
        protected TextView nickname;
        protected TextView summary;
        protected Button operateBtn;
    }
}
