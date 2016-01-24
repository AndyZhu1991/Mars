package com.koolew.mars;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.utils.ContactUtil;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.koolew.mars.infos.TypedUserInfo.TYPE_FAN;
import static com.koolew.mars.infos.TypedUserInfo.TYPE_FOLLOWED;
import static com.koolew.mars.infos.TypedUserInfo.TYPE_NO_REGISTER;
import static com.koolew.mars.infos.TypedUserInfo.TYPE_STRANGER;


public class FriendContactFragment
        extends RecyclerListFragmentMould<FriendContactFragment.FriendContactAdapter> {

    private static final String TAG = "koolew-FriendContactF";

    private List<ContactUtil.SimpleContactInfo> mContacts;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment FriendContactFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendContactFragment newInstance() {
        FriendContactFragment fragment = new FriendContactFragment();
        return fragment;
    }

    public FriendContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected FriendContactAdapter useThisAdapter() {
        return new FriendContactAdapter(getActivity());
    }

    @Override
    protected int getThemeColor() {
        return getActivity().getResources().getColor(R.color.koolew_deep_blue);
    }

    @Override
    protected String getRefreshRequestUrl() {
        return null;
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        if (mContacts == null || mContacts.size() == 0) {
            new GetContactsTask().execute();
            return null;
        }
        else {
            return ApiWorker.getInstance().requestContactFriend(
                    mContacts, mRefreshListener, mRefreshErrorListener);
        }
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return null;
    }

    @Override
    protected boolean handleRefreshResult(JSONObject result) {
        if (getActivity() == null) {
            return false;
        }

        try {
            JSONArray relations = result.getJSONArray("relations");
            mAdapter.setData(relations);
            mAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            handleJsonException(result, e);
        }
        return false;
    }

    @Override
    protected boolean handleLoadMoreResult(JSONObject result) {
        return false;
    }

    class GetContactsTask extends AsyncTask<Void, Void, List<ContactUtil.SimpleContactInfo>> {

        @Override
        protected List<ContactUtil.SimpleContactInfo> doInBackground(Void... params) {
            return ContactUtil.getPhoneContacts(getActivity());
        }

        @Override
        protected void onPostExecute(List<ContactUtil.SimpleContactInfo> simpleContactInfos) {
            mContacts = simpleContactInfos;
            if (mContacts == null || mContacts.size() == 0) {
                mRefreshLayout.setRefreshing(false);
            }
            else {
                onRefresh();
            }
        }
    }


    public class FriendContactAdapter extends FriendSimpleAdapter {

        public FriendContactAdapter(Context context) {
            super(context);
        }

        @Override
        public int getCustomItemCount() {
            return super.getCustomItemCount() + (mContacts == null ? 0 : mContacts.size());
        }

        @Override
        public Object getItem(int position) {
            return position < mData.size() ? mData.get(position)
                    : mContacts.get(position - mData.size());
        }

        @Override
        public int getCustomItemViewType(int position) {
            return position < mData.size() ?
                    super.getCustomItemViewType(position) : TYPE_NO_REGISTER;
        }

        @Override
        protected boolean friendTypeFilter(int type) {
            if (    type == TYPE_FAN ||
                    type == TYPE_STRANGER ||
                    type == TYPE_FOLLOWED) {
                return true;
            }

            return false;
        }

        @Override
        protected void onFriendClick(int position) {
            if (getCustomItemViewType(position) == TYPE_NO_REGISTER) {
                return;
            }
            super.onFriendClick(position);
        }

        @Override
        protected void onOperate(int position) {
            if (getItemViewType(position) == TYPE_NO_REGISTER) {
                inviteContact(((ContactUtil.SimpleContactInfo) getItem(position)).getNumber());
            }
            else {
                super.onOperate(position);
            }
        }

        protected void retrievalContactName(FriendInfo friendInfo) {
            if (mContacts == null || mContacts.size() == 0) {
                return;
            }
            for (ContactUtil.SimpleContactInfo contactInfo : mContacts) {
                if (contactInfo.getNumber().equals(friendInfo.phoneNumber)) {
                    friendInfo.contactName = contactInfo.getName();
                    //mAllContacts.remove(contactInfo);
                    break;
                }
            }
        }
    }
}
