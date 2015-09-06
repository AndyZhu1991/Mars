package com.koolew.mars;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koolew.mars.imageloader.ImageLoaderHelper;
import com.koolew.mars.infos.KooCountUserInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class TopicKooRankActivity extends BaseActivity {

    public static final String KEY_KOO_COUNT_USER_INFO = "koo count user info";

    private KooCountUserInfo[] kooCountUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_koo_rank);

        Object[] os = (Object[]) getIntent().getSerializableExtra(KEY_KOO_COUNT_USER_INFO);
        kooCountUserInfo = new KooCountUserInfo[os.length];
        for (int i = 0; i < os.length; i++) {
            kooCountUserInfo[i] = (KooCountUserInfo) os[i];
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TopicKooRankAdapter());
    }


    class TopicKooRankItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CircleImageView avatar;
        private TextView nickname;
        private ImageView kooIcon;
        private TextView kooCount;

        public TopicKooRankItemHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            nickname = (TextView) itemView.findViewById(R.id.nickname);
            kooIcon = (ImageView) itemView.findViewById(R.id.koo_icon);
            kooCount = (TextView) itemView.findViewById(R.id.koo_count);
        }

        @Override
        public void onClick(View v) {
            FriendInfoActivity.startThisActivity(TopicKooRankActivity.this,
                    kooCountUserInfo[getAdapterPosition()].getUid());
        }
    }

    class TopicKooRankAdapter extends RecyclerView.Adapter<TopicKooRankItemHolder> {

        @Override
        public TopicKooRankItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.topic_koo_rank_item, parent, false);
            return new TopicKooRankItemHolder(itemView);
        }

        @Override
        public void onBindViewHolder(TopicKooRankItemHolder holder, int position) {
            ImageLoader.getInstance().displayImage(kooCountUserInfo[position].getAvatar(),
                    holder.avatar, ImageLoaderHelper.avatarLoadOptions);
            holder.nickname.setText(kooCountUserInfo[position].getNickname());
            if (position == 0) {
                holder.kooIcon.setVisibility(View.VISIBLE);
            }
            else {
                holder.kooIcon.setVisibility(View.INVISIBLE);
            }
            holder.kooCount.setText(String.valueOf(kooCountUserInfo[position].getKooCount()));
        }

        @Override
        public int getItemCount() {
            return kooCountUserInfo.length;
        }
    }
}
