package com.koolew.mars.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 7/8/15.
 */
public class CoinRuleItem extends LinearLayout {

    private TextView mDescription;

    private String mDescriptionText;
    private int mCoinCount;

    public CoinRuleItem(Context context) {
        this(context, null);
    }

    public CoinRuleItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoinRuleItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.coin_rule_item, this);
        mDescription = (TextView) findViewById(R.id.description);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CoinRuleItem, 0, 0);
        mDescriptionText = a.getString(R.styleable.CoinRuleItem_description_text);
        mCoinCount = a.getInteger(R.styleable.CoinRuleItem_coin_count, 0);

        setupDescription();
    }

    private void setupDescription() {
        String coinCountString = String.format("%+d", mCoinCount);
        int textColor = getResources().getColor(R.color.koolew_light_gray);
        int coinColor = 0xFFFEA19F;
        ForegroundColorSpan textSpan = new ForegroundColorSpan(textColor);
        ForegroundColorSpan coinSpan = new ForegroundColorSpan(coinColor);

        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(mDescriptionText + coinCountString);
        ssBuilder.setSpan(textSpan, 0, mDescriptionText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssBuilder.setSpan(coinSpan, mDescriptionText.length(),
                mDescriptionText.length() + coinCountString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mDescription.setText(ssBuilder);
    }
}
