package com.koolew.mars;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Response;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class CommonTopicActivity extends Activity {

    public static final String KEY_UID = "uid";

    private String mUid;

    private ListView mListView;
    private TopicAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_topic);

        mUid = getIntent().getStringExtra(KEY_UID);

        mListView = (ListView) findViewById(R.id.list_view);

        doLoad();
    }

    private void doLoad() {
        ApiWorker.getInstance().requestCommonTopic(mUid, mResponseListener, null);
    }

    private Response.Listener<JSONObject> mResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
            try {
                if (jsonObject.getInt("code") == 0) {
                    JSONArray topics = jsonObject.getJSONObject("result").getJSONArray("topics");
                    mAdapter = new CommonTopicAdapter(CommonTopicActivity.this);
                    mAdapter.setData(topics);
                    mListView.setAdapter(mAdapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private class CommonTopicAdapter extends TopicAdapter {

        CommonTopicAdapter(Context context) {
            super(context);
        }

        @Override
        public TopicItem jsonObject2TopicItem(JSONObject jsonObject) {
            try {
                return new TopicItem(
                        jsonObject.getString("topic_id"),
                        jsonObject.getString("content"),
                        jsonObject.getString("thumb_url"),
                        jsonObject.getInt("video_cnt"),
                        jsonObject.getLong("update_time"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View root = super.getView(position, convertView, parent);

            ((ViewHolder) root.getTag()).videoCount.setText(
                    getString(R.string.part_video_count, ((TopicItem) getItem(position)).videoCount));

            return root;
        }
    }
}
