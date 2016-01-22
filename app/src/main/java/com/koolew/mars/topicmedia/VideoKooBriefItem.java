package com.koolew.mars.topicmedia;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.FriendInfoActivity;
import com.koolew.mars.R;
import com.koolew.mars.VideoKooRankActivity;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.utils.JsonUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinchangzhu on 12/18/15.
 */
public class VideoKooBriefItem extends MediaItem {

    private static final int TYPE = UniversalMediaAdapter.registerGenerator(
            new UniversalMediaAdapter.ItemViewHolderGenerator() {
                @Override
                protected int layoutResId() {
                    return R.layout.video_koo_brief;
                }

                @Override
                protected Class<?> holderClass() {
                    return ItemViewHolder.class;
                }
            }
    );

    private BaseVideoInfo videoInfo;
    private UserKooInfo[] userKooInfos = new UserKooInfo[2];

    public VideoKooBriefItem(BaseVideoInfo videoInfo, JSONArray kooRanks) {
        this.videoInfo = videoInfo;
        int userKooCount = kooRanks.length();
        if (userKooCount >= 1) {
            try {
                userKooInfos[0] = new UserKooInfo(kooRanks.getJSONObject(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (userKooCount >= 2) {
            try {
                userKooInfos[1] = new UserKooInfo(kooRanks.getJSONObject(1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasKooRankUser() {
        return userKooInfos[0] != null;
    }

    @Override
    protected int getType() {
        return TYPE;
    }

    public static class ItemViewHolder extends MediaHolder<VideoKooBriefItem>
            implements View.OnClickListener {

        private View titleLayout;

        private TextView kooCount;
        private TextView kooItemCount;

        private View[] userViews = new View[2];
        private ImageView[] avatars = new ImageView[2];
        private TextView[] nicknames = new TextView[2];
        private TextView[] userKoos = new TextView[2];

        private View dashLine;
        private View noKooView;

        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);

            titleLayout = itemView.findViewById(R.id.title_layout);
            titleLayout.setOnClickListener(this);

            kooCount = (TextView) itemView.findViewById(R.id.koo_count);
            kooItemCount = (TextView) itemView.findViewById(R.id.koo_item_count);

            userViews[0] = itemView.findViewById(R.id.user_view0);
            userViews[0].setOnClickListener(this);
            avatars[0] = (ImageView) itemView.findViewById(R.id.avatar0);
            nicknames[0] = (TextView) itemView.findViewById(R.id.nickname0);
            userKoos[0] = (TextView) itemView.findViewById(R.id.koo_count0);

            userViews[1] = itemView.findViewById(R.id.user_view1);
            userViews[1].setOnClickListener(this);
            avatars[1] = (ImageView) itemView.findViewById(R.id.avatar1);
            nicknames[1] = (TextView) itemView.findViewById(R.id.nickname1);
            userKoos[1] = (TextView) itemView.findViewById(R.id.koo_count1);

            dashLine = itemView.findViewById(R.id.dash_line);
            noKooView = itemView.findViewById(R.id.no_koo);
        }

        @Override
        protected void onBindItem() {
            kooCount.setText(mContext.getString(R.string.video_koo_brief_title_count,
                    mItem.videoInfo.getKooTotal()));
            for (int i = 0; i < 2 && mItem.userKooInfos[i] != null; i++) {
                ImageLoader.getInstance().displayImage(mItem.userKooInfos[i].userInfo.getAvatar(),
                        avatars[i], ImageLoaderHelper.avatarLoadOptions);
                nicknames[i].setText(mItem.userKooInfos[i].userInfo.getNickname());
                userKoos[i].setText(String.valueOf(mItem.userKooInfos[i].kooNum));
            }
            if (mItem.userKooInfos[0] == null) {
                userViews[0].setVisibility(View.GONE);
                noKooView.setVisibility(View.VISIBLE);
            }
            if (mItem.userKooInfos[1] == null) {
                userViews[1].setVisibility(View.GONE);
                dashLine.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if (v == titleLayout) {
                Intent intent = new Intent(mContext, VideoKooRankActivity.class);
                intent.putExtra(VideoKooRankActivity.KEY_VIDEO_ID, mItem.videoInfo.getVideoId());
                mContext.startActivity(intent);
            }
            else if (v == userViews[0]) {
                FriendInfoActivity.startThisActivity(mContext, mItem.userKooInfos[0].userInfo.getUid());
            }
            else if (v == userViews[1]) {
                FriendInfoActivity.startThisActivity(mContext, mItem.userKooInfos[1].userInfo.getUid());
            }
        }
    }

    static class UserKooInfo {
        private BaseUserInfo userInfo;
        private int kooNum;

        public UserKooInfo(JSONObject jsonObject) {
            JSONObject userJson = JsonUtil.getJSONObjectIfHas(jsonObject, "user");
            userInfo = new BaseUserInfo(userJson);
            kooNum = JsonUtil.getIntIfHas(jsonObject, "koo_num");
        }
    }
}
