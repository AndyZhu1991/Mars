package com.koolew.mars;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.TypedUserInfo;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.utils.VideoLoader;
import com.koolew.mars.webapi.ApiErrorCode;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by jinchangzhu on 9/22/15.
 */
public class PlayFragment extends MainBaseFragment implements View.OnClickListener,
        VideoLoader.LoadListener, SurfaceHolder.Callback, IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnCompletionListener {

    private static final int MODE_WATCH = 1;
    private static final int MODE_RESULT = 2;
    private static final int MODE_FINISH = 3;

    private static final int POSITION_LEFT = 0;
    private static final int POSITION_RIGHT = 1;

    private static final int JUDGE_STATUS_FALSE = 0;
    private static final int JUDGE_STATUS_TRUE = 1;
    private static final int JUDGE_STATUS_TIME_OUT = 2;

    private TextView mTitle;

    private FrameLayout mDisplayArea;
    private SurfaceView mPlaySurface;
    private ImageView mVideoThumb;
    private ImageView mFinishedImage;
    private ProgressBar mLoadingProgress;

    private View mTopResultFrame;

    private TextView mLeftResultText;
    private CircleImageView mLeftAvatar;
    private TextView mLeftSupportBtn;
    private TextView mLeftKooCountText;
    private TextView mLeftFollowBtn;
    private View mTopLeftShader;
    private View mBottomLeftLayout;
    private TextView mLeftUserDesc;

    private TextView mRightResultText;
    private CircleImageView mRightAvatar;
    private TextView mRightSupportBtn;
    private TextView mRightKooCountText;
    private TextView mRightFollowBtn;
    private View mTopRightShader;
    private View mBottomRightLayout;
    private TextView mRightUserDesc;

    private View mBottomResultFrame;
    private TextView mResultText;
    private TextView mNextGroupText;

    private View mFinishLayout;
    private View mCongratulationText;
    private View mFinishedText;
    private TextView mNextRoundCountDownText;
    private View mNextRoundImmediatelyBtn;
    private TextView mNextRoundPayText;

    private View mBlockTouchFrame;

    private int mMode;

    private CurrentVideoInfo mCurrentLeftVideoInfo;
    private CurrentVideoInfo mCurrentRightVideoInfo;

    private String mJudgedVideoId;

    private int mCurrentGroup;
    private int mTotalGroup;
    private long mWaitTime;
    private int mNextRoundPay = 10; // Default: 10

    private BaseVideoInfo mLastLeftVideoInfo;
    private BaseVideoInfo mLastRightVideoInfo;
    private String mLastLeftUserId;
    private String mLastRightUserId;

    private IjkMediaPlayer mMediaPlayer;
    private PlayerRecycler mPlayerRecycler;
    private int mCurrentPlayPosition;

    private VideoLoader mVideoLoader;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarInterface.setToolbarTitle(getString(R.string.title_play));
        mToolbarInterface.setToolbarColor(0xFF1F1F1F);

        mCurrentPlayPosition = POSITION_LEFT;

        mPlayerRecycler = new PlayerRecycler();

        mVideoLoader = new VideoLoader(getActivity());
        mVideoLoader.setLoadListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mToolbarInterface.setTopIconCount(1);
        mToolbarInterface.setTopIconImageResource(0, R.mipmap.ic_play_share);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_play, container, false);

        mTitle = (TextView) root.findViewById(R.id.title);

        int screenWidthPix = Utils.getScreenWidthPixel(getActivity());
        int displayAreaPadding = getResources().getDimensionPixelSize(R.dimen.display_area_padding);
        int displayAreaHeight = (screenWidthPix - displayAreaPadding * 2) / 4 * 3
                + displayAreaPadding * 2;
        mDisplayArea = (FrameLayout) root.findViewById(R.id.display_area);
        mDisplayArea.getLayoutParams().height = displayAreaHeight;
        mPlaySurface = (SurfaceView) root.findViewById(R.id.play_surface);
        mPlaySurface.getHolder().addCallback(this);
        mVideoThumb = (ImageView) root.findViewById(R.id.video_thumb);
        mFinishedImage = (ImageView) root.findViewById(R.id.finished_image);
        mLoadingProgress = (ProgressBar) root.findViewById(R.id.loading_progress);

        mTopResultFrame = root.findViewById(R.id.top_result_frame);

        mLeftResultText = (TextView) root.findViewById(R.id.left_result_text);
        mLeftAvatar = (CircleImageView) root.findViewById(R.id.left_avatar);
        mLeftAvatar.setOnClickListener(this);
        mLeftSupportBtn = (TextView) root.findViewById(R.id.left_support_btn);
        mLeftSupportBtn.setOnClickListener(this);
        mLeftKooCountText = (TextView) root.findViewById(R.id.left_koo_count_text);
        mLeftFollowBtn = (TextView) root.findViewById(R.id.left_follow_btn);
        mLeftFollowBtn.setOnClickListener(this);
        mTopLeftShader = root.findViewById(R.id.top_left_shader);
        mBottomLeftLayout = root.findViewById(R.id.bottom_left_layout);
        mBottomLeftLayout.setOnClickListener(this);
        mLeftUserDesc = (TextView) root.findViewById(R.id.left_user_desc);

        mRightResultText = (TextView) root.findViewById(R.id.right_result_text);
        mRightAvatar = (CircleImageView) root.findViewById(R.id.right_avatar);
        mRightAvatar.setOnClickListener(this);
        mRightSupportBtn = (TextView) root.findViewById(R.id.right_support_btn);
        mRightSupportBtn.setOnClickListener(this);
        mRightKooCountText = (TextView) root.findViewById(R.id.right_koo_count_text);
        mRightFollowBtn = (TextView) root.findViewById(R.id.right_follow_btn);
        mRightFollowBtn.setOnClickListener(this);
        mTopRightShader = root.findViewById(R.id.top_right_shader);
        mBottomRightLayout = root.findViewById(R.id.bottom_right_layout);
        mBottomRightLayout.setOnClickListener(this);
        mRightUserDesc = (TextView) root.findViewById(R.id.right_user_desc);

        mBottomResultFrame = root.findViewById(R.id.bottom_result_frame);
        mResultText = (TextView) root.findViewById(R.id.result_text);
        mNextGroupText = (TextView) root.findViewById(R.id.next_group);
        mNextGroupText.setOnClickListener(this);

        mFinishLayout = root.findViewById(R.id.finish_layout);
        mCongratulationText = root.findViewById(R.id.congratulation_text);
        mFinishedText = root.findViewById(R.id.finished_text);
        mNextRoundCountDownText = (TextView) root.findViewById(R.id.next_round_count_down_text);
        mNextRoundImmediatelyBtn = root.findViewById(R.id.play_next_round_immediately);
        mNextRoundImmediatelyBtn.setOnClickListener(this);
        mNextRoundPayText = (TextView) root.findViewById(R.id.next_round_pay_text);

        mBlockTouchFrame = root.findViewById(R.id.block_touch_frame);
        mBlockTouchFrame.setOnClickListener(this);

        requestDefaultGroup();

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destoryNextRoundCountDownTimer();
        mPlayerRecycler.destory();
    }

    @Override
    public void onTopIconClick(int position) {
        if (mMode == MODE_FINISH) {
            return;
        }
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
            case R.id.next_group:
                onNextGroupClick();
                break;
            case R.id.play_next_round_immediately:
                onPlayNextRoundImmediatelyClick();
                break;
        }
    }

    private void onLeftAvatarClick() {
        FriendInfoActivity.startThisActivity(getActivity(), mLastLeftUserId);
    }

    private void onLeftSupportClick() {
        judge(mCurrentLeftVideoInfo.getVideoId());
    }

    private void onLeftFollowClick() {
        mLeftFollowBtn.setEnabled(false);
        ApiWorker.getInstance().followUser(mLastLeftUserId,
                new FollowUserListener(mLeftFollowBtn), null);
    }

    private void onBottomLeftLayoutClick() {
        if (mCurrentPlayPosition == POSITION_RIGHT &&
                mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mLoadingProgress.setVisibility(View.VISIBLE);
            switchVideo();
        }
    }

    private void onRightAvatarClick() {
        FriendInfoActivity.startThisActivity(getActivity(), mLastRightUserId);
    }

    private void onRightSupportClick() {
        judge(mCurrentRightVideoInfo.getVideoId());
    }

    private void onRightFollowClick() {
        mRightFollowBtn.setEnabled(false);
        ApiWorker.getInstance().followUser(mLastRightUserId,
                new FollowUserListener(mRightFollowBtn), null);
    }

    private void onBottomRightLayoutClick() {
        if (mCurrentPlayPosition == POSITION_LEFT &&
                mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mLoadingProgress.setVisibility(View.VISIBLE);
            switchVideo();
        }
    }

    private void onNextGroupClick() {
        nextGroup();
    }

    private void onPlayNextRoundImmediatelyClick() {
        mBlockTouchFrame.setVisibility(View.VISIBLE);
        if (mWaitTime == 0) {
            ApiWorker.getInstance().requestDefaultPlayGroup(new DefaultGroupListener(),
                    new DefaultGroupErrorListener());
        }
        else {
            ApiWorker.getInstance().requestPayPlayGroup(new PayRoundListener(),
                    new DefaultGroupErrorListener());
        }
    }

    private class PayRoundListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            mBlockTouchFrame.setVisibility(View.INVISIBLE);
            try {
                int code = response.getInt("code");
                if (code == 0) {
                    updateCurrentGroup(response.getJSONObject("result").getJSONObject("next"));
                    if (!hasNextGroup()) {
                        mLoadingProgress.setVisibility(View.INVISIBLE);
                        switchToFinishMode();
                        return;
                    }

                    startPlayGroup();
                }
                else if (code == ApiErrorCode.COIN_NOT_ENOUGH) {
                    Toast.makeText(getActivity(), R.string.play_not_enough_coin, Toast.LENGTH_LONG)
                            .show();
                }
                else {
                    onDefaultGroupError(response.getString("msg"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

    private void requestDefaultGroup() {
        mBlockTouchFrame.setVisibility(View.VISIBLE);
        ApiWorker.getInstance().requestDefaultPlayGroup(
                new DefaultGroupListener(), new DefaultGroupErrorListener());
    }

    class DefaultGroupListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            mBlockTouchFrame.setVisibility(View.INVISIBLE);
            try {
                if (response.getInt("code") == 0) {
                    updateCurrentGroup(response.getJSONObject("result").getJSONObject("next"));
                    if (!hasNextGroup()) {
                        mLoadingProgress.setVisibility(View.INVISIBLE);
                        switchToFinishMode();
                        return;
                    }

                    startPlayGroup();
                }
                else {
                    onDefaultGroupError(response.getString("msg"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class DefaultGroupErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            mBlockTouchFrame.setVisibility(View.INVISIBLE);
            onDefaultGroupError(error.getLocalizedMessage());
        }
    }

    private void onDefaultGroupError(String errMsg) {
        Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
    }


    private void judge(String videoId) {
        mJudgedVideoId = videoId;
        stopPlayGroup();
        mBlockTouchFrame.setVisibility(View.VISIBLE);
        ApiWorker.getInstance().judgeVideo(videoId, mJudgeListener, mJudgeErrorListener);
    }

    private Response.Listener<JSONObject> mJudgeListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mBlockTouchFrame.setVisibility(View.INVISIBLE);
            try {
                int code = response.getInt("code");
                if (code == 0) {
                    JSONObject result = response.getJSONObject("result");
                    JSONObject last = result.getJSONObject("last");
                    JSONArray videos = last.getJSONArray("videos");
                    updateLastVideoInfo(videos);
                    int status = last.getInt("status");
                    if (status == 2) {
                        onJudgeTimeout();
                    }
                    else {
                        onJudgeResult(status);
                    }

                    updateCurrentGroup(result.getJSONObject("next"));
                }
                else if (code == ApiErrorCode.COIN_NOT_ENOUGH) {
                    Toast.makeText(getActivity(), R.string.play_not_enough_coin, Toast.LENGTH_SHORT)
                            .show();
                }
                else {
                    onJudgeError(response.getString("msg"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.ErrorListener mJudgeErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mBlockTouchFrame.setVisibility(View.INVISIBLE);
            onJudgeError(error.getLocalizedMessage());
        }
    };

    private void onJudgeError(String errMsg) {
        Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
    }

    private void updateLastVideoInfo(JSONArray videos) {
        mLastLeftUserId = mCurrentLeftVideoInfo.userInfo.getUid();
        mLastRightUserId = mCurrentRightVideoInfo.userInfo.getUid();
        try {
            BaseVideoInfo videoInfo0 = new BaseVideoInfo(videos.getJSONObject(0));
            BaseVideoInfo videoInfo1 = new BaseVideoInfo(videos.getJSONObject(1));
            if (mCurrentLeftVideoInfo.getVideoId().equals(videoInfo0.getVideoId())) {
                mLastLeftVideoInfo = videoInfo0;
                mLastRightVideoInfo = videoInfo1;
            }
            else {
                mLastLeftVideoInfo = videoInfo1;
                mLastRightVideoInfo = videoInfo0;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static final float SHADER_COLOR_ALPHA = 0.7f;
    private static final float AVATAR_COLOR_ALPHA = 1 - SHADER_COLOR_ALPHA;
    private static final int AVATAR_COLOR_ELEMENT = (int) (255 * AVATAR_COLOR_ALPHA);
    private static final ColorFilter LOSE_COLOR_FILTER = new LightingColorFilter(
            Color.rgb(AVATAR_COLOR_ELEMENT, AVATAR_COLOR_ELEMENT, AVATAR_COLOR_ELEMENT),
                    Color.rgb((int) (255 * SHADER_COLOR_ALPHA), (int) (86 * SHADER_COLOR_ALPHA),
                            (int) (86 * SHADER_COLOR_ALPHA))
    );
    private static final ColorFilter WIN_COLOR_FILTER = new LightingColorFilter(
            Color.rgb(AVATAR_COLOR_ELEMENT, AVATAR_COLOR_ELEMENT, AVATAR_COLOR_ELEMENT),
                    Color.rgb((int) (125 * SHADER_COLOR_ALPHA), (int) (139 * SHADER_COLOR_ALPHA),
                            (int) (151 * SHADER_COLOR_ALPHA))
    );
    private void onJudgeResult(int resultStatus) {
        switchToResultMode(resultStatus);

        int winPosition;
        if (mLastLeftVideoInfo.getKooTotal() > mLastRightVideoInfo.getKooTotal()) {
            winPosition = POSITION_LEFT;
        }
        else if (mLastRightVideoInfo.getKooTotal() > mLastLeftVideoInfo.getKooTotal()) {
            winPosition = POSITION_RIGHT;
        }
        else {
            if (mJudgedVideoId.equals(mLastLeftVideoInfo.getVideoId())) {
                winPosition = POSITION_LEFT;
            }
            else {
                winPosition = POSITION_RIGHT;
            }
        }
        if (winPosition == POSITION_LEFT) {
            mLeftResultText.setText(R.string.win);
            mLeftAvatar.setColorFilter(WIN_COLOR_FILTER);
            mRightResultText.setText(R.string.lose);
            mRightAvatar.setColorFilter(LOSE_COLOR_FILTER);
        }
        else {
            mRightResultText.setText(R.string.win);
            mRightAvatar.setColorFilter(WIN_COLOR_FILTER);
            mLeftResultText.setText(R.string.lose);
            mLeftAvatar.setColorFilter(LOSE_COLOR_FILTER);
        }
    }

    private void onJudgeTimeout() {

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
            mCurrentGroup = jsonObject.getInt("cur");
            mTotalGroup = jsonObject.getInt("total");
            mWaitTime = jsonObject.getLong("wait_time");
            mNextRoundPay = jsonObject.getInt("pay");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startPlayGroup() {
        switchToWatchMode();

        mCurrentPlayPosition = POSITION_LEFT;
        mLoadingProgress.setVisibility(View.VISIBLE);
        mVideoLoader.loadVideo(null, mCurrentLeftVideoInfo.getVideoUrl());
    }

    private void stopPlayGroup() {
        mPlayerRecycler.recycle(mMediaPlayer);
        mMediaPlayer = null;
    }

    // VideoLoader.LoadListener
    @Override
    public void onLoadComplete(Object player, String url, String filePath) {
        mLoadingProgress.setVisibility(View.INVISIBLE);
        if ((url.equals(mCurrentLeftVideoInfo.getVideoUrl())) && mMediaPlayer == null) {
            mVideoLoader.loadVideo(null, mCurrentRightVideoInfo.getVideoUrl());
        }

        if ((url.equals(mCurrentLeftVideoInfo.getVideoUrl())
                && mCurrentPlayPosition == POSITION_LEFT)
                ||
                (url.equals(mCurrentRightVideoInfo.getVideoUrl())
                        && mCurrentPlayPosition == POSITION_RIGHT)) {
            try {
                mPlayerRecycler.recycle(mMediaPlayer);
                mMediaPlayer = mPlayerRecycler.obtain();
                mMediaPlayer.setDataSource(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.prepareAsync();
            onPlay(url);
        }
    }

    @Override
    public void onLoadProgress(String url, float progress) {
    }


    // SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(mPlaySurface.getHolder());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }


    // IjkPlayer
    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mMediaPlayer.start();
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        switchVideo();
    }

    private void switchVideo() {
        if (mCurrentPlayPosition == POSITION_LEFT) {
            mCurrentPlayPosition = POSITION_RIGHT;
            mVideoLoader.loadVideo(null, mCurrentRightVideoInfo.getVideoUrl());
        }
        else if (mCurrentPlayPosition == POSITION_RIGHT) {
            mCurrentPlayPosition = POSITION_LEFT;
            mLoadingProgress.setVisibility(View.VISIBLE);
            mVideoLoader.loadVideo(null, mCurrentLeftVideoInfo.getVideoUrl());
        }
    }

    private void onPlay(String url) {
        CurrentVideoInfo currentVideoInfo;
        if (url.equals(mCurrentLeftVideoInfo.getVideoUrl())) {
            currentVideoInfo = mCurrentLeftVideoInfo;
            mDisplayArea.setBackgroundColor(getResources().getColor(R.color.play_left_color));
            mBottomLeftLayout.setBackgroundColor(getResources().getColor(R.color.play_left_color));
            mBottomRightLayout.setBackgroundColor(getResources().getColor(
                    R.color.play_right_dark_color));
            mLeftUserDesc.setTextColor(Color.WHITE);
            mRightUserDesc.setTextColor(0x4CFFFFFF);
        }
        else if (url.equals(mCurrentRightVideoInfo.getVideoUrl())) {
            currentVideoInfo = mCurrentRightVideoInfo;
            mDisplayArea.setBackgroundColor(getResources().getColor(R.color.play_right_color));
            mBottomRightLayout.setBackgroundColor(getResources().getColor(
                    R.color.play_right_color));
            mBottomLeftLayout.setBackgroundColor(getResources().getColor(
                    R.color.play_left_dark_color));
            mRightUserDesc.setTextColor(Color.WHITE);
            mLeftUserDesc.setTextColor(0x4CFFFFFF);
        }
        else {
            // Error!
            throw new RuntimeException("Current play url error!");
        }

        mTitle.setText(currentVideoInfo.topicInfo.getTitle());
    }

    private void switchToWatchMode() {
        if (mMode == MODE_RESULT) {
            mTopResultFrame.setVisibility(View.INVISIBLE);
            mBottomResultFrame.setVisibility(View.INVISIBLE);
        }
        else if (mMode == MODE_FINISH) {
            mFinishLayout.setVisibility(View.INVISIBLE);
            mFinishedImage.setVisibility(View.INVISIBLE);
        }

        mToolbarInterface.setToolbarMiddleTitle(String.format("%d/%d", mCurrentGroup, mTotalGroup));

        mMode = MODE_WATCH;
    }

    private void switchToResultMode(int judgeStatus) {
        mTopResultFrame.setVisibility(View.VISIBLE);
        mBottomResultFrame.setVisibility(View.VISIBLE);

        if (mJudgedVideoId.equals(mLastLeftVideoInfo.getVideoId())) {
            mTopLeftShader.setVisibility(View.INVISIBLE);
            mTopRightShader.setVisibility(View.VISIBLE);
        }
        else {
            mTopLeftShader.setVisibility(View.VISIBLE);
            mTopRightShader.setVisibility(View.INVISIBLE);
        }

        refreshUserInfo();

        if (judgeStatus == JUDGE_STATUS_FALSE) {
            mResultText.setText(R.string.guess_false_message);
        }
        else if (judgeStatus == JUDGE_STATUS_TRUE) {
            mResultText.setText("+5");
        }

        mToolbarInterface.setToolbarMiddleTitle("");

        mMode = MODE_RESULT;
    }

    private void switchToFinishMode() {
        mTopResultFrame.setVisibility(View.INVISIBLE);
        mBottomResultFrame.setVisibility(View.INVISIBLE);
        mFinishedImage.setVisibility(View.VISIBLE);
        mFinishLayout.setVisibility(View.VISIBLE);

        if (mNextRoundImmediatelyBtn.getBottom() > mFinishLayout.getHeight()) {
            mCongratulationText.setVisibility(View.GONE);
            mFinishedText.setVisibility(View.GONE);
        }

        mNextRoundCountDownTimer = new Timer();
        mNextRoundCountDownTimer.schedule(new NextRoundCountDownTimerTask(), 0, 1000);
        mNextRoundPayText.setText("-" + mNextRoundPay);

        mTitle.setText("");

        mMode = MODE_FINISH;
    }

    private void refreshUserInfo() {
        ImageLoader.getInstance().displayImage(mCurrentLeftVideoInfo.userInfo.getAvatar(),
                mLeftAvatar, ImageLoaderHelper.avatarLoadOptions);
        ImageLoader.getInstance().displayImage(mCurrentRightVideoInfo.userInfo.getAvatar(),
                mRightAvatar, ImageLoaderHelper.avatarLoadOptions);

        mLeftKooCountText.setText(String.valueOf(mLastLeftVideoInfo.getKooTotal()));
        mRightKooCountText.setText(String.valueOf(mLastRightVideoInfo.getKooTotal()));

        if (mCurrentLeftVideoInfo.userInfo.getType() == TypedUserInfo.TYPE_FOLLOWED
                || mCurrentLeftVideoInfo.userInfo.getType() == TypedUserInfo.TYPE_FRIEND) {
            mLeftFollowBtn.setText(R.string.followed);
            mLeftFollowBtn.setEnabled(false);
        }
        else {
            mLeftFollowBtn.setText(R.string.follow);
            mLeftFollowBtn.setEnabled(true);
        }
        if (mCurrentRightVideoInfo.userInfo.getType() == TypedUserInfo.TYPE_FOLLOWED
                || mCurrentRightVideoInfo.userInfo.getType() == TypedUserInfo.TYPE_FRIEND) {
            mRightFollowBtn.setText(R.string.followed);
            mRightFollowBtn.setEnabled(false);
        }
        else {
            mRightFollowBtn.setText(R.string.follow);
            mRightFollowBtn.setEnabled(true);
        }
    }

    private Timer mNextRoundCountDownTimer;

    class NextRoundCountDownTimerTask extends TimerTask {
        @Override
        public void run() {
            mNextRoundCountDownText.post(new Runnable() {
                @Override
                public void run() {
                    int second = (int) (mWaitTime % 60);
                    int minute = (int) (mWaitTime / 60 % 60);
                    int hour = (int) (mWaitTime / 60 / 60);
                    mNextRoundCountDownText.setText(
                            String.format("%02d:%02d:%02d", hour, minute, second));

                    mWaitTime--;
                    if (mWaitTime == 0) {
                        destoryNextRoundCountDownTimer();
                        nextFreeRoundAvailable();
                    }
                }
            });
        }
    }

    private void destoryNextRoundCountDownTimer() {
        if (mNextRoundCountDownTimer != null) {
            mNextRoundCountDownTimer.cancel();
            mNextRoundCountDownTimer = null;
        }
    }

    private void nextFreeRoundAvailable() {
        mNextRoundPayText.setVisibility(View.GONE);
    }

    private void nextGroup() {
        if (hasNextGroup()) {
            startPlayGroup();
        }
        else {
            switchToFinishMode();
        }
    }

    private boolean hasNextGroup() {
        return mCurrentLeftVideoInfo != null &&
                !TextUtils.isEmpty(mCurrentLeftVideoInfo.getVideoId());
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


    class PlayerRecycler {
        private Stack<IjkMediaPlayer> mStack = new Stack<>();

        public IjkMediaPlayer obtain() {
            IjkMediaPlayer player;
            if (mStack.size() == 0) {
                player = newPlayer();
            }
            else {
                player = mStack.pop();
            }
            player.setDisplay(mPlaySurface.getHolder());
            return player;
        }

        private IjkMediaPlayer newPlayer() {
            IjkMediaPlayer player = new IjkMediaPlayer();
            player.setOnPreparedListener(PlayFragment.this);
            player.setOnCompletionListener(PlayFragment.this);
            return player;
        }

        public void recycle(final IjkMediaPlayer player) {
            new Thread() {
                @Override
                public void run() {
                    recycleSync(player);
                }
            }.start();
        }

        private void recycleSync(IjkMediaPlayer player) {
            if (player == null) {
                return;
            }
            if (player.isPlaying()) {
                player.stop();
            }
            player.reset();
            synchronized (mStack) {
                mStack.push(player);
            }
        }

        public void destory() {
            new Thread() {
                @Override
                public void run() {
                    recycleSync(mMediaPlayer);
                    while (mStack.size() > 0) {
                        mStack.pop().release();
                    }
                }
            }.start();
        }
    }
}
