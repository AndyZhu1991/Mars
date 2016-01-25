package com.koolew.mars;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.TypedUserInfo;
import com.koolew.mars.statistics.BaseV4Fragment;
import com.koolew.mars.utils.ThreadUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.KoolewVideoView;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiErrorCode;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jinchangzhu on 9/22/15.
 */
public class PlayFragment extends BaseV4Fragment implements View.OnClickListener,
        MediaPlayer.OnCompletionListener, TitleBarView.OnRightLayoutClickListener {

    public static final String KEY_SQUARE_ID = "square_id";

    private static final int POSITION_LEFT = 0;
    private static final int POSITION_RIGHT = 1;

    private String mSquareId;

    private TitleBarView mTitleBar;

    private TextView mTitle;

    private FrameLayout mDisplayArea;
    private KoolewVideoView mVideoView;
    private ImageView mFinishedImage;

    private int mCurrentPlayPosition;

    private CircleImageView mLeftAvatar;
    private TextView mLeftSupportBtn;
    private TextView mLeftFollowBtn;
    private View mTopLeftShader;
    private View mBottomLeftLayout;

    private CircleImageView mRightAvatar;
    private TextView mRightSupportBtn;
    private TextView mRightFollowBtn;
    private View mTopRightShader;
    private View mBottomRightLayout;

    private View mArrow;

    private View mFinishLayout;
    private View mCongratulationText;
    private View mFinishedText;

    private View mBlockTouchFrame;

    private CurrentVideoInfo mCurrentLeftVideoInfo;
    private CurrentVideoInfo mCurrentRightVideoInfo;

    private String mJudgedVideoId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitleBar = ((TitleFragmentActivity) getActivity()).getTitleBar();
        mTitleBar.setTitle(R.string.title_play);
        mTitleBar.setRightImage(R.mipmap.ic_play_share);
        mTitleBar.setRightLayoutVisibility(View.VISIBLE);
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.koolew_black));
        mTitleBar.setOnRightLayoutClickListener(this);

        mCurrentPlayPosition = POSITION_LEFT;

        mSquareId = getActivity().getIntent().getStringExtra(KEY_SQUARE_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_play, container, false);

        mTitle = (TextView) root.findViewById(R.id.title);

        int screenWidthPix = Utils.getScreenWidthPixel(getActivity());
        int displayAreaPadding = getResources().getDimensionPixelSize(R.dimen.display_area_padding);
        int displayAreaHeight = (screenWidthPix - displayAreaPadding * 2) / 4 * 3
                + displayAreaPadding * 2;
        mDisplayArea = (FrameLayout) root.findViewById(R.id.display_area);
        mDisplayArea.getLayoutParams().height = displayAreaHeight;
        mVideoView = (KoolewVideoView) root.findViewById(R.id.video_view);
        mVideoView.setCompletionListener(this);
        mFinishedImage = (ImageView) root.findViewById(R.id.finished_image);

        mLeftAvatar = (CircleImageView) root.findViewById(R.id.left_avatar);
        mLeftAvatar.setOnClickListener(this);
        mLeftSupportBtn = (TextView) root.findViewById(R.id.left_support_btn);
        mLeftSupportBtn.setOnClickListener(this);
        mLeftFollowBtn = (TextView) root.findViewById(R.id.left_follow_btn);
        mLeftFollowBtn.setOnClickListener(this);
        mBottomLeftLayout = root.findViewById(R.id.bottom_left_layout);
        mBottomLeftLayout.setOnClickListener(this);

        mRightAvatar = (CircleImageView) root.findViewById(R.id.right_avatar);
        mRightAvatar.setOnClickListener(this);
        mRightSupportBtn = (TextView) root.findViewById(R.id.right_support_btn);
        mRightSupportBtn.setOnClickListener(this);
        mRightFollowBtn = (TextView) root.findViewById(R.id.right_follow_btn);
        mRightFollowBtn.setOnClickListener(this);
        mBottomRightLayout = root.findViewById(R.id.bottom_right_layout);
        mBottomRightLayout.setOnClickListener(this);

        mArrow = root.findViewById(R.id.arrow);

        mFinishLayout = root.findViewById(R.id.finish_layout);
        mFinishLayout.setOnClickListener(this);
        mCongratulationText = root.findViewById(R.id.congratulation_text);
        mFinishedText = root.findViewById(R.id.finished_text);

        mBlockTouchFrame = root.findViewById(R.id.block_touch_frame);
        mBlockTouchFrame.setOnClickListener(this);

        requestDefaultGroup();

        return root;
    }

    @Override
    protected void onPageStart() {
        super.onPageStart();
        mVideoView.startPlay();
    }

    @Override
    public void onRightLayoutClick() {
        CurrentVideoInfo shareVideoInfo = mCurrentPlayPosition == POSITION_LEFT
                ? mCurrentLeftVideoInfo : mCurrentRightVideoInfo;
        ShareVideoWindow shareVideoWindow = new ShareVideoWindow(getActivity(),
                shareVideoInfo, shareVideoInfo.topicInfo.getTitle());
        shareVideoWindow.showAtLocation(getView(), Gravity.TOP, 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_avatar:
                onLeftAvatarClick();
                break;
            case R.id.left_support_btn:
                onLeftSupportClick();
                break;
            case R.id.left_follow_btn:
                onLeftFollowClick();
                break;
            case R.id.bottom_left_layout:
                onBottomLeftLayoutClick();
                break;
            case R.id.right_avatar:
                onRightAvatarClick();
                break;
            case R.id.right_support_btn:
                onRightSupportClick();
                break;
            case R.id.right_follow_btn:
                onRightFollowClick();
                break;
            case R.id.bottom_right_layout:
                onBottomRightLayoutClick();
                break;
        }
    }

    private void onLeftAvatarClick() {
        FriendInfoActivity.startThisActivity(getActivity(), mCurrentLeftVideoInfo.getUserInfo().getUid());
    }

    private void onLeftSupportClick() {
        judge(mCurrentLeftVideoInfo.getVideoId());
    }

    private void onLeftFollowClick() {
        ApiWorker.getInstance().followUser(mCurrentLeftVideoInfo.getUserInfo().getUid(),
                new FollowUserListener(mLeftFollowBtn), new FollowUserErrorListener());
    }

    private void onBottomLeftLayoutClick() {
        if (mCurrentPlayPosition == POSITION_RIGHT) {
            switchVideo();
        }
    }

    private void onRightAvatarClick() {
        FriendInfoActivity.startThisActivity(getActivity(), mCurrentRightVideoInfo.getUserInfo().getUid());
    }

    private void onRightSupportClick() {
        judge(mCurrentRightVideoInfo.getVideoId());
    }

    private void onRightFollowClick() {
        ApiWorker.getInstance().followUser(mCurrentLeftVideoInfo.getUserInfo().getUid(),
                new FollowUserListener(mRightFollowBtn), new FollowUserErrorListener());
    }

    private void onBottomRightLayoutClick() {
        if (mCurrentPlayPosition == POSITION_LEFT) {
            switchVideo();
        }
    }

    class FollowUserListener implements Response.Listener<JSONObject> {
        private TextView mButton;

        public FollowUserListener(TextView button) {
            mButton = button;
        }

        @Override
        public void onResponse(JSONObject response) {
            try {
                if (response.getInt("code") == 0) {
                    mButton.setText(R.string.followed);
                }
                else {
                    Toast.makeText(getActivity(), R.string.follow_failed, Toast.LENGTH_LONG).show();
                    mButton.setEnabled(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class FollowUserErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO
        }
    }

    private void requestDefaultGroup() {
        mBlockTouchFrame.setVisibility(View.VISIBLE);
        ApiWorker.getInstance().queueGetRequest(UrlHelper.getDefaultPlayGroupUrl(mSquareId),
                mJudgeListener, mJudgeErrorListener);
    }

    private Response.Listener<JSONObject> mJudgeListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mBlockTouchFrame.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBlockTouchFrame.setVisibility(View.INVISIBLE);
                }
            }, 500);

            try {
                int code = response.getInt("code");
                if (code == 0) {
                    updateCurrentGroup(response.getJSONObject("result").getJSONObject("next"));
                    startPlayGroup();
                }
                else if (code == ApiErrorCode.NO_MORE_ITEMS) {
                    switchToFinishMode();
                }
                else {
                    onJudgeError(response.getString("msg"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void judge(String videoId) {
        mJudgedVideoId = videoId;
        stopPlayGroup();
        mBlockTouchFrame.setVisibility(View.VISIBLE);
        ApiWorker.getInstance().queueGetRequest(UrlHelper.getJudgeUrl(mSquareId, videoId),
                mJudgeListener, mJudgeErrorListener);
    }

    private Response.ErrorListener mJudgeErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mBlockTouchFrame.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBlockTouchFrame.setVisibility(View.INVISIBLE);
                }
            }, 500);
            onJudgeError(error.getLocalizedMessage());
        }
    };

    private void onJudgeError(String errMsg) {
        Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
    }

    private void updateCurrentGroup(JSONObject jsonObject) {
        try {
            JSONArray videos = jsonObject.getJSONArray("videos");
            CurrentVideoInfo videoInfo0 = new CurrentVideoInfo(videos.getJSONObject(0));
            CurrentVideoInfo videoInfo1 = new CurrentVideoInfo(videos.getJSONObject(1));
            if (System.currentTimeMillis() % 2 == 0) {
                mCurrentLeftVideoInfo = videoInfo0;
                mCurrentRightVideoInfo = videoInfo1;
            }
            else {
                mCurrentLeftVideoInfo = videoInfo1;
                mCurrentRightVideoInfo = videoInfo0;
            }
            refreshUserInfo();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void switchToFinishMode() {
        mFinishedImage.setVisibility(View.VISIBLE);
        mTitle.setVisibility(View.GONE);
        mFinishLayout.setVisibility(View.VISIBLE);
        mTitleBar.setRightLayoutVisibility(View.INVISIBLE);
    }

    private void startPlayGroup() {
        mCurrentPlayPosition = POSITION_LEFT;
        mArrow.setVisibility(View.VISIBLE);
        mArrow.setX(getArrowLeftOffsetX());
        play(mCurrentLeftVideoInfo);
    }

    private void stopPlayGroup() {
        mVideoView.stop();
    }

    @Override
    public void onCompletion(MediaPlayer iMediaPlayer) {
        ThreadUtil.executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                switchVideo();
            }
        });
    }

    private void switchVideo() {
        if (mCurrentPlayPosition == POSITION_LEFT) {
            mCurrentPlayPosition = POSITION_RIGHT;
            doArrowLeft2RightAnimation();
            play(mCurrentRightVideoInfo);
        }
        else if (mCurrentPlayPosition == POSITION_RIGHT) {
            mCurrentPlayPosition = POSITION_LEFT;
            doArrowRight2LeftAnimation();
            play(mCurrentLeftVideoInfo);
        }
    }

    private void play(BaseVideoInfo videoInfo) {
        mVideoView.setVideoInfo(videoInfo);
        mVideoView.startPlay();

        onPlay(videoInfo.getVideoUrl());
    }

    private void onPlay(String url) {
        if (this.isDetached()) {
            return;
        }
        CurrentVideoInfo currentVideoInfo;
        if (url.equals(mCurrentLeftVideoInfo.getVideoUrl())) {
            currentVideoInfo = mCurrentLeftVideoInfo;
            mDisplayArea.setBackgroundColor(getResources().getColor(R.color.play_left_color));
            mTitle.setBackgroundColor(getResources().getColor(R.color.play_left_color));
            mBottomLeftLayout.setBackgroundColor(getResources().getColor(R.color.play_left_color));
            mBottomRightLayout.setBackgroundColor(getResources().getColor(
                    R.color.play_right_dark_color));
        }
        else if (url.equals(mCurrentRightVideoInfo.getVideoUrl())) {
            currentVideoInfo = mCurrentRightVideoInfo;
            mDisplayArea.setBackgroundColor(getResources().getColor(R.color.play_right_color));
            mTitle.setBackgroundColor(getResources().getColor(R.color.play_right_color));
            mBottomRightLayout.setBackgroundColor(getResources().getColor(
                    R.color.play_right_color));
            mBottomLeftLayout.setBackgroundColor(getResources().getColor(
                    R.color.play_left_dark_color));
        }
        else {
            // Error!
            throw new RuntimeException("Current play url error!");
        }

        mTitle.setText(getString(R.string.topic) + getString(R.string.colon) +
                currentVideoInfo.topicInfo.getTitle());
    }

    private int arrowLeftOffsetX = 0;
    private int getArrowLeftOffsetX() {
        if (arrowLeftOffsetX == 0) {
            int screenWidth = Utils.getScreenWidthPixel(getActivity());
            int arrowWidth = mArrow.getWidth();
            arrowLeftOffsetX = screenWidth / 4 - arrowWidth / 2;
        }
        return arrowLeftOffsetX;
    }

    private int arrowRightOffsetX = 0;
    private int getArrowRightOffsetX() {
        if (arrowRightOffsetX == 0) {
            int screenWidth = Utils.getScreenWidthPixel(getActivity());
            int arrowWidth = mArrow.getWidth();
            arrowRightOffsetX = screenWidth / 4 * 3 - arrowWidth / 2;
        }
        return arrowRightOffsetX;
    }

    private static final long ARROW_ANIMATION_DURATION = 350; // ms
    private void doArrowLeft2RightAnimation() {
        ObjectAnimator.ofFloat(mArrow, "x", getArrowLeftOffsetX(), getArrowRightOffsetX())
                .setDuration(ARROW_ANIMATION_DURATION)
                .start();
    }

    private void doArrowRight2LeftAnimation() {
        ObjectAnimator.ofFloat(mArrow, "x", getArrowRightOffsetX(), getArrowLeftOffsetX())
                .setDuration(ARROW_ANIMATION_DURATION)
                .start();
    }

    private void refreshUserInfo() {
        ImageLoader.getInstance().displayImage(mCurrentLeftVideoInfo.userInfo.getAvatar(),
                mLeftAvatar, ImageLoaderHelper.avatarLoadOptions);
        ImageLoader.getInstance().displayImage(mCurrentRightVideoInfo.userInfo.getAvatar(),
                mRightAvatar, ImageLoaderHelper.avatarLoadOptions);

        if (mCurrentLeftVideoInfo.userInfo.getType() == TypedUserInfo.TYPE_SELF) {
            mLeftFollowBtn.setVisibility(View.INVISIBLE);
        }
        else {
            mLeftFollowBtn.setVisibility(View.VISIBLE);
            if (mCurrentLeftVideoInfo.userInfo.getType() == TypedUserInfo.TYPE_FOLLOWED
                    || mCurrentLeftVideoInfo.userInfo.getType() == TypedUserInfo.TYPE_FRIEND) {
                mLeftFollowBtn.setText(R.string.followed);
                mLeftFollowBtn.setEnabled(false);
                mLeftFollowBtn.setBackgroundResource(R.drawable.play_follow_btn_followed_bg);
            } else {
                mLeftFollowBtn.setText(R.string.follow);
                mLeftFollowBtn.setEnabled(true);
                mLeftFollowBtn.setBackgroundResource(R.drawable.play_follow_left_btn_bg);
            }
        }

        if (mCurrentRightVideoInfo.userInfo.getType() == TypedUserInfo.TYPE_SELF) {
            mRightFollowBtn.setVisibility(View.INVISIBLE);
        }
        else {
            mRightFollowBtn.setVisibility(View.VISIBLE);
            if (mCurrentRightVideoInfo.userInfo.getType() == TypedUserInfo.TYPE_FOLLOWED
                    || mCurrentRightVideoInfo.userInfo.getType() == TypedUserInfo.TYPE_FRIEND) {
                mRightFollowBtn.setText(R.string.followed);
                mRightFollowBtn.setEnabled(false);
                mRightFollowBtn.setBackgroundResource(R.drawable.play_follow_btn_followed_bg);
            } else {
                mRightFollowBtn.setText(R.string.follow);
                mRightFollowBtn.setEnabled(true);
                mRightFollowBtn.setBackgroundResource(R.drawable.play_follow_right_btn_bg);
            }
        }
    }

    public static void startThisFragment(Context context, String squareId) {
        Bundle extras = new Bundle();
        extras.putString(KEY_SQUARE_ID, squareId);
        TitleFragmentActivity.launchFragment(context, PlayFragment.class, extras);
    }

    class CurrentVideoInfo extends BaseVideoInfo {
        private TypedUserInfo userInfo;
        private BaseTopicInfo topicInfo;

        public CurrentVideoInfo(JSONObject jsonObject) {
            super(jsonObject);

            try {
                if (jsonObject.has("user")) {
                    userInfo = new TypedUserInfo(jsonObject.getJSONObject("user"));
                    mUserInfo = userInfo;
                }
                if (jsonObject.has("topic")) {
                    topicInfo = new BaseTopicInfo(jsonObject.getJSONObject("topic"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
