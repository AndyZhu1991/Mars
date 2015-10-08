package com.koolew.mars;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import com.koolew.mars.statistics.BaseV4Fragment;

/**
 * Created by jinchangzhu on 5/31/15.
 */
public class MainBaseFragment extends BaseV4Fragment {

    protected OnFragmentInteractionListener mListener;
    protected ToolbarOperateInterface mToolbarInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            mToolbarInterface = (ToolbarOperateInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener"
                    + " and ToolbarOperateInterface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarInterface.unregisterAllTopRedPoint();
        mToolbarInterface.setToolbarMiddleTitle("");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mToolbarInterface = null;
    }

    public void onTopIconClick(int position) {
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public interface ToolbarOperateInterface {
        void setToolbarColor(int color);

        void setToolbarTitle(String title);

        void setToolbarMiddleTitle(String middleTitle);

        void setTopIconCount(int count);

        void setTopIconImageResource(int position, int resource);

        void setTopRedPointPath(int position, String path);

        void unregisterAllTopRedPoint();
    }
}
