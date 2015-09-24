package com.koolew.mars;

import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseCommentInfo;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jinchangzhu on 7/30/15.
 */
public class CheckDanmakuFragment extends BaseVideoListFragment {

    public static final String KEY_VIDEO_ID = "video id";

    private String mVideoId;

    private List<BaseCommentInfo> mComments;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVideoId = getActivity().getIntent().getStringExtra(KEY_VIDEO_ID);
        mComments = new ArrayList<>();
    }

    public CheckDanmakuFragment() {
        super();

        isNeedLoadMore = true;
    }

    protected VideoCardAdapter useThisAdapter() {
        return new CheckDanmakuAdapter(getActivity());
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        return ApiWorker.getInstance().requestSingleVideo(mVideoId, mRefreshListener, null);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        return ApiWorker.getInstance().getVideoComment(mVideoId, getLastCommentTime(),
                mLoadMoreListener, null);
    }

    @Override
    protected boolean handleRefresh(JSONObject response) {
        mComments.clear();
        return super.handleRefresh(response);
    }

    @Override
    protected boolean handleLoadMore(JSONObject response) {
        try {
            if (response.getInt("code") == 0) {
                JSONArray comments = response.getJSONObject("result").getJSONArray("comments");
                if (comments == null || comments.length() == 0) {
                    return false;
                }
                int length = comments.length();
                for (int i = 0; i < length; i++) {
                    mComments.add(new BaseCommentInfo(comments.getJSONObject(i)));
                }
                mAdapter.notifyDataSetChanged();
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected JSONArray getVideosFromResponse(JSONObject response) {
        JSONArray videos = new JSONArray();
        try {
            if (response.getInt("code") == 0) {
                JSONObject video = response.getJSONObject("result").getJSONObject("video");
                videos.put(video);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videos;
    }

    private long getLastCommentTime() {
        if (mComments.size() == 0) {
            return Long.MAX_VALUE;
        }
        else {
            return mComments.get(mComments.size() - 1).getCreateTime();
        }
    }


    private final static int TYPE_COMMENT_TITLE = VideoCardAdapter.TYPE_SUB_CLASS_USE_START;
    private final static int TYPE_COMMENT = TYPE_COMMENT_TITLE + 1;
    class CheckDanmakuAdapter extends VideoCardAdapter {

        public CheckDanmakuAdapter(Context context) {
            super(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            switch (getItemViewType(position)) {
                case TYPE_COMMENT_TITLE:
                    return getCommentTitle(position, convertView);
                case TYPE_COMMENT:
                    return getCommentView(position, convertView);
            }
            return super.getView(position, convertView, parent);
        }

        private View getCommentTitle(int position, View convertView) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.check_danmaku_comment_title, null);
            }

            ((TextView) convertView.findViewById(R.id.title)).setText(getString
                    (R.string.support_count, mData.get(0).getKooTotal()));

            return convertView;
        }

        private BaseCommentInfo getCommentItem(int position) {
            return mComments.get(position - 3);
        }

        private View getCommentView(int position, View convertView) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.check_danmaku_comment_item, null);
                convertView.setTag(new CommentViewHolder(convertView));
            }

            CommentViewHolder holder = (CommentViewHolder) convertView.getTag();
            holder.position = position;
            BaseCommentInfo item = getCommentItem(position);
            ImageLoader.getInstance().displayImage(item.getUserInfo().getAvatar(), holder.avatar);
            holder.time.setText(Utils.buildTimeSummary(getActivity(), item.getCreateTime() * 1000));

            ForegroundColorSpan nicknameSpan = new ForegroundColorSpan(0xFFDB5E5F);
            ForegroundColorSpan remainSpan = new ForegroundColorSpan(0xFF4E677A);

            SpannableStringBuilder ssBuilder = new SpannableStringBuilder();
            ssBuilder.append(item.getUserInfo().getNickname())
                    .append(getString(R.string.say))
                    .append(getString(R.string.colon))
                    .append(item.getContent());
            ssBuilder.setSpan(nicknameSpan, 0, item.getUserInfo().getNickname().length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssBuilder.setSpan(remainSpan, item.getUserInfo().getNickname().length(),
                    ssBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.comment.setText(ssBuilder);

            return convertView;
        }

        @Override
        public int getCount() {
//            if (mComments.size() == 0) {
//                return 0;
//            }
//            else {
                return mComments.size() + 3 /* TITLE & VIDEO_ITEM & COMMENT_TITLE */;
            //}
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_TITLE;
            }
            else if (position == 1) {
                return TYPE_VIDEO_ITEM;
            }
            else if (position == 2) {
                return TYPE_COMMENT_TITLE;
            }
            else {
                return TYPE_COMMENT;
            }
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 2 /* COMMENT_TITLE, COMMENT */;
        }
    }

    class CommentViewHolder implements View.OnClickListener {
        int position;
        CircleImageView avatar;
        TextView comment;
        TextView time;

        public CommentViewHolder(View itemView) {
            avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            avatar.setOnClickListener(this);
            comment = (TextView) itemView.findViewById(R.id.comment);
            time = (TextView) itemView.findViewById(R.id.time);
        }

        @Override
        public void onClick(View v) {
            BaseCommentInfo item = ((CheckDanmakuAdapter) mAdapter).getCommentItem(position);
            FriendInfoActivity.startThisActivity(getActivity(), item.getUserInfo().getUid());
        }
    }
}
