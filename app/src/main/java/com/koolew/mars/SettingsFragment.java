package com.koolew.mars;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.koolew.mars.infos.MyAccountInfo;
import com.koolew.mars.preference.PreferenceAdapter;
import com.koolew.mars.preference.PreferenceGroupTitle;
import com.koolew.mars.preference.PreferenceHelper;
import com.koolew.mars.preference.SwitchPreference;
import com.koolew.mars.preference.TreePreference;


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
        mToolbarInterface.setToolbarTitle(R.string.title_settings);
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

    private void setupAdapter() {
        Context context = getActivity();
        mAdapter = new PreferenceAdapter(context);

        mAdapter.add(new PreferenceGroupTitle(context, R.string.intelligent));
        mAdapter.add(new TreePreference(context, R.string.push_settings, PushSettingsActivity.class));
        mAdapter.add(new SwitchPreference(context, R.string.intelligent_save_data_mode,
                PreferenceHelper.KEY_INTEL_SAVE_DATA, PreferenceHelper.DEFAULT_INTEL_SAVE_DATA));

        mAdapter.add(new PreferenceGroupTitle(context, R.string.service));
        mAdapter.add(new TreePreference(context, R.string.official_wechat, null));
        mAdapter.add(new TreePreference(context, R.string.contact_koolew_service, null));

        mAdapter.add(new PreferenceGroupTitle(context, R.string.about));
        mAdapter.add(new TreePreference(context, R.string.want_talk, null));
        mAdapter.add(new TreePreference(context, R.string.privacy_policy, null));
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
