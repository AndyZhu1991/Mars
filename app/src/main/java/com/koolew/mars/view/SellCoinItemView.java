package com.koolew.mars.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koolew.mars.R;
import com.koolew.mars.shop.Subject;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by jinchangzhu on 3/15/16.
 */
public class SellCoinItemView extends RelativeLayout implements View.OnClickListener {

    private Subject mSubject;

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

        super.setOnClickListener(this);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
    }

    public void setSubject(Subject subject) {
        mSubject = subject;

        ImageLoader.getInstance().displayImage(subject.getIcon(), coinIconView);
        coinCountView.setText(subject.getDesc());
        coinPriceView.setText(String.format("￥%.2f", subject.getPrice()));
    }

    public void setOnBuyListener(OnBuyListener onBuyListener) {
        mOnBuyListener = onBuyListener;
    }

    @Override
    public void onClick(View v) {
        if (mOnBuyListener != null) {
            mOnBuyListener.onBuy(mSubject);
        }
    }

    public interface OnBuyListener {
        void onBuy(Subject subject);
    }
}