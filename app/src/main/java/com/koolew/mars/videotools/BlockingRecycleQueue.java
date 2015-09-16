package com.koolew.mars.videotools;

import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by jinchangzhu on 9/10/15.
 */
public abstract class BlockingRecycleQueue<T> {
    protected Stack<T> cache;
    protected ArrayBlockingQueue<Wrapper<T>> queue;

    public BlockingRecycleQueue(int maxItemCount) {
        cache = new Stack<>();
        queue = new ArrayBlockingQueue<>(maxItemCount);
    }

    public void put(T frame) {
        try {
            queue.put(new Wrapper<>(frame));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public T take() {
        try {
            Wrapper<T> wrapper = queue.take();
            if (!wrapper.isNull()) {
                return wrapper.element;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public T obtain() {
        synchronized (cache) {
            if (cache.size() == 0) {
                return generateNewFrame();
            } else {
                return cache.pop();
            }
        }
    }

    public void recycle(T frame) {
        synchronized (cache) {
            cache.push(frame);
        }
    }

    public void stop() {
        try {
            queue.put(new Wrapper<T>());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected abstract T generateNewFrame();

    public void releaseAllCachedItem() {
        while (cache.size() > 0) {
            releaseOneItem(cache.pop());
        }
    }

    protected void releaseOneItem(T item) {}

    private class Wrapper<T> {
        private T element;
        private boolean isNullOne;

        private Wrapper(T element) {
            this.element = element;
            isNullOne = false;
        }

        private Wrapper() {
            element = null;
            isNullOne = true;
        }

        private boolean isNull() {
            return element == null || isNullOne;
        }
    }
}
