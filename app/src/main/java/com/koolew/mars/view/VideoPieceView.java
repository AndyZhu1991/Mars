package com.koolew.mars.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.koolew.mars.AppProperty;
import com.koolew.mars.R;
import com.koolew.mars.video.VideoRecordingSession;

/**
 * Created by jinchangzhu on 7/18/15.
 */
public class VideoPieceView extends View {

    private static final float VIDEO_TOTAL_LEN = AppProperty.RECORD_VIDEO_MAX_LEN;

    private static final int DEFAULT_MARK_COLOR = 0xFF757373;
    private static final int DEFAULT_SELECTED_COLOR = 0xFFF4D288;

    private int mMarkColor;
    private int mSelectedColor;

    private VideoRecordingSession.VideoPieceItem mVideoPieceItem;

    public VideoPieceView(Context context) {
        this(context, null);
    }

    public VideoPieceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPieceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleBarView, 0, 0);
        mMarkColor = a.getColor(R.styleable.VideoPieceView_mark_color, DEFAULT_MARK_COLOR);
        mSelectedColor = a.getColor(R.styleable.VideoPieceView_selected_color, DEFAULT_SELECTED_COLOR);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float videoBegin = mVideoPieceItem.getBeginInSession();
        float videoEnd = mVideoPieceItem.getEndInSession();

        if (videoBegin >= VIDEO_TOTAL_LEN) {
            return;
        }

        boolean isOverLength;
        if (videoEnd > VIDEO_TOTAL_LEN) {
            isOverLength = true;
        }
        else {
            isOverLength = false;
        }

        int width = getWidth();
        int height = getHeight();
        int left = (int) (width * (videoBegin / VIDEO_TOTAL_LEN));
        int right = (int) (width * (videoEnd / VIDEO_TOTAL_LEN));
        if (right > width) {
            right = width;
        }

        if (right - left > 0) {
            Paint paint = new Paint();
            if (mVideoPieceItem.isSelected()) {
                paint.setColor(mSelectedColor);
            } else {
                paint.setColor(mMarkColor);
            }
            canvas.drawRect(left, 0, right, height, paint);
        }
    }

    public void setVideoPieceItem(VideoRecordingSession.VideoPieceItem pieceItem) {
        mVideoPieceItem = pieceItem;
    }
}
