package com.koolew.mars.topicmedia;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.R;
import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.KooCountUserInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by jinchangzhu on 11/27/15.
 */
public class TopStarsItem extends MediaItem {

    private static final int TYPE = UniversalMediaAdapter.registerGenerator(
            new UniversalMediaAdapter.ItemViewHolderGenerator() {
                @Override
                protected int layoutResId() {
                    return R.layout.top_stars_layout;
                }

                @Override
                protected Class<?> holderClass() {
                    return ItemViewHolder.class;
                }
            }
    );

    private KooCountUserInfo[] allStars;
    private boolean showCrowns = false;

    TopStarsItem(KooCountUserInfo[] allStars) {
        this.allStars = allStars;
    }

    @Override
    protected int getType() {
        return TYPE;
    }

    public void setShowCrowns(boolean showCrowns) {
        this.showCrowns = showCrowns;
    }

    static class ItemViewHolder extends MediaHolder<TopStarsItem> {

        private static final int[] STARS_AVATAR_RES_ID = {
                R.id.first_koo,
                R.id.second_koo,
                R.id.third_koo,
                R.id.forth_koo,
                R.id.fifth_koo,
        };

        private static final int[] STARS_CROWN_RES_ID = {
                R.id.first_crown,
                R.id.second_crown,
                R.id.third_crown,
        };

        private TextView starsRankTitle;
        private ImageView[] starsAvatar = new ImageView[STARS_AVATAR_RES_ID.length];
        private ImageView[] starsCrown = new ImageView[STARS_CROWN_RES_ID.length];
        private View topicManager;

        public ItemViewHolder(UniversalMediaAdapter adapter, View itemView) {
            super(adapter, itemView);

            starsRankTitle = (TextView) itemView.findViewById(R.id.stars_rank_title);
            for (int i = 0; i < starsAvatar.length; i++) {
                starsAvatar[i] = (ImageView) itemView.findViewById(STARS_AVATAR_RES_ID[i]);
            }
            for (int i = 0; i < starsCrown.length; i++) {
                starsCrown[i] = (ImageView) itemView.findViewById(STARS_CROWN_RES_ID[i]);
            }
            topicManager = itemView.findViewById(R.id.topic_manager);
        }

        @Override
        protected void onBindItem() {
            starsRankTitle.setText(mContext.getString(R.string.stars_rank, mItem.allStars.length));

            for (int i = 0; i < starsAvatar.length && i < mItem.allStars.length; i++) {
                ImageLoader.getInstance().displayImage(mItem.allStars[i].getAvatar(), starsAvatar[i],
                        ImageLoaderHelper.avatarLoadOptions);
            }

            for (int i = 0; i < starsCrown.length; i++) {
                int visibility = (mItem.showCrowns && i < mItem.allStars.length)
                        ? View.VISIBLE : View.INVISIBLE;
                starsCrown[i].setVisibility(visibility);
            }

            topicManager.setVisibility(mItem.showCrowns ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
