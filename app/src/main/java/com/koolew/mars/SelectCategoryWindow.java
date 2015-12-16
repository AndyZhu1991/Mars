package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

/**
 * Created by jinchangzhu on 11/12/15.
 */
public class SelectCategoryWindow extends PopupWindow implements View.OnClickListener {

    private Context mContext;

    public SelectCategoryWindow(Context context) {
        super(context);
        mContext = context;

        View contentView = LayoutInflater.from(context).inflate(R.layout.add_topic_or_movie, null);
        setContentView(contentView);

        contentView.findViewById(R.id.create_topic).setOnClickListener(this);
        contentView.findViewById(R.id.join_movie).setOnClickListener(this);
        contentView.findViewById(R.id.out_view).setOnClickListener(this);

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(0x00000000));
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.create_topic:
                JoinVideoTopicFragment.startThisFragment(mContext);
                break;
            case R.id.join_movie:
                mContext.startActivity(new Intent(mContext, JoinMovieActivity.class));
                break;
            case R.id.out_view:
                dismiss();
                break;
        }
    }
}
