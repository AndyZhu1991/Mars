package com.koolew.mars.video;

import android.content.Context;

import com.koolew.mars.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 7/18/15.
 */
public class VideoRecordingSession {

    private List<VideoPieceItem> mVideoPieces;

    private Context mContext;
    private long mId;
    private String mCurrentWorkDir;
    private int mSelectedItem;


    public VideoRecordingSession(Context context) {
        mContext = context;
        mVideoPieces = new ArrayList<>();
        mId = System.currentTimeMillis();
        mCurrentWorkDir = Utils.getCacheDir(mContext) + mId + "/";
        new File(mCurrentWorkDir).mkdir();
        mSelectedItem = -1;
    }

    public void add(VideoPieceItem item) {
        mVideoPieces.add(item);
    }

    public void remove(int position) {
        new File(get(position).getVideoPath()).delete();
        mVideoPieces.remove(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        mVideoPieces.add(toPosition, mVideoPieces.remove(fromPosition));
    }

    public VideoPieceItem get(int position) {
        return mVideoPieces.get(position);
    }

    public int getVideoCount() {
        return mVideoPieces.size();
    }

    public long getVideoLength(int startPosition, int endPosition) {
        long totalLength = 0;
        for (int i = startPosition; i < endPosition; i++) {
            totalLength += get(i).getVideoLength();
        }

        return totalLength;
    }

    public long getFrontVideoLength(int endPosition) {
        return getVideoLength(0, endPosition);
    }

    public long getTotalVideoLength() {
        return getFrontVideoLength(getVideoCount());
    }


    public class VideoPieceItem {

        private long mStartMillis;
        private long mEndMillis;


        public VideoPieceItem() {
            this(System.currentTimeMillis());
        }

        public VideoPieceItem(long startMillis) {
            mStartMillis = startMillis;
            mEndMillis = 0;
        }

        public void finishRecord() {
            finishRecord(System.currentTimeMillis());
        }

        public void finishRecord(long endMillis) {
            mEndMillis = endMillis;
        }

        public long getStartMillis() {
            return mStartMillis;
        }

        public long getId() {
            return mStartMillis;
        }

        public String getVideoPath() {
            return mCurrentWorkDir + getId() + ".mp4";
        }

        /**
         * @return video length in ms, non-exact
         */
        public long getVideoLength() {
            return mEndMillis - mStartMillis;
        }

        public float getBeginInSession() {
            float beforeSec = 0.0f;
            for (int i = 0; i < mVideoPieces.size(); i++) {
                if (get(i) == this) {
                    break;
                }
                beforeSec += get(i).getVideoLength() / 1000.0f;
            }

            return beforeSec;
        }

        public float getEndInSession() {
            return getBeginInSession() + getVideoLength() / 1000.0f;
        }

        public boolean isSelected() {
            if (mSelectedItem < 0) {
                return false;
            }
            return get(mSelectedItem) == this;
        }
    }
}
