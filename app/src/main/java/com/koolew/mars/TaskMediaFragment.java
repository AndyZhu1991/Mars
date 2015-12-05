package com.koolew.mars;

import android.content.Context;

import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.topicmedia.MediaItem;
import com.koolew.mars.topicmedia.VideoDetailTitleItem;

/**
 * Created by jinchangzhu on 12/4/15.
 */
public class TaskMediaFragment extends FeedsMediaFragment {

    public TaskMediaFragment(String topicId) {
        super(topicId);
    }

    @Override
    protected FeedsMediaAdapter useThisAdapter() {
        return new TaskMediaAdapter(getActivity());
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.koolew_light_green);
    }


    public static class TaskMediaAdapter extends FeedsMediaAdapter {

        public TaskMediaAdapter(Context context) {
            super(context);
        }

        @Override
        protected MediaItem generatorTitleItem(BaseTopicInfo topicInfo) {
            MediaItem titleItem = super.generatorTitleItem(topicInfo);
            if (titleItem instanceof VideoDetailTitleItem) {
                ((VideoDetailTitleItem) titleItem).setTitleType(VideoDetailTitleItem.TitleType.TASK);
            }
            return titleItem;
        }
    }
}
