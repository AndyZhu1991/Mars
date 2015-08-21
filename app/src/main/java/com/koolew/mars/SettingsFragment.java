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

import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.preference.OperationPreference;
import com.koolew.mars.preference.PreferenceAdapter;
import com.koolew.mars.preference.PreferenceGroupTitle;
import com.koolew.mars.preference.PreferenceHelper;
import com.koolew.mars.preference.SwitchPreference;
import com.koolew.mars.preference.TreePreference;
import com.koolew.mars.utils.DialogUtil;
import com.koolew.mars.utils.FileUtil;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileFilter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends MainBaseFragment implements View.OnClickListener {

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
        mListView.setOnItemClickListener(mAdapter);

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
                FileUtil.deleteFilesFromDir(new File(Utils.getCacheDir(getActivity())),
                        new FileFilter() {
                            @Override
                            public boolean accept(File pathname) {
                                if (pathname.getAbsolutePath().endsWith(".mp4")) {
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });
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
                ApiWorker.getInstance().logout();
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
}
