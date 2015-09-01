package com.koolew.mars;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.infos.BaseVideoInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 7/27/15.
 */
public class WorldVideoCardAdapter extends DetailTitleVideoCardAdapter {

    public WorldVideoCardAdapter(Context context) {
        super(context);
    }

    @Override
    protected View getVideoItemView(int position, View convertView, ViewGroup parent) {
        View root = super.getVideoItemView(position, convertView, parent);

        ((TextView) root.findViewById(R.id.danmaku_send_text)).setText(R.string.share);
        ((ImageView) root.findViewById(R.id.danmaku_send_ic)).setImageResource(R.mipmap.ic_share);

        root.findViewById(R.id.danmaku_send_text).setVisibility(View.INVISIBLE);
        root.findViewById(R.id.danmaku_send_ic).setVisibility(View.INVISIBLE);

        return root;
    }

    @Override
    public int addData(JSONArray videos) {
        int addedCount = 0;
        int count = videos.length();
        for (int i = 0; i < count; i++) {
            try {
                JSONObject videoJson = videos.getJSONObject(i);
                BaseVideoInfo videoInfo = new BaseVideoInfo(videoJson);
                if (!hasVideo(videoInfo)) {
                    mData.add(videoInfo);
                    addedCount++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return addedCount;
    }

    private boolean hasVideo(BaseVideoInfo otherVideo) {
        for (BaseVideoInfo videoInfo: mData) {
            return videoInfo.getVideoId().equals(otherVideo.getVideoId());
        }
        return false;
    }
}
