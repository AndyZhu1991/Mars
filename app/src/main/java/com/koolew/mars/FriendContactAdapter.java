package com.koolew.mars;

import android.content.Context;

import com.koolew.mars.utils.ContactUtil;

import java.util.List;

/**
 * Created by jinchangzhu on 7/1/15.
 */
public class FriendContactAdapter extends FriendSimpleAdapter {

    protected List<ContactUtil.SimpleContactInfo> mAllContacts;

    public FriendContactAdapter(Context context, List<ContactUtil.SimpleContactInfo> allContacts) {
        super(context);
        mAllContacts = allContacts;
    }

    @Override
    public int getCount() {
        return super.getCount() + mAllContacts.size();
    }

    @Override
    public Object getItem(int position) {
        return position < mData.size() ? mData.get(position)
                : mAllContacts.get(position - mData.size());
    }

    @Override
    public int getItemViewType(int position) {
        return position < mData.size() ? mData.get(position).type : TYPE_NO_REGISTER;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1 /* TYPE_NO_REGISTER */;
    }

    @Override
    protected boolean friendTypeFilter(int type) {
        if (    type == FriendSimpleAdapter.TYPE_INVITED_ME ||
                type == FriendSimpleAdapter.TYPE_STRANGER ||
                type == FriendSimpleAdapter.TYPE_SENT_INVITATION) {
            return true;
        }

        return false;
    }

    protected void retrievalContactName(FriendInfo friendInfo) {
        if (mAllContacts == null || mAllContacts.size() == 0) {
            return;
        }
        for (ContactUtil.SimpleContactInfo contactInfo : mAllContacts) {
            if (contactInfo.getNumber().equals(friendInfo.phoneNumber)) {
                friendInfo.contactName = contactInfo.getName();
                //mAllContacts.remove(contactInfo);
                break;
            }
        }
    }
}
