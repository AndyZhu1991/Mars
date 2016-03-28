package com.koolew.mars;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koolew.mars.shop.Subject;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.view.SellCoinItemView;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by jinchangzhu on 3/16/16.
 */
public class ShopFragment extends MainBaseFragment implements SellCoinItemView.OnBuyListener,
        Response.ErrorListener, Response.Listener<JSONObject> {

    private static final int[] SELL_COIN_IDS = {
            R.id.sell_coin1,
            R.id.sell_coin2,
            R.id.sell_coin3,
            R.id.sell_coin4,};

    private SellCoinItemView[] mSellCoinItemViews = new SellCoinItemView[4];
    private View mProgressLayout;

    private IWeiboShareAPI mWeiboShareAPI;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_shop, container, false);

        for (int i = 0; i < mSellCoinItemViews.length; i++) {
            mSellCoinItemViews[i] = (SellCoinItemView) root.findViewById(SELL_COIN_IDS[i]);
            mSellCoinItemViews[i].setOnBuyListener(this);
        }
        mProgressLayout = root.findViewById(R.id.progress_layout);
        mProgressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just block touch event.
            }
        });

        mToolbarInterface.setTopIconCount(0);

        requestPrices();

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(getContext(), WeiboConstants.APP_KEY);
        mWeiboShareAPI.registerApp();

        mToolbarInterface.setToolbarTitle(getString(R.string.shop));
        mToolbarInterface.setToolbarColor(getResources().getColor(R.color.koolew_black));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGetPricesRequest != null) {
            mGetPricesRequest.cancel();
            mGetPricesRequest = null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mWeiboShareAPI.handleWeiboResponse(intent, (IWeiboHandler.Response) getActivity());
    }

    @Override
    public void onBuy(Subject subject) {
        new RequestOrderTask().execute(subject);
    }

    private class RequestOrderTask extends AsyncTask<Subject, Void, String> {
        private Dialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = DialogUtil.getGeneralProgressDialog(getContext(), R.string.please_wait_a_moment);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(Subject... subjects) {
            JSONObject paramJson = new JSONObject();
            try {
                paramJson.put("subject_id", subjects[0].getSubjectId());
                paramJson.put("count", 1);
                JSONObject returnJson = ApiWorker.getInstance().doPostRequestSync(
                        UrlHelper.WEIBO_ORDER_URL, paramJson);
                int code = returnJson.getInt("code");
                if (code == 0) {
                    JSONObject result = returnJson.getJSONObject("result");
                    String sign = result.getString("sign");
                    String signBefore = result.getString("sign_before");
                    String signType = result.getString("sign_type");
                    return new StringBuilder(signBefore)
                            .append("&")
                            .append("sign_type")
                            .append("=")
                            .append(signType)
                            .append("&")
                            .append("sign")
                            .append("=")
                            .append(URLEncoder.encode(sign, "UTF-8"))
                            .toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String order) {
            if (TextUtils.isEmpty(order)) {
                mProgressDialog.dismiss();
                Toast.makeText(getContext(), R.string.connect_server_failed, Toast.LENGTH_SHORT).show();
            }
            else {
                mProgressDialog.dismiss();
                mWeiboShareAPI.launchWeiboPay(getActivity(), order);
            }
        }
    }

    private JsonObjectRequest mGetPricesRequest;
    private void requestPrices() {
        mGetPricesRequest = ApiWorker.getInstance().queueGetRequest(
                UrlHelper.COIN_PRICES_URL, this, this);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mGetPricesRequest = null;
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.get_prices_failed)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPrices();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().onBackPressed();
                    }
                })
                .show();
    }

    @Override
    public void onResponse(JSONObject response) {
        mGetPricesRequest = null;

        try {
            int code = response.getInt("code");
            if (code == 0) {
                JSONArray subjectsJson = response.getJSONObject("result").getJSONArray("subjects");
                Gson gson = new Gson();
                Type type = new TypeToken<List<Subject>>(){}.getType();
                List<Subject> subjects = gson.fromJson(subjectsJson.toString(), type);

                for (int i = 0; i < mSellCoinItemViews.length; i++) {
                    if (subjects.size() > i) {
                        mSellCoinItemViews[i].setSubject(subjects.get(i));
                        mSellCoinItemViews[i].setVisibility(View.VISIBLE);
                    }
                }

                mProgressLayout.setVisibility(View.INVISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
