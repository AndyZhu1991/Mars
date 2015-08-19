package com.koolew.mars.video;

import com.koolew.mars.utils.MathUtil;
import com.koolew.mars.utils.Mp4ParserUtil;

import java.io.IOException;

/**
 * Created by jinchangzhu on 8/19/15.
 */
public class VideoUtil {

    public static boolean isMp4FileBroken(String url, String filePath) {
        // 暂时采用的方法是通过得到duration来判断视频文件有没有损坏
        try {
            double duration = Mp4ParserUtil.getDuration(filePath);
            if (MathUtil.equalsApproximate(duration, 0.0, 0.01)) {
                return true;
            }
        } catch (IOException e) {
            return true;
        } catch (NullPointerException e) {
            return true;
        }

        return false;
    }
}
