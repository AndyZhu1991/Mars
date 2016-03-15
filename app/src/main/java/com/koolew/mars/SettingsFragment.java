package com.koolew.mars;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.android.preference.OperationPreference;
import com.koolew.android.preference.PreferenceAdapter;
import com.koolew.android.preference.PreferenceGroupTitle;
import com.koolew.mars.preference.PreferenceHelper;
import com.koolew.android.preference.SwitchPreference;
import com.koolew.android.preference.TreePreference;
import com.koolew.mars.update.Updater;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.android.downloadmanager.Downloader;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends MainBaseFragment implements View.OnClickListener,
        Response.ErrorListener, Response.Listener<JSONObject> {

    private ListView mListView;
    private PreferenceAdapter mAdapter;
    private Button mLogoutBtn;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarInterface.setToolbarTitle(getString(R.string.title_settings));
        mToolbarInterface.setToolbarColor(getResources().getColor(R.color.koolew_deep_blue));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        mListView = (ListView) root.findViewById(R.id.list_view);
        if (mAdapter == null) {
            setupAdapter();
        }
        mListView.setAdapter(mAdapter);

        mLogoutBtn = (Button) root.findViewById(R.id.logout);
        mLogoutBtn.setOnClickListener(this);

        mToolbarInterface.setTopIconCount(0);

        return root;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private View.OnClickListener mClearCacheListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.clear_cache_confirm_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearCache();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    };

    private View.OnClickListener mCheckUpdateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Updater updater = Updater.newInstance(getActivity());
            if (updater != null) {
                updater.checkUpdate();
            }
        }
    };

    private void clearCache() {

        new AsyncTask<Void, Void, Void>() {
            ProgressDialog clearingDialog;

            @Override
            protected void onPreExecute() {
                clearingDialog = DialogUtil.getGeneralProgressDialog(
                        getActivity(), R.string.clearing_cache);
                clearingDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                ImageLoader.getInstance().clearDiskCache();
                Downloader.getInstance().cleanCache();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                clearingDialog.dismiss();
            }
        }.execute();
    }

    private void setupAdapter() {
        Context context = getActivity();
        mAdapter = new PreferenceAdapter(context);

        mAdapter.add(new PreferenceGroupTitle(context, R.string.intelligent));
        mAdapter.add(new TreePreference(context, R.string.push_settings, PushSettingsActivity.class));
        mAdapter.add(new SwitchPreference(context, R.string.intelligent_save_data_mode,
                PreferenceHelper.KEY_INTEL_SAVE_DATA, PreferenceHelper.DEFAULT_INTEL_SAVE_DATA));

        mAdapter.add(new PreferenceGroupTitle(context, R.string.cache));
        OperationPreference clearCachePref = new OperationPreference(context, R.string.clear_cache);
        clearCachePref.setOnClickListener(mClearCacheListener);
        mAdapter.add(clearCachePref);

        mAdapter.add(new PreferenceGroupTitle(context, R.string.about));
        //mAdapter.add(new TreePreference(context, R.string.want_talk, null));
        mAdapter.add(new TreePreference(context, R.string.privacy_policy, PrivacyPolicyActivity.class));
        mAdapter.add(new TreePreference(context, R.string.user_agreement, UserAgreementActivity.class));
        OperationPreference checkUpdatePref = new OperationPreference(context, R.string.check_update);
        checkUpdatePref.setOnClickListener(mCheckUpdateListener);
        mAdapter.add(checkUpdatePref);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logout:
                logout();
                break;
        }
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.logout_confirm);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ApiWorker.getInstance().logout(SettingsFragment.this, SettingsFragment.this);
                MyAccountInfo.clear();
                Intent intent = new Intent(getActivity(), LaunchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // TODO
    }

    @Override
    public void onResponse(JSONObject response) {
        // TODO
    }
}
