package com.koolew.mars;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.koolew.mars.infos.BaseVideoInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.MaxLengthWatcher;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.view.KoolewVideoView;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;


public class SendDanmakuActivity extends BaseActivity
        implements TextView.OnEditorActionListener, View.OnTouchListener {

    private static final String TAG = "koolew-SendDanmakuA";

    public static final String KEY_VIDEO_INFO = "video info";

    private BaseVideoInfo mVideoInfo;

    private SendDanmakuVideoView mVideoView;

    private TitleBarView mTitleBar;
    private EditText mDanmakuEdit;
    private Button mConfirmBtn;
    private View mBottomLayout;

    private ViewGroup mSendingDanmakuLayout;
    private View mSendingDanmaku;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_danmaku);

        mVideoInfo = (BaseVideoInfo) getIntent().getSerializableExtra(KEY_VIDEO_INFO);

        initViews();
    }

    private void initViews() {
        mVideoView = (SendDanmakuVideoView) findViewById(R.id.video_view);
        mVideoView.setVideoInfo(mVideoInfo);
        mVideoView.startPlay();

        mSendingDanmakuLayout = (ViewGroup) findViewById(R.id.sending_danmaku_layout);
        mSendingDanmakuLayout.setOnTouchListener(this);

        mTitleBar = (TitleBarView) findViewById(R.id.title_bar);

        mDanmakuEdit = (EditText) findViewById(R.id.danmaku_edit);
        mDanmakuEdit.setHint(getString(R.string.danmaku_word_limit_hint, AppProperty.DANMAKU_MAX_WORD));
        mDanmakuEdit.setOnEditorActionListener(this);
        mDanmakuEdit.addTextChangedListener(
                new DanmakuLengthWatcher(AppProperty.DANMAKU_MAX_WORD, mDanmakuEdit));
        mConfirmBtn = (Button) findViewById(R.id.confirm_btn);
        mBottomLayout = findViewById(R.id.bottom_layout);

        Utils.showSoftKeyInput(mDanmakuEdit, 300);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onDanmakuConfirm(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mSendingDanmaku == null) {
            return false;
        }

        if (v == mSendingDanmakuLayout) {
            moveDanmakuTo((int) event.getX(), (int) event.getY());
            if (event.getAction() == MotionEvent.ACTION_UP) {
                pauseVideoPlay();
            }
            return true;
        }

        return false;
    }

    private void pauseVideoPlay() {
        if (mVideoView.getMediaPlayer().isPlaying()) {
            mVideoView.getMediaPlayer().pause();
        }
    }

    public void onSendClick(View v) {
        if (mVideoView.getMediaPlayer().isPlaying()) {
            Toast.makeText(this, R.string.danmaku_no_time_hint, Toast.LENGTH_LONG).show();
        }
        else {
            String content = mDanmakuEdit.getText().toString();
            String videoId = mVideoInfo.getVideoId();
            float showTime = mVideoView.getMediaPlayer().getCurrentPosition() / 1000.0f;
            float x = 1.0f * mSendingDanmaku.getX() / mSendingDanmakuLayout.getWidth();
            float y = 1.0f * mSendingDanmaku.getY() / mSendingDanmakuLayout.getHeight();
            ApiWorker.getInstance().sendDanmaku(content, videoId, showTime, x, y,
                    mResponseListener, null);

            mProgressDialog = DialogUtil.getGeneralProgressDialog(this, R.string.sending_danmaku);
            mProgressDialog.show();
        }
    }

    public void onDanmakuConfirm(View v) {
        if (mDanmakuEdit.getText().length() == 0) {
            Toast.makeText(SendDanmakuActivity.this,
                    R.string.danmaku_no_word_hint, Toast.LENGTH_SHORT).show();
        }
        else {
            mTitleBar.setVisibility(View.INVISIBLE);
            mDanmakuEdit.setVisibility(View.INVISIBLE);
            InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mDanmakuEdit.getWindowToken(), 0);
            mConfirmBtn.setVisibility(View.INVISIBLE);
            mBottomLayout.setVisibility(View.VISIBLE);

            mSendingDanmaku = LayoutInflater.from(this).inflate(R.layout.danmaku_item, null);
            ImageLoader.getInstance().displayImage(MyAccountInfo.getAvatar(),
                    (ImageView) mSendingDanmaku.findViewById(R.id.avatar));
            ((TextView) mSendingDanmaku.findViewById(R.id.message)).setText(mDanmakuEdit.getText());

            mSendingDanmakuLayout.addView(mSendingDanmaku);
            moveDanmakuTo(mSendingDanmakuLayout.getWidth() / 2,
                    mSendingDanmakuLayout.getHeight() / 2);

            mVideoView.locatingDanmaku = true;
            mVideoView.startPlay();
        }
    }

    private Response.Listener<JSONObject> mResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            mProgressDialog.dismiss();
            try {
                if (jsonObject.getInt("code") == 0) {
                    onBackPressed();
                }
                else {
                    Toast.makeText(SendDanmakuActivity.this,
                            R.string.danmaku_send_faild, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void onCancelClick(View v) {
        onBackPressed();
    }

    private void moveDanmakuTo(int x, int y) {
        int targetLeft   = x - mSendingDanmaku.getWidth()  / 2;
        int targetTop    = y - mSendingDanmaku.getHeight() / 2;
        int targetRight  = x + mSendingDanmaku.getWidth()  / 2;
        int targetBottom = y + mSendingDanmaku.getHeight() / 2;

        if (targetBottom > mSendingDanmakuLayout.getHeight()) {
            targetTop -= targetBottom - mSendingDanmakuLayout.getHeight();
        }
        if (targetRight > mSendingDanmakuLayout.getWidth()) {
            targetLeft -= targetRight - mSendingDanmakuLayout.getWidth();
        }
        if (targetTop < 0) {
            targetTop = 0;
        }
        if (targetLeft < 0) {
            targetLeft = 0;
        }

        mSendingDanmaku.setX(targetLeft);
        mSendingDanmaku.setY(targetTop);
    }

    class DanmakuLengthWatcher extends MaxLengthWatcher {
        public DanmakuLengthWatcher(int maxLen, EditText editText) {
            super(maxLen, editText);
        }

        @Override
        public void onTextOverInput() {
            Toast.makeText(SendDanmakuActivity.this,
                    getString(R.string.danmaku_over_input_hint, maxLen),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static class SendDanmakuVideoView extends KoolewVideoView {

        private boolean locatingDanmaku = false;

        public SendDanmakuVideoView(Context context) {
            super(context);
        }

        public SendDanmakuVideoView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected synchronized void start() {
            if (locatingDanmaku) {
                super.start();
            }
        }

        public MediaPlayer getMediaPlayer() {
            return mMediaPlayer;
        }
    }
}
