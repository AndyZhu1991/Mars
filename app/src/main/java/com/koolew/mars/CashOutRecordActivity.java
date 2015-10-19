package com.koolew.mars;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.koolew.mars.mould.LoadMoreAdapter;
import com.koolew.mars.mould.RecyclerListFragmentMould;
import com.koolew.mars.statistics.BaseV4FragmentActivity;
import com.koolew.mars.utils.JsonUtil;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CashOutRecordActivity extends BaseV4FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_out_record);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new CashOutRecordFragment());
        fragmentTransaction.commit();
    }

    public static class CashOutRecordFragment extends RecyclerListFragmentMould<CashOutRecordAdapter> {

        public CashOutRecordFragment() {
            isNeedLoadMore = true;
            mLayoutResId = R.layout.refresh_recycler_without_shadow;
        }

        @Override
        protected CashOutRecordAdapter useThisAdapter() {
            return new CashOutRecordAdapter(getActivity());
        }

        @Override
        protected int getThemeColor() {
            return getResources().getColor(R.color.koolew_red);
        }

        @Override
        protected JsonObjectRequest doRefreshRequest() {
            return ApiWorker.getInstance().getCashOutRecord(mRefreshListener, null);
        }

        @Override
        protected JsonObjectRequest doLoadMoreRequest() {
            return ApiWorker.getInstance().getCashOutRecord(mAdapter.getLastRecordDate(),
                    mLoadMoreListener, null);
        }

        @Override
        protected boolean handleRefresh(JSONObject response) {
            JSONArray withdrawals = getWithdrawals(response);
            mAdapter.setData(withdrawals);
            return withdrawals.length() > 0;
        }

        @Override
        protected boolean handleLoadMore(JSONObject response) {
            JSONArray withdrawals = getWithdrawals(response);
            mAdapter.addData(withdrawals);
            return withdrawals.length() > 0;
        }

        private JSONArray getWithdrawals(JSONObject response) {
            try {
                return response.getJSONObject("result").getJSONArray("withdrawals");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new JSONArray();
        }
    }

    static class CashOutRecordAdapter extends LoadMoreAdapter {

        private Context mContext;
        private List<CashOutRecord> mData = new ArrayList<>();

        public CashOutRecordAdapter(Context context) {
            mContext = context;
        }

        public void setData(JSONArray records) {
            mData.clear();
            addRecords(records);
            notifyDataSetChanged();
        }

        public void addData(JSONArray records) {
            int originSize = mData.size();
            addRecords(records);
            notifyItemRangeInserted(originSize, records.length());
        }

        private void addRecords(JSONArray records) {
            int length = records.length();
            for (int i = 0; i < length; i++) {
                try {
                    mData.add(new CashOutRecord(records.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
            return new CashOutRecordItemHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.cash_out_record, parent, false));
        }

        @Override
        public void onBindCustomViewHolder(RecyclerView.ViewHolder holder, int position) {
            CashOutRecordItemHolder recordHolder = (CashOutRecordItemHolder) holder;
            CashOutRecord record = mData.get(position);
            recordHolder.date.setText(new SimpleDateFormat("yyyy.MM.dd")
                    .format(new Date(record.date * 1000)));
            recordHolder.amount.setText(String.valueOf(record.amount));
            recordHolder.status.setText(CASH_OUT_STATUS_TEXT[record.status]);
            recordHolder.status.setTextColor(mContext.getResources().
                    getColor(CASH_OUT_STATUS_TEXT_COLOR[record.status]));
        }

        @Override
        public int getCustomItemCount() {
            return mData.size();
        }

        private long getLastRecordDate() {
            if (mData.size() == 0) {
                return Long.MAX_VALUE;
            }
            else {
                return mData.get(mData.size() - 1).date;
            }
        }

        class CashOutRecordItemHolder extends RecyclerView.ViewHolder {

            private TextView date;
            private TextView amount;
            private TextView status;

            public CashOutRecordItemHolder(View itemView) {
                super(itemView);

                date = (TextView) itemView.findViewById(R.id.date);
                amount = (TextView) itemView.findViewById(R.id.amount);
                status = (TextView) itemView.findViewById(R.id.status);
            }
        }
    }

    private static final int CASH_OUT_STATUS_REQUESTED = 0;
    private static final int CASH_OUT_STATUS_DONE      = 1;
    private static final int CASH_OUT_STATUS_REJECTED  = 2;

    private static final int[] CASH_OUT_STATUS_TEXT = {
            R.string.cash_out_status_requested,
            R.string.cash_out_status_done,
            R.string.cash_out_status_rejected,
    };

    private static final int[] CASH_OUT_STATUS_TEXT_COLOR = {
            R.color.koolew_deep_green,
            R.color.koolew_red,
            R.color.settings_item_text_color,
    };

    static class CashOutRecord {
        private long date;
        private double amount;
        private int status;

        public CashOutRecord(JSONObject jsonObject) {
            date = JsonUtil.getLongIfHas(jsonObject, "create_time");
            amount = JsonUtil.getDoubleIfHas(jsonObject, "amount");
            status = JsonUtil.getIntIfHas(jsonObject, "status");
        }
    }
}
