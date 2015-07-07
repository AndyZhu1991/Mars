package com.koolew.mars;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Response;
import com.koolew.mars.utils.Utils;
import com.koolew.mars.webapi.ApiWorker;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestPasswordFragment extends Fragment implements View.OnClickListener{

    private View mSendFrame;
    private Button mSendCall;
    private Button mSendMessage;
    private Button mResendPassword;

    private Timer mResendTimer;
    private int mResendCounter;

    protected OnFragmentInteractionListener mListener;

    public RequestPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_request_password, container, false);

        mSendFrame = root.findViewById(R.id.send_frame);
        mSendCall = (Button) root.findViewById(R.id.send_call);
        mSendMessage = (Button) root.findViewById(R.id.send_message);
        mResendPassword = (Button) root.findViewById(R.id.resend_password);

        mSendCall.setOnClickListener(this);
        mSendMessage.setOnClickListener(this);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mResendTimer != null) {
            mResendTimer.cancel();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_call:
                sendCall();
                break;
            case R.id.send_message:
                sendMessage();
                break;
        }
    }

    public void sendMessage() {
        if (!checkPhoneNumber(mListener.getPhoneNumber())) {
            return;
        }

        ApiWorker.getInstance().requestPasswordMessage(
                mListener.getPhoneNumber(), mResponseListener, null);

        showResendFrame();
        startResendTimer();
    }

    public void sendCall() {
        if (!checkPhoneNumber(mListener.getPhoneNumber())) {
            return;
        }

        ApiWorker.getInstance().requestPasswordCall(
                mListener.getPhoneNumber(), mResponseListener, null);

        showResendFrame();
        startResendTimer();
    }

    private boolean checkPhoneNumber(String number) {
        if (Utils.isChinaPhoneNumber(number)) {
            return true;
        }
        else {
            Toast.makeText(getActivity(),
                    R.string.please_input_correct_phone_num, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private Response.Listener<JSONObject> mResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject jsonObject) {
        }
    };

    private void showResendFrame() {
        mResendPassword.setVisibility(View.VISIBLE);
        mSendFrame.setVisibility(View.INVISIBLE);
    }

    private void showSendFrame() {
        mSendFrame.setVisibility(View.VISIBLE);
        mResendPassword.setVisibility(View.INVISIBLE);
    }

    private void startResendTimer() {
        mResendCounter = 60;
        mResendPassword.setText(getString(R.string.resend_password_waiting, mResendCounter));
        mResendTimer = new Timer();
        mResendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mResendCounter--;
                        if (mResendCounter == 0) {
                            mResendTimer.cancel();
                            showSendFrame();
                        }
                        mResendPassword.setText(
                                getString(R.string.resend_password_waiting, mResendCounter));
                    }
                });
            }
        }, 1000, 1000);
    }

    public interface OnFragmentInteractionListener {
        String getPhoneNumber();
        void onPhoneNumberInvalid();
    }
}
