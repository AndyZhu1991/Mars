package com.koolew.mars;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.view.SellCoinItemView;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public void onDestroy() {
        super.onDestroy();
        if (mGetPricesRequest != null) {
            mGetPricesRequest.cancel();
            mGetPricesRequest = null;
        }
    }

    @Override
    public void onBuy(int coinCount, float price) {

    }

    private JsonObjectRequest mGetPricesRequest;
    private void requestPrices() {
        mGetPricesRequest = ApiWorker.getInstance().queueGetRequest(UrlHelper.COIN_PRICES_URL, this, this);
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
                JSONArray pricesArray = response.getJSONObject("result").getJSONArray("prices");
                CoinPrice[] coinPrices = new CoinPrice[pricesArray.length()];
                for (int i = 0; i < coinPrices.length; i++) {
                    coinPrices[i] = new CoinPrice(pricesArray.getJSONObject(i));
                }

                for (int i = 0; i < mSellCoinItemViews.length; i++) {
                    if (coinPrices.length > i) {
                        mSellCoinItemViews[i].setCoinCount(coinPrices[i].mCoinCount);
                        mSellCoinItemViews[i].setPrice(coinPrices[i].mPrice);
                    }
                    else {
                        mSellCoinItemViews[i].setVisibility(View.INVISIBLE);
                    }
                }

                mProgressLayout.setVisibility(View.INVISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static class CoinPrice {
        private int mCoinCount;
        private float mPrice;

        private CoinPrice(JSONObject jsonObject) {
            try {
                mCoinCount = jsonObject.getInt("coin");
                mPrice = (float) jsonObject.getDouble("price");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
