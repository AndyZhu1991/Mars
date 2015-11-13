package com.koolew.mars;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.koolew.mars.utils.Utils;

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

        setWidth((int) Utils.dpToPixels(context, 130));
        setHeight((int) Utils.dpToPixels(context, 105));
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(0x00000000));
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.create_topic:
                mContext.startActivity(new Intent(mContext, AddTopicActivity.class));
                break;
            case R.id.join_movie:
                mContext.startActivity(new Intent(mContext, JoinMovieActivity.class));
                break;
        }
    }
}
