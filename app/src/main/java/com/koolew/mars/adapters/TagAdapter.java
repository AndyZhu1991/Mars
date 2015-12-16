package com.koolew.mars.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koolew.mars.R;
import com.koolew.mars.infos.Tag;
import com.koolew.mars.remoteconfig.RemoteConfigManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinchangzhu on 12/16/15.
 */
public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagItemHolder> {

    public static final int TAGS_VIDEO = 0;
    public static final int TAGS_MOVIE = 1;

    private Context mContext;
    private List<Tag> mTags = new ArrayList<>();
    private int mSelectedPosition = 0;
    private int textColorSelected = Color.GRAY;

    private OnSelectedTagChangedListener tagChangedListener;


    public TagAdapter(Context context) {
        mContext = context;
    }

    public void initTags(int tagsType, boolean isNeedFakeRecommend) {
        if (isNeedFakeRecommend) {
            mTags.add(generateFakeRecommendTag());
        }

        List<Tag> realTags = null;
        if (tagsType == TAGS_VIDEO) {
            realTags = RemoteConfigManager.getInstance().getVideoTagsConfig().getConfig();
        }
        else if (tagsType == TAGS_MOVIE) {
            realTags = RemoteConfigManager.getInstance().getMovieTagsConfig().getConfig();
        }
        for (Tag tag: realTags) {
            mTags.add(tag);
        }
    }

    public void setTextColorSelected(int textColorSelected) {
        this.textColorSelected = textColorSelected;
    }

    public void setTagChangedListener(OnSelectedTagChangedListener tagChangedListener) {
        this.tagChangedListener = tagChangedListener;
    }

    private Tag generateFakeRecommendTag() {
        JSONObject tagJson = new JSONObject();
        try {
            tagJson.put(Tag.KEY_NAME, mContext.getString(R.string.recommend));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new Tag(tagJson);
    }

    @Override
    public TagItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TagItemHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.tag_item, parent, false));
    }

    @Override
    public void onBindViewHolder(TagItemHolder holder, int position) {
        Tag tag = mTags.get(position);
        holder.textView.setText(tag.getName());
        if (mSelectedPosition == position) {
            holder.textView.setTextColor(textColorSelected);
            holder.textView.setBackgroundResource(R.drawable.tag_selected_bg);
        }
        else {
            holder.textView.setTextColor(mContext.getResources()
                    .getColor(R.color.koolew_deep_half_transparent_white));
            holder.textView.setBackgroundResource(R.drawable.tag_unselected_bg);
        }
    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }


    class TagItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView;

        public TagItemHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            textView = (TextView) itemView;
        }

        @Override
        public void onClick(View v) {
            int lastPosition = mSelectedPosition;
            int currentPosition = getAdapterPosition();
            if (lastPosition != currentPosition) {
                mSelectedPosition = currentPosition;
                notifyItemChanged(lastPosition);
                notifyItemChanged(currentPosition);
                if (tagChangedListener != null) {
                    Tag selectedTag = mTags.get(currentPosition);
                    if (TextUtils.isEmpty(selectedTag.getId())) {
                        selectedTag = null;
                    }
                    tagChangedListener.onSelectedTagChanged(selectedTag);
                }
            }
        }
    }

    public interface OnSelectedTagChangedListener {
        void onSelectedTagChanged(Tag tag);
    }
}
