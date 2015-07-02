package com.koolew.mars.preference;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jinchangzhu on 7/2/15.
 */
public class PreferenceAdapter extends BaseAdapter implements AbsListView.OnItemClickListener {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<BasePreference> mData;
    private Map<Class, Integer> mTypeMap;

    public PreferenceAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mData = new ArrayList<BasePreference>();
        mTypeMap = new HashMap<Class, Integer>();
    }

    public void add(BasePreference preference) {
        mData.add(preference);
        if (!mTypeMap.containsKey(preference.getClass())) {
            mTypeMap.put(preference.getClass(), mTypeMap.size());
        }
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
    public int getItemViewType(int position) {
        return mTypeMap.get(getItem(position).getClass());
    }

    @Override
    public int getViewTypeCount() {
        return mTypeMap.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BasePreference item = (BasePreference) getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(item.getLayoutResourceId(), null);
        }

        item.onBindView(convertView);

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((BasePreference) getItem(position)).onClick(view);
    }
}
