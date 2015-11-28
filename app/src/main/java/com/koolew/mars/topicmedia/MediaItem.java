package com.koolew.mars.topicmedia;

/**
 * Created by jinchangzhu on 11/26/15.
 */
abstract class MediaItem {

    protected abstract int getType();

    // 如果在分页中用到的话，重写这个函数
    protected long getUpdateTime() {
        return Long.MAX_VALUE;
    }
}
