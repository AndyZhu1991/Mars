package com.koolew.mars;

import android.content.Context;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.KooCountUserInfo;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.topicmedia.MediaItem;
import com.koolew.mars.topicmedia.TopStarsItem;
import com.koolew.mars.topicmedia.VideoDetailTitleItem;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.webapi.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by jinchangzhu on 12/4/15.
 */
public class WorldMediaFragment extends CommonMediaFragment<WorldMediaFragment.WorldMediaAdapter> {

    private int mCurrentPage;

    public WorldMediaFragment(String topicId) {
        super(topicId);
    }

    @Override
    protected WorldMediaAdapter useThisAdapter() {
        return new WorldMediaAdapter(getActivity());
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_blue);
    }

    @Override
    protected String getRefreshRequestUrl() {
        return UrlHelper.getWorldTopicVideoUrl(mTopicId, mCurrentPage);
    }

    @Override
    protected String getLoadMoreRequestUrl() {
        return UrlHelper.getWorldTopicVideoUrl(mTopicId, mCurrentPage);
    }

    @Override
    protected JsonObjectRequest doLoadMoreRequest() {
        mCurrentPage++;
        return super.doLoadMoreRequest();
    }

    @Override
    protected JsonObjectRequest doRefreshRequest() {
        mCurrentPage = 0;
        return super.doRefreshRequest();
    }

    public static class WorldMediaAdapter extends CommonMediaFragment.CommonMediaAdapter {

        private VideoDetailTitleItem mTitleItem;

        public WorldMediaAdapter(Context context) {
            super(context);
        }

        @Override
        protected MediaItem generatorTitleItem(BaseTopicInfo topicInfo) {
            MediaItem titleItem = super.generatorTitleItem(topicInfo);
            if (titleItem instanceof VideoDetailTitleItem) {
                mTitleItem = (VideoDetailTitleItem) titleItem;
                mTitleItem.setTitleType(VideoDetailTitleItem.TitleType.WORLD);
            }
            return titleItem;
        }

        @Override
        protected void onRefreshResult(JSONObject result, List<MediaItem> data) {
            JSONArray starsArray = JsonUtil.getJSONArrayIfHas(result, "koo_ranks", new JSONArray());
            int starsCount = starsArray.length();
            KooCountUserInfo[] stars = new KooCountUserInfo[starsCount];
            for (int i = 0; i < starsCount; i++) {
                try {
                    stars[i] = new KooCountUserInfo(starsArray.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (stars.length > 0 && stars[0].getUid().equals(MyAccountInfo.getUid())
                    && mTitleItem != null) {
                mTitleItem.setIsManager(true);
            }

            if (stars.length > 0) {
                data.add(1, new TopStarsItem(stars));
            }
        }

        @Override
        protected void addData(List<MediaItem> data) {
            addDataUnique(data);
        }
    }
}
