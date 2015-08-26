package com.koolew.mars;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.koolew.mars.danmaku.DanmakuItemInfo;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.player.ScrollPlayer;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.KooAnimationView;
import com.koolew.mars.webapi.ApiErrorCode;
import com.koolew.mars.webapi.ApiWorker;
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

    protected final static int TYPE_TITLE = 0;
    protected final static int TYPE_NO_VIDEO = 1;
    protected final static int TYPE_VIDEO_ITEM = 2;

    protected Context mContext;
    protected LayoutInflater mInflater;
    protected List<BaseVideoInfo> mData;

    protected TextView mTitleText;
    protected String mTopicTitle;

    private OnDanmakuSendListener mDanmakuSendListener;
    private OnKooClickListener mKooClickListener;
    private OnMoreMenuClickListener mMoreMenuListener;

    public VideoCardAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mData = new ArrayList<>();
    }

    public void setTopicTitle(String topicTitle) {
        mTopicTitle = topicTitle;
        if (mTitleText != null) {
            mTitleText.setText(mTopicTitle);
        }
    }

    public void setData(JSONArray videos) {
        mData.clear();
        addData(videos);
    }

    public int addData(JSONArray videos) {
        int length = 0;
        try {
            length = videos.length();
            for (int i = 0; i < length; i++) {
                mData.add(new BaseVideoInfo(videos.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return length;
    }

    public long getOldestVideoTime() {
        return mData.get(mData.size() - 1).getCreateTime();
    }

    public void removeVideo(String videoId) {
        int count = mData.size();
        for (int i = 0; i < count; i++) {
            if (mData.get(i).getVideoId().equals(videoId)) {
                mData.remove(i);
                break;
            }
        }
    }

    @Override
    public int getCount() {
        return mData.size() == 0 ? 2 : mData.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position == 0) { // Title
            return null;
        }
        else {         // First position is title
            return mData.get(position - 1);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_TITLE;
        }
        else {
            return mData.size() == 0 ? TYPE_NO_VIDEO : TYPE_VIDEO_ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case TYPE_TITLE:
                return getTitleView();
            case TYPE_NO_VIDEO:
                return getNoVideoView();
            case TYPE_VIDEO_ITEM:
                return getVideoItemView(position, convertView, parent);
        }
        return null;
    }

    protected View getTitleView() {
        View view = mInflater.inflate(R.layout.topic_title_layout, null);
        mTitleText = ((TextView) view.findViewById(R.id.title));
        mTitleText.setText(mTopicTitle);
        return view;
    }

    private View getNoVideoView() {
        return mInflater.inflate(R.layout.topic_no_video_layout, null);
    }

    protected View getVideoItemView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.video_card_item, null);
            ViewHolder holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.position = position;
        BaseVideoInfo item = getItemData(position);
        ImageLoader.getInstance().displayImage(item.getVideoThumb(),
                holder.videoThumb, ImageLoaderHelper.topicThumbLoadOptions);
        holder.progressBar.setVisibility(View.INVISIBLE);
        BaseUserInfo userInfo = item.getUserInfo();
        ImageLoader.getInstance().displayImage(userInfo.getAvatar(),
                holder.avatar, ImageLoaderHelper.avatarLoadOptions);
        holder.avatar.setTag(userInfo.getUid());
        holder.nickname.setText(userInfo.getNickname());
        holder.videoDate.setText(new SimpleDateFormat("yyyy-MM-dd").
                format(new Date(item.getCreateTime() * 1000)));
        holder.videoLayout.setTag(item.getVideoUrl());

        holder.danmakuSendLayout.setTag(item);
        holder.kooLayout.setTag(item.getVideoId());

        return convertView;
    }

    public BaseVideoInfo getItemData(int position) {
        return (BaseVideoInfo) getItem(position);
    }

    private int getVideoCardVideoHeight() {
        Resources res = mContext.getResources();
        int videoCardPadding = res.getDimensionPixelSize(R.dimen.video_card_padding);
        int screenWidth = Utils.getScreenWidthPixel(mContext);
        // The video  width:height == 4:3
        return (screenWidth - videoCardPadding * 2) / 4 * 3;
    }

    public void setOnDanmakuSendListener(OnDanmakuSendListener listener) {
        mDanmakuSendListener = listener;
    }

    public void setOnKooClickListener(OnKooClickListener listener) {
        mKooClickListener = listener;
    }

    public void setOnMoreMenuClickListener(OnMoreMenuClickListener listener) {
        mMoreMenuListener = listener;
    }

    public void onKooClick(String videoId) {
        ApiWorker.getInstance().kooVideo(videoId, 1, new KooListener(), null);
        if (mKooClickListener != null) {
            mKooClickListener.onKooClick(videoId);
        }
    }

    class KooListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                int code = response.getInt("code");
                if (code == 0) {
                    MyAccountInfo.setCoinNum(MyAccountInfo.getCoinNum() - 1);
                }
                else if (code == ApiErrorCode.COIN_NOT_ENOUGH) {
                    Toast.makeText(mContext, R.string.not_enough_coin_hint, Toast.LENGTH_SHORT)
                            .show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onAvatarClick(String uid) {
        if (uid.equals(MyAccountInfo.getUid())) {
            return;
        }
        Intent intent = new Intent(mContext, FriendInfoActivity.class);
        intent.putExtra(FriendInfoActivity.KEY_UID, uid);
        mContext.startActivity(intent);
    }

    interface OnDanmakuSendListener {
        void onDanmakuSend(BaseVideoInfo videoInfo);
    }

    interface OnKooClickListener {
        void onKooClick(String videoId);
    }

    interface OnMoreMenuClickListener {
        void onMoreMenuClick(String videoId, String uid);
    }

    class ViewHolder implements View.OnClickListener {

        public int position;

        public ImageView imageMore;
        public FrameLayout videoLayout;
        public RelativeLayout danmakuContainer;
        public ImageView videoThumb;
        public ProgressBar progressBar;
        public CircleImageView avatar;
        public TextView nickname;
        public TextView videoDate;

        public LinearLayout kooLayout;
        public ImageView kooIcon;
        public LinearLayout danmakuSendLayout;
        public KooAnimationView kooAnimationView;

        public ViewHolder(View convertView) {
            imageMore = (ImageView) convertView.findViewById(R.id.more);
            imageMore.setOnClickListener(this);
            videoLayout = (FrameLayout) convertView.findViewById(R.id.video_layout);
            videoLayout.getLayoutParams().height = getVideoCardVideoHeight();
            danmakuContainer = (RelativeLayout) convertView.findViewById(R.id.danmaku_container);
            videoThumb = (ImageView) convertView.findViewById(R.id.video_thumb);
            progressBar = (ProgressBar) convertView.findViewById(R.id.progress);
            avatar = (CircleImageView) convertView.findViewById(R.id.avatar);
            avatar.setOnClickListener(this);
            nickname = (TextView) convertView.findViewById(R.id.nickname);
            videoDate = (TextView) convertView.findViewById(R.id.video_date);
            danmakuSendLayout = (LinearLayout) convertView.findViewById(R.id.danmaku_send_layout);
            danmakuSendLayout.setOnClickListener(this);
            kooLayout = (LinearLayout) convertView.findViewById(R.id.koo_layout);
            kooLayout.setOnClickListener(this);
            kooIcon = (ImageView) convertView.findViewById(R.id.koo_icon);
            kooAnimationView = (KooAnimationView) convertView.findViewById(R.id.koo_animation_view);
        }

        @Override
        public void onClick(View v) {
            if (v == imageMore) {
                if (mMoreMenuListener != null) {
                    BaseVideoInfo videoInfo = getItemData(position);
                    mMoreMenuListener.onMoreMenuClick(videoInfo.getVideoId(),
                            videoInfo.getUserInfo().getUid());
                }
            }
            else if (v == avatar) {
                onAvatarClick(getItemData(position).getUserInfo().getUid());
            }
            else if (v == kooLayout) {
                onKooClick(getItemData(position).getVideoId());
                PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", 1f, 0.5f, 1f);
                PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", 1f, 3f, 1f);
                PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f, 3f, 1f);
                ObjectAnimator.ofPropertyValuesHolder(kooIcon, pvhX, pvhY, pvhZ)
                        .setDuration(400)
                        .start();
                kooAnimationView.startAnimation();
            }
            else if (v == danmakuSendLayout) {
                if (mDanmakuSendListener != null) {
                    mDanmakuSendListener.onDanmakuSend(getItemData(position));
                }
            }
        }
    }

    public class TopicScrollPlayer extends ScrollPlayer {

        public TopicScrollPlayer(ListView listView) {
            super(listView);
        }

        private BaseVideoInfo getVideoInfoByItemView(View itemView) {
            return getItemData(((ViewHolder) itemView.getTag()).position);
        }

        @Override
        public boolean isItemView(View childView) {
            return mData.size() > 0 && childView.getTag() instanceof ViewHolder;
        }

        @Override
        public ViewGroup getSurfaceContainer(View itemView) {
            return ((ViewHolder) itemView.getTag()).videoLayout;
        }

        @Override
        public ViewGroup getDanmakuContainer(View itemView) {
            return ((ViewHolder) itemView.getTag()).danmakuContainer;
        }

        @Override
        public ArrayList<DanmakuItemInfo> getDanmakuList(View itemView) {
            return getVideoInfoByItemView(itemView).getDanmakus();
        }

        @Override
        public ImageView getThumbImage(View itemView) {
            return ((ViewHolder) itemView.getTag()).videoThumb;
        }

        @Override
        public View getProgressView(View itemView) {
            return ((ViewHolder) itemView.getTag()).progressBar;
        }

        @Override
        public String getVideoUrl(View itemView) {
            return getVideoInfoByItemView(itemView).getVideoUrl();
        }
    }
}
