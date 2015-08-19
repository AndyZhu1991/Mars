package com.koolew.mars.video;

import android.content.Context;
import android.graphics.Bitmap;

import com.koolew.mars.utils.FileUtil;
import com.koolew.mars.utils.Mp4ParserUtil;
import com.koolew.mars.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jinchangzhu on 7/18/15.
 */
public class VideoRecordingSession {

    private static final String CONCATED_VIDEO_NAME = "concated.mp4";
    private static final String VIDEO_THUMB_NAME = "thumb.png";

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

    public String getWorkDir() {
        return mCurrentWorkDir;
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

    public void concatVideo() {
        if (mVideoPieces.size() == 0) {
            throw new RuntimeException("Zero videos to concat!");
        }
        if (mVideoPieces.size() == 1) {
            return;
        }

        List<String> videos = new LinkedList<>();
        for (VideoPieceItem videoItem: mVideoPieces) {
            videos.add(videoItem.getVideoPath());
        }

        String concatedFilePath = mCurrentWorkDir + CONCATED_VIDEO_NAME;
        try {
            Mp4ParserUtil.mp4Cat(videos, concatedFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getConcatedVideoName() {
        if (mVideoPieces.size() == 0) {
            return null;
        }
        if (mVideoPieces.size() == 1) {
            return mVideoPieces.get(0).getVideoPath();
        }

        return mCurrentWorkDir + CONCATED_VIDEO_NAME;
    }

    public String getVideoThumbName() {
        return mCurrentWorkDir + VIDEO_THUMB_NAME;
    }

    public void generateThumb() {
        Bitmap thumbBmp = ImageLoader.getInstance()
                .loadImageSync("file://" + mVideoPieces.get(0).getVideoPath());
        File f = new File(mCurrentWorkDir, VIDEO_THUMB_NAME);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            thumbBmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteSession() {
        new Thread() {
            @Override
            public void run() {
                FileUtil.deleteFileOrDir(mCurrentWorkDir);
            }
        }.start();
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
