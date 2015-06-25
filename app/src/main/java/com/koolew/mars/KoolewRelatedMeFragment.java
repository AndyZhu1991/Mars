package com.koolew.mars;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;


public class KoolewRelatedMeFragment extends Fragment {

    private ListView mListView;
    private RelatedMeAdapter mAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment KoolewRelatedMeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KoolewRelatedMeFragment newInstance() {
        KoolewRelatedMeFragment fragment = new KoolewRelatedMeFragment();
        return fragment;
    }

    public KoolewRelatedMeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_koolew_related_me, container, false);

        mListView = (ListView) root.findViewById(R.id.list_view);
        mAdapter = new RelatedMeAdapter();
        generateTestData(mAdapter.mData);
        mListView.setAdapter(mAdapter);

        return root;
    }

    private void generateTestData(List<RelatedMeItem> list) {
        for (int i = 0; i < 100; i++) {
            list.add(new RelatedMeItem("title " + i, i));
        }
    }

    class RelatedMeItem {
        String title;
        int videoCount;

        RelatedMeItem(String title, int videoCount) {
            this.title = title;
            this.videoCount = videoCount;
        }
    }

    private static final int[] LEFT_LAYOUT_COLORS = {
            0xFFFF5656, 0xFFFC7B7B, 0xFFFFAE82, 0xFFFFC282,
            0xFFF4D288, 0xFFFFC282, 0xFFFFAE82, 0xFFFC7B7B,
    };

    class RelatedMeAdapter extends BaseAdapter {

        List<RelatedMeItem> mData;

        RelatedMeAdapter() {
            mData = new LinkedList<RelatedMeItem>();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.koolew_relative_me_item, null);
                ViewHolder holder = new ViewHolder();
                holder.leftLayout = (LinearLayout) convertView.findViewById(R.id.left_layout);
                holder.videoCount = (TextView) convertView.findViewById(R.id.video_count);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            ((GradientDrawable) holder.leftLayout.getBackground())
                    .setColor(LEFT_LAYOUT_COLORS[position % 8]);
            holder.videoCount.setText("" + mData.get(position).videoCount);
            holder.title.setText(mData.get(position).title);

            return convertView;
        }
    }

    class ViewHolder {
        LinearLayout leftLayout;
        TextView videoCount;
        TextView title;
    }
}
