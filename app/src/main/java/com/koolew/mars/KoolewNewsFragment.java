package com.koolew.mars;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.koolew.mars.utils.WebApiUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class KoolewNewsFragment extends Fragment {

    private static final String TAG = "koolew-KoolewNewsF";

    private ListView mListView;
    private TopicInvitationAdapter mAdapter;

    private RequestQueue mRequestQueue;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment KoolewNewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KoolewNewsFragment newInstance() {
        KoolewNewsFragment fragment = new KoolewNewsFragment();
        return fragment;
    }

    public KoolewNewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRequestQueue = Volley.newRequestQueue(getActivity());

        requestFeedsTopic();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_koolew_news, container, false);
        mListView = (ListView) root.findViewById(R.id.list_view);
        if (mAdapter != null) {
            mListView.setAdapter(mAdapter);
        }
        return root;
    }

    private void requestFeedsTopic() {
        String url = WebApiUtil.FEEDS_TOPIC_URL;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response: " + response);
                        try {
                            mAdapter = new TopicInvitationAdapter(getActivity());
                            JSONArray cards = response.getJSONObject("result").getJSONArray("cards");
                            int length = cards.length();
                            for (int i = 0; i < length; i++) {
                                mAdapter.mData.add(cards.getJSONObject(i));
                            }
                            if (mListView != null) {
                                mListView.setAdapter(mAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return WebApiUtil.getStandardPostHeaders();
            }
        };
        mRequestQueue.add(jsonObjectRequest);
    }

}
