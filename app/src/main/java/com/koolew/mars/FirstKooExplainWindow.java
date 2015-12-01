package com.koolew.mars;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;


/**
 * Created by jinchangzhu on 12/1/15.
 */
public class FirstKooExplainWindow extends PopupWindow implements View.OnClickListener {

    private Context mContext;

    public FirstKooExplainWindow(Context context) {
        super(context);
        mContext = context;

        View contentView = LayoutInflater.from(context).inflate(R.layout.first_koo_explain, null);
        setContentView(contentView);

        setExplainText((TextView) contentView.findViewById(R.id.give_koo_title));

        contentView.setOnClickListener(this);

        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(0x00000000));
    }

    private void setExplainText(TextView explainTitle) {
        String explainStringHead = mContext.getString(R.string.first_koo_explain_title_head);
        String actionKooString = mContext.getString(R.string.koo_action);
        String explainStringTail = mContext.getString(R.string.first_koo_explain_title_tail);
        int commonColor = 0xFFD8D8D8;
        int kooActionColor = 0xFFF4D288;
        ForegroundColorSpan headSpan = new ForegroundColorSpan(commonColor);
        ForegroundColorSpan kooActionSpan = new ForegroundColorSpan(kooActionColor);
        ForegroundColorSpan tailSpan = new ForegroundColorSpan(commonColor);

        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(
                explainStringHead + actionKooString + explainStringTail);
        ssBuilder.setSpan(headSpan, 0, explainStringHead.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssBuilder.setSpan(kooActionSpan, explainStringHead.length(),
                explainStringHead.length() + actionKooString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssBuilder.setSpan(tailSpan, explainStringHead.length() + actionKooString.length(),
                explainStringHead.length() + actionKooString.length() + explainStringTail.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        explainTitle.setText(ssBuilder);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
