package com.koolew.mars;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.FriendInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class ImportPhoneFriendsActivity extends BaseActivity {

    private static final String TAG = "koolew-ImportPhoneFrdsA";

    private RequestQueue mRequestQueue;
    private FriendsAdapter mFriendsAdapter;
    private FriendInfo[] friendInfos;
    private boolean[] friendSelectedFlag;

    private GridView mFriendsGrid;
    private ProgressDialog mLoadFriendsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_phone_friends);

        Utils.setStatusBarColorFromResource(this, R.mipmap.blur_background);

        mRequestQueue = Volley.newRequestQueue(this);

        mFriendsGrid = (GridView) findViewById(R.id.friends_grid);
        mFriendsGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                friendSelectedFlag[position] = !friendSelectedFlag[position];
                mFriendsAdapter.notifyDataSetChanged();
            }
        });
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        int friendItemWidth = getResources().getDimensionPixelSize(R.dimen.friend_select_item_width);
        int leftMargin = (outMetrics.widthPixels - friendItemWidth * 4) / 5;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mFriendsGrid.getLayoutParams();
        params.setMargins(leftMargin, params.topMargin, params.rightMargin, params.bottomMargin);
        mFriendsGrid.setLayoutParams(params);

        mLoadFriendsDialog = new ProgressDialog(this);
        mLoadFriendsDialog.setMessage(getString(R.string.loading_friends));
        mLoadFriendsDialog.setIndeterminate(true);
        mLoadFriendsDialog.setCanceledOnTouchOutside(false);

        new GetRecommendedFriendsTask().execute();
    }

    public void onDoneClick(View v) {

        // Start another Activity here

        String url = UrlHelper.ADD_FRIEND_URL;
        JSONObject requestJson = new JSONObject();
        try {
            JSONArray friendUids = new JSONArray();
            int friendCount = 0;
            for (int i = 0; i < friendInfos.length; i++) {
                if (friendSelectedFlag[i]) {
                    friendUids.put(friendInfos[i].getUid());
                    friendCount++;
                }
            }
            if (friendCount == 0) {
                startMainActivity();
                return;
            }
            else {
                requestJson.put("to", friendUids);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(
                Request.Method.POST, url, requestJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response -> " + response.toString());
                        try {
                            if (response.getInt("code") == 0) {
                                Toast.makeText(ImportPhoneFriendsActivity.this,
                                        "Add friends success.", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Add friends success.");
                                startMainActivity();
                            }
                            else {
                                Toast.makeText(ImportPhoneFriendsActivity.this,
                                        "Add friends error", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Add friends error: " + response);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage(), error);
                        Toast.makeText(ImportPhoneFriendsActivity.this,
                                R.string.connect_server_failed, Toast.LENGTH_SHORT).show();
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

    private void startMainActivity() {
        Intent intent = new Intent(ImportPhoneFriendsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    class FriendsAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        FriendsAdapter() {
            inflater = LayoutInflater.from(ImportPhoneFriendsActivity.this);
        }

        @Override
        public int getCount() {
            return friendInfos.length;
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
                view = inflater.inflate(R.layout.friend_select_item_layout, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.avatar = (ImageView) view.findViewById(R.id.avatar);
                holder.checkIndicator = (ImageView) view.findViewById(R.id.check_indicator);
                holder.nickname = (TextView) view.findViewById(R.id.nickname);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ImageLoader.getInstance().displayImage(friendInfos[position].getAvatar(),
                    holder.avatar, ImageLoaderHelper.avatarLoadOptions);
            if (friendSelectedFlag[position]) {
                holder.checkIndicator.setVisibility(View.VISIBLE);
            }
            else {
                holder.checkIndicator.setVisibility(View.INVISIBLE);
            }
            holder.nickname.setText(friendInfos[position].getNickname());

            return view;
        }
    }

    static class ViewHolder {
        ImageView avatar;
        ImageView checkIndicator;
        TextView nickname;
    }

    class GetRecommendedFriendsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mLoadFriendsDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            JSONObject response = ApiWorker.getInstance().requestContactFriendV2Sync(null);
            try {
                JSONArray friendJsons = response.getJSONObject("result").getJSONArray("relations");
                friendInfos = new FriendInfo[friendJsons.length()];
                friendSelectedFlag = new boolean[friendJsons.length()];
                for (int i = 0; i < friendJsons.length(); i++) {
                    Log.d(TAG, i + ": " + friendJsons.get(i));
                    friendInfos[i] = FriendInfo.fromJson((JSONObject) friendJsons.get(i));
                    friendSelectedFlag[i] = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException ne) {
                ne.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mFriendsAdapter = new FriendsAdapter();
            mFriendsGrid.setAdapter(mFriendsAdapter);
            mLoadFriendsDialog.dismiss();
        }
    }
}
