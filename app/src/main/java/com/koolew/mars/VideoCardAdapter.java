package com.koolew.mars;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jinchangzhu on 6/4/15.
 */
public class VideoCardAdapter extends BaseAdapter {

    static DisplayImageOptions imgDisplayOptions = new DisplayImageOptions.Builder()
            //.showStubImage(R.drawable.stub_image)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            //.imageScaleType(ImageScaleType.EXACT)
            .build();

    private Context mContext;
    private LayoutInflater mInflater;
    private List<JSONObject> mData;


    public VideoCardAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mData = new ArrayList<JSONObject>();
    }

    public void setData(JSONObject jsonObject) {

        int code;
        try {
            code = jsonObject.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("There is no integer field named \"code\"");
        }

        if (code != 0) {
            throw new IllegalArgumentException(
                    String.format("The \"code\" field is %d, expected: 0.", code));
        }

        try {
            JSONObject result = jsonObject.getJSONObject("result");
            JSONArray videos = result.getJSONArray("videos");
            int length = videos.length();
            for (int i = 0; i < length; i++) {
                mData.add((JSONObject) videos.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("JSONObject error:\n" + jsonObject);
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.video_card_item, null);
            ViewHolder holder = new ViewHolder();
            convertView.setTag(holder);
            holder.videoLayout = (FrameLayout) convertView.findViewById(R.id.video_layout);
            holder.videoLayout.getLayoutParams().height = getVideoCardVideoHeight();
            holder.videoThumb = (ImageView) convertView.findViewById(R.id.video_thumb);
            holder.avatar = (CircleImageView) convertView.findViewById(R.id.avatar);
            holder.nickname = (TextView) convertView.findViewById(R.id.nickname);
            holder.videoDate = (TextView) convertView.findViewById(R.id.video_date);
        }

        try {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            ImageLoader.getInstance().displayImage(mData.get(position).getString("thumb_url"),
                    holder.videoThumb, imgDisplayOptions);
            JSONObject userInfo = mData.get(position).getJSONObject("user_info");
            ImageLoader.getInstance().displayImage(userInfo.getString("avatar"),
                    holder.avatar, imgDisplayOptions);
            holder.nickname.setText(userInfo.getString("nickname"));
            holder.videoDate.setText(new SimpleDateFormat("yyyy-MM-dd").
                    format(new Date(mData.get(position).getLong("create_time"))));
            holder.videoLayout.setTag(mData.get(position).getString("video_url"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private int getVideoCardVideoHeight() {
        Resources res = mContext.getResources();
        int videoCardPadding = res.getDimensionPixelSize(R.dimen.video_card_padding);
        int screenWidth = Utils.getScreenWidthPixel(mContext);
        // The video  width:height == 4:3
        return (screenWidth - videoCardPadding * 2) / 4 * 3;
    }

    public int getPositionByVideoLayout(FrameLayout videoLayout) {
        String video_url = (String) videoLayout.getTag();
        int count = mData.size();
        for (int i = 0; i < count; i++) {
            try {
                if (mData.get(i).getString("video_url").equals(video_url)) {
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    public static class ViewHolder {

        public FrameLayout videoLayout;
        //public VideoView videoView;
        public ImageView videoThumb;
        public CircleImageView avatar;
        public TextView nickname;
        public TextView videoDate;
    }
}
