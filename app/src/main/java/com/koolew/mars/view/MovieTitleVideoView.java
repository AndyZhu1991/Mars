package com.koolew.mars.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.koolew.mars.R;
import com.koolew.mars.utils.Utils;

/**
 * Created by jinchangzhu on 12/9/15.
 */
public class MovieTitleVideoView extends KoolewVideoView implements MediaPlayer.OnCompletionListener {

    private ImageView mPlayImage;

    public MovieTitleVideoView(Context context) {
        super(context);
    }

    public MovieTitleVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        isNeedLooping = false;

        mPlayImage = new ImageView(context);
        mPlayImage.setImageResource(R.mipmap.ic_play);
        FrameLayout.LayoutParams lp = new LayoutParams(
                (int) Utils.dpToPixels(context, 21), (int) Utils.dpToPixels(context, 28));
        lp.gravity = Gravity.CENTER;
        addView(mPlayImage, lp);

        setOnClickListener(mClickListener);
    }

    @Override
    protected MediaPlayer generateMediaPlayer() {
        MediaPlayer mediaPlayer = super.generateMediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        return mediaPlayer;
    }

    @Override
    protected void start() {
        if (mPlayImage.getVisibility() == INVISIBLE) {
            super.start();
        }
    }

    @Override
    public void stop() {
        mPlayImage.setVisibility(VISIBLE);
        super.stop();
    }

    private View.OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPlayImage.getVisibility() == VISIBLE) {
                mPlayImage.setVisibility(INVISIBLE);
                startPlay();
            }
            else {
                mPlayImage.setVisibility(VISIBLE);
                stop();
            }
        }
    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        stop();
    }
}
