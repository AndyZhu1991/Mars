package com.koolew.mars;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.koolew.mars.danmaku.DanmakuItemInfo;
import com.koolew.mars.danmaku.DanmakuShowManager;
import com.koolew.mars.danmaku.DanmakuThread;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.MaxLengthWatcher;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.utils.VideoLoader;
import com.koolew.mars.view.TitleBarView;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class SendDanmakuActivity extends Activity
        implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener,
        TextView.OnEditorActionListener, View.OnTouchListener {

    private static final String TAG = "koolew-SendDanmakuA";

    public static final String KEY_VIDEO_JSON = "video_json";

    private JSONObject mVideoItem;

    private FrameLayout mPlayLayout;
    private SurfaceView mPlaySurface;
    private ImageView mThumb;

    private TitleBarView mTitleBar;
    private EditText mDanmakuEdit;
    private View mBottomLayout;

    private IjkMediaPlayer mMediaPlayer;
    private VideoLoader mVideoLoader;

    private ViewGroup mDanmakuContainer;
    private DanmakuShowManager mDanmakuManager;
    private DanmakuThread mDanmakuThread;
    private ViewGroup mSendingDanmakuLayout;
    private View mSendingDanmaku;
    private ProgressDialog mProgressDialog;

    private long mVideoLength = 0; // In ms


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_danmaku);

        try {
            mVideoItem = new JSONObject(getIntent().getStringExtra(KEY_VIDEO_JSON));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        initViews();

        initMembers();
    }

    private void initMembers() {
        try {
            JSONArray comment;
            if (mVideoItem.has("comment")) {
                comment = mVideoItem.getJSONArray("comment");
            }
            else {
                comment = new JSONArray();
            }
            mDanmakuManager = new DanmakuShowManager(this, mDanmakuContainer,
                    DanmakuItemInfo.fromJSONArray(comment));
            mDanmakuThread = new DanmakuThread(this, mDanmakuManager, new DanmakuThread.PlayerWrapper() {
                @Override
                public long getCurrentPosition() {
                    return mMediaPlayer.getCurrentPosition();
                }

                @Override
                public boolean isPlaying() {
                    return mMediaPlayer.isPlaying();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mVideoLoader = new VideoLoader(this);
        mVideoLoader.setLoadListener(new VideoLoader.LoadListener() {
            @Override
            public void onLoadComplete(Object player, String url, String filePath) {
                try {
                    mMediaPlayer.setDataSource(filePath);
                    mMediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLoadProgress(String url, float progress) {
            }
        });
    }

    private void initViews() {
        mPlayLayout = (FrameLayout) findViewById(R.id.play_layout);
        RelativeLayout.LayoutParams plp = (RelativeLayout.LayoutParams) mPlayLayout.getLayoutParams();
        plp.height = Utils.getScreenWidthPixel(this) / 4 * 3;
        mPlayLayout.setLayoutParams(plp);

        mPlaySurface = (SurfaceView) findViewById(R.id.play_surface);
        mPlaySurface.getHolder().addCallback(mSurfaceCallback);
        mThumb = (ImageView) findViewById(R.id.thumb);
        try {
            ImageLoader.getInstance().displayImage(mVideoItem.getString("thumb_url"), mThumb);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mDanmakuContainer = (ViewGroup) findViewById(R.id.danmaku_container);
        mDanmakuContainer.setOnTouchListener(this);
        mSendingDanmakuLayout = (ViewGroup) findViewById(R.id.sending_danmaku_layout);
        mSendingDanmakuLayout.setOnTouchListener(this);

        mTitleBar = (TitleBarView) findViewById(R.id.title_bar);

        mDanmakuEdit = (EditText) findViewById(R.id.danmaku_edit);
        mDanmakuEdit.setOnEditorActionListener(this);
        mDanmakuEdit.addTextChangedListener(
                new DanmakuLengthWatcher(AppProperty.DANMAKU_MAX_WORD, mDanmakuEdit));
        mBottomLayout = findViewById(R.id.bottom_layout);

        Utils.showSoftKeyInput(mDanmakuEdit, 300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDanmakuThread.stopDanmaku();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (mDanmakuEdit.getText().length() == 0) {
                Toast.makeText(SendDanmakuActivity.this,
                        R.string.danmaku_no_word_hint, Toast.LENGTH_SHORT).show();
                return true;
            }
            else {
                mTitleBar.setVisibility(View.INVISIBLE);
                mDanmakuEdit.setVisibility(View.INVISIBLE);
                mSendingDanmakuLayout.setVisibility(View.INVISIBLE);
                mBottomLayout.setVisibility(View.VISIBLE);

                mSendingDanmaku = LayoutInflater.from(this).inflate(R.layout.danmaku_item, null);
                ImageLoader.getInstance().displayImage(MyAccountInfo.getAvatar(),
                        (ImageView) mSendingDanmaku.findViewById(R.id.avatar));
                ((TextView) mSendingDanmaku.findViewById(R.id.message)).setText(mDanmakuEdit.getText());

                mSendingDanmakuLayout.addView(mSendingDanmaku);
                mSendingDanmaku.setVisibility(View.INVISIBLE);

                mMediaPlayer.start();
                mDanmakuThread.start();
                new Thread() {
                    @Override
                    public void run() {
                        while (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                            try {
                                Thread.sleep(40); // 40ms == 1frame, 25fps
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (mMediaPlayer.getCurrentPosition() > 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mThumb.setVisibility(View.INVISIBLE);
                                    }
                                });

                                break; // break while
                            }
                        }
                    }
                }.start();
            }
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mSendingDanmaku == null) {
            return false;
        }

        if (v == mDanmakuContainer) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mMediaPlayer.pause();
                mSendingDanmakuLayout.setVisibility(View.VISIBLE);
                moveDanmakuTo((int) event.getX(), (int) event.getY());
                mSendingDanmaku.setVisibility(View.VISIBLE);
            }
            return true;
        }
        else if (v == mSendingDanmakuLayout) {
            moveDanmakuTo((int) event.getX(), (int) event.getY());
            return true;
        }

        return false;
    }

    public void onSendClick(View v) {
        if (mSendingDanmaku.getVisibility() == View.INVISIBLE) {
            Toast.makeText(this, R.string.danmaku_not_located_hint, Toast.LENGTH_LONG).show();
        }
        else {
            String content = mDanmakuEdit.getText().toString();
            String videoId = null;
            try {
                videoId = mVideoItem.getString("video_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            float showTime = mMediaPlayer.getCurrentPosition() / 1000.0f;
            float x = 1.0f * mSendingDanmaku.getX() / mSendingDanmakuLayout.getWidth();
            float y = 1.0f * mSendingDanmaku.getY() / mSendingDanmakuLayout.getHeight();
            ApiWorker.getInstance().sendDanmaku(content, videoId, showTime, x, y,
                    mResponseListener, null);

            mProgressDialog = DialogUtil.getGeneralProgressDialog(this, R.string.sending_danmaku);
            mProgressDialog.show();
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

    SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mMediaPlayer = new IjkMediaPlayer();
            mMediaPlayer.setDisplay(mPlaySurface.getHolder());
            try {
                mVideoLoader.loadVideo(mMediaPlayer, mVideoItem.getString("video_url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setOnPreparedListener(SendDanmakuActivity.this);
            mMediaPlayer.setOnCompletionListener(SendDanmakuActivity.this);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {}
    };

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


    // IMediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mVideoLength = iMediaPlayer.getDuration();
        mMediaPlayer.pause();
    }
    // IMediaPlayer.OnCompletionListener
    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        mMediaPlayer.start();
    }
}
