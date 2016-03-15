package com.koolew.mars;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.infos.BaseTopicInfo;
import com.koolew.mars.infos.BaseUserInfo;
import com.koolew.mars.infos.TypedUserInfo;
import com.koolew.mars.statistics.BaseActivity;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.android.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;
import com.koolew.mars.webapi.UrlHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalSearchActivity extends BaseActivity implements View.OnClickListener, TextWatcher,
        Response.ErrorListener, Response.Listener<JSONObject>, TextView.OnEditorActionListener {

    private LayoutInflater mInflater;
    private Dialog mConnectDialog;

    private EditText keywordEdit;
    private View cancelView;

    private RecyclerView historyRecycler;
    private RecyclerView searchRecycler;

    private HistoryAdapter historyAdapter;
    private SearchAdapter searchAdapter;

    private JsonObjectRequest searchRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_search);

        Utils.setStatusBarColorBurn(this, 0xFF373737);

        initMembers();
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        historyAdapter.saveKeys();
    }

    private void initMembers() {
        mInflater = LayoutInflater.from(this);
        mConnectDialog = DialogUtil.getConnectingServerDialog(this);
    }

    private void initViews() {
        keywordEdit = (EditText) findViewById(R.id.keyword);
        keywordEdit.addTextChangedListener(this);
        keywordEdit.setOnEditorActionListener(this);
        cancelView = findViewById(R.id.cancel);
        cancelView.setOnClickListener(this);

        historyRecycler = (RecyclerView) findViewById(R.id.history);
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter();
        historyRecycler.setAdapter(historyAdapter);

        searchRecycler = (RecyclerView) findViewById(R.id.real_time_search);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter();
        searchRecycler.setAdapter(searchAdapter);
    }

    private void showSearchRecycler() {
        searchRecycler.setVisibility(View.VISIBLE);
        historyRecycler.setVisibility(View.INVISIBLE);
    }

    private void showHistoryRecycler() {
        historyRecycler.setVisibility(View.VISIBLE);
        searchRecycler.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                onBackPressed();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (searchRequest != null) {
            searchRequest.cancel();
            searchRequest = null;
        }
        if (s.length() == 0) {
            showHistoryRecycler();
        }
        else {
            searchRequest = ApiWorker.getInstance().queueGetRequest(
                    UrlHelper.getGlobalSearchUrl(s.toString()), this, this);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            Utils.hideSoftKey(v);
            historyAdapter.updateKey(v.getText().toString());
            return true;
        }
        return false;
    }

    @Override
    public void onResponse(JSONObject response) {
        searchRequest = null;
        try {
            if (response.getInt("code") == 0) {
                JSONObject result = response.getJSONObject("result");
                searchAdapter.updateSearchResult(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        searchRequest = null;
    }


    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final int MAX_HISTORY_COUNT = 10;

    private static final int TYPE_HISTORY = 1;
    private static final int TYPE_CLEAR_HISTORY = 2;

    class HistoryAdapter extends RecyclerView.Adapter {

        private ArrayList<SearchHistoryItem> history = new ArrayList<>();
        private SharedPreferences keyPref;

        public HistoryAdapter() {
            keyPref = getSharedPreferences(KEY_SEARCH_HISTORY, Context.MODE_PRIVATE);
            Set<String> keySet = keyPref.getStringSet(KEY_SEARCH_HISTORY, new HashSet<String>());
            for (String record: keySet) {
                history.add(SearchHistoryItem.fromRecord(record));
            }
            Collections.sort(history);
        }

        public void updateKey(String key) {
            if (history.size() > 0 && history.get(0).key.equals(key)) {
                return;
            }

            SearchHistoryItem historyItem = SearchHistoryItem.createByCurrentTime(key);

            int existIndex = history.indexOf(historyItem);
            if (existIndex > 0) {
                history.remove(existIndex);
                notifyItemRemoved(existIndex);
            }

            history.add(0, historyItem);
            notifyItemInserted(0);

            if (history.size() > MAX_HISTORY_COUNT) {
                history.remove(MAX_HISTORY_COUNT);
                notifyItemRemoved(MAX_HISTORY_COUNT);
            }
        }

        public void saveKeys() {
            Set<String> recordSet = new HashSet<>();
            for (SearchHistoryItem historyItem: history) {
                recordSet.add(historyItem.toString());
            }

            SharedPreferences.Editor editor = keyPref.edit();
            editor.putStringSet(KEY_SEARCH_HISTORY, recordSet);
            editor.apply();
        }

        public void clearKeys() {
            history.clear();
            notifyDataSetChanged();
            saveKeys();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_HISTORY:
                    return new HistoryHolder(mInflater.inflate(
                            R.layout.search_history_item, parent, false));
                case TYPE_CLEAR_HISTORY:
                    return new ClearHistoryHolder(mInflater.inflate(
                            R.layout.clear_search_history_item, parent, false));
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_HISTORY) {
                HistoryHolder historyHolder = (HistoryHolder) holder;
                historyHolder.textView.setText(history.get(position).key);
            }
        }

        @Override
        public int getItemCount() {
            if (history.size() == 0) {
                return 0;
            }
            else {
                return history.size() + 1;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position < history.size()) {
                return TYPE_HISTORY;
            }
            else {
                return TYPE_CLEAR_HISTORY;
            }
        }

        class HistoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView textView;

            public HistoryHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                textView = (TextView) itemView.findViewById(R.id.text_view);
            }

            @Override
            public void onClick(View v) {
                keywordEdit.setText(textView.getText());
                keywordEdit.setSelection(textView.getText().length());
            }
        }

        class ClearHistoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public ClearHistoryHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                clearKeys();
            }
        }
    }

    static class SearchHistoryItem implements Comparable<SearchHistoryItem> {
        private long time;
        private String key;

        @Override
        public String toString() {
            return time + " " + key;
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof SearchHistoryItem) {
                return ((SearchHistoryItem) o).key.equals(this.key);
            }
            else {
                return false;
            }
        }

        @Override
        public int compareTo(@NonNull SearchHistoryItem another) {
            return (int) (another.time - this.time);
        }

        public static SearchHistoryItem fromRecord(String record) {
            SearchHistoryItem historyItem = new SearchHistoryItem();
            int firstSpace = record.indexOf(" ");
            if (firstSpace <= 0) {
                if (MarsApplication.DEBUG) {
                    throw new RuntimeException("History record error: " + record);
                }
                else {
                    return historyItem;
                }
            }

            try {
                historyItem.time = Long.valueOf(record.substring(0, firstSpace));
            } catch (NumberFormatException nfe) {
                if (MarsApplication.DEBUG) {
                    throw nfe;
                }
            }
            historyItem.key = record.substring(firstSpace + 1);

            return historyItem;
        }

        public static SearchHistoryItem createByCurrentTime(String key) {
            SearchHistoryItem historyItem = new SearchHistoryItem();
            historyItem.time = System.currentTimeMillis();
            historyItem.key = key;
            return historyItem;
        }
    }


    private static final int TYPE_USER_TITLE = 1;
    private static final int TYPE_USER = 2;
    private static final int TYPE_TOPIC_TITLE = 3;
    private static final int TYPE_TOPIC = 4;

    class SearchAdapter extends RecyclerView.Adapter {

        private List<TypedUserInfo> users = new ArrayList<>();
        private List<BaseTopicInfo> topics = new ArrayList<>();

        private void updateSearchResult(JSONObject result) {
            try {
                JSONArray users = result.getJSONArray("users");
                this.users.clear();
                for (int i = 0; i < users.length(); i++) {
                    this.users.add(new TypedUserInfo(users.getJSONObject(i)));
                }

                JSONArray topics = result.getJSONArray("topics");
                this.topics.clear();
                for (int i = 0; i < topics.length(); i++) {
                    this.topics.add(BaseTopicInfo.dynamicTopicInfo(topics.getJSONObject(i)));
                }

                if (this.users.size() == 0 && this.topics.size() == 0) {
                    showHistoryRecycler();
                }
                else {
                    showSearchRecycler();
                }

                searchAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_USER_TITLE:
                    return new UserTitleHolder(mInflater.inflate(R.layout.global_search_title,
                            parent, false));
                case TYPE_USER:
                    return new UserHolder(mInflater.inflate(R.layout.global_search_user,
                            parent, false));
                case TYPE_TOPIC_TITLE:
                    return new TopicTitleHolder(mInflater.inflate(R.layout.global_search_title,
                            parent, false));
                case TYPE_TOPIC:
                    return new TopicHolder(mInflater.inflate(R.layout.global_search_topic,
                            parent, false));
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case TYPE_USER_TITLE:
                    break;
                case TYPE_USER:
                    ((UserHolder) holder).bindUserInfo(users.get(position - userTitleCount()));
                    break;
                case TYPE_TOPIC_TITLE:
                    break;
                case TYPE_TOPIC:
                    int pos = position - userTitleCount() - userCount() - topicTitleCount();
                    ((TopicHolder) holder).bindTopicInfo(topics.get(pos));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return userTitleCount() + userCount() + topicTitleCount() + topicCount();
        }

        @Override
        public int getItemViewType(int position) {
            int frontCount = 0;

            frontCount += userTitleCount();
            if (position < frontCount) {
                return TYPE_USER_TITLE;
            }

            frontCount += userCount();
            if (position < frontCount) {
                return TYPE_USER;
            }

            frontCount += topicTitleCount();
            if (position < frontCount) {
                return TYPE_TOPIC_TITLE;
            }

            frontCount += topicCount();
            if (position < frontCount) {
                return TYPE_TOPIC;
            }

            throw new RuntimeException("No type for position: " + position);
        }

        private int userTitleCount() {
            return users.size() == 0 ? 0 : 1;
        }

        private int userCount() {
            return users.size();
        }

        private int topicTitleCount() {
            return topics.size() == 0 ? 0 : 1;
        }

        private int topicCount() {
            return topics.size();
        }

        abstract class TitleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            protected TextView title;
            protected TextView seeAll;

            public TitleHolder(View itemView) {
                super(itemView);

                title = (TextView) itemView.findViewById(R.id.title);
                seeAll = (TextView) itemView.findViewById(R.id.see_all);
                seeAll.setOnClickListener(this);
            }
        }

        class UserTitleHolder extends TitleHolder {

            public UserTitleHolder(View itemView) {
                super(itemView);

                title.setText(R.string.user);
            }

            @Override
            public void onClick(View v) {
                SearchUserFragment.startThisFragment(
                        GlobalSearchActivity.this, keywordEdit.getText().toString());
            }
        }

        class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private ImageView avatar;
            private TextView nickname;
            private TextView summary;
            private TextView operationBtn;

            private TypedUserInfo userInfo;

            public UserHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                avatar = (ImageView) itemView.findViewById(R.id.avatar);
                nickname = (TextView) itemView.findViewById(R.id.nickname);
                summary = (TextView) itemView.findViewById(R.id.summary);
                operationBtn = (TextView) itemView.findViewById(R.id.operation_btn);
                operationBtn.setOnClickListener(this);
            }

            private void bindUserInfo(TypedUserInfo userInfo) {
                this.userInfo = userInfo;

                ImageLoader.getInstance().displayImage(userInfo.getAvatar(), avatar);
                nickname.setText(userInfo.getNickname());
                summary.setText(buildSummary());
                setOperationBtn();
            }

            private String buildSummary() {
                String fansSummary = userInfo.getFansCount() == 0 ? "" :
                        getString(R.string.fan_count_global_search, userInfo.getFansCount());
                String followsSummary = userInfo.getFollowsCount() == 0 ? "" :
                        getString(R.string.follow_count_global_search, userInfo.getFollowsCount());
                String line = userInfo.getFansCount() > 0 && userInfo.getFollowsCount() > 0
                        ? " | " : "";
                return fansSummary + line + followsSummary;
            }

            private void setOperationBtn() {
                switch (userInfo.getType()) {
                    case BaseUserInfo.TYPE_SELF:
                        operationBtn.setVisibility(View.INVISIBLE);
                        break;
                    case BaseUserInfo.TYPE_FAN:
                    case BaseUserInfo.TYPE_STRANGER:
                        operationBtn.setBackgroundResource(R.drawable.btn_bg_add_follow);
                        operationBtn.setText(R.string.add_follow);
                        operationBtn.setTextColor(getResources()
                                .getColorStateList(R.color.btn_add_follow_text_color));
                        break;
                    case BaseUserInfo.TYPE_FOLLOWED:
                        operationBtn.setBackgroundResource(R.drawable.btn_bg_followed);
                        operationBtn.setText(R.string.followed);
                        operationBtn.setTextColor(0xFF6ED4E4);
                        break;
                    case BaseUserInfo.TYPE_FRIEND:
                        operationBtn.setBackgroundResource(R.drawable.btn_bg_followed_each_other);
                        operationBtn.setText(R.string.followed_each_other);
                        operationBtn.setTextColor(0xFF9EADB7);
                        break;
                }
            }

            @Override
            public void onClick(View v) {
                if (v == itemView) {
                    FriendInfoActivity.startThisActivity(GlobalSearchActivity.this, userInfo.getUid());
                }
                else if (v == operationBtn) {
                    if (userInfo.isFollowed()) {
                        new AlertDialog.Builder(GlobalSearchActivity.this)
                                .setMessage(R.string.unfollow_confirm)
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mConnectDialog.show();
                                        ApiWorker.getInstance().unfollowUser(userInfo.getUid(),
                                                new FriendUnfollowListener(userInfo), new FriendOpErrorListener());
                                    }
                                })
                                .show();
                    }
                    else {
                        mConnectDialog.show();
                        ApiWorker.getInstance().followUser(userInfo.getUid(),
                                new FriendFollowListener(userInfo), new FriendOpErrorListener());
                    }
                }
            }

            abstract class FriendOpListener implements Response.Listener<JSONObject> {
                protected TypedUserInfo userInfo;

                public FriendOpListener(TypedUserInfo userInfo) {
                    this.userInfo = userInfo;
                }

                @Override
                public void onResponse(JSONObject response) {
                    mConnectDialog.dismiss();
                    try {
                        int code = response.getInt("code");
                        if (code == 0) {
                            onOpSuccess();
                            notifyItemChanged(getAdapterPosition());
                        }
                    } catch (JSONException e) {
                        opFailed();
                    }
                }

                protected abstract void onOpSuccess();
            }

            private void opFailed() {
                Toast.makeText(GlobalSearchActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
            }

            class FriendFollowListener extends FriendOpListener {

                public FriendFollowListener(TypedUserInfo userInfo) {
                    super(userInfo);
                }

                @Override
                protected void onOpSuccess() {
                    userInfo.doFollow();
                }
            }

            class FriendUnfollowListener extends FriendOpListener {

                public FriendUnfollowListener(TypedUserInfo userInfo) {
                    super(userInfo);
                }

                @Override
                protected void onOpSuccess() {
                    userInfo.doUnfollow();
                }
            }

            class FriendOpErrorListener implements Response.ErrorListener {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mConnectDialog.dismiss();
                    opFailed();
                }
            }
        }

        class TopicTitleHolder extends TitleHolder {

            public TopicTitleHolder(View itemView) {
                super(itemView);

                title.setText(R.string.topic);
            }

            @Override
            public void onClick(View v) {
                SearchTopicFragment.startThisFragment(GlobalSearchActivity.this,
                        keywordEdit.getText().toString());
            }
        }

        class TopicHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private ImageView thumb;
            private TextView title;
            private ImageView captureBtn;

            private BaseTopicInfo topicInfo;

            public TopicHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                thumb = (ImageView) itemView.findViewById(R.id.thumb);
                title = (TextView) itemView.findViewById(R.id.title);
                captureBtn = (ImageView) itemView.findViewById(R.id.capture_btn);
                captureBtn.setOnClickListener(this);
            }

            public void bindTopicInfo(BaseTopicInfo topicInfo) {
                this.topicInfo = topicInfo;
                ImageLoader.getInstance().displayImage(topicInfo.getThumb(), thumb);
                title.setText(topicInfo.getTitle());
                if (topicInfo.getCategory().equals(BaseTopicInfo.CATEGORY_VIDEO)) {
                    captureBtn.setImageResource(R.mipmap.ic_btn_capture_video);
                }
                else {
                    captureBtn.setImageResource(R.mipmap.ic_btn_capture_movie);
                }
            }

            @Override
            public void onClick(View v) {
                if (v == itemView) {
                    TopicMediaActivity.startThisActivity(GlobalSearchActivity.this,
                            topicInfo.getTopicId(), TopicMediaActivity.TYPE_WORLD);
                }
                else if (v == captureBtn) {
                    topicInfo.gotoCapture(GlobalSearchActivity.this);
                }
            }
        }
    }
}
