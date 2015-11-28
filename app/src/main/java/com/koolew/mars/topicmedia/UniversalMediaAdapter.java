package com.koolew.mars.topicmedia;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koolew.mars.MarsApplication;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.MovieTopicInfo;
import com.koolew.mars.mould.LoadMoreAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jinchangzhu on 11/26/15.
 */
public abstract class UniversalMediaAdapter extends LoadMoreAdapter {

    private static int ITEM_TYPE_COUNT = 0;
    private static final Map<Integer, ItemViewHolderGenerator> generatorMap = new HashMap<>();

    static int registerGenerator(ItemViewHolderGenerator generator) {
        ITEM_TYPE_COUNT++;
        generatorMap.put(ITEM_TYPE_COUNT, generator);
        return ITEM_TYPE_COUNT;
    }

    protected Context mContext;
    protected BaseTopicInfo mTopicInfo;
    protected List<MediaItem> mData = new ArrayList<>();

    public UniversalMediaAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        return generatorMap.get(viewType).generateViewHolder(this, parent);
    }

    @Override
    public void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MediaHolder) holder).bindItem(mData.get(position));
    }

    @Override
    public int getCustomItemCount() {
        return mData.size();
    }

    @Override
    public int getCustomItemViewType(int position) {
        int type = mData.get(position).getType();
        if (type == 0) {
            throw new RuntimeException("Did you forget call registerGenerator or not return TYPE?");
        }
        return type;
    }

    protected void setData(List<MediaItem> data) {
        mData.clear();
        for (MediaItem mediaItem: data) {
            mData.add(mediaItem);
        }
        notifyDataSetChanged();
    }

    protected void addData(List<MediaItem> data) {
        int originLen = mData.size();
        for (MediaItem mediaItem: data) {
            mData.add(mediaItem);
        }
        notifyItemRangeInserted(originLen, data.size());
    }


    protected BaseTopicInfo generateTopicInfo(JSONObject topicJson) {
        String category;
        if (!topicJson.has(BaseTopicInfo.KEY_CATEGORY)) {
            if (MarsApplication.DEBUG) {
                throw new RuntimeException("There is no category in topic");
            }
            else {
                JSONObject fakeTopic = new JSONObject();
                try {
                    fakeTopic.put(BaseTopicInfo.KEY_CATEGORY, "no_category");
                    fakeTopic.put(BaseTopicInfo.KEY_TITLE, "Error!");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return new BaseTopicInfo(fakeTopic);
            }
        }
        else {
            try {
                category = topicJson.getString(BaseTopicInfo.KEY_CATEGORY);
            } catch (JSONException e) {
                throw new RuntimeException("Never here!!!");
            }
        }

        if (category.equals("video")) {
            return new BaseTopicInfo(topicJson);
        }
        else if (category.equals("movie")) {
            return new MovieTopicInfo(topicJson);
        }
        else {
            return new BaseTopicInfo(topicJson);
        }
    }

    protected JSONObject findTopicJson(JSONObject result) {
        return result;
    }

    protected MediaItem generatorTitleItem(BaseTopicInfo topicInfo) {
        if (topicInfo.getCategory().equals("video")) {
            return new VideoDetailTitleItem(topicInfo);
        }
        else if (topicInfo.getCategory().equals("movie")) {
            return new MovieDetailTitleItem((MovieTopicInfo) topicInfo);
        }
        else {
            return new BasicTitleItem(topicInfo);
        }
    }


    /**
     *
     * @param result The result that returned by http response
     * @return Is need next page
     */
    public boolean handleRefreshResult(JSONObject result) {
        List<MediaItem> data = new ArrayList<>();
        mTopicInfo = generateTopicInfo(findTopicJson(result));
        data.add(generatorTitleItem(mTopicInfo));

        String category = mTopicInfo.getCategory();
        if (!category.equals("movie") || category.equals("video")) {
            // TODO Unknown category
            return false;
        }

        JSONArray refreshArray = getRefreshArray(result);
        int length = refreshArray.length();
        if (length == 0) {
            // TODO No data
            setData(data);
            return false;
        }
        else {
            for (int i = 0; i < length; i++) {
                try {
                    data.add(fromEveryRefreshObject(refreshArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            onRefreshResult(result, data);
            setData(data);
            return true;
        }
    }

    protected void onRefreshResult(JSONObject result, List<MediaItem> data) {
    }

    protected abstract JSONArray getRefreshArray(JSONObject result);

    protected abstract MediaItem fromEveryRefreshObject(JSONObject itemObject);

    /**
     *
     * @param result The result that returned by http response
     * @return Is need next page
     */
    public boolean handleLoadMoreResult(JSONObject result) {
        JSONArray loadMoreArray = getLoadMoreArray(result);
        int length = loadMoreArray.length();
        if (length == 0) {
            onLoadMoreResult(result, null);
            return false;
        }
        else {
            List<MediaItem> data = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                try {
                    data.add(fromEveryLoadMoreObject(loadMoreArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            onLoadMoreResult(result, data);
            addData(data);
            return true;
        }
    }

    protected void onLoadMoreResult(JSONObject result, List<MediaItem> data) {
    }

    protected abstract JSONArray getLoadMoreArray(JSONObject result);

    protected abstract MediaItem fromEveryLoadMoreObject(JSONObject itemObject);


    static abstract class ItemViewHolderGenerator {
        protected MediaHolder generateViewHolder(UniversalMediaAdapter adapter,
                                                             ViewGroup parent) {
            try {
                Constructor<?> constructor = holderClass().getDeclaredConstructor(
                        UniversalMediaAdapter.class, View.class);
                View holderView = LayoutInflater.from(parent.getContext())
                        .inflate(layoutResId(), parent, false);
                return (MediaHolder) constructor.newInstance(adapter, holderView);
            } catch (Exception e) {
                throw new RuntimeException("Holder reflect error!");
            }
        }

        protected abstract int layoutResId();
        protected abstract Class<?> holderClass();
    }
}
