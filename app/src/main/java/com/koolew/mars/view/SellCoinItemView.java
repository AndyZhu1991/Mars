package com.koolew.mars.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 3/15/16.
 */
public class SellCoinItemView extends RelativeLayout implements View.OnClickListener {

    private int coinCount = 0;
    private float price = 0.0f;

    private ImageView coinIconView;
    private TextView coinCountView;
    private TextView coinPriceView;

    private OnBuyListener mOnBuyListener;

    public SellCoinItemView(Context context) {
        this(context, null);
    }

    public SellCoinItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SellCoinItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.shop_buy_coin_item, this);
        setBackgroundResource(R.drawable.shop_coin_item_bg);

        coinIconView = (ImageView) findViewById(R.id.coin_icon);
        coinCountView = (TextView) findViewById(R.id.coin_count);
        coinPriceView = (TextView) findViewById(R.id.coin_price);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SellCoinItemView, 0, 0);
        Drawable coinDrawable = a.getDrawable(R.styleable.SellCoinItemView_ic_coin);
        coinIconView.setImageDrawable(coinDrawable);
        a.recycle();

        setOnClickListener(this);
    }

    public void setCoinCount(int count) {
        this.coinCount = count;
        coinCountView.setText(coinCount + getContext().getString(R.string.coin));
    }

    public int getCoinCount() {
        return coinCount;
    }

    public void setPrice(float price) {
        this.price = price;
        coinPriceView.setText(String.format("¥ %.2f", price));
    }

    public float getPrice() {
        return price;
    }

    public void setOnBuyListener(OnBuyListener onBuyListener) {
        mOnBuyListener = onBuyListener;
    }

    @Override
    public void onClick(View v) {
        if (mOnBuyListener != null) {
            mOnBuyListener.onBuy(coinCount, price);
        }
    }

    public interface OnBuyListener {
        void onBuy(int coinCount, float price);
    }
}